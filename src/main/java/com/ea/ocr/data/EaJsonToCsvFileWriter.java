/**
 * 
 */
package com.ea.ocr.data;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Datta Tembare
 *
 */
public class EaJsonToCsvFileWriter {
	private static final Logger log = LoggerFactory.getLogger(EaJsonToCsvFileWriter.class);

	/**
	 * @param args
	 */
	/*public static void main(String[] args) {

		String fileName = "C:/EA/10.csv";
		LinkedList<String> elementOrder = new LinkedList<String>();
		elementOrder.add("Sr No");
		elementOrder.add("Voter Id");
		elementOrder.add("Name");
		elementOrder.add("Age");
		elementOrder.add("Gender");
		elementOrder.add("Father");
		elementOrder.add("Mother");
		elementOrder.add("Husband");
		elementOrder.add("Wife");
		elementOrder.add("Relative");
		elementOrder.add("House No");
		elementOrder.add("Work");
		elementOrder.add("Mobile No");
		elementOrder.add("Email Id");
		elementOrder.add("Children");
		elementOrder.add("Family Size");
		elementOrder.add("Religion");
		elementOrder.add("Cast");
		elementOrder.add("Category");
		elementOrder.add("Last Voted To");
		elementOrder.add("Possible AAP Voter");

		LinkedList<String> cols = new LinkedList<String>();
		cols.add("X");
		cols.add("नामावली का प्रकार");
		cols.add("नामावली की पहचान");
		cols.add("पुरूष");
		cols.add("महिला");
		cols.add("तृतीय लिंग");
		cols.add("कुल");
		
		LinkedList<String> cols1 = new LinkedList<String>();
		cols.add("आरम्भिक क्रम संख्या");
		cols.add("अंतिम क्रम संख्या");
		cols.add("पुरूष");
		cols.add("स्त्री");
		cols.add("अन्य");
		cols.add("योग");

		try {
			JsonConfigReader con = new JsonConfigReader("./src/main/resources/mp.json");
			con.setFinalElementOrder(elementOrder);
			con.setLastPageColmnOrder(cols);
			con.setFirstPageColmnOrder(cols1);

			Gson gson = new Gson();
			JsonElement json = gson.fromJson(new FileReader("C:/EA/EA-Workspace/ea-ocr/log/verify.json"),
					JsonElement.class);
			new EaJsonToCsvFileWriter().writeCsv(con, json.toString(), new File(fileName));
		} catch (JSONException | IOException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException
				| SecurityException e) {
			e.printStackTrace();
		}
	}*/

	/**
	 * 
	 * @param config.getFinalElementOrder()
	 * @param jsonString
	 * @param fileName
	 * @throws JSONException
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 */
	public void writeCsv(JsonConfigReader config, String jsonString, File fileName) throws JSONException, IOException,
			IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		jsonString = jsonString.replaceAll("null", "");
		jsonString = jsonString.replaceAll("NO TEXT", "");
		jsonString = "{\"fileContent\":" + jsonString.replaceAll("null", "") + "}";
		JSONObject output = new JSONObject(jsonString);
		JSONArray fileArr = output.getJSONArray("fileContent");
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < fileArr.length(); i++) {
			JSONObject obj = fileArr.getJSONObject(i);
			CsvHelper helper = new CsvHelper(obj.toString());

			if (helper.isPageNumberExist()) {
				sb.append("Page Number:" + obj.getString("pageNumber"));
			}

			if (helper.isFirstPageExist()) {
				sb.append("First Page:" + obj.getString("firstPage"));
			}

			if (helper.isHeaderExist()) {
				sb.append(" header:" + obj.getString("header"));
				sb.append("\r\n");
			}

			if (helper.isAddressExist()) {
				sb.append(" address:" + obj.getString("address"));
				sb.append("\r\n");
			}

			if (helper.isContentExist()) {
				JSONArray contentArr = obj.getJSONArray("content");
				if (fileName.getName().contains("-brief.csv")) {
					if (i < 2) { // firstPage indexes 0,1
						sb.append("\r\n");
						sb.append(CDL.toString(orderedJsonArray(config.getFirstPageColmnOrder(), contentArr)));
					} else if (i == fileArr.length() - 1) { // lastPage
						sb.append("\r\n");
						sb.append(CDL.toString(orderedJsonArray(config.getLastPageColmnOrder(), contentArr)));
					}
				} else {
					if (i <= 2) { // firstPage indexes 0,1,2
						sb.append("\r\n");
						sb.append(CDL.toString(orderedJsonArray(config.getFirstPageColmnOrder(), contentArr)));
					} else if (i == fileArr.length() - 1) { // lastPage
						sb.append("\r\n");
						sb.append(CDL.toString(orderedJsonArray(config.getLastPageColmnOrder(), contentArr)));
					} else {
						sb.append(CDL.toString(orderedJsonArray(config.getFinalElementOrder(), contentArr)));
					}
				}
			}

			if (helper.isFooterExist()) {
				sb.append("footer:" + obj.getString("footer"));
				sb.append("\r\n");
			}

			if (helper.isDetailsExist()) {
				sb.append(obj.toString());
				sb.append("\r\n");
			}

		}
		FileUtils.writeStringToFile(fileName, sb.toString(), "UTF-8");
		log.info("CSV file {} written successfully.", fileName.toString());
	}

	/**
	 * @param elementOrder
	 * @param contentArr
	 * @return
	 * @throws JSONException
	 */
	private JSONArray orderedJsonArray(LinkedList<String> elementOrder, JSONArray contentArr) throws JSONException {
		if (elementOrder != null && elementOrder.size() > 0) {
			LinkedList<JSONObject> orderedList = new LinkedList<>();
			for (int j = 0; j < contentArr.length(); j++) {
				JSONObject obj1 = contentArr.getJSONObject(j);
				LinkedHashMap<String, String> jsonOrderedMap = orderedMap(elementOrder, obj1.toString());
				orderedList.add(new JSONObject(jsonOrderedMap));
			}
			JSONArray orderedArray = new JSONArray(orderedList);
			return orderedArray;
		}
		return contentArr;
	}

	/**
	 * 
	 * @param parserName
	 * @param inputJson
	 * @return
	 */
	private LinkedHashMap<String, String> orderedMap(LinkedList<String> elementOrder, String inputJson) {
		Gson gson = new Gson();
		JsonElement jsonElement = gson.fromJson(inputJson, JsonElement.class);
		JsonObject jsonObject = jsonElement.getAsJsonObject();

		Map<String, String> map = new TreeMap<>();
		map = (Map<String, String>) gson.fromJson(jsonObject, map.getClass());

		LinkedHashMap<String, String> newMap = new LinkedHashMap<>();

		for (String col : elementOrder) {
			col = col.replaceAll("\"", "");
			newMap.put(col, map.get(col));
		}
		newMap.putAll(map);

		return newMap;
	}

}