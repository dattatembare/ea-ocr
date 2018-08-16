/**
 * 
 */
package com.ea.ocr.data;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import com.ea.ocr.im.ImageGeometry;
import com.ea.ocr.tesseract.ReadImageText;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Datta Tembare
 *
 */
public class ExtractTableData {
	JsonElement table;

	/**
	 * 
	 * @param jsonElement
	 * @return
	 */
	public boolean isTableExist(JsonElement jsonElement) {
		Gson gson = new Gson();
		ExtractTableData thing = gson.fromJson(jsonElement, ExtractTableData.class);
		if (thing.table != null) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param props
	 * @param config
	 * @param file
	 * @param ele
	 * @param page
	 * @return
	 */
	public String fetchTableData(EaOcrProperties props, JsonConfigReader config, File file, JsonElement ele,
			String page) {
		ReadImageText readImageText = new ReadImageText(props);
		StringBuffer sb = new StringBuffer();
		LinkedList<String> firstRowkeys = new LinkedList<String>();
		Object[] firstRowVals = null;
		LinkedHashMap<String, String> firstRowMap = new LinkedHashMap<>();

		JsonArray tableArray = ele.getAsJsonObject().getAsJsonArray("table");

		int i = 0;
		for (JsonElement eleTable : tableArray) {
			if (i == 0) {
				// Table column Names
				Gson gson = new Gson();
				JsonObject columnNames = eleTable.getAsJsonObject();
				firstRowMap = gson.fromJson(columnNames, firstRowMap.getClass());
				for (String dim : firstRowMap.keySet()) {
					firstRowkeys.add(getOcrText(readImageText, file, config.getDefaultTesseractLang(), dim));
				}
				
				if (page.equals("firstPage")) {
					config.setFirstPageColmnOrder(firstRowkeys);
				} else if (page.equals("lastPage")) {
					config.setLastPageColmnOrder(firstRowkeys);
				}

				firstRowVals = firstRowMap.values().toArray();
			} else {
				// Table rows
				JsonArray tableRows = eleTable.getAsJsonArray();
				int j = 0;
				for (JsonElement eleRows : tableRows) {
					if (j == 0) {
						sb.append("{");
					}
					sb.append("\"" + firstRowkeys.get(j) + "\":\""
							+ getOcrText(readImageText, file, firstRowVals[j].toString(), eleRows.getAsString())
							+ "\"");
					if (i == tableArray.size() - 1 && j == tableRows.size() - 1) {
						sb.append("}");
					} else if (j == eleTable.getAsJsonArray().size() - 1) {
						sb.append("},");
					} else {
						sb.append(",");
					}
					j++;
				}
			}
			i++;
		}

		return sb.toString();
	}

	/**
	 * 
	 * @param readImageText
	 * @param file
	 * @param lang
	 * @param geo
	 * @return
	 */
	private String getOcrText(ReadImageText readImageText, File file, String lang, String geo) {
		String text = readImageText.ocrText(file, lang, ImageGeometry.getGeometry(geo)).trim();
		if (text.isEmpty()) {
			text = "X";
		} else {
			text = text.replaceAll("\n", " ");
			text = text.replaceAll(":", "");
			text = text.replaceAll(",", "");
			text = text.replaceAll("\"", "'");
			text = text.replace("{", "(");
			text = text.replace("}", ")");
			text = text.replace("\\", "");
			text = text.replace("=", "");
			text = text.replace("-", "");
		}
		return text;
	}
}
