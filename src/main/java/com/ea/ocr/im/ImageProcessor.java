/**
 * 
 */
package com.ea.ocr.im;

import java.awt.Rectangle;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.process.Pipe;
import org.im4java.process.ProcessStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ea.ocr.data.EaOcrProperties;
import com.ea.ocr.data.JsonConfigReader;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

/**
 * @author UX012064
 *
 */
public class ImageProcessor {
	private static final Logger log = LoggerFactory.getLogger(ImageProcessor.class);
	
	EaOcrProperties props;
	ConvertCmd cmd;
	
	public ImageProcessor(EaOcrProperties props) {
		this.props = props;
		// Create command, Set Env path
		cmd = new ConvertCmd();
		cmd.setSearchPath(props.getImEnvPath());
	}
	
	public static void cropImage(String inputFile, String outputPath, Rectangle cropGeometry) throws Exception {
		File imageFile = new File(inputFile);
		BufferedImage img = ImageIO.read(imageFile);
		//Rectangle cropGeometry = ImageGeometry.getGeometry(geometry);
		
		IMOperation op = new IMOperation();
		op.addImage();
		op.crop(cropGeometry.width, cropGeometry.height, cropGeometry.x, cropGeometry.y);
		//op.addRawArgs("+profile","*");
		//op.addRawArgs("-quality","90.0");
		op.addImage();

		ConvertCmd cmd = new ConvertCmd();
		cmd.setSearchPath("C:/Program Files/ImageMagick-7.0.7-Q16/");

		cmd.run(op,img,outputPath);
	}
	
	public void cropNborder(JsonConfigReader config, String inputFile, String outputPath, String geometry,
			boolean trim) throws IOException, InterruptedException, IM4JavaException {
		File imageFile = new File(inputFile);
		BufferedImage img = ImageIO.read(imageFile);
		Rectangle cropGeometry = ImageGeometry.getGeometry(geometry);
		
		IMOperation op = new IMOperation();
		op.addImage();
		op.crop(cropGeometry.width, cropGeometry.height, cropGeometry.x, cropGeometry.y);
		//op.addRawArgs("+profile","*");
		//op.addRawArgs("-quality","90.0");
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
		op.addImage();

		ConvertCmd cmd = new ConvertCmd();
		cmd.setSearchPath("C:/Program Files/ImageMagick-7.0.7-Q16/");
		cmd.run(op,img,outputPath);
	}
	
	public InputStream createCrop(String inputFile, String outputPath, String geometry) throws FileNotFoundException {
		
		InputStream in = new FileInputStream(inputFile);
		Rectangle rectangle =ImageGeometry.getGeometry(geometry);
		
		IMOperation op = new IMOperation();
		op.addImage("-");

		op.crop(rectangle.height, rectangle.width, rectangle.x, rectangle.y);

		op.addImage("-");
		Pipe pipeIn = new Pipe(in, null);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Pipe pipeOut = new Pipe(null, out);

		// set up command
		ConvertCmd convert = new ConvertCmd();
		convert.setSearchPath("C:/Program Files/ImageMagick-7.0.7-Q16/");
		convert.setInputProvider(pipeIn);
		convert.setOutputConsumer(pipeOut);
		try {
			// convert.createScript("/home/dan/tmp/log.txt", op);
			convert.run(op);
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		return new ByteArrayInputStream(out.toByteArray());
	}
	
    public static BufferedImage readImage(File file) throws IOException {

        ImageInputStream stream = ImageIO.createImageInputStream(file);
        Iterator<ImageReader> iter = ImageIO.getImageReaders(stream);
        while (iter.hasNext()) {
            ImageReader reader = iter.next();
            reader.setInput(stream);

            BufferedImage image = null;
            ICC_Profile profile = null;
            try {
                image = reader.read(0);
            } catch (IIOException e) {
                
            }
            return image;
        }

        return null;
    }
    
    public static void resizeImage(){
    	InputStream is = null;
    	OutputStream os = null;
		try {
			is = new FileInputStream(new File("C:/EA/mp2-out/im/153/153-001/3/1-0.png"));
			os = new FileOutputStream(new File("C:/EA/mp2-out/im/153/153-001/3/result-1-0.png"));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} //input

		IMOperation op = new IMOperation();
		op.addImage("-");
		op.resize(200, 200);
		op.addImage("-");
		Pipe pipe = new Pipe(is, os);

		// set up command
		ConvertCmd convert = new ConvertCmd();
		convert.setSearchPath("C:/Program Files/ImageMagick-7.0.7-Q16/");
		convert.setInputProvider(pipe);
		convert.setOutputConsumer(pipe);
		
		try {
			convert.run(op);
		} catch (IOException | InterruptedException | IM4JavaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}
