package com.callba.phone.util;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.android.internal.telephony.ITelephony;

public class PhoneUtils {
	
	/**
	 * 根据传入的TelephonyManager来取得系统的ITelephony实例.
	 * 
	 * @param telephony
	 * @return 系统的ITelephony实例
	 * @throws Exception
	 */
	public static  ITelephony getITelephony(TelephonyManager telephony)
			throws Exception{
		 Method getITelephonyMethod = telephony.getClass().getDeclaredMethod("getITelephony");   
	        getITelephonyMethod.setAccessible(true);//私有化函数也能使用   
	        return (ITelephony)getITelephonyMethod.invoke(telephony);  
	}
	
	/**
	 * 校验是否为合法的电话号码格式
	 * @param phoneNumber
	 * @return
	 */
	public static boolean isAvailPhoneNumber(String phoneNumber) {
		Pattern pattern = Pattern.compile("[+]?\\d+([-]\\d+)*");
		Matcher matcher = pattern.matcher(phoneNumber);
		
		return matcher.matches();
	}
	
	/**
	 * 格式化电话号码
	 * @param srcNumber
	 * @return
	 */
	public static String formatAvailPhoneNumber(String srcNumber) {
		if(TextUtils.isEmpty(srcNumber)) {
			return srcNumber;
		}
		
		if(srcNumber.startsWith("+860")) {
			srcNumber = srcNumber.substring(3 );
		} else if(srcNumber.startsWith("+861")) {
			srcNumber = srcNumber.replaceFirst("\\+86", "");
		} else if(srcNumber.startsWith("+86")) {
			srcNumber = srcNumber.replaceFirst("\\+86", "0");
		} else if(srcNumber.startsWith("+")) {
			srcNumber = srcNumber.replaceFirst("\\+", "00");
		}
		
		srcNumber = srcNumber.replaceAll("-", "");
		srcNumber = srcNumber.replaceAll(" ", "");
		
		return srcNumber;
	}
	
	/**
	 * 格式化 登录名
	 * @author zhw
	 *
	 * @param srcNumber
	 * @return
	 */
	public static String formatAvailLoginUser(String srcNumber) {
		if(TextUtils.isEmpty(srcNumber)) {
			return srcNumber;
		}
		
		if(srcNumber.startsWith("+86")) {
			srcNumber = srcNumber.substring(3);
		} else if(srcNumber.startsWith("+")) {
			srcNumber = srcNumber.substring(1);
		} else if(srcNumber.startsWith("86")
				&& srcNumber.length() > 11) {
			srcNumber = srcNumber.substring(2);
		}
		
		srcNumber = srcNumber.replace("-", "");
		
		return srcNumber;
	}
}
