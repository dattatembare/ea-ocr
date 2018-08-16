package com.ea.ocr.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * @author Datta Tembare
 *
 */
public class CsvHelper {
	JsonElement pageNumber;
	JsonElement header;
	JsonElement address;
	JsonElement content;
	JsonElement footer;
	JsonElement details;
	JsonElement firstPage;

	boolean pageNumberExist;
	boolean headerExist;
	boolean addressExist;
	boolean contentExist;
	boolean footerExist;
	boolean detailsExist;
	boolean firstPageExist;

	public CsvHelper(String jsonStr) {
		Gson gson = new Gson();
		CsvHelper helper = gson.fromJson(jsonStr, CsvHelper.class);
		if (helper.pageNumber != null) {
			setPageNumberExist(true);
		}
		if (helper.header != null) {
			setHeaderExist(true);
		}
		if (helper.address != null) {
			setAddressExist(true);
		}
		if (helper.content != null) {
			setContentExist(true);
		}
		if (helper.footer != null) {
			setFooterExist(true);
		}
		if(helper.details != null){
			setDetailsExist(true);
		}
		if(helper.firstPage != null){
			setFirstPageExist(true);	
		}
	}

	public boolean isPageNumberExist() {
		return pageNumberExist;
	}

	public void setPageNumberExist(boolean pageNumberExist) {
		this.pageNumberExist = pageNumberExist;
	}

	public boolean isHeaderExist() {
		return headerExist;
	}

	public void setHeaderExist(boolean headerExist) {
		this.headerExist = headerExist;
	}

	public boolean isAddressExist() {
		return addressExist;
	}

	public void setAddressExist(boolean addressExist) {
		this.addressExist = addressExist;
	}

	public boolean isContentExist() {
		return contentExist;
	}

	public void setContentExist(boolean contentExist) {
		this.contentExist = contentExist;
	}

	public boolean isFooterExist() {
		return footerExist;
	}

	public void setFooterExist(boolean footerExist) {
		this.footerExist = footerExist;
	}

	public boolean isDetailsExist() {
		return detailsExist;
	}

	public void setDetailsExist(boolean detailsExist) {
		this.detailsExist = detailsExist;
	}

	public boolean isFirstPageExist() {
		return firstPageExist;
	}

	public void setFirstPageExist(boolean firstPageExist) {
		this.firstPageExist = firstPageExist;
	}
	
}
