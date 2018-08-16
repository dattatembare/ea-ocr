/**
 * 
 */
package com.ea.ocr.im;

import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.IdentifyCmd;
import org.im4java.process.ArrayListOutputConsumer;
import org.im4java.process.Pipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ea.ocr.data.EaOcrProperties;
import com.ea.ocr.data.JsonConfigReader;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

/**
 * @author Datta Tembare
 *
 */

//@Component
public class FormatImages {
	private static final Logger log = LoggerFactory.getLogger(FormatImages.class);

	EaOcrProperties props;
	ConvertCmd cmd;
	
	public FormatImages(EaOcrProperties props) {
		this.props = props;
		// Create command, Set Env path
		cmd = new ConvertCmd();
		cmd.setSearchPath(props.getImEnvPath());
	}
	
	/*public static void main(String[] args) {
		EaOcrProperties prop = new EaOcrProperties();
		JsonConfigReader config = null;
		try {
			config = new JsonConfigReader("./src/main/resources/mp.json");
		} catch (JsonSyntaxException | JsonIOException | FileNotFoundException e1) {
			log.error(e1.getMessage());
		}

		prop.setImEnvPath("C:/Program Files/ImageMagick-7.0.7-Q16");
		
		FormatImages formatImagesObj = new FormatImages(prop);

		// Pull 12 page row dimensions
		LinkedList<String> pageRows = config.getPageCropDimentions();

		// Crop all voters data to 30 pieces
		LinkedList<String> persons = config.getPersonCropDimentions();

		// Split the single person details to 6 pieces
		LinkedHashMap<String, String> personDetails = config.getElementCropDimentions();
		
		String cropDirPath = "C:/EA/mp2-out4/test";
		String cleanFile = cropDirPath+"/clean-3.png";
		log.info("START");
		int m = 0;
		for (String geometry : pageRows) {
			Rectangle row = ImageGeometry.getGeometry(geometry);
			if (m > 0 && m < 11) {
				int j = 0;
				for (String personGeo : persons) {
					// String personFilePath = cropDirPath + "/" + m + "-" + j +
					// ".png";
					// formatImagesObj.createCrop(cropFilePath, personFilePath,
					// personGeo);
					Rectangle person = ImageGeometry.getGeometry(personGeo);
					int k = 0;
					for (Map.Entry<String, String> entry : personDetails.entrySet()) {
						String personDetailsFilePath = cropDirPath + "/" + m + "-" + j + "-" + k + ".png";
						Rectangle element = ImageGeometry.getGeometry(entry.getKey());
						int x = row.x + person.x + element.x;
						int y = row.y + person.y + element.y;
						Rectangle finalGeo = new Rectangle(x, y, element.width, element.height);
						if (k == 4) {
							formatImagesObj.cropNborder(config, cleanFile, personDetailsFilePath, finalGeo,
									config.getCleaning().get(2));
						} else {
							formatImagesObj.cropNborder(config, cleanFile, personDetailsFilePath, finalGeo, true);
						}
						k++;
					}
					j++;
				}
			} else {
				String cropFilePath = cropDirPath + "/" + m + ".png";
				formatImagesObj.createCrop(cleanFile, cropFilePath, geometry);
			}
			m++;
		}
		log.info("START");
	}*/
	 

	/**
	 * 
	 * @param lastPageNo
	 * @param inputFilepath
	 * @param cleanFilePath
	 */
	public LinkedHashMap<File, String> processPage(JsonConfigReader config, File inputFile, String cropDirPath,
			long lastPageNo) {
		String inputFilePath = inputFile.getAbsolutePath();
		log.info("2. START file {} formatting", inputFilePath);

		String cleanFile = cropDirPath + "/clean-" + inputFile.getName();

		// clean and darken text on whole page
		cleanNdarkenTextOnPage(config, inputFilePath, cleanFile);

		// Image name is page number of pdf
		long pageNo = Long.parseLong(FilenameUtils.getBaseName(inputFile.getName()));

		// Define pages to crop to rows, this is required because last some
		// pages has overall numbers
		// Crop page to rows, then Crop single person's details and crop more to
		// separate single person details

		LinkedHashMap<File, String> file2Process = new LinkedHashMap<>();

		if (pageNo > 2 && pageNo <= lastPageNo && isValidDimention(config, cleanFile)) {

			// header convert hr-3.png +repage -crop {DIMENSIONS} +repage
			// +adjoin hr-3-0.png
			LinkedList<String> pageRows = config.getPageCropDimentions();

			// Crop one person details
			LinkedList<String> persons = config.getPersonCropDimentions();

			// Split the single person details
			LinkedHashMap<String, String> personDetails = config.getElementCropDimentions();
			
			List<String> deleteFiles = new ArrayList<>();
			deleteFiles.add(cleanFile);

			int i = 0;
			for (String geometry : pageRows) {
				String cropFilePath = cropDirPath + "/" + i + ".png";
				createCrop(cleanFile, cropFilePath, geometry);

				if (i > 0 && i < 11) {
					deleteFiles.add(cropFilePath);
					int j = 0;
					for (String personGeo : persons) {
						String personFilePath = cropDirPath + "/" + i + "-" + j + ".png";
						createCrop(cropFilePath, personFilePath, personGeo);
						// cropNClean(cropFilePath, personFilePath, personGeo);
						deleteFiles.add(personFilePath);
						int k = 0;
						for (Map.Entry<String, String> entry : personDetails.entrySet()) {
							String personDetailsFilePath = cropDirPath + "/" + i + "-" + j + "-" + k + ".png";
							if (k == 4) {
								cropNborder(config, personFilePath, personDetailsFilePath, entry.getKey(),
										config.getCleaning().get(2));
							} else {
								cropNborder(config, personFilePath, personDetailsFilePath, entry.getKey(), true);
							}
							file2Process.put(new File(personDetailsFilePath), entry.getValue());
							k++;
						}
						j++;
					}
				} else {
					file2Process.put(new File(cropFilePath), config.getDefaultTesseractLang()); // LANGUAGE_HINDI
				}
				i++;
			}
			
			log.info("Delete unwanted files");
			/*for (String deleteFile : deleteFiles) {
				deleteFile(deleteFile);
			}*/
		} else {
			file2Process.put(new File(cleanFile), config.getDefaultTesseractLang());
		}

		log.info("END file formatting");
		return file2Process;
	}

	public boolean isValidDimention(JsonConfigReader config, String cleanFile) {
		if(identyfyImg(cleanFile).get("dimentions").equals(config.getPageDimention())){
			return true;
		}
		return false;
	}
	
	/**
	 * 1. Set the environment-variable IM4JAVA_TOOLPATH. This variable should
	 * contain a list of directories to search for your tools separated by your
	 * platform-pathdelemiter (on *NIX typically ":", on Windows ";").
	 * 
	 * 2. Globally set the searchpath from within your java-progam: String
	 * myPath="C:\\Programs\\ImageMagick;C:\\Programs\\exiftool";
	 * ProcessStarter.setGlobalSearchPath(myPath);
	 * 
	 * 3. This will override any values set with IM4JAVA_TOOLPATH. Set the
	 * search path for an individual command: String
	 * imPath="C:\\Programs\\ImageMagick"; ConvertCmd cmd = new ConvertCmd();
	 * cmd.setSearchPath(imPath);
	 * 
	 * @param inputFilepath
	 * @param cleanFilePath
	 * @throws IM4JavaException
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void executeCmd(IMOperation op) {
		// execute the operation
		try {
			cmd.run(op);
		} catch (IOException | InterruptedException | IM4JavaException e) {
			log.error(e.getMessage());
		} 
	}

	/**
	 * IMOperation object for convert hr-3.png -brightness-contrast 5 -depth 8
	 * -quality 100 -flatten -sharpen 0x2.5 -morphology close diamond:1
	 * hr-03.png
	 * 
	 * @param config
	 * 
	 * @param inputFilepath
	 * @param outputFilePath
	 * @return
	 */
	public void cleanNdarkenTextOnPage(JsonConfigReader config, String inputFilepath, String outputFilePath) {
		IMOperation op = new IMOperation();
		op.addImage(inputFilepath);
		if (config.getCleaning().get(0)) {
			op.brightnessContrast(5d, 5d);
			op.depth(8);
			op.quality(100d);
			op.morphology("Smooth", "Ring:1");
		}

		// op.flatten();
		// op.sharpen(0d, 2.5);
		// op.morphology("close", "diamond:1");
		// op.morphology("close", "square:2:2");
		// op.morphology("Smooth", "Octagon:1");

		op.addImage(outputFilePath);

		executeCmd(op);
	}

	/**
	 * 
	 * @param inputFilepath
	 * @param outputFilePath
	 * @param cropGeometry
	 */
	public void createCrop(String inputFilepath, String outputFilePath, String cropGeometry) {
		ImageGeometry geometry = getImgGeometry(cropGeometry);
		ImageGeometry originalFileGeo = getImgGeometry(identyfyImg(inputFilepath).get("geometry"));

		IMOperation op = new IMOperation();
		op.addImage(inputFilepath);
		op.crop(geometry.getHeight(), geometry.getWidth(), geometry.getXscale() + originalFileGeo.getXscale(),
				geometry.getYscale() + originalFileGeo.getYscale());
		op.addImage(outputFilePath);

		executeCmd(op);

		op = null;
	}

	/**
	 * 
	 * @param inputFilepath
	 * @param outputFilePath
	 * @param cropGeometry
	 */
	public void cropNClean(String inputFilepath, String outputFilePath, String cropGeometry) {
		ImageGeometry geometry = getImgGeometry(cropGeometry);
		ImageGeometry originalFileGeo = getImgGeometry(identyfyImg(inputFilepath).get("geometry"));

		IMOperation op = new IMOperation();
		op.addImage(inputFilepath);
		op.crop(geometry.getHeight(), geometry.getWidth(), geometry.getXscale() + originalFileGeo.getXscale(),
				geometry.getYscale() + originalFileGeo.getYscale());
		// clean
		op.brightnessContrast(15d, 15d);
		op.depth(8);
		op.quality(100d);
		op.sharpen(0.0, 2.5);

		// op.morphology("Erode", "Octagon:1");
		// op.morphology("Open", "Octagon:1");
		// op.morphology("Close", "Octagon:1");
		// op.morphology("Smooth", "Octagon:1");
		// op.morphology("Erode", "Ring:1");
		// op.morphology("Open", "Ring:1");
		// op.morphology("Close", "Ring:1");
		// op.morphology("Smooth", "Ring:1");
		op.morphology("close", "diamond:1");
		op.addRawArgs("-threshold", "65%%");
		// op.addRawArgs("-blur", "1x1");

		op.addImage(outputFilePath);

		executeCmd(op);

		op = null;
	}

	/**
	 * 
	 * @param config
	 * @param inputFilepath
	 */
	public void cropNborder(JsonConfigReader config, String inputFilepath, String outputFilePath, String cropGeometry,
			boolean trim) {
		ImageGeometry geometry = getImgGeometry(cropGeometry);
		ImageGeometry originalFileGeo = getImgGeometry(identyfyImg(inputFilepath).get("geometry"));
		IMOperation op = new IMOperation();
		op.addImage(inputFilepath);
		op.crop(geometry.getHeight(), geometry.getWidth(), geometry.getXscale() + originalFileGeo.getXscale(),
				geometry.getYscale() + originalFileGeo.getYscale());
		// op.repage();
		if (trim) {
			op.trim();
		}
		op.bordercolor("White");
		op.border(7, 7);
		op.gravity("center");
		if (trim && config.getCleaning().get(1)) {
			op.addRawArgs("-resize", "150%%");
			//op.addRawArgs("-black-threshold", "30%%");
		}

		// op.repage();
		// op.adjoin();
		// op.brightnessContrast(10d, 10d);
		// op.unsharp(25.0, 25.0);
		op.addImage(outputFilePath);
		executeCmd(op);
		op = null;
	}
	
 	/**
	 * 
	 * @param config
	 * @param inputFilepath
	 */
	public void cropNborder(JsonConfigReader config, String inputFilepath, String outputFilePath, Rectangle cropGeometry,
			boolean trim) {
		
		IMOperation op = new IMOperation();
		op.addImage(inputFilepath);
		op.crop(cropGeometry.width, cropGeometry.height, cropGeometry.x, cropGeometry.y);
		// op.repage();
		if (trim) {
			op.trim();
		}
		op.bordercolor("White");
		op.border(7, 7);
		op.gravity("center");
		if (trim && config.getCleaning().get(1)) {
			op.addRawArgs("-resize", "150%%");
			//op.addRawArgs("-black-threshold", "30%%");
		}

		// op.repage();
		// op.adjoin();
		// op.brightnessContrast(10d, 10d);
		// op.unsharp(25.0, 25.0);
		op.addImage(outputFilePath);
		executeCmd(op);
		op = null;
	}

	/**
	 * 
	 * @param inputFilepath
	 */
	public void addBlackThreshold(String inputFilepath) {
		IMOperation op = new IMOperation();
		op.addImage(inputFilepath);
		op.negate();
		op.lat(25, 30, 30);
		op.negate();
		op.addRawArgs("-threshold", "50%%");
		// op.blur(1.0);
		op.addImage(inputFilepath);
		executeCmd(op);

		op = null;
	}

	/**
	 * 
	 * @param fileName
	 * @return
	 */
	private Map<String, String> identyfyImg(String fileName) {
		IMOperation op = new IMOperation();
		op.addImage(fileName);

		IdentifyCmd identifyCmd = new IdentifyCmd();
		ArrayListOutputConsumer output = new ArrayListOutputConsumer();
		identifyCmd.setOutputConsumer(output);
		try {
			identifyCmd.run(op);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		ArrayList<String> cmdOutput = output.getOutput();
		if (cmdOutput.size() != 1)
			return null;
		String line = cmdOutput.get(0);
		String[] arr = line.split(" ");

		Map<String, String> info = new HashMap<>();
		info.put("filePath", arr[0]);
		info.put("fileType", arr[1]);
		info.put("dimentions", arr[2]);
		info.put("geometry", arr[3]);
		info.put("depth", arr[4]);
		info.put("scale", arr[5]);
		info.put("colors", arr[6]);
		info.put("fileSize", arr[7]);
		info.put("userTime", arr[8]);
		info.put("unknown", arr[9]);
		return info;
	}

	/**
	 * Not using it
	 * 
	 * @param in
	 * @param rectangle
	 * @return
	 */
	public InputStream createCrop(InputStream in, Rectangle rectangle) {
		IMOperation op = new IMOperation();
		op.addImage("-");

		op.crop(rectangle.height, rectangle.width, rectangle.x, rectangle.y);

		op.addImage("-");
		Pipe pipeIn = new Pipe(in, null);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Pipe pipeOut = new Pipe(null, out);

		// set up command
		ConvertCmd convert = new ConvertCmd();
		convert.setInputProvider(pipeIn);
		convert.setOutputConsumer(pipeOut);
		try {
			// convert.createScript("/home/dan/tmp/log.txt", op);
			convert.run(op);
			// log.info("createCrop() :{}", convert.toString());
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		return new ByteArrayInputStream(out.toByteArray());
	}

	/**
	 * 
	 * @param geometryStr
	 * @return
	 */
	private ImageGeometry getImgGeometry(String geometryStr) {
		geometryStr = geometryStr.replace("x", ",").replace("+", ",").replace("-", ",-");
		String[] valList = geometryStr.split(",");

		ImageGeometry ig = new ImageGeometry();
		ig.setHeight(Integer.parseInt(valList[0]));
		ig.setWidth(Integer.parseInt(valList[1]));
		ig.setXscale(Integer.parseInt(valList[2]));
		ig.setYscale(Integer.parseInt(valList[3]));
		return ig;
	}

	public static boolean isInteger(String s) {
		boolean isValidInteger = false;
		try {
			Integer.parseInt(s);
			isValidInteger = true;
		} catch (NumberFormatException ex) {
			// s is not an integer
			log.error(ex.getMessage());
		}
		return isValidInteger;
	}
	
}
