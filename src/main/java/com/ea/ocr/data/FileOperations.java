/**
 * 
 */
package com.ea.ocr.data;

import java.awt.Rectangle;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ea.ocr.im.ImageGeometry;

/**
 * @author Datta Tembare
 *
 */
public class FileOperations {
	private static final Logger log = LoggerFactory.getLogger(FileOperations.class);
	private long oldLen;

	public void deleteFile(String fileName) {
		try {
			File file = new File(fileName);
			if (file.delete()) {
				// log.info("File {} is deleted!", file.getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param dir
	 */
	public static void createDirWithReadWritePermissions(String dir) {
		File f = new File(dir);
		f.setExecutable(true, true);
		f.setReadable(true, true);
		f.setWritable(true, true);
		f.mkdirs();
	}

	/**
	 * 
	 * @param dirPath
	 * @return
	 */
	public File[] directoryList(File dirPath) {
		return dirPath.listFiles(File::isDirectory);
	}

	/*
	 * private File[] fileList(String dirPath) { return new
	 * File(dirPath).listFiles(File::isFile); }
	 */

	public long filesLength(String dir) {
		log.info("Check number of files in dir {}", dir);
		long len = 0;

		int n = 0;
		for (int i = 0; i < 10; i++) {
			len = new File(dir).listFiles(File::isFile).length;
			log.debug("len {} - oldLen {}:", len, oldLen);
			if (oldLen == len) {
				n++;
			}
		}
		oldLen = len;
		log.debug("n: {}, len: {}", n, len);
		if (len > 0 && n == 10) {
			return len;
		} else {
			try {
				//log.info("Wait 10 seconds to check files count.");
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				log.error(e.getMessage());
			}
			return filesLength(dir);
		}
	}

	public boolean waitIfNotExist(File file) {
		if (file.exists()) {
			return true;
		} else {
			try {
				log.info("Wait 10 seconds to generate file {}.", file.getAbsolutePath());
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				log.error(e.getMessage());
			}
			return waitIfNotExist(file);
		}
	}

	public LinkedList<PDFDetails> fileDetailsList(JsonConfigReader config, String cropDirPath, long pngFilesLength,
			long lastPageNo, boolean isValidDimention) {
		LinkedList<PDFDetails> fileDetailsList = new LinkedList<>();

		for (int pageNo = 1; pageNo <= pngFilesLength; pageNo++) {
			String cleanFileDir = cropDirPath+"/"+pageNo;
			String cleanFile = cleanFileDir + "/clean-" + pageNo + ".png";
			
			PDFDetails page = new PDFDetails();
			page.setPageNumber(pageNo);
			page.setLastPage(lastPageNo);
			page.setTotalPages(pngFilesLength);
			page.setCleanFile(cleanFile);
			
			LinkedList<PageDetails> pageDetails = page.getPageDetails();
			
			if (pageNo > 2 && pageNo <= lastPageNo && isValidDimention) {
				// 12 page rows
				LinkedList<String> pageRows = config.getPageCropDimentions();
				// Crop one person details
				LinkedList<String> persons = config.getPersonCropDimentions();
				// Split the single person details
				LinkedHashMap<String, String> personDetails = config.getElementCropDimentions();
				// Pull elements
				LinkedList<String> elements = config.getElementOrder();

				int i = 0;
				for (String geometry : pageRows) {
					Rectangle row = ImageGeometry.getGeometry(geometry);
					if (i > 0 && i < 11) {
						int j = 0;
						for (String person : persons) {
							Rectangle personGeo = ImageGeometry.getGeometry(person);
							int k = 0;
							for (Map.Entry<String, String> entry : personDetails.entrySet()) {
								String personDetailsFilePath = cleanFileDir + "/" + i + "-" + j + "-" + k + ".png";
								Rectangle personDetailGeo = ImageGeometry.getGeometry(entry.getKey());
								Rectangle cropGro = new Rectangle(row.x + personGeo.x + personDetailGeo.x,
										row.y + personGeo.y + personDetailGeo.y, personDetailGeo.width,
										personDetailGeo.height);

								PageDetails file = new PageDetails();
								file.setGeometry(cropGro);
								file.setLanguage(entry.getValue());
								file.setFileName(personDetailsFilePath);
								file.setElementName(elements.get(k));
								pageDetails.add(file);
								k++;
							}
							j++;
						}
					} else {
						String cropFilePath = cleanFileDir + "/" + i + ".png";

						PageDetails file = new PageDetails();
						file.setGeometry(row);
						file.setLanguage(config.getDefaultTesseractLang());
						file.setFileName(cropFilePath);
						if(i==0){
							file.setElementName("header");
						}else if(i ==11){
							file.setElementName("footer");
						}
						pageDetails.add(file);
					}
					i++;
				}
			} 
			page.setPageDetails(pageDetails);
			fileDetailsList.add(page);
		}
		return fileDetailsList;
	}
}
