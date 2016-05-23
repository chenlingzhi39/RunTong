package com.callba.phone.pay.alipay;


public class YeePayUtils {

	public static boolean checkYDCard(String name,String passWord) {

		if (name.length() == 17&&passWord.length() == 18) {
			return true;
		}
		return false;
	}
	
	public static boolean checkLTCard(String name,String passWord) {
		
		if (name.length() == 15&&passWord.length() == 19) {
			return true;
		}
		return false;
	}
	public static boolean checkDXCard(String name,String passWord) {
		
		if (name.length() == 19&&passWord.length() == 18) {
			return true;
		}
		return false;
	}
}
