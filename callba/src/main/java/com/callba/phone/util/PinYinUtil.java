package com.callba.phone.util;

import android.text.TextUtils;

import com.callba.phone.bean.SearchSortKeyBean;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.ArrayList;
import java.util.List;

public class PinYinUtil {

	/**
	 * 汉字转换位汉语拼音首字母，英文字符不变
	 * @param chines 汉字
	 * @return 拼音
	 */
	public static String converterToFirstSpell(String chines) {
		String pinyinName = "";
		if(TextUtils.isEmpty(chines)) {
			return "#";
		}
		char[] nameChar = chines.toCharArray();
		HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
		defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		for (int i = 0; i < nameChar.length; i++) {
			if (nameChar[i] > 128) {
				try {
					String[] str = PinyinHelper.toHanyuPinyinStringArray(
							nameChar[i], defaultFormat);
					if (str != null) {
						pinyinName += String.valueOf(str[0].charAt(0)).toUpperCase();
						pinyinName += str[0].substring(1);
					}
				} catch (BadHanyuPinyinOutputFormatCombination e) {
					e.printStackTrace();
				}
			} else {
				pinyinName += nameChar[i];
			}
		}
		return pinyinName;
	}
	
	/**
	 * 汉字转换位拼音 搜索实体类
	 * @param chines
	 * @return
	 */
	public static SearchSortKeyBean converterPinYinToSearchBean(String chines) {
		SearchSortKeyBean searchSortKeyBean = new SearchSortKeyBean();
		List<String> fullPinYins = new ArrayList<String>();
		List<String> shortPinYins = new ArrayList<String>();
		
		char[] nameChar = chines.toCharArray();
		HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
		defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		for (int i = 0; i < nameChar.length; i++) {
			StringBuilder singlePinYin = new StringBuilder();
			StringBuilder fristLetter = new StringBuilder();
			
			if (nameChar[i] > 128) {
				try {
					String[] str = PinyinHelper.toHanyuPinyinStringArray(
							nameChar[i], defaultFormat);
					if (str != null) {
						singlePinYin.append(str[0]);
					}
				} catch (BadHanyuPinyinOutputFormatCombination e) {
					e.printStackTrace();
				}
			} else {
				singlePinYin.append(nameChar[i]);
			}
			
			if(TextUtils.isEmpty(singlePinYin)) {
				//过滤空字符
				continue;
			}
			
			String friLetter = String.valueOf(singlePinYin.charAt(0)).toUpperCase();
			fristLetter.append(friLetter);
			
			if(!TextUtils.isEmpty(singlePinYin.toString())) {
				fullPinYins.add(singlePinYin.toString());
			}
			if(!TextUtils.isEmpty(fristLetter.toString())) {
				shortPinYins.add(fristLetter.toString());
			}
		}
		
		searchSortKeyBean.setChineseNameString(chines);
		searchSortKeyBean.setFullPinYinArray(fullPinYins);
		searchSortKeyBean.setShortPinYinArray(shortPinYins);
		
		return searchSortKeyBean;
	}
	
	/**
	 * 获取姓名大写简称
	 * @param fullPinYin
	 * @return
	 */
	public static String getUpperCase(String fullPinYin) {
		char[] cs = fullPinYin.toCharArray();
		
		StringBuilder stringBuilder = new StringBuilder();
		for(char c : cs) {
			if(c >= 'A' && c <= 'Z') {
				stringBuilder.append(c);
			}
		}
		
		return stringBuilder.toString();
	}
	
	private static final String NUM_1 = " ";
	private static final String NUM_2 = "abc";
	private static final String NUM_3 = "def";
	private static final String NUM_4 = "ghi";
	private static final String NUM_5 = "jkl";
	private static final String NUM_6 = "mno";
	private static final String NUM_7 = "pqrs";
	private static final String NUM_8 = "tuv";
	private static final String NUM_9 = "wxyz";
	private static final String NUM_0 = "+";
	
	/**
	 * 根据九宫格数字转换成字母组合(所有组合)
	 * @param numbers
	 * @return
	 */
	public static List<String> convertNumber2FullPinYinGroup(String numbers) {
		List<String> combinedPinYinGroups = new ArrayList<String>();
		List<String> tempCombineGroups = new ArrayList<String>();
		
		List<String> oriNumString = new ArrayList<String>();
		
		if(numbers.contains("0")
				|| numbers.contains("1")
				|| "#".equals(numbers)
				|| "*".equals(numbers) ) {
			
			return combinedPinYinGroups;
		}
		
		for(int i=0; i<numbers.length(); i++) {
			switch (numbers.toCharArray()[i]) {
				case '0':
					oriNumString.add(NUM_0);
					break;
				case '1':
					oriNumString.add(NUM_1);
					break;
				case '2':
					oriNumString.add(NUM_2);
					break;
				case '3':
					oriNumString.add(NUM_3);
					break;
				case '4':
					oriNumString.add(NUM_4);
					break;
				case '5':
					oriNumString.add(NUM_5);
					break;
				case '6':
					oriNumString.add(NUM_6);
					break;
				case '7':
					oriNumString.add(NUM_7);
					break;
				case '8':
					oriNumString.add(NUM_8);
					break;
				case '9':
					oriNumString.add(NUM_9);
					break;
				default:
					break;
			}
		}
		
		if(oriNumString.isEmpty()) {
			return combinedPinYinGroups;
		}
		
		String s1 = oriNumString.get(0);
		char[] cs = s1.toCharArray();
		
		for(char c : cs) {
			tempCombineGroups.add(String.valueOf(c));
		}
		
		if(oriNumString.size() == 1) {
			combinedPinYinGroups.clear();
			combinedPinYinGroups.addAll(tempCombineGroups);
			
			return combinedPinYinGroups;
		}
		
		for(int j=1; j<oriNumString.size(); j++) {
			List<String> tempStringList = combineFullPinyinGroup(tempCombineGroups, oriNumString.get(j));
			tempCombineGroups.clear();
			
			tempCombineGroups.addAll(tempStringList);
		}
		
		combinedPinYinGroups.clear();
		combinedPinYinGroups.addAll(tempCombineGroups);
		
		return combinedPinYinGroups;
	}
	
	private static List<String> combineFullPinyinGroup(List<String> list1, String str2) {
		List<String> list = new ArrayList<String>();
		
		char[] cs = str2.toCharArray();
		
		for(int i=0; i<list1.size(); i++) {
			String strList1 = list1.get(i);
			for(int j=0; j<cs.length; j++) {
				String temStrList1 = strList1;
				temStrList1 += cs[j];
				
				list.add(temStrList1.toString());
			}
		}
		
		return list;
	}
	
	/**
	 * 根据九宫格数字转换成字母组合（在上次查询的基础上获取组合）
	 * @param numbers
	 * @return
	 */
	public static List<String> convertNumber2PinYinGroup(String number, List<String> lastSearchLetterGroup) {
		List<String> combinedPinYinGroups = new ArrayList<String>();
		
		String lastNumberLetters = "";
		
		if(number == null || "".equals(number)) {
			return combinedPinYinGroups;
		}
		
		char lastLetter = number.charAt(number.length()-1);
		
		switch (lastLetter) {
			case '0':
				lastNumberLetters = NUM_0;
				break;
			case '1':
				lastNumberLetters = NUM_1;
				break;
			case '2':
				lastNumberLetters = NUM_2;
				break;
			case '3':
				lastNumberLetters = NUM_3;
				break;
			case '4':
				lastNumberLetters = NUM_4;
				break;
			case '5':
				lastNumberLetters = NUM_5;
				break;
			case '6':
				lastNumberLetters = NUM_6;
				break;
			case '7':
				lastNumberLetters = NUM_7;
				break;
			case '8':
				lastNumberLetters = NUM_8;
				break;
			case '9':
				lastNumberLetters = NUM_9;
				break;
			default:
				break;
		}
		
		if(lastSearchLetterGroup==null || lastSearchLetterGroup.isEmpty()) {
			for(char c : lastNumberLetters.toCharArray()) {
				combinedPinYinGroups.add(String.valueOf(c));
			}
			return combinedPinYinGroups;
		}
		
		for(int j=0; j<lastSearchLetterGroup.size(); j++) {
			List<String> tempStringList = combinePinyinGroup(lastNumberLetters, lastSearchLetterGroup.get(j));
			
			combinedPinYinGroups.addAll(tempStringList);
		}
		
		return combinedPinYinGroups;
	}
	
	
	private static List<String> combinePinyinGroup(String lastLetter, String lastGroupStr) {
		List<String> list = new ArrayList<String>();
		char[] lastLetterGroup = lastLetter.toCharArray();
		
		for(char c : lastLetterGroup) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(lastGroupStr);
			stringBuilder.append(c);
			
			list.add(stringBuilder.toString());
		}
		
		return list;
	}
}
