/**
 * 
 */
package com.ea.ocr.data;

import java.util.LinkedList;

/**
 * @author Datta Tembare
 *
 */
public class PDFDetails {
	private long pageNumber;
	private long totalPages;
	private long lastPage;
	private String cleanFile;
	private LinkedList<PageDetails> pageDetails = new LinkedList<>();
	
	public long getPageNumber() {
		return pageNumber;
	}
	public void setPageNumber(long pageNumber) {
		this.pageNumber = pageNumber;
	}
	public long getTotalPages() {
		return totalPages;
	}
	public void setTotalPages(long totalPages) {
		this.totalPages = totalPages;
	}
	public long getLastPage() {
		return lastPage;
	}
	public void setLastPage(long lastPage) {
		this.lastPage = lastPage;
	}
	public String getCleanFile() {
		return cleanFile;
	}
	public void setCleanFile(String cleanFile) {
		this.cleanFile = cleanFile;
	}
	public LinkedList<PageDetails> getPageDetails() {
		return pageDetails;
	}
	public void setPageDetails(LinkedList<PageDetails> pageDetails) {
		this.pageDetails = pageDetails;
	}
}
