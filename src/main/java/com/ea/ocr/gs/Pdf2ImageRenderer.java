/**
 * 
 */
package com.ea.ocr.gs;

import java.io.IOException;

/**
 * @author Datta Tembare
 *
 */
public class Pdf2ImageRenderer {

	/**
	 * @param args
	 */
	//public static void main(String[] args) {
		// Library doesn't support all variable
		/*
		 * try {
		 * 
		 * NativeLibrary.addSearchPath(
		 * "gsdll64","C:/Program Files/gs/gs9.23/bin");
		 * 
		 * load PDF document PDFDocument document = new PDFDocument();
		 * document.load(new File("C:/EA/Gray/engine/CMB0030001.PDF"));
		 * 
		 * create renderer SimpleRenderer renderer = new SimpleRenderer();
		 * 
		 * set resolution (in DPI) renderer.setResolution(300);
		 * 
		 * render List<Image> images = renderer.render(document);
		 * 
		 * // write images to files to disk as PNG 
		 * try { for (int i = 0; i < images.size(); i++) {
		 * ImageIO.write((RenderedImage) images.get(i),
		 * "png", new File((i + 1) + ".png")); } 
		 * } catch (IOException e) {
		 * 	System.out.println("ERROR: " + e.getMessage()); 
		 * }
		 * 
		 * } catch (Exception e) { System.out.println("ERROR: " +
		 * e.getMessage()); }
		 */

	//}
	
	/**
	 * 
	 * @param libPath
	 * @param inputFile
	 * @param outputFile
	 */
	public static void convertPdf2png(String gsLibPath, String inputPdfFile, String outputFilePath) {
		// "-sDEVICE=pngalpha" , "-sDEVICE=png16m" png16m
		// PNG gray
		ProcessBuilder processBuilder = new ProcessBuilder(gsLibPath, "-q",
				"-dQUIET", "-dSAFER", "-dBATCH", "-dNOPAUSE", "-dNOPROMPT", "-dMaxBitmap=900000000",
				"-dAlignToPixels=0", "-dGridFitTT=2", "-sDEVICE=pnggray", "-r1200", "-dDownScaleFactor=4",
				"-dTextAlphaBits=4", "-dGraphicsAlphaBits=4", "-dUseTrimBox", "-dQFactor=1",
				"-dColorConversionStrategy=/Gray", "-dProcessColorModel=/DeviceGray",
				"-sOutputFile="+outputFilePath+"/%d.png", inputPdfFile);
		try {
			processBuilder.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
