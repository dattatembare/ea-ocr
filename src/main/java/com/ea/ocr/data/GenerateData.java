package com.ea.ocr.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ea.ocr.gs.Pdf2ImageRenderer;
import com.ea.ocr.im.FormatImages;
import com.ea.ocr.im.ImageGeometry;
import com.ea.ocr.tesseract.ReadImageText;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import static com.ea.ocr.data.EaOcrConstants.*;
import static com.ea.ocr.data.StringOperations.*;

/**
 * @author Datta Tembare
 *
 */

@Component
public class GenerateData {
	private static final Logger log = LoggerFactory.getLogger(GenerateData.class);

	@Autowired
	EaOcrProperties props;

	private long srNo;

	/*public static void main(String[] args) {
		String jStr = "600x65+1715+2080";
		String[] arr = jStr.split("=");
		String language = arr.length > 1 ? arr[1] : "hin";
		System.out.println(language);
	}*/

	/**
	 * 
	 * @param pdfFilePath
	 * @param jsonFile
	 */
	public void generateJsonFile(String pdfFilePath, String outputFilePath, String jsonFile) {
		log.info("Process PDF {}", pdfFilePath);
		FormatImages formatImagesObj = new FormatImages(props);
		ReadImageText readImageTextObj = new ReadImageText(props);
		FileOperations fileOperations = new FileOperations();

		// Create JSON and CSV directories
		FileOperations.createDirWithReadWritePermissions(outputFilePath + JSON_DIR);
		FileOperations.createDirWithReadWritePermissions(outputFilePath + CSV_DIR);
		JsonConfigReader config = null;
		try {
			config = new JsonConfigReader(jsonFile);
		} catch (JsonSyntaxException | JsonIOException | FileNotFoundException e1) {
			log.error(e1.getMessage());
		}

		File[] directories = fileOperations.directoryList(new File(pdfFilePath));
		for (File d : directories) {
			File[] pdfFiles = new File(d.getAbsolutePath()).listFiles(File::isFile);
			for (File f : pdfFiles) {
				srNo = 1;
				// Create output directory for GhostSCript
				String gsOutDir = outputFilePath + GS_DIR + FilenameUtils.getBaseName(d.getName()) + "/"
						+ FilenameUtils.getBaseName(f.getName());
				log.info("1. Directory for GhosScript output {}", gsOutDir);

				// Create directory for GhosScript output
				FileOperations.createDirWithReadWritePermissions(gsOutDir);

				// Convert pdf to high scale png files
				Pdf2ImageRenderer.convertPdf2png(props.getGsEnvPath(), f.getAbsolutePath(), gsOutDir);

				// Pull png files to process
				Map<String, Long> voterCountsMap = null;
				long lastPageNo = 0;
				long pngFilesLength = fileOperations.filesLength(gsOutDir);

				if (config.getLastPageFinder().get(0).equals("scanFirstPage")) {
					String inputFilepath = gsOutDir + "/1.png";
					voterCountsMap = voterCounts(config, new File(inputFilepath));
					lastPageNo = pngFilesLength;
				} else if (config.getLastPageFinder().get(0).equals("scanThirdPage")) {
					String inputFilepath = gsOutDir + "/" + pngFilesLength + ".png";
					voterCountsMap = voterCounts(config, new File(inputFilepath));
					inputFilepath = gsOutDir + "/3.png";
					lastPageNo = (long) findLastPage(inputFilepath, config.getLastPageFinder().get(1));
				} else if (config.getLastPageFinder().get(0).equals("scanLastPage")) {
					String inputFilepath = gsOutDir + "/" + pngFilesLength + ".png";
					voterCountsMap = voterCounts(config, new File(inputFilepath));
					//double records = voterCountsMap.get("I");
					//lastPageNo = (long) Math.ceil(records / 30) + 2;
					lastPageNo = pngFilesLength - 5; //This is just for MP, if new State has lastpage scan then it need to be configured.
				}
				
				// Consider length-5 if tesseract didn't full the correct text
				if (lastPageNo == 0) {
					lastPageNo = pngFilesLength - 5;
				}
				long countFromLastPage = voterCountsMap.get("I");
				long rangeFrom = ((lastPageNo - 3) * 30) - 30;
				long rangeTo = ((lastPageNo - 3) * 30) + 30;
				
				log.info("GS extracted files {}, last page number {}", pngFilesLength, lastPageNo);

				Map<String, String> firstNLastPage = new HashMap<>();

				StringBuffer fileContent = new StringBuffer();
				fileContent.append("[");

				// Using regular for loop to process files in ascending order,
				// for each loop returns like 1 10 11 ... 2 3 4 like this
				for (int i = 1; i <= pngFilesLength; i++) {
					// Create output directory for ImageMagick
					String cleanFilePath = outputFilePath + IM_DIR + FilenameUtils.getBaseName(d.getName()) + "/"
							+ FilenameUtils.getBaseName(f.getName()) + "/" + i;
					FileOperations.createDirWithReadWritePermissions(cleanFilePath);

					// Process images using Imagemagick
					LinkedHashMap<File, String> processedImages = formatImagesObj.processPage(config,
							new File(gsOutDir + "/" + i + ".png"), cleanFilePath, lastPageNo);
					log.info("3. Read image using teserract");
					fileContent.append("{\"pageNumber\":\"" + i + "\",");

					int j = 1;
					LinkedHashMap<String, String> personDetails = null;
					for (Map.Entry<File, String> entry : processedImages.entrySet()) {

						// Read text using Tesseract OCR
						String fileName = entry.getKey().getAbsolutePath();
						// log.info("Tesseract input file {}", fileName);

						String text = readImageTextObj.ocrText(entry.getKey(), entry.getValue()).trim();
						// log.info("Text retuned by Tesseract {} ", text);

						if (text.isEmpty()) {
							formatImagesObj.addBlackThreshold(fileName);
							text = readImageTextObj.ocrText(entry.getKey(), entry.getValue()).trim();
							if (text.isEmpty()) {
								log.info("Tesseract didn't return text for file {}", fileName);
								text = NO_TEXT;
							}
						}

						// Build the JSON file
						if (i == 1) {
							fileContent.append("\"details\":\"" + removeSpecialChars(text) + "\"},");
							if (config.getFirstPage().size() > 0) {
								String firstPage = firstPage(config, entry.getKey());
								firstNLastPage.put("firstPage", firstPage);
								fileContent.append(firstPage);
							}
						} else if (i == 2) {
							fileContent.append(detailsStr(text));
						} else if (i > 2 && i <= lastPageNo) {
							if (fileName.contains("\\clean-") || fileName.contains("/clean-")) {
								fileContent.append("\"details\":\"" + removeSpecialChars(text) + "\"},");
							}else if (fileName.contains("\\0.png") || fileName.contains("/0.png")) {
								fileContent.append("\"header\":\"" + removeSpecialChars(text) + "\",");
								fileContent.append("\"address\":\""
										+ removeSpecialChars(getAddress(config, entry.getKey())) + "\",");
								fileContent.append("\"content\":[");
							} else if (fileName.contains("\\11.png") || fileName.contains("/11.png")) {
								fileContent.append("{}],");
								fileContent.append("\"footer\":\"" + removeSpecialChars(text) + "\"},");
								if (i == pngFilesLength) {
									fileContent.append("{}");
								}
							} else {
								boolean lastEleFlag = processedImages.size() == j + 1 ? true : false;
								personDetails = buildJson(config, fileName, text, personDetails);

								if (personDetails != null && personDetails.size() == config.getElementOrder().size()) {
									fileContent.append("{");
									long sn = 0;
									for (Map.Entry<String, String> person : personDetails.entrySet()) {
										if ("Sr No".equals(person.getKey())) {
											sn = checkNCorrectSrNo(person.getValue(), voterCountsMap);
											if (sn != 0 && countFromLastPage != 0 && countFromLastPage > rangeFrom && countFromLastPage < rangeTo) {
												if(sn == countFromLastPage){
													lastPageNo = i;
												}else if(i == lastPageNo && lastEleFlag && sn < countFromLastPage) {
													lastPageNo = i + 1;
												}
											}
											fileContent.append("\"" + person.getKey() + "\":\"" + sn + "\",");
										} else {
											fileContent.append(
													"\"" + person.getKey() + "\":\"" + person.getValue() + "\",");
										}
									}
									int n = 1;
									if (config.getNewElements() != null && config.getNewElements().size() > 0) {
										for (String newEle : config.getNewElements()) {
											if (!personDetails.keySet().contains(newEle)) {
												fileContent.append("\"" + newEle + "\":\"null\"");
												if (config.getNewElements().size() != n) {
													fileContent.append(",");
												}
											}
											n++;
										}
									} else {
										fileContent.append("null");
									}
									fileContent.append("},");
								}
							}
						} else if (i == pngFilesLength) { // last page
							fileContent.append(detailsStr(text));
							if (config.getLastPageDimentions().size() > 0) {
								String lastPage = lastPage(config, entry.getKey());
								firstNLastPage.put("lastPage", lastPage);
								fileContent.append(lastPage);
							}
						} else {
							fileContent.append(detailsStr(text));
						}
						j++;
					}
					// log.info("Page Number: {} content {}", i,
					// fileContent.toString());
					
					try {
						FileUtils.deleteDirectory(new File(cleanFilePath));
					} catch (IOException e) {
						log.error(e.getMessage());
					}
				}

				fileContent.append("]");
				log.info("********** JSON File content ************ {}", fileContent.toString());
				log.info("Validate JSON string");
				String jsonString = validateJson(fileContent.toString());

				File jFile = new File(outputFilePath + JSON_DIR + FilenameUtils.getBaseName(f.getName()) + ".json");
				File csvFile = new File(outputFilePath + CSV_DIR + FilenameUtils.getBaseName(f.getName()) + ".csv");
				File jbFile = new File(
						outputFilePath + JSON_DIR + FilenameUtils.getBaseName(f.getName()) + "-brief.json");
				File csvbFile = new File(
						outputFilePath + CSV_DIR + FilenameUtils.getBaseName(f.getName()) + "-brief.csv");

				EaJsonToCsvFileWriter csvWriter = new EaJsonToCsvFileWriter();

				try {
					log.info("Write JSON file.");
					FileUtils.writeStringToFile(jFile, jsonString, "UTF-8");

					log.info("Write JSON to CSV file.");
					csvWriter.writeCsv(config, jsonString, csvFile);

					String briefJsonString = briefJsonStr(firstNLastPage);
					log.info("Write Brief JSON file.");
					FileUtils.writeStringToFile(jbFile, briefJsonString, "UTF-8");

					log.info("Write Brief CSV file.");
					csvWriter.writeCsv(config, briefJsonString, csvbFile);
				} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException
						| JSONException | IOException e) {
					log.error(e.getMessage());
				}

				try {
					FileUtils.deleteDirectory(new File(gsOutDir));
				} catch (IOException e) {
					log.error(e.getMessage());
				} 
			}
		}

	}

	/**
	 * 
	 * @param config
	 * @param file
	 * @return
	 */
	private Map<String, Long> voterCounts(JsonConfigReader config, File file) {
		Map<String, Long> voterCounts = new HashMap<>();
		for (Map.Entry<String, String> vc : config.getVoterCounts().entrySet()) {
			long count = 0;
			try {
				count = Long.parseLong(getOcrText(file, LANGUAGE_ENGLISH, vc.getValue()));
				log.info("{} - count {}", vc.getKey(), count);
			} catch (NumberFormatException ex) {
				log.error("Tesseract didn't return correct number.");
			}
			voterCounts.put(vc.getKey(), count);
		}
		return voterCounts;
	}

	private LinkedHashMap<String, String> buildJson(JsonConfigReader config, String fileName, String text,
			LinkedHashMap<String, String> map) {
		if (fileName.contains("-0.png")) {
			map = new LinkedHashMap<String, String>();
			map.put(config.getElementOrder().get(0).toString(), text);
		} else if (fileName.contains("-1.png")) {
			map.put(config.getElementOrder().get(1).toString(), formatText(text));
		} else if (fileName.contains("-2.png")) {
			map.put(config.getElementOrder().get(2).toString(), formatText(text));
		} else if (fileName.contains("-3.png")) {
			map.put(getRelation(config, text), getRelationVal(text));
		} else if (fileName.contains("-4.png")) {
			map.put(config.getElementOrder().get(4).toString(), formatText(text));
		} else if (fileName.contains("-5.png")) {
			map.put(config.getElementOrder().get(5).toString(), age(text));
		} else if (fileName.contains("-6.png")) {
			map.put(config.getElementOrder().get(6).toString(), getGender(config, formatText(text)));
			int cnt = 0;
			for (String s : map.values()) {
				if (s.equals(NO_TEXT)) {
					cnt++;
				}
			}
			if (cnt == config.getElementOrder().size()) {
				map = null;
			}
		}

		return map;
	}

	private long checkNCorrectSrNo(String text, Map<String, Long> voterCountsMap) {
		text = removeSpecialChars(text);
		long num = 0;
		try {
			num = Integer.parseInt(text);
			if (num != srNo) {
				num = srNo;
			}
		} catch (NumberFormatException ex) {
				num = srNo;
		}
		srNo++;
		return num;
	}

	
	/**
	 * 
	 * @param text
	 * @return
	 */
	private String getGender(JsonConfigReader config, String text) {
		if (!text.equals(NO_TEXT)) {
			List<String> genders = config.getGenders();
			if (genders.contains(text)) {
				return text;
			} else {
				Map<String, Integer> scoreMap = new HashMap<>();
				for (String gen : config.getGenders()) {
					int score = 0;
					for (char c : text.toCharArray()) {
						if (gen.contains(EMPTY_STR + c)) {
							score++;
						}
					}
					scoreMap.put(gen, score);
				}

				int maxScore = Collections.max(scoreMap.values());
				for (Map.Entry<String, Integer> score : scoreMap.entrySet()) {
					if (score.getValue() == maxScore) {
						return score.getKey();
					}
				}
			}
		}
		return text;
	}

	/**
	 * 
	 * @param config
	 * @param text
	 * @return
	 */
	private String getRelation(JsonConfigReader config, String text) {
		if (!text.equals(NO_TEXT)) {
			String textArr[] = text.split(" ");
			text = formatText(textArr[0]);

			if (config.getRelations().values().contains(text)) {
				for (Map.Entry<String, String> reln : config.getRelations().entrySet()) {
					if (reln.getValue().equals(text)) {
						return reln.getKey();
					}
				}
			} else {
				Map<String, Integer> scoreMap = new HashMap<>();

				for (Map.Entry<String, String> reln : config.getRelations().entrySet()) {
					int score = 0;
					for (char c : text.toCharArray()) {
						if (reln.getValue().contains(EMPTY_STR + c)) {
							score++;
						}
					}
					scoreMap.put(reln.getKey(), score);
				}

				int maxScore = Collections.max(scoreMap.values());
				for (Map.Entry<String, Integer> score : scoreMap.entrySet()) {
					if (score.getValue() == maxScore) {
						return score.getKey();
					}
				}
			}
		}
		return "Relative";
	}

	private long findLastPage(String file, String geometry) {
		log.info("find last page {} - {}", file, geometry);
		long lastPageNo = 0;
		String pageNo = getOcrText(new File(file), LANGUAGE_ENGLISH, geometry);
		try {
			lastPageNo = Long.parseLong(pageNo);
		} catch (NumberFormatException e) {
			log.info("Teseeract didn't pull correct text for file {}", file);
		}

		return lastPageNo;
	}

	private String getAddress(JsonConfigReader config, File file) {
		return getOcrText(file, config.getDefaultTesseractLang(), config.getAddressDimentions());
	}

	private String firstPage(JsonConfigReader config, File file) {
		StringBuffer sb = new StringBuffer();
		sb.append("{\"firstPage\":{");
		for (Map.Entry<String, String> fp : config.getFirstPage().entrySet()) {
			sb.append("\"" + fp.getKey() + "\":\"" + getOcrText(file, config.getDefaultTesseractLang(), fp.getValue())
					+ "\",");
		}

		if (config.getPollingCenter().size() > 0) {
			sb.append("\"Polling Center\":{");
			int i = 1;
			for (Map.Entry<String, String> pc : config.getPollingCenter().entrySet()) {
				String[] arr = pc.getValue().split("=");
				String language = arr.length > 1 ? arr[1] : config.getDefaultTesseractLang();
				sb.append("\"" + pc.getKey() + "\":\"" + getOcrText(file, language, arr[0]) + "\"");
				if (i != config.getPollingCenter().size()) {
					sb.append(",");
				}
				i++;
			}
			sb.append("}");
		}
		sb.append("}},{");

		JsonArray jarr = config.getFirstPageTable();
		ExtractTableData tableData = new ExtractTableData();
		for (JsonElement ele : jarr) {
			if (ele.isJsonObject() && tableData.isTableExist(ele)) {
				sb.append("\"content\":[");
				sb.append(tableData.fetchTableData(props, config, file, ele, "firstPage"));
				sb.append("]");
			}
		}
		sb.append("},");

		return sb.toString();
	}

	private String lastPage(JsonConfigReader config, File file) {
		StringBuffer sb = new StringBuffer();
		JsonArray jarr = config.getLastPageDimentions();
		ExtractTableData tableData = new ExtractTableData();

		int r = 1;
		for (JsonElement ele : jarr) {
			if (ele.isJsonObject() && tableData.isTableExist(ele)) {
				sb.append("{\"content\":[");
				sb.append(tableData.fetchTableData(props, config, file, ele, "lastPage"));
				sb.append("]}");
			} else {
				if (r == 1) {
					sb.append("\"details\":{");
					sb.append("\"" + r + "\":\"" + getOcrText(file, config.getDefaultTesseractLang(), ele.getAsString())
							+ "\",");
				} else {
					sb.append("\"" + r + "\":\"" + getOcrText(file, config.getDefaultTesseractLang(), ele.getAsString())
							+ "\"");
					if (r != jarr.size()) {
						sb.append(",");
					}
				}
			}
			r++;
		}
		return sb.toString();
	}

	private String getOcrText(File file, String lang, String geo) {
		ReadImageText readImageText = new ReadImageText(props);
		String text = readImageText.ocrText(file, lang, ImageGeometry.getGeometry(geo)).trim();
		return removeSpecialChars(text);
	}

	/**
	 * 
	 * @param pdfFilePath
	 * @param outputFilePath
	 * @param jsonFile
	 */
	public void generateJsonFileWithThreads(String pdfFilePath, String outputFilePath, String jsonFile) {
		log.info("Process PDF {}", pdfFilePath);
		// Create JSON and CSV directories
		FileOperations.createDirWithReadWritePermissions(outputFilePath + JSON_DIR);
		FileOperations.createDirWithReadWritePermissions(outputFilePath + CSV_DIR);
		JsonConfigReader config = null;
		try {
			config = new JsonConfigReader(jsonFile);
		} catch (JsonSyntaxException | JsonIOException | FileNotFoundException e1) {
			log.error(e1.getMessage());
		}
		
		ThreadsToRunTools gsThread = new ThreadsToRunTools(props, config, pdfFilePath, outputFilePath,"gs");
		ThreadsToRunTools imThread = new ThreadsToRunTools(props, config, pdfFilePath, outputFilePath,"im");
		ThreadsToRunTools tsThread = new ThreadsToRunTools(props, config, pdfFilePath, outputFilePath,"ts");
		
		//Start Ghost script command execution
		gsThread.start();
		
		//Sleep for 5mins and Start imagemagick command execution
		try {
			imThread.sleep(300000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		imThread.start();
		
		//Sleep for 15 min and Start Tesseract command execution
		try {
			tsThread.sleep(900000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		tsThread.start();
	}
}
