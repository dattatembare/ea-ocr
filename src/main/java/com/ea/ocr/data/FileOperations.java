/**
 * 
 */
package com.ea.ocr.data;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
				log.info("Wait 10 seconds to check files count.");
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
				log.info("Wait 15 seconds to generate file {}.", file.getAbsolutePath());
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				log.error(e.getMessage());
			}
			return waitIfNotExist(file);
		}
	}
}
