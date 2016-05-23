package com.callba.phone.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class DBopenHelper extends SQLiteOpenHelper {
	private static final int DEFAULT_DB_VERSION = 1;// 当前数据库版本
	public static final String CONTACT_FRIENDS_TABLE_NAME = "friends";//好友
	public static final String BACK_CALL_TABLE_NAME = "backcall_history";//回拨
	public static final String DIRECT_CALL_TABLE_NAME = "directcall_history";//直拨
	public static final String BLACK_LIST_TABLE_NAME = "blacklist";// 黑名单
	public static final String MSM_TABLE_NAME = "msm";// 短信
	public static final String ADVERTISEMENT_TABLE_NAME = "advertisement";// 广告
	public static final String BACK_CALL_DB_NAME = "runtong_db";
	private String name;

	/**
	 * 
	 * @param context
	 * @param name
	 * @param tableName
	 */
	public DBopenHelper(Context context, String name, String tableName) {
		this(context, name, null, DEFAULT_DB_VERSION);
		this.name = tableName;
	}

	public DBopenHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql1 = "CREATE TABLE IF NOT EXISTS "
				+ BACK_CALL_TABLE_NAME
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, phoneNumber TEXT, displayName TEXT, TimeStamp TEXT)";
		db.execSQL(sql1);
//		String sql2 = "CREATE TABLE IF NOT EXISTS "
//				+ CONTACT_FRIENDS_TABLE_NAME
//				+ " (lookup_key INTEGER PRIMARY KEY AUTOINCREMENT, phoneNumber TEXT, displayName TEXT, location TEXT,photoName TEXT)";
//		db.execSQL(sql2);
		String sql3 = "CREATE TABLE IF NOT EXISTS "
				+ BLACK_LIST_TABLE_NAME
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT,lookup_key TEXT, phoneNumber TEXT, displayName TEXT, location TEXT,photoName TEXT)";
		db.execSQL(sql3);
		String sql4 = "CREATE TABLE IF NOT EXISTS "
				+ DIRECT_CALL_TABLE_NAME
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT,lookup_key TEXT, phoneNumber TEXT, displayName TEXT, TimeStamp TEXT,CallStatus integer)";
		db.execSQL(sql4);
		String sql = "CREATE TABLE IF NOT EXISTS "
				+ MSM_TABLE_NAME
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, phoneNumber TEXT, displayName TEXT, TimeStamp TEXT,body TEXT,type INTEGER,isRead INTEGER default 0,serviceId INTEGER)";
		db.execSQL(sql);
		String sql5 = "CREATE TABLE IF NOT EXISTS "
				+ ADVERTISEMENT_TABLE_NAME
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, pictureName TEXT, pictureUrl TEXT, videoName TEXT,videoUrl TEXT)";
		db.execSQL(sql5);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// 当数据库版本改变后调用此方法
		int upgradeVersion = oldVersion;

//		if (1 == upgradeVersion) {
//			// Drop tables
//			db.execSQL("DROP TABLE IF EXISTS " + DIRECT_CALL_TABLE_NAME);
//			db.execSQL("DROP TABLE IF EXISTS " + BLACK_LIST_TABLE_NAME);
//			// Create tables
//			upgradeVersion = 2;
//			onCreate(db);
//		}
//		if (2 == upgradeVersion) {
//			upgradeVersion = 3;
//			Logger.v("onUpgrade DB", "新建短消息数据库");
//		}
//		if (3 == upgradeVersion || 4 == upgradeVersion || 5 == upgradeVersion) {
//			// Drop tables
//			db.execSQL("DROP TABLE IF EXISTS " + MSM_TABLE_NAME);
//			Logger.v("onUpgrade DB", "新建短消息数据库");
//			String sql = "CREATE TABLE IF NOT EXISTS "
//					+ MSM_TABLE_NAME
//					+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, phoneNumber TEXT, displayName TEXT, TimeStamp TEXT,body TEXT,type INTEGER,isRead INTEGER default 0)";
//			db.execSQL(sql);
//			upgradeVersion = 6;
//			onCreate(db);
//		}
//		if (6 == upgradeVersion) {
//			String sql = "ALTER TABLE " + MSM_TABLE_NAME
//					+ " ADD serviceId INTEGER";
//			db.execSQL(sql);
//			upgradeVersion = 7;
//		}
//		if (7 == upgradeVersion) {
//			String sql5 = "CREATE TABLE IF NOT EXISTS "
//					+ ADVERTISEMENT_TABLE_NAME
//					+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, pictureName TEXT, pictureUrl TEXT, videoName TEXT,videoUrl TEXT)";
//			db.execSQL(sql5);
//			upgradeVersion = 8;
//		}
	}
}
