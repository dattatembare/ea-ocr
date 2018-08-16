/**
 * 
 */
package com.ea.ocr.data;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * @author Datta Tembare
 *
 */
public class JsonConfigReader {
	private LinkedList<Boolean> cleaning;
	private LinkedList<Object> elementOrder;
	private LinkedHashMap<String, String> relations = new LinkedHashMap<>();
	private LinkedList<String> genders;
	private LinkedList<String> newElements;
	private LinkedList<String> finalElementOrder;
	private String defaultTesseractLang;
	private String otherTesseractLang;
	private String pageDimention;
	private String addressDimentions;
	private LinkedList<String> pageCropDimentions;
	private LinkedList<String> personCropDimentions;
	private LinkedHashMap<String, String> elementCropDimentions = new LinkedHashMap<>();
	private LinkedHashMap<String, String> firstPage = new LinkedHashMap<>();
	private JsonArray firstPageTable;
	private LinkedHashMap<String, String> pollingCenter = new LinkedHashMap<>();
	private LinkedList<String> lastPageFinder;
	private JsonArray lastPageDimentions;
	private LinkedHashMap<String, String> voterCounts = new LinkedHashMap<>();
	private LinkedList<String> firstPageColmnOrder;
	private LinkedList<String> lastPageColmnOrder;

	public JsonConfigReader(String jsonFile) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		Gson gson = new Gson();
		JsonElement json = gson.fromJson(new FileReader(jsonFile), JsonElement.class);
		JsonObject jObj = json.getAsJsonObject();
		
		JsonArray clean = jObj.getAsJsonArray("cleaning");
		this.cleaning = gson.fromJson(clean, LinkedList.class);

		JsonArray eleOrder = jObj.getAsJsonArray("elementOrder");
		this.elementOrder = gson.fromJson(eleOrder, LinkedList.class);

		JsonObject relns = jObj.get("relations").getAsJsonObject();
		this.relations = gson.fromJson(relns, relations.getClass());

		JsonArray gendersExpected = jObj.getAsJsonArray("genders");
		this.genders = gson.fromJson(gendersExpected, LinkedList.class);

		JsonArray newElem = jObj.getAsJsonArray("newElements");
		this.newElements = gson.fromJson(newElem, LinkedList.class);

		JsonArray finalEleOrder = jObj.getAsJsonArray("finalElementOrder");
		this.finalElementOrder = gson.fromJson(finalEleOrder, LinkedList.class);

		defaultTesseractLang = jObj.get("defaultTesseractLang").getAsString();
		otherTesseractLang = jObj.get("otherTesseractLang").getAsString();
		
		pageDimention = jObj.get("pageDimention").getAsString();
		
		addressDimentions = jObj.get("address").getAsString();

		JsonArray page = jObj.getAsJsonArray("pageCrop");
		this.pageCropDimentions = gson.fromJson(page, LinkedList.class);

		JsonArray person = jObj.getAsJsonArray("personCrop");
		this.personCropDimentions = gson.fromJson(person, LinkedList.class);

		JsonObject elements = jObj.get("elementCrop").getAsJsonObject();
		this.elementCropDimentions = gson.fromJson(elements, elementCropDimentions.getClass());
		
		JsonObject firstPg = jObj.get("firstPage").getAsJsonObject();
		this.firstPage = gson.fromJson(firstPg, firstPage.getClass());
		
		JsonObject pollingCen = jObj.get("pollingCenter").getAsJsonObject();
		this.pollingCenter = gson.fromJson(pollingCen, pollingCenter.getClass());
		
		firstPageTable = jObj.getAsJsonArray("firstPageTable");

		JsonArray lastPageFind = jObj.getAsJsonArray("lastPageFinder");
		this.lastPageFinder = gson.fromJson(lastPageFind, LinkedList.class);
		
		lastPageDimentions = jObj.getAsJsonArray("lastPage");
		
		JsonObject vCounts = jObj.get("voterCounts").getAsJsonObject();
		this.voterCounts = gson.fromJson(vCounts, voterCounts.getClass());
	}
	
	public LinkedList<Boolean> getCleaning() {
		return cleaning;
	}

	public void setCleaning(LinkedList<Boolean> cleaning) {
		this.cleaning = cleaning;
	}

	public LinkedList<Object> getElementOrder() {
		return elementOrder;
	}

	public void setElementOrder(LinkedList<Object> elementOrder) {
		this.elementOrder = elementOrder;
	}

	public LinkedHashMap<String, String> getRelations() {
		return relations;
	}

	public void setRelations(LinkedHashMap<String, String> relations) {
		this.relations = relations;
	}

	public LinkedList<String> getGenders() {
		return genders;
	}

	public void setGenders(LinkedList<String> genders) {
		this.genders = genders;
	}

	public LinkedList<String> getNewElements() {
		return newElements;
	}

	public void setNewElements(LinkedList<String> newElements) {
		this.newElements = newElements;
	}

	public LinkedList<String> getFinalElementOrder() {
		return finalElementOrder;
	}

	public void setFinalElementOrder(LinkedList<String> finalElementOrder) {
		this.finalElementOrder = finalElementOrder;
	}

	public String getDefaultTesseractLang() {
		return defaultTesseractLang;
	}

	public void setDefaultTesseractLang(String defaultTesseractLang) {
		this.defaultTesseractLang = defaultTesseractLang;
	}

	public String getOtherTesseractLang() {
		return otherTesseractLang;
	}

	public void setOtherTesseractLang(String otherTesseractLang) {
		this.otherTesseractLang = otherTesseractLang;
	}
	
	public String getPageDimention() {
		return pageDimention;
	}

	public void setPageDimention(String pageDimention) {
		this.pageDimention = pageDimention;
	}

	public String getAddressDimentions() {
		return addressDimentions;
	}

	public void setAddressDimentions(String addressDimentions) {
		this.addressDimentions = addressDimentions;
	}

	public LinkedList<String> getPageCropDimentions() {
		return pageCropDimentions;
	}

	public void setPageCropDimentions(LinkedList<String> pageCropDimentions) {
		this.pageCropDimentions = pageCropDimentions;
	}

	public LinkedList<String> getPersonCropDimentions() {
		return personCropDimentions;
	}

	public void setPersonCropDimentions(LinkedList<String> personCropDimentions) {
		this.personCropDimentions = personCropDimentions;
	}

	public LinkedHashMap<String, String> getElementCropDimentions() {
		return elementCropDimentions;
	}

	public void setElementCropDimentions(LinkedHashMap<String, String> elementCropDimentions) {
		this.elementCropDimentions = elementCropDimentions;
	}
	
	public JsonArray getFirstPageTable() {
		return firstPageTable;
	}

	public void setFirstPageTable(JsonArray firstPageTable) {
		this.firstPageTable = firstPageTable;
	}

	public LinkedHashMap<String, String> getFirstPage() {
		return firstPage;
	}

	public void setFirstPage(LinkedHashMap<String, String> firstPage) {
		this.firstPage = firstPage;
	}

	public LinkedHashMap<String, String> getPollingCenter() {
		return pollingCenter;
	}

	public void setPollingCenter(LinkedHashMap<String, String> pollingCenter) {
		this.pollingCenter = pollingCenter;
	}

	public LinkedList<String> getLastPageFinder() {
		return lastPageFinder;
	}

	public void setLastPageFinder(LinkedList<String> lastPageFinder) {
		this.lastPageFinder = lastPageFinder;
	}

	public JsonArray getLastPageDimentions() {
		return lastPageDimentions;
	}

	public void setLastPageDimentions(JsonArray lastPageDimentions) {
		this.lastPageDimentions = lastPageDimentions;
	}

	public LinkedHashMap<String, String> getVoterCounts() {
		return voterCounts;
	}

	public void setVoterCounts(LinkedHashMap<String, String> voterCounts) {
		this.voterCounts = voterCounts;
	}

	public LinkedList<String> getFirstPageColmnOrder() {
		return firstPageColmnOrder;
	}

	public void setFirstPageColmnOrder(LinkedList<String> firstPageColmnOrder) {
		this.firstPageColmnOrder = firstPageColmnOrder;
	}

	public LinkedList<String> getLastPageColmnOrder() {
		return lastPageColmnOrder;
	}

	public void setLastPageColmnOrder(LinkedList<String> lastPageColmnOrder) {
		this.lastPageColmnOrder = lastPageColmnOrder;
	}

}
