/**
 * 
 */
package com.ea.ocr.data;

import static com.ea.ocr.data.EaOcrConstants.*;

import java.util.Map;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * @author Datta Tembare
 *
 */
public class StringOperations {
	/**
	 * 
	 * @param text
	 * @return
	 */
	public static String formatText(String text) {
		text = text.replaceAll("\n", EMPTY_STR);
		return removeSpecialChars(text);
	}
	
	/**
	 * 
	 * @param str
	 * @return
	 */
	public static String removeSpecialChars(String str) {
		str = str.replaceAll(":", EMPTY_STR);
		str = str.replaceAll(",", EMPTY_STR);
		str = str.replaceAll("\"", "'");
		str = str.replace("{", "(");
		str = str.replace("}", ")");
		str = str.replace("\\", EMPTY_STR);
		str = str.replace("=", EMPTY_STR);
		str = str.replace("-", EMPTY_STR);
		return str;
	}
	
	/**
	 * @param firstNLastPage
	 * @param fileContent
	 */
	public static String briefJsonStr(Map<String, String> firstNLastPage) {
		StringBuffer fileContent = new StringBuffer();
		fileContent.append("[");
		fileContent.append(firstNLastPage.get("firstPage"));
		fileContent.append(firstNLastPage.get("lastPage"));
		fileContent.append("]");

		return validateJson(fileContent.toString());
	}
	
	/**
	 * 
	 * @param str
	 * @return
	 */
	public static String validateJson(String str) {
		Gson gson = new Gson();
		JsonElement jsonElement = gson.fromJson(str, JsonElement.class);
		/*
		 * JsonArray contentArray = jsonElement.getAsJsonArray(); JsonElement
		 * lastPage = contentArray.get(l); //jsonElement.getAsJsonObject();
		 * JsonObject jsonObject = lastPage.getAsJsonObject(); JsonArray
		 * lastPagecontentArray = jsonObject.getAsJsonArray("content");
		 */
		return jsonElement.toString();
	}
	
	/**
	 * @param text
	 * @return
	 */
	public static String detailsStr(String text) {
		return "\"details\":\"" + removeSpecialChars(text) + "\"},";
	}
	
	/**
	 * @param text
	 * @return
	 */
	public static String houseNo(String text) {
		text = text.replaceAll(NO_TEXT, EMPTY_STR);
		return formatText(text);
	}
	
	/**
	 * 
	 * @param text
	 * @return
	 */
	public static String age(String text) {
		if (!text.equals(NO_TEXT)) {
			text = formatText(text);
			text = text.replaceAll("4A", "4");
			text = text.replaceAll("T7", "7");
			text = text.replaceAll("T1", "7");
			text = text.replaceAll("MA", "4");
			text = text.replaceAll("A", "4");
			text = text.replaceAll("B", "8");
			text = text.replaceAll("T", "7");
			text = Pattern.compile("^[0-9]{3}$").matcher(text).matches() ? text.substring(1) : text;
		}
		return text;
	}
	
	/**
	 * 
	 * @param text
	 * @return
	 */
	public static String getRelationVal(String text) {
		String relationVal = "";
		if (!text.equals(NO_TEXT)) {
			if (text.contains(":")) {
				relationVal = text.substring(text.indexOf(":") + 1).trim();
			} else {
				String textArr[] = text.split(" ");
				for (int i = 0; i < textArr.length; i++) {
					if (i > 2) {
						relationVal = relationVal + textArr[i];
					}
				}
			}
		}
		if (relationVal.isEmpty()) {
			relationVal = text;
		}
		return formatText(relationVal);
	}

}
