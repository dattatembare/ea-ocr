/**
 * 
 */
package com.ea.ocr.im;

import java.awt.Rectangle;

/**
 * @author Datta Tembare
 *
 */
public class ImageGeometry {
	private Integer height;
	private Integer width;
	private Integer xscale;
	private Integer yscale;
	
	public Integer getHeight() {
		return height;
	}
	public void setHeight(Integer height) {
		this.height = height;
	}
	public Integer getWidth() {
		return width;
	}
	public void setWidth(Integer width) {
		this.width = width;
	}
	public Integer getXscale() {
		return xscale;
	}
	public void setXscale(Integer xscale) {
		this.xscale = xscale;
	}
	public Integer getYscale() {
		return yscale;
	}
	public void setYscale(Integer yscale) {
		this.yscale = yscale;
	}
	
	public static Rectangle getGeometry(String geometry){
		geometry = geometry.replace("x", ",").replace("+", ",").replace("-", ",-");
		String[] valList = geometry.split(",");
		
		int width = Integer.parseInt(valList[0]);
		int height = Integer.parseInt(valList[1]);
		int xscale = Integer.parseInt(valList[2]);
		int yscale = Integer.parseInt(valList[3]);
		
		// x-scale, y-scale, width and height
		return new Rectangle(xscale, yscale, width, height);
	}
	
}
