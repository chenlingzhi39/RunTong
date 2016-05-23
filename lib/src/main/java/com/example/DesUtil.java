package com.example;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class DesUtil {
	/**
	 * 使用Des加密
	 * @param src
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static String encrypt(String src, String key) throws Exception {
		byte[] srcByte = src.getBytes();
		byte[] keyByte = key.getBytes();
		
		SecureRandom sr = new SecureRandom();
		DESKeySpec dks = new DESKeySpec(keyByte);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey securekey = keyFactory.generateSecret(dks);
		Cipher cipher = Cipher.getInstance("DES");
		cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);
		byte[] resultBytes = cipher.doFinal(srcByte);
		
		String hs = "";
		String stmp = "";
		for (int n = 0; n < resultBytes.length; n++) {
			stmp = (Integer.toHexString(resultBytes[n] & 0XFF));
			if (stmp.length() == 1)
				hs = hs + "0" + stmp;
			else
				hs = hs + stmp;
		}
		return hs.toUpperCase();
	}
}
