/**
 * 
 */
package com.ea.ocr.im;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ea.ocr.data.EaOcrProperties;
import com.ea.ocr.data.JsonConfigReader;

/**
 * @author Datta Tembare
 *
 */
public class CropPage implements Runnable{
	private static final Logger log = LoggerFactory.getLogger(CropPage.class);
	
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
    
    /*public static void main(String[] args) {
    	log.info("Start");
    	EaOcrProperties prop = new EaOcrProperties();
		prop.setImEnvPath("C:/Program Files/ImageMagick-7.0.7-Q16");
		
		JsonConfigReader config = null;
		try {
			config = new JsonConfigReader("./src/main/resources/mp.json");
		} catch (JsonSyntaxException | JsonIOException | FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	CropPage cp = new CropPage(prop, config, "C:/EA/mp2-out/im/153/152-003/7/", "C:/EA/mp2-out/im/153/152-003/7/clean-7.png");
    	cp.cropPage();
    	log.info("End");
	}*/
    
    public void run(){
        try{
        	cropPage();
        }catch(Exception err){
            err.printStackTrace();
        }
    }
    
    /*private void cropPage() {
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
	}*/
    
    public void cropPage() {
		// Pull 12 page row dimensions
		LinkedList<String> pageRows = config.getPageCropDimentions();

		// Crop all voters data to 30 pieces 
		LinkedList<String> persons = config.getPersonCropDimentions();

		// Split the single person details to 6 pieces
		LinkedHashMap<String, String> personDetails = config.getElementCropDimentions();
		
		BufferedImage inputImg = null;
		try {
			inputImg = ImageIO.read(new File(cleanFile));
		} catch (IOException e) {
			e.printStackTrace();
		}

		ExecutorService executor= Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		int m = 0;
		for (String geometry : pageRows) {
			Rectangle row = ImageGeometry.getGeometry(geometry);
			if (m > 0 && m < 11) {
				int j = 0;
				for (String person : persons) {
					Rectangle personGeo = ImageGeometry.getGeometry(person);
					int k = 0;
					for (Map.Entry<String, String> entry : personDetails.entrySet()) {
						String personDetailsFilePath = cropDirPath + "/" + m + "-" + j + "-" + k + ".png";
						Rectangle personDetailGeo = ImageGeometry.getGeometry(entry.getKey());
						Rectangle cropGro = new Rectangle(row.x + personGeo.x + personDetailGeo.x,
								row.y + personGeo.y + personDetailGeo.y, personDetailGeo.width, personDetailGeo.height);
						executor.execute(new CropNborder(props, config, inputImg, k, cropGro, personDetailsFilePath));  
						k++;
					}
					j++;
				}
			}else{
				String cropFilePath = cropDirPath + "/" + m + ".png";
				formatImagesObj.cropImage(inputImg, cropFilePath, row);
			}
			m++;
		}
		
		executor.shutdown();
	}
    
    
}
