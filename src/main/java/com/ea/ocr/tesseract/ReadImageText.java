/**
 * 
 */
package com.ea.ocr.tesseract;

import static com.ea.ocr.data.EaOcrConstants.*;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ea.ocr.data.EaOcrProperties;
import com.ea.ocr.im.FormatImages;
import com.ea.ocr.im.ImageGeometry;
import com.sun.jna.NativeLibrary;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

/**
 * @author Datta Tembare
 *
 */

@Component
public class ReadImageText {
	private static final Logger log = LoggerFactory.getLogger(FormatImages.class);
	Tesseract hinInstance;
	Tesseract hinEngInstance;
	Tesseract engInstance;

	public ReadImageText(EaOcrProperties props) {
		hinInstance = getTesseractInstance(props.getTesseractEnvPath(), LANGUAGE_HINDI);
		hinEngInstance = getTesseractInstance(props.getTesseractEnvPath(), LANGUAGE_HINDI_ENGLISH);
		engInstance = getTesseractInstance(props.getTesseractEnvPath(), LANGUAGE_ENGLISH);
	}

	/**
	 * @param args
	 */
	/*public static void main(String[] args) {
		EaOcrProperties prop = new EaOcrProperties();
		prop.setTesseractEnvPath("C:/Program Files (x86)/Tesseract-OCR/tessdata/");
		ReadImageText imText = new ReadImageText(prop);

		File file = new File("C:/EA/mp-out/gs/1/28.png");
		String resultText = imText.ocrText(file, "hin", "2550x600+0+0");
		log.info("resultText: {}", resultText);
	}*/

	/**
	 * 
	 * @param instance
	 * @param file
	 * @return
	 */
	public String ocrText(File file, String lang) {
		String resultText = null;
		Tesseract instance = null;

		if (lang.equals(LANGUAGE_HINDI)) {
			instance = hinInstance;
		} else if (lang.equals(LANGUAGE_HINDI_ENGLISH)) {
			instance = hinEngInstance;
		} else if (lang.equals(LANGUAGE_ENGLISH)) {
			instance = engInstance;
		}

		try {
			resultText = instance.doOCR(file);
			log.debug("Result for image {} - {}", file, resultText);
		} catch (TesseractException e) {
			log.error(e.getMessage());
		}

		return resultText;
	}

	/**
	 * 
	 * @param file
	 * @param lang
	 * @param geometry
	 * @return
	 */
	public String ocrText(File file, String lang, ImageGeometry geometry) {
		String resultText = null;
		Tesseract instance = null;

		if (lang.equals(LANGUAGE_HINDI)) {
			instance = hinInstance;
		} else if (lang.equals(LANGUAGE_HINDI_ENGLISH)) {
			instance = hinEngInstance;
		} else if (lang.equals(LANGUAGE_ENGLISH)) {
			instance = engInstance;
		}

		// define an equal or smaller region of interest on the image. Follow:
		// x-scale, y-scale, width and height
		Rectangle rect = new Rectangle(geometry.getXscale(), geometry.getYscale(), geometry.getWidth(),
				geometry.getHeight());

		try {
			resultText = instance.doOCR(ImageIO.read(file), rect);
			log.debug("resultText: {}", resultText);
		} catch (TesseractException | IOException e) {
			log.error(e.getMessage());
		}

		return resultText;
	}

	/**
	 * 
	 * @param file
	 * @param lang
	 * @param rect
	 * @return
	 */
	public String ocrText(File file, String lang, Rectangle rect) {
		String resultText = null;
		Tesseract instance = null;

		if (lang.equals(LANGUAGE_HINDI)) {
			instance = hinInstance;
		} else if (lang.equals(LANGUAGE_HINDI_ENGLISH)) {
			instance = hinEngInstance;
		} else if (lang.equals(LANGUAGE_ENGLISH)) {
			instance = engInstance;
		}

		try {
			resultText = instance.doOCR(ImageIO.read(file), rect);
			// log.info("resultText: {}", resultText);
		} catch (TesseractException | IOException e) {
			log.error(e.getMessage());
		}

		return resultText;
	}

	/**
	 * 
	 * @param file
	 * @param lang
	 * @param rect
	 * @return
	 */
	public String ocrText(File file, String lang, String dim) {
		String resultText = null;
		Tesseract instance = null;

		if (lang.equals(LANGUAGE_HINDI)) {
			instance = hinInstance;
		} else if (lang.equals(LANGUAGE_HINDI_ENGLISH)) {
			instance = hinEngInstance;
		} else if (lang.equals(LANGUAGE_ENGLISH)) {
			instance = engInstance;
		}

		try {
			resultText = instance.doOCR(ImageIO.read(file), ImageGeometry.getGeometry(dim));
			// log.info("resultText: {}", resultText);
		} catch (TesseractException | IOException e) {
			log.error(e.getMessage());
		}

		return resultText;
	}

	/**
	 * 
	 * @param tessdataPath
	 * @param lng
	 * @return
	 */
	public Tesseract getTesseractInstance(String tessdataPath, String lng) {
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

		Tesseract instance = new Tesseract();
		instance.setDatapath(tessdataPath);
		instance.setLanguage(lng);
		// instance.setPageSegMode(7);
		return instance;
	}

}
