/**
 * 
 */
package com.ea.ocr.im;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ea.ocr.data.EaOcrProperties;
import com.ea.ocr.data.JsonConfigReader;

/**
 * @author Datta Tembare
 *
 */
public class CropPage implements Runnable{
	private EaOcrProperties props;
	private JsonConfigReader config;
	private String cropDirPath;
	private String cleanFile;
	private FormatImages formatImagesObj;
	
    public CropPage(EaOcrProperties props, JsonConfigReader config, String cropDirPath, String cleanFile){
    	this.props = props;
    	this.config = config;
        this.cropDirPath = cropDirPath;
        this.cleanFile = cleanFile;
        formatImagesObj = new FormatImages(props);
    }
    
    public void run(){
        try{
        	cropPage();
        }catch(Exception err){
            err.printStackTrace();
        }
    }
    
    private void cropPage() {
		// Pull 12 page row dimensions
		LinkedList<String> pageRows = config.getPageCropDimentions();

		// Crop all voters data to 30 pieces 
		LinkedList<String> persons = config.getPersonCropDimentions();

		// Split the single person details to 6 pieces
		LinkedHashMap<String, String> personDetails = config.getElementCropDimentions();

		ExecutorService executor= Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		int m = 0;
		for (String geometry : pageRows) {
			String cropFilePath = cropDirPath + "/" + m + ".png";
			//formatImagesObj.cropCommand(cleanFile, cropFilePath, geometry); 
			formatImagesObj.createCrop(cleanFile, cropFilePath, geometry);
			if (m > 0 && m < 11) {
				int j = 0;
				for (String personGeo : persons) {
					String personFilePath = cropDirPath + "/" + m + "-" + j + ".png";
					//formatImagesObj.cropCommand(cropFilePath, personFilePath, personGeo);
					formatImagesObj.createCrop(cropFilePath, personFilePath, personGeo);
					int k = 0;
					for (Map.Entry<String, String> entry : personDetails.entrySet()) {
						String personDetailsFilePath = cropDirPath + "/" + m + "-" + j + "-" + k + ".png";
						executor.execute(new CropNborder(props, config, personFilePath, k, entry.getKey(), personDetailsFilePath));  
						k++;
					}
					j++;
				}
			}
			m++;
		}
		
		executor.shutdown();
	}
    
    
}
