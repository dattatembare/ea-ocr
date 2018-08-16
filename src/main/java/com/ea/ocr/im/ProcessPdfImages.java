/**
 * 
 */
package com.ea.ocr.im;

import static com.ea.ocr.data.EaOcrConstants.*;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ea.ocr.data.EaOcrProperties;
import com.ea.ocr.data.FileOperations;
import com.ea.ocr.data.JsonConfigReader;
import com.ea.ocr.tesseract.ReadImageText;

/**
 * @author Datta Tembare
 *
 */
public class ProcessPdfImages implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(ProcessPdfImages.class);

	private EaOcrProperties props;
	private JsonConfigReader config;
	private String gsOutDir;
	private String imDirPath;
	private FormatImages formatImagesObj;

	public ProcessPdfImages(EaOcrProperties props, JsonConfigReader config, String gsOutDir, String imDirPath) {
		this.props = props;
		this.config = config;
		this.gsOutDir = gsOutDir;
		this.imDirPath = imDirPath;
		this.formatImagesObj = new FormatImages(props);
	}

	public void run() {
		try {
			processImages();
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	/**
	 * @param formatImagesObj
	 * @param fileOperations
	 * @param dirName
	 * @param fileName
	 * @param gsOutDir
	 */
	private void processImages() {
		FileOperations fileOperations = new FileOperations();
		
		// Pull png files to process
		long pngFilesLength = 0;
		if (fileOperations.waitIfNotExist(new File(gsOutDir))) {
			pngFilesLength = fileOperations.filesLength(gsOutDir);
		}
		
		long lastPageNo = lastPgNumber(pngFilesLength, gsOutDir);
		// Consider length-5 if tesseract didn't full the correct text
		if (lastPageNo == 0) {
			lastPageNo = pngFilesLength - 5;
		}

		log.info("GS extracted files {}, last page number {}", pngFilesLength, lastPageNo);

		// last page formatting
		String lastPagePath = imDirPath + pngFilesLength;
		FileOperations.createDirWithReadWritePermissions(lastPagePath);

		String gsLastPage = gsOutDir + "/" + pngFilesLength + ".png";
		String cleanLastPage = lastPagePath + "/clean-" + pngFilesLength + ".png";
		formatImagesObj.cleanNdarkenTextOnPage(config, gsLastPage, cleanLastPage);

		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		// Using regular for loop to process files in ascending order,
		// for each loop returns like 1 10 11 ... 2 3 4 like this
		for (int i = 1; i < pngFilesLength; i++) {
			// Create output directory for ImageMagick
			String cropDirPath = imDirPath + i;
			log.info("Directory for Imagemagick output {}", cropDirPath);
			FileOperations.createDirWithReadWritePermissions(cropDirPath);

			String inputFile = gsOutDir + "/" + i + ".png";
			String cleanFile = cropDirPath + "/clean-" + i + ".png";
			formatImagesObj.cleanNdarkenTextOnPage(config, inputFile, cleanFile);

			// Call crop command
			if (i > 2 && i <= lastPageNo && formatImagesObj.isValidDimention(config, cleanFile)) {
				executor.execute(new CropPage(props, config, cropDirPath, cleanFile));
			}
		}
		executor.shutdown();
	}

	private long lastPgNumber(long pngFilesLength, String gsOutDir) {
		long lastPageNo = 0;
		if (config.getLastPageFinder().get(0).equals("scanFirstPage")) {
			lastPageNo = pngFilesLength;
		} else if (config.getLastPageFinder().get(0).equals("scanThirdPage")) {
			String inputFilepath = gsOutDir + "/3.png";
			lastPageNo = (long) findLastPage(inputFilepath, config.getLastPageFinder().get(1));
		} else if (config.getLastPageFinder().get(0).equals("scanLastPage")) {
			// double records = voterCountsMap.get("I");
			// lastPageNo = (long) Math.ceil(records / 30) + 2;
			lastPageNo = pngFilesLength - 5; // This is just for MP, if
												// new State has
												// lastpage scan then it
												// need to be
												// configured.
		}
		return lastPageNo;
	}

	private long findLastPage(String file, String geometry) {
		log.info("find last page {} - {}", file, geometry);
		long lastPageNo = 0;
		ReadImageText readImageTextObj = new ReadImageText(props);
		String pageNo = readImageTextObj.ocrText(new File(file), LANGUAGE_ENGLISH, ImageGeometry.getGeometry(geometry))
				.trim();
		try {
			lastPageNo = Long.parseLong(pageNo);
		} catch (NumberFormatException e) {
			log.info("Teseeract didn't pull correct text for file {}", file);
		}

		return lastPageNo;
	}

}
