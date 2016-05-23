package com.callba.phone.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.callba.R;
import com.callba.phone.bean.AddressDao;

public class NumberAddressService {

	public static String getAddress(String number, String path,Context context) {
		// String pattern = "^1[3458]\\d{9}$";
		String address = context.getString(R.string.unknown_location);
		// if (number.matches(pattern)) {
		// // 手机号码
		// address = query("select location from mob_location where _id = ? ",
		// new String[] { number.substring(0, 7) },path);
		// if (address.equals("")) {
		// address = number;
		// }
//		String callnum= PhoneUtils.formatAvailPhoneNumber(number);
		number = formatAvailPhoneNumber(number);
		boolean flag1 = number.length() == 11 && !number.startsWith("0");
		boolean flag2 = number.length() > 11 && number.startsWith("01")
				&& !number.startsWith("010");
		if (flag1 || flag2) {
			// 手机号码
			if (number.startsWith("01")) {
				number=number.substring(1);
				Logger.v("shoujihaoma ", number);
			}
			address = query("select location from mob_location where _id = ? ",
					new String[] { number.substring(0, 7) }, path);
//			if (address.equals("")) {
//				address = number;
//			}
		} else {
			// 固定电话
			int len = number.length();
			switch (len) {
			case 4: // 模拟器
				address = context.getString(R.string.simulator);
				break;

			case 7: // 本地号码
				address = context.getString(R.string.local);
				break;

			case 8: // 本地号码
				address = context.getString(R.string.local);
				break;

			case 10: // 3位区号，7位号码
				address = query(
						"select location from tel_location where _id = ? limit 1",
						new String[] { number.substring(0, 3) }, path);
//				if (address.equals("")) {
//					address = number;
//				}
				break;

			case 11: // 3位区号，8位号码 或4位区号，7位号码
				address = query(
						"select location from tel_location where _id = ? limit 1",
						new String[] { number.substring(0, 3) }, path);
				if (address.equals("")) {
					address = query(
							"select location from tel_location where _id = ? limit 1",
							new String[] { number.substring(0, 4) }, path);
//					if (address.equals("")) {
//						address = number;
//					}
				}
				break;

			case 12: // 4位区号，8位号码
				address = query(
						"select location from tel_location where _id = ? limit 1",
						new String[] { number.substring(0, 4) }, path);
//				if (address.equals("")) {
//					address = number;
//				}
				break;

			default:
				break;
			}
		}
		return address;
	}
	public static String formatAvailPhoneNumber(String srcNumber) {
		if(TextUtils.isEmpty(srcNumber)) {
			return srcNumber;
		}
		 if(srcNumber.startsWith("+861")||srcNumber.startsWith("+860")) {
			srcNumber = srcNumber.substring(3 );
		} 
		srcNumber = srcNumber.replaceAll("-", "");
		srcNumber = srcNumber.replaceAll(" ", "");
		
		return srcNumber;
	}
	private static String query(String sql, String[] selectionArgs, String path) {
		String result = "";
		// String path = Environment.getExternalStorageDirectory()
		// + "/security/db/address.db";
//		Logger.v("NumberAddressService", "AddressDao.getAddressDB(path)"+path);
		try {
			SQLiteDatabase db = AddressDao.getAddressDB(path);
			if (db.isOpen()) {
				Cursor cursor = db.rawQuery(sql, selectionArgs);
				if (cursor.moveToNext()) {
					result = cursor.getString(0);
				}
				cursor.close();
				db.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

}
