package com.callba.phone.util;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.os.Build.VERSION;

/** 
 * 剪切板管理类
 * @Author  zhw
 * @Version V1.0  
 * @Createtime：2014年5月19日 下午3:44:15 
 */
public class CalldaClipBoardHelper {
	
	/**
	 * 从剪切板中获取数据
	 * @return
	 */
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public static String getFromClipBoard(Context context) {
		String text = "";
		
		try {
			if(VERSION.SDK_INT < 11) {
				android.text.ClipboardManager mClipboardManager = 
						(android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
				
				CharSequence charSequence = mClipboardManager.getText();
				if(charSequence != null) {
					text = charSequence.toString();
				}
			} else {
				android.content.ClipboardManager mClipboardManager = 
						(android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
				ClipData clipData = mClipboardManager.getPrimaryClip();
				CharSequence charSequence = clipData.getItemAt(0).getText();
				if(charSequence != null) {
					text = charSequence.toString();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return text;
	}
	
	/**
	 * 保存文字到剪切板
	 * @param text
	 */
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public static void setToClipBoard(Context context, String text) {
		
		if(VERSION.SDK_INT < 11) {
			android.text.ClipboardManager mClipboardManager = 
					(android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
			mClipboardManager.setText(text);
		} else {
			android.content.ClipboardManager mClipboardManager = 
					(android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clipData = ClipData.newPlainText(text, text);
			mClipboardManager.setPrimaryClip(clipData);
		}
	}
}
 