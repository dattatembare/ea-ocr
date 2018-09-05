/**
 * 
 */
package com.ea.ocr.data;

import static com.ea.ocr.data.EaOcrConstants.*;
import static com.ea.ocr.data.StringOperations.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.LongStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ea.ocr.gs.Pdf2ImageRenderer;
import com.ea.ocr.im.CropPage;
import com.ea.ocr.im.FormatImages;
import com.ea.ocr.im.ImageGeometry;
import com.ea.ocr.tesseract.ReadImageText;
import com.ea.ocr.tesseract.TextReader;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

/**
 * @author Datta Tembare
 *
 */

@Component
public class GenerateFiles {
	private static final Logger log = LoggerFactory.getLogger(GenerateFiles.class);

	@Autowired
	EaOcrProperties props;

	private long srNo;
	
	/**
	 * 
	 * @param pdfFilePath
	 * @param jsonFile
	 */
	public void execute(String pdfFilePath, String outputFilePath, String jsonFile) {
		log.info("Process PDF {}", pdfFilePath);
		FormatImages formatImagesObj = new FormatImages(props);
		ReadImageText readImageTextObj = new ReadImageText(props);
		FileOperations fileOperations = new FileOperations();

		// Create JSON and CSV directories
		FileOperations.createDirWithReadWritePermissions(outputFilePath + JSON_DIR);
		FileOperations.createDirWithReadWritePermissions(outputFilePath + CSV_DIR);
		JsonConfigReader config = configObj(jsonFile);

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
					// double records = voterCountsMap.get("I");
					// lastPageNo = (long) Math.ceil(records / 30) + 2;
					lastPageNo = pngFilesLength - 5; // This is just for MP, if
														// new State has
														// lastpage scan then it
														// need to be
														// configured.
				}

				// Consider length-5 if tesseract didn't full the correct text
				if (lastPageNo == 0) {
					lastPageNo = pngFilesLength - 5;
				}
				
				long lpn = lastPageNo;
				boolean isValidDimention = formatImagesObj.isValidDimention(config, gsOutDir + "/3.png");
				
				log.info("GS extracted files {}, last page number {}", pngFilesLength, lastPageNo);

				String cropDirPath = outputFilePath + IM_DIR + FilenameUtils.getBaseName(d.getName()) + "/"
						+ FilenameUtils.getBaseName(f.getName());
				
				// Generate clean files
				log.info("2. Crop page for voter details");
				LongStream.range(1, 6).parallel()
				.forEach(i -> generateCleanFiles(config, gsOutDir, cropDirPath, i, lpn, isValidDimention));
				LongStream.range(6, pngFilesLength + 1).parallel()
						.forEach(i -> generateCleanFiles(config, gsOutDir, cropDirPath, i, lpn, isValidDimention));
				log.info("generateCleanFiles operation performed successfully!");
				
				// Pull fileDetails for all PDF pages
				LinkedList<PDFDetails> fileDetails = fileOperations.fileDetailsList(config, cropDirPath, pngFilesLength, lastPageNo, isValidDimention);

				Map<String, String> firstNLastPage = new HashMap<>();
				StringBuffer fileContent = new StringBuffer();
				TextReader textReader = new TextReader(props);
				fileContent.append("[");
				for (PDFDetails page : fileDetails) {
					// Read text through Tesseract
					log.info("3. Read text for file {}", page.getCleanFile());
					
					long pageNo = page.getPageNumber();
					fileContent.append("{\"pageNumber\":\"" + pageNo + "\",");
					if (pageNo > 2 && pageNo <= lastPageNo && isValidDimention) {
						//Process images in parallel 
						textReader.processBatch(page);
						// Use page obj to build StringBuffer
						buildPageString(config, page, fileContent, voterCountsMap);
						if (pageNo == pngFilesLength) {
							fileContent.append("{}");
						}
					} else {
						File fileName = new File(page.getCleanFile());
						String text = readImageTextObj.ocrText(fileName, config.getDefaultTesseractLang());

						// Build the JSON file
						if (pageNo == 1) {
							fileContent.append("\"details\":\"" + removeSpecialChars(text) + "\"},");
							if (config.getFirstPage().size() > 0) {
								String firstPage = firstPage(config, fileName);
								firstNLastPage.put("firstPage", firstPage);
								fileContent.append(firstPage);
							}
						} else if (pageNo == 2) {
							fileContent.append(detailsStr(text));
						} else if (pageNo == pngFilesLength) { // last page
							fileContent.append(detailsStr(text));
							if (config.getLastPageDimentions().size() > 0) {
								String lastPage = lastPage(config, fileName);
								firstNLastPage.put("lastPage", lastPage);
								fileContent.append(lastPage);
							}
						} else {
							fileContent.append(detailsStr(text));
						}
					}
				}
				fileContent.append("]");

				// Validate JSON String, Generate JSON and CSV files
				log.info("********** JSON File content ************ {}", fileContent.toString());
				log.info("Validate JSON string");
				String jsonString = validateJson(fileContent.toString());
				gnerateJsonNcsvFiles(outputFilePath, config, f, firstNLastPage, jsonString);

				// Delete Files
				try {
					FileUtils.deleteDirectory(new File(gsOutDir));
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
		}

	}

	private void buildPageString(JsonConfigReader config, PDFDetails pdf, StringBuffer fileContent,	Map<String, Long> voterCountsMap) {
		LinkedHashMap<String, String> personDetails = null;
		LinkedList<PageDetails> pageFiles = pdf.getPageDetails();
		long lastPageNo = pdf.getLastPage();
		long countFromLastPage = voterCountsMap.get("I");
		long rangeFrom = ((lastPageNo - 3) * 30) - 30;
		long rangeTo = ((lastPageNo - 3) * 30) + 30;

		if (pageFiles.size() > 0) {
			int j = 0;
			for (PageDetails page : pageFiles) {
				String fileName = page.getFileName();
				String elementName = page.getElementName();
				String text = page.getFileText();

				if (elementName.equals("header")) {
					fileContent.append("\"" + elementName + "\":\"" + removeSpecialChars(text) + "\",");
					fileContent.append(
							"\"address\":\"" + removeSpecialChars(getAddress(config, new File(fileName))) + "\",");
					fileContent.append("\"content\":[");
				} else if (elementName.equals("footer")) {
					fileContent.append("{}],");
					fileContent.append("\"" + elementName + "\":\"" + removeSpecialChars(text) + "\"},");
					/*
					 * if (i == pngFilesLength) { fileContent.append("{}"); }
					 */
				} else {
					boolean lastEleFlag = pageFiles.size() == j + 1 ? true : false;
					personDetails = buildJson(config, fileName, text, personDetails);

					if (personDetails != null && personDetails.size() == config.getElementOrder().size()) {
						fileContent.append("{");
						long sn = 0;
						for (Map.Entry<String, String> person : personDetails.entrySet()) {
							if ("Sr No".equals(person.getKey())) {
								sn = checkNCorrectSrNo(person.getValue(), voterCountsMap);
								if (sn != 0 && countFromLastPage != 0 && countFromLastPage > rangeFrom
										&& countFromLastPage < rangeTo) {
									if (sn == countFromLastPage) {
										pdf.setLastPage(pdf.getPageNumber());
										// lastPageNo = i;
									} else if (pdf.getPageNumber() == lastPageNo && lastEleFlag
											&& sn < countFromLastPage) {
										// lastPageNo = i + 1;
										pdf.setLastPage(pdf.getPageNumber() + 1);
										// TODO if lastpage size increased by
										// one then need to crop page
									}
								}
								fileContent.append("\"" + person.getKey() + "\":\"" + sn + "\",");
							} else {
								fileContent.append("\"" + person.getKey() + "\":\"" + person.getValue() + "\",");
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
				j++;
			}
		} else {
			ReadImageText readImageTextObj = new ReadImageText(props);
			String text = readImageTextObj.ocrText(new File(pdf.getCleanFile()), config.getDefaultTesseractLang());
			fileContent.append("\"details\":\"" + removeSpecialChars(text) + "\"},");
		}
	}

	/**
	 * @param jsonFile
	 * @return
	 */
	private JsonConfigReader configObj(String jsonFile) {
		JsonConfigReader config = null;
		try {
			config = new JsonConfigReader(jsonFile);
		} catch (JsonSyntaxException | JsonIOException | FileNotFoundException e1) {
			log.error(e1.getMessage());
		}
		return config;
	}

	private void generateCleanFiles(JsonConfigReader config, String gsOutDir, String cleanFileDir, long pageNo, long lastPageNo, boolean isValidDimention) {
		cleanFileDir = cleanFileDir + "/" + pageNo;
		FileOperations.createDirWithReadWritePermissions(cleanFileDir);

		FormatImages formatImagesObj = new FormatImages(props);
		String inputFilepath = gsOutDir + "/" + pageNo + ".png";
		String outputFilePath = cleanFileDir + "/clean-" + pageNo + ".png";

		formatImagesObj.cleanNdarkenTextOnPage(config, inputFilepath, outputFilePath);
		
		// Process through Imagemagick
		if (pageNo > 2 && pageNo <= lastPageNo && isValidDimention) {
			new CropPage(props, config, cleanFileDir, outputFilePath).cropPage();
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

	/**
	 * 
	 * @param file
	 * @param lang
	 * @param geo
	 * @return
	 */
	private String getOcrText(File file, String lang, String geo) {
		ReadImageText readImageText = new ReadImageText(props);
		String text = readImageText.ocrText(file, lang, ImageGeometry.getGeometry(geo)).trim();
		return removeSpecialChars(text);
	}

	/**
	 * 
	 * @param file
	 * @param geometry
	 * @return
	 */
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

	/**
	 * 
	 * @param config
	 * @param file
	 * @return
	 */
	private String firstPage(JsonConfigReader config, File file) {
		StringBuffer sb = new StringBuffer();
		sb.append("{\"firstPage\":{");
		
		LinkedHashMap<String, String> fp = config.getFirstPage();
		fp.entrySet()
		.parallelStream()
		.parallel()
		.forEach(entry -> {
			sb.append("\"" + entry.getKey() + "\":\"" + getOcrText(file, config.getDefaultTesseractLang(), entry.getValue())
			+ "\",");
		});
		
		/*for (Map.Entry<String, String> fp : config.getFirstPage().entrySet()) {
			sb.append("\"" + fp.getKey() + "\":\"" + getOcrText(file, config.getDefaultTesseractLang(), fp.getValue())
					+ "\",");
		}*/

		if (config.getPollingCenter().size() > 0) {
			sb.append("\"Polling Center\":{");
			//int i = 1;
			LinkedHashMap<String, String> pc = config.getFirstPage();
			fp.entrySet()
			.parallelStream()
			.parallel()
			.forEach(entry -> {
				String[] arr = entry.getValue().split("=");
				String language = arr.length > 1 ? arr[1] : config.getDefaultTesseractLang();
				sb.append("\"" + entry.getKey() + "\":\"" + getOcrText(file, language, arr[0]) + "\",");
			});
			
			/*for (Map.Entry<String, String> pc : config.getPollingCenter().entrySet()) {
				String[] arr = pc.getValue().split("=");
				String language = arr.length > 1 ? arr[1] : config.getDefaultTesseractLang();
				sb.append("\"" + pc.getKey() + "\":\"" + getOcrText(file, language, arr[0]) + "\"");
				if (i != config.getPollingCenter().size()) {
					sb.append(",");
				}
				i++;
			}*/
			sb.append("\"\":\"\"}");
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

	/**
	 * 
	 * @param config
	 * @param file
	 * @return
	 */
	private String getAddress(JsonConfigReader config, File file) {
		return getOcrText(file, config.getDefaultTesseractLang(), config.getAddressDimentions());
	}

	/**
	 * 
	 * @param config
	 * @param fileName
	 * @param text
	 * @param map
	 * @return
	 */
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

	/**
	 * 
	 * @param text
	 * @param voterCountsMap
	 * @return
	 */
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
	 * @param page
	 * @return
	 */
	private BufferedImage bufferedImage(File cleanFile) {
		BufferedImage inputImg = null;
		try {
			inputImg = ImageIO.read(cleanFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return inputImg;
	}

	/**
	 * @param outputFilePath
	 * @param config
	 * @param f
	 * @param firstNLastPage
	 * @param jsonString
	 */
	private void gnerateJsonNcsvFiles(String outputFilePath, JsonConfigReader config, File f,
			Map<String, String> firstNLastPage, String jsonString) {
		File jFile = new File(outputFilePath + JSON_DIR + FilenameUtils.getBaseName(f.getName()) + ".json");
		File csvFile = new File(outputFilePath + CSV_DIR + FilenameUtils.getBaseName(f.getName()) + ".csv");
		File jbFile = new File(outputFilePath + JSON_DIR + FilenameUtils.getBaseName(f.getName()) + "-brief.json");
		File csvbFile = new File(outputFilePath + CSV_DIR + FilenameUtils.getBaseName(f.getName()) + "-brief.csv");

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
	}
}
