package com.ea.ocr.data;

import java.awt.Rectangle;

/**
 * @author Datta Tembare
 *
 */
public class PageDetails {
	private Rectangle geometry;
	private String language;
	private String fileText;
	private String fileName;
	private String elementName;
	
	public Rectangle getGeometry() {
		return geometry;
	}
	public void setGeometry(Rectangle geometry) {
		this.geometry = geometry;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getFileText() {
		return fileText;
	}
	public void setFileText(String fileText) {
		this.fileText = fileText;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getElementName() {
		return elementName;
	}
	public void setElementName(String elementName) {
		this.elementName = elementName;
	}
}
