/**
 * 
 */
package com.ea.ocr.data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;

/**
 * @author Datta Tembare
 *
 */

@Component
@PropertySources({ @PropertySource("classpath:ea.ocr.properties") })
public class EaOcrProperties {
	
	@Value("${gs.env.path}")
	private String gsEnvPath;

	@Value("${im.env.path}")
	private String imEnvPath;

	@Value("${tesseract.env.path}")
	private String tesseractEnvPath;

	public String getGsEnvPath() {
		return gsEnvPath;
	}

	public void setGsEnvPath(String gsEnvPath) {
		this.gsEnvPath = gsEnvPath;
	}

	public String getImEnvPath() {
		return imEnvPath;
	}

	public void setImEnvPath(String imEnvPath) {
		this.imEnvPath = imEnvPath;
	}

	public String getTesseractEnvPath() {
		return tesseractEnvPath;
	}

	public void setTesseractEnvPath(String tesseractEnvPath) {
		this.tesseractEnvPath = tesseractEnvPath;
	}

}
