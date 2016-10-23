package com.callba.phone.bean;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
/**
 * 拼音/号码检索key
 * @author zhw
 */
public class SearchSortKeyBean implements Serializable {
	private static final long serialVersionUID = 2L;

	// 检索到的文字高亮显示的颜色
	private static final String HIGH_LIGHT_COLOR = "#fa4407";

	private String chineseNameString = "";
	private List<String> shortPinYinArray;
	private List<String> fullPinYinArray;

	public String getChineseNameString() {
		return chineseNameString;
	}

	public void setChineseNameString(String chineseNameString) {
		this.chineseNameString = chineseNameString;
	}

	public String getShortPinYinString() {
		if (shortPinYinArray == null) {
			return "";
		}

		StringBuilder builder = new StringBuilder();
		for (String str : shortPinYinArray) {
			builder.append(str);
		}

		return builder.toString();
	}

	public String getFullPinYinString() {
		if (fullPinYinArray == null) {
			return "";
		}

		StringBuilder builder = new StringBuilder();
		for (String str : fullPinYinArray) {
			builder.append(str);
		}

		return builder.toString();
	}

	public List<String> getChineseNameArray() {
		List<String> chineseNameArray = new ArrayList<String>();
		char[] cs = chineseNameString.toCharArray();
		for (char c : cs) {
			chineseNameArray.add(String.valueOf(c));
		}
		return chineseNameArray;
	}

	public List<String> getShortPinYinArray() {
		return shortPinYinArray;
	}

	public void setShortPinYinArray(List<String> shortPinYinArray) {
		this.shortPinYinArray = shortPinYinArray;
	}

	public List<String> getFullPinYinArray() {
		return fullPinYinArray;
	}

	public void setFullPinYinArray(List<String> fullPinYinArray) {
		this.fullPinYinArray = fullPinYinArray;
	}

	/**
	 * 根据简拼部分字母获取格式化后的全拼
	 * 
	 * @param shortPinYin
	 * @return
	 */
	public String getFormattedFullPinYinByShortPY(String inputShortPinYin) {
		if(TextUtils.isEmpty(inputShortPinYin)) {
			return getFullPinYinString();
		}
		
		String shortPinYin = getShortPinYinString();
		
		inputShortPinYin = inputShortPinYin.toUpperCase();
		
		int shortPYIndex = shortPinYin.indexOf(inputShortPinYin);
		int length = inputShortPinYin.length();
		
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < fullPinYinArray.size(); i++) {
			String singlePinYin = fullPinYinArray.get(i);
			if (i >= shortPYIndex && i < shortPYIndex + length) {
				String s1 = singlePinYin.substring(0, 1);
				String s2 = singlePinYin.substring(1);

				builder.append("<font color='" + HIGH_LIGHT_COLOR + "'>" + s1
						+ "</font>");
				builder.append(s2);
			} else {
				builder.append(singlePinYin);
			}
		}

		return builder.toString();
	}

	/**
	 * 根据全拼部分获取格式化后的全拼
	 * 
	 * @param fullPinYin
	 * @return
	 */
	public String getFormattedFullPinYinByFullPY(String inputFullPinYin) {
		String fullPinYin = getFullPinYinString();
		int inputLength = inputFullPinYin.length();
		
		String tempFullPinYin = fullPinYin;
		
		for(int i=0; i<fullPinYin.length(); i++) {
			if(i+inputLength > fullPinYin.length()) break;
				
			String tempString = fullPinYin.substring(i, i+inputLength);
			if(tempString.equalsIgnoreCase(inputFullPinYin)) {
				
				tempFullPinYin = fullPinYin.replaceFirst(tempString, "<font color='"
						+ HIGH_LIGHT_COLOR + "'>" + tempString + "</font>");
				break;
			}
		}
		
		return tempFullPinYin;
	}

	 /**
	 * 根据简拼获取格式化后的中文
	 * @param shortPinYin
	 * @return
	 */
	 public String getFormattedChineseNameByShortPY(String inputShortPinYin) {
		if(inputShortPinYin == null || "".equals(inputShortPinYin)) {
			return chineseNameString;
		}
		
		inputShortPinYin = inputShortPinYin.toUpperCase();
		
		String shortPinYin = getShortPinYinString();

		int shortPYIndex = shortPinYin.indexOf(inputShortPinYin);
		int length = inputShortPinYin.length();
		
		List<String> chineseNameArray = getChineseNameArray();
		
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < chineseNameArray.size(); i++) {
			String singleChinese = chineseNameArray.get(i);
			if (i >= shortPYIndex && i < shortPYIndex + length) {
				String s1 = singleChinese.substring(0, 1);
				String s2 = singleChinese.substring(1);
		
				builder.append("<font color='" + HIGH_LIGHT_COLOR + "'>" + s1
						+ "</font>");
				builder.append(s2);
			} else {
				builder.append(singleChinese);
			}
		}
		
		return builder.toString();
	 }
	 
	 /**
	  * 根据全拼部分获取格式化后的中文
	  * @param inputFullPinYin
	  * @return
	  */
	 public String getFormattedChineseNameByFullPY(String inputFullPinYin) {
		 if(inputFullPinYin == null || "".equals(inputFullPinYin)) {
			 return chineseNameString;
		 }
		 
		 inputFullPinYin = inputFullPinYin.toLowerCase();
		 
		 String fullPinYinString = getFullPinYinString().toLowerCase();
		 List<String> fullPinYinArray = getFullPinYinArray();
		 List<String> chineseNameArray = getChineseNameArray();
		 
		 int index = fullPinYinString.indexOf(inputFullPinYin);
		 int indexLength = inputFullPinYin.length();
		 
		 boolean isStartIndexRecord = false;
		 int currLength = 0;
		 int arrayStartIndex = 0;	//中文列表开始索引
		 int arrayEndIndex = 0;		//中文列表结束索引
		 for(int i=0; i<fullPinYinArray.size(); i++) {
			 String str = fullPinYinArray.get(i);
			 currLength += str.length();
			 
			 if(isStartIndexRecord && currLength == index + indexLength) {
				 arrayEndIndex = i;
				 break;
			 } else if(isStartIndexRecord && currLength > index + indexLength){
				 arrayEndIndex = i-1;
				 break;
			 }
			 
			 if(!isStartIndexRecord && currLength >= index) {
				 arrayStartIndex = i;
				 isStartIndexRecord = true;
			 }
		 }
		 
		 StringBuilder builder = new StringBuilder();
		 for(int i=0; i<chineseNameArray.size(); i++) {
			 if(i>=arrayStartIndex && i<=arrayEndIndex) {
				 if(inputFullPinYin.contains(fullPinYinArray.get(i).toLowerCase())) {
					 builder.append("<font color='" + HIGH_LIGHT_COLOR + "'>" 
							 	+ chineseNameArray.get(i) + "</font>");
				 } else {
					 builder.append(chineseNameArray.get(i));
				 }
			 } else {
				 builder.append(chineseNameArray.get(i));
			 }
		 }
		 
		 return builder.toString();
	 }
}
