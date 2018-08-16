package com.ea.ocr.data;

import static com.ea.ocr.data.EaOcrConstants.*;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ea.ocr.gs.Pdf2ImageRenderer;
import com.ea.ocr.im.ProcessPdfImages;
import com.ea.ocr.tesseract.GenerateJsonNCsv;

/**
 * @author Datta Tembare
 *
 */

public class ThreadsToRunTools extends Thread {
	private static final Logger log = LoggerFactory.getLogger(ThreadsToRunTools.class);
	private EaOcrProperties props;
	private JsonConfigReader config;
	private String pdfFilePath;
	private String outputFilePath;
	private String process;

	public ThreadsToRunTools(EaOcrProperties props, JsonConfigReader config, String pdfFilePath, String outputFilePath,
			String process) {
		this.props = props;
		this.config = config;
		this.pdfFilePath = pdfFilePath;
		this.outputFilePath = outputFilePath;
		this.process = process;
	}

	@Override
	public void run() {
		if (process.equals("gs")) {
			log.info("1. Start Ghost Script commands execution");
			executeGhostScript();
		} else if (process.equals("im")) {
			log.info("2. Start Imagemagick commands execution");
			executeImagemagick();
		} else if (process.equals("ts")) {
			log.info("3. Start Tesseract execution");
			executeTesseract();
		}
	}

	/**
	 * Execute ghost Script as part of thread 1
	 */
	private void executeGhostScript() {
		File[] directories = new FileOperations().directoryList(new File(pdfFilePath));
		for (File d : directories) {
			File[] pdfFiles = new File(d.getAbsolutePath()).listFiles(File::isFile);
			for (File f : pdfFiles) {
				// Create output directory for GhostSCript
				String gsOutDir = outputFilePath + GS_DIR + FilenameUtils.getBaseName(d.getName()) + "/"
						+ FilenameUtils.getBaseName(f.getName());
				log.info("Directory for GhosScript output {}", gsOutDir);

				// Create directory for GhosScript output
				FileOperations.createDirWithReadWritePermissions(gsOutDir);

				// Convert pdf to high scale png files
				Pdf2ImageRenderer.convertPdf2png(props.getGsEnvPath(), f.getAbsolutePath(), gsOutDir);

				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					log.error(e.getMessage());
				}
			}
		}
	}

	/**
	 * Execute Imagemagick Script as part of thread 2
	 */
	private void executeImagemagick() {
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		FileOperations fileOperations = new FileOperations();
		File[] directories = fileOperations.directoryList(new File(pdfFilePath));
		for (File d : directories) {
			File[] pdfFiles = new File(d.getAbsolutePath()).listFiles(File::isFile);
			for (File f : pdfFiles) {
				// Create output directory for GhostSCript
				String dirName = FilenameUtils.getBaseName(d.getName());
				String fileName = FilenameUtils.getBaseName(f.getName());
				String gsOutDir = outputFilePath + GS_DIR + dirName + "/" + fileName;
				String imDirPath = outputFilePath + IM_DIR + dirName + "/" + fileName + "/";

				executor.execute(new ProcessPdfImages(props, config, gsOutDir, imDirPath));
			}
		}

		executor.shutdown();
	}

	private void executeTesseract() {
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		File[] directories = new FileOperations().directoryList(new File(pdfFilePath));
		for (File d : directories) {
			File[] pdfFiles = new File(d.getAbsolutePath()).listFiles(File::isFile);
			for (File pdfName : pdfFiles) {
				executor.execute(new GenerateJsonNCsv(props, config, pdfName, outputFilePath, d.getName()));
			}
		}

		executor.shutdown();
	}
}
