package com.ea.ocr.im;

import java.io.File;

import com.ea.ocr.data.EaOcrProperties;
import com.ea.ocr.data.FileOperations;
import com.ea.ocr.data.JsonConfigReader;

public class CropNborder implements Runnable {
	private JsonConfigReader config;
	private String personFilePath;
	private String personDetailsFilePath;
	private FormatImages formatImagesObj;
	private int k;
	private String geometry;

		public CropNborder(EaOcrProperties props, JsonConfigReader config, String personFilePath, int k,
				String geometry, String personDetailsFilePath){
	    	this.config = config;
	        this.personFilePath = personFilePath;
	        this.personDetailsFilePath = personDetailsFilePath;
	        this.geometry = geometry;
	        this.formatImagesObj = new FormatImages(props);
	    }
		
	    public void run(){
	        try{
	        	executeCropNborder();
	        }catch(Exception err){
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
			if(new FileOperations().waitIfNotExist(new File(personFilePath))){
				if (k == 4) {
					formatImagesObj.cropNborder(config, personFilePath,
							personDetailsFilePath, geometry, config.getCleaning().get(2)); 
				} else {
					formatImagesObj.cropNborder(config, personFilePath,
							personDetailsFilePath, geometry, true);
				}
			}
		}
		
		
}