/**
 * 
 */
package com.ea.ocr.tesseract;

import static com.ea.ocr.data.EaOcrConstants.NO_TEXT;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ea.ocr.data.EaOcrProperties;
import com.ea.ocr.data.PageDetails;
import com.ea.ocr.data.PDFDetails;
import com.ea.ocr.im.FormatImages;
import com.sun.jna.NativeLibrary;

import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.TesseractException;

/**
 * @author UX012064
 *
 */
public class TextReader {
	private static final Logger log = LoggerFactory.getLogger(TextReader.class);
	private EaOcrProperties props;
	private String tesseractEnvPath;

	public TextReader(EaOcrProperties props) {
		this.tesseractEnvPath = props.getTesseractEnvPath();
		this.props = props;
	}

	/*public static void main(String[] args) throws InterruptedException {
		EaOcrProperties prop = new EaOcrProperties();
		prop.setTesseractEnvPath("C:/Program Files (x86)/Tesseract-OCR/tessdata/");
		TextReader imText = new TextReader(prop);

		Map<File, String> map = imText.processBatch("C:/EA/mp2-out1/im/153/152-003/7", "hin");
		for (Map.Entry<File, String> person : map.entrySet()) {
			log.info("file {} - Tesseract result: {}", person.getKey().getAbsolutePath(), person.getValue());
		}
	}*/

	/**
	 * 
	 * @param imText
	 * @return
	 */
	private Map<File, String> processBatch(String inpDir, String language) {
		File[] files = new File(inpDir).listFiles();
		log.info("There are {} png files to OCR in parallel.", files.length);
		Map<File, String> map = new HashMap<>();
		Arrays.stream(files).parallel().forEach((file) -> {
			String text = ocrText(getTesseractInstance(tesseractEnvPath, "hin"), file);
			map.put(file, text);
		});
		return map;
	}
	
	/**
	 * 
	 * @param imText
	 * @return
	 */
	public void processBatch(PDFDetails page) {
		if (waitIfNotAvailable(new File(page.getCleanFile()).getParentFile())) {
			FormatImages formatImagesObj = new FormatImages(props);
			
			List<PageDetails> files = page.getPageDetails().stream()
				     .collect(Collectors.toList());
			
			files.parallelStream()
				.parallel()
				.forEach((file) -> {
				Tesseract1 instance = getTesseractInstance(tesseractEnvPath, file.getLanguage());
				String text = ocrText(instance, new File(file.getFileName())).trim();
				if (text.isEmpty()) {
					formatImagesObj.addBlackThreshold(file.getFileName());
					text = ocrText(instance, new File(file.getFileName())).trim();
					if (text.isEmpty()) {
						log.info("Tesseract didn't return text for file {}", file.getFileName());
						text = NO_TEXT;
					}
				}
				file.setFileText(text);
			});
		}
	}
	
	public boolean waitIfNotAvailable(File file) {
		if (file.listFiles().length == 213) {
			return true;
		} else {
			try {
				//log.info("Wait 5 seconds to generate file {}.", file.getAbsolutePath());
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				log.error(e.getMessage());
			}
			return waitIfNotAvailable(file);
		}
	}

	/**
	 * 
	 * @param instance
	 * @param file
	 * @return
	 * @throws TesseractException
	 */
	public String ocrText(Tesseract1 instance, File file) {
		try {
			return instance.doOCR(file);
		} catch (TesseractException e) {
			log.error("Error while reading file {} {}",file.getAbsoluteFile(), e.getMessage());
		}
		return "";
	}

	/**
	 * 
	 * @param tessdataPath
	 * @param lng
	 * @return
	 */
	public Tesseract1 getTesseractInstance(String tessdataPath, String lng) {
		// OPTION:1
		/*
		 * ProcessBuilder pb = new ProcessBuilder("CMD", "/C", "SET");
		 * Map<String, String> env = pb.environment();
		 * env.put("TESSDATA_PREFIX",
		 * "C:/Program Files (x86)/Tesseract-OCR/tessdata/"); try { Process p =
		 * pb.start(); } catch (IOException e1) { // TODO Auto-generated catch
		 * block e1.printStackTrace(); }
		 */

		// OPTION:2 Alternative, use native library
		NativeLibrary.addSearchPath("TESSDATA_PREFIX", tessdataPath);

		Tesseract1 instance = new Tesseract1();
		instance.setDatapath(tessdataPath);
		instance.setLanguage(lng);
		// instance.setPageSegMode(7);
		return instance;
	}
}
