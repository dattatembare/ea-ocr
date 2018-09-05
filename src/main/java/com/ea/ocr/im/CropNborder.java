package com.ea.ocr.im;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.ea.ocr.data.EaOcrProperties;
import com.ea.ocr.data.JsonConfigReader;

public class CropNborder implements Runnable {
	private JsonConfigReader config;
	private BufferedImage inputImg;
	private String personDetailsFilePath;
	private FormatImages formatImagesObj;
	private int k;
	private Rectangle geometry;

	public CropNborder(EaOcrProperties props, JsonConfigReader config, BufferedImage inputImg, int k, Rectangle geometry,
			String personDetailsFilePath) {
		this.config = config;
		this.inputImg = inputImg;
		this.personDetailsFilePath = personDetailsFilePath;
		this.geometry = geometry;
		this.formatImagesObj = new FormatImages(props);
	}

	public void run() {
		try {
			executeCropNborder();
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	/**
	 * @param config
	 * @param personFilePath
	 * @param k
	 * @param entry
	 * @param personDetailsFilePath
	 */
	private void executeCropNborder() {
			if (k == 4) {
				formatImagesObj.cropNborder(config, inputImg, personDetailsFilePath, geometry,
						config.getCleaning().get(2));
			} else {
				formatImagesObj.cropNborder(config, inputImg, personDetailsFilePath, geometry, true);
			}
	}

}