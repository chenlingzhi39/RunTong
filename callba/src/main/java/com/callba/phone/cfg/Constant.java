package com.callba.phone.cfg;

import android.os.Environment;

/**
 * 保存系统配置的全局常量
 * @author Administrator
 */
public class Constant {
	public static final String PACKAGE_NAME = "callba";
	
	
	public static final String SENT_SMS_ACTION = "sent_sms_action";
	public static final String DELIVERED_SMS_ACTION = "delivered_sms_action";
	
	/**
	 * 秘钥
	 */
	public static final String SECRET_KEY = "version_key";
	
	/**
	 * 是否首次启动
	 */
	public static final String ISFRISTSTART = "isFristStart";
	
	/**
	 * 自动登录
	 */
	public static final String Auto_Login = "Auto_Login";
	
	/**
	 * 回拨自动接听
	 */
	public static final String BackCall_AutoAnswer = "BackCall_AutoAnswer";
	
	/**
	 * 键盘音设置
	 */
	public static final String KeyboardSetting = "KeyboardSetting";
	
	/**
	 * 监听系统拨号盘
	 */
	public static final String SYSTEM_DIAL_SETTING = "SystemDialSetting";
	
	//呼叫设置
	public static final String CALL_SETTING = "CallSetting";
	public static final String CALL_SETTING_ZHI_NENG = "zhineng";
	public static final String CALL_SETTING_HUI_BO = "huibo";
	public static final String CALL_SETTING_ZHI_BO = "zhibo";
	public static final String CALL_SETTING_SHOU_DONG = "shoudong";
	public static final String CALL_SETTING_WU_WANG_HB = "wuwanghb";
	
	//区号
	public static final String QU_HAO = "Quhao";
	
	/**
	 * 用户名、密码
	 */
	public static final String LOGIN_USERNAME = "username";
	public static final String LOGIN_PASSWORD = "pd_key";
	public static final String LOGIN_ENCODED_PASSWORD = "encode_key";
	/**
	 * 是否显示升级提示
	 */
	public static final String IS_NOTICE_UPGRADE = "upgradeNotice";
	
	/**
	 * 联系人个数
	 */
	public static final String CONTACTS_SIZE = "contactssize";
	
	public static final String IS_FROMGUIDE = "isFromGuide";
	
	/**
	 * 联系人搜索索引
	 */
	public static final String CONTACT_SEARCH_INDEX = "ABCDEFGHIJKLMNOPQRSTUVWXYZ#";
	
	/**
	 * 拦截系统拨号 号码及时间
	 */
	public static final String SYS_DIALER_CALLEE = "dialer_callee";
	public static final String SYS_DIALER_CALLTIME = "dialer_calltime";
	
	/**
	 * MainTab onResume 调用时的广播
	 */
	public static final String ACTION_TAB_ONRESUME = "action_tab_onresume";
	/**
	 * 移动代码
	 */
	public static final String ydCode="8";
	/**
	 * 联通代码
	 */
	public static final String ltCode="9";
	/**
	 * 电信代码
	 */
	public static final String dxCode="10";
	public static final String server_url = "https://msp.alipay.com/x.htm";
	 
	public static int reLoginTimes=0;
	
	
	/******************************************************/
	//					CONFIG PARAMS
	/******************************************************/
	
	/**
	 * 获取版本信息失败重试次数
	 */
	public static final int GETVERSION_RETRY_TIMES = 3;
	/**
	 * 登录失败重试次数
	 */
	public static final int LOGIN_RETRY_TIMES = 3;
	/**
	 * SIP呼叫失败重试次数
	 */
	public static final int SIP_CALL_RETRY_TIMES = 3;
	/**
	 * 九宫格拼音查询联系人，包含最近通话记录个数
	 */
	public static final int NINEPAD_QUERY_CALLLOG_COUNT = 10;
	/**
	 * 拦截系统拨号盘 保存的被叫号码是否可用时间（毫秒）
	 */
	public static final int SYS_DIALER_CALLEE_REMAIN_TIME = 10*1000;
	/**
	 * 无网回拨号码
	 */
	public static final String WWCALLBACK_NUM = "13340229980";
	/**
	 * 该软件下载自哪里
	 */
	public static final String DOWNLOAD_FROM = "";
	/**
	 * 接口接收的参数,记录调用该接口的软件类型
	 */
	public static final String SOFTWARE_TYPE = "android";
	/**
	 * 解压包位置
	 */
	public static String ZIP_PATH="";
	/**
	 * 数据库位置
	 */
	public static String DB_PATH="";
	/**
	 * zip压缩包名字
	 */
	public static final String DB_NAME="callHomeDB.zip";
	/**
	 * DBFILE_NAME 路径位置名字 /db
	 */
	public static final String DBFILE_NAME="/db";
	/**
	 * DBFILE_NAME 路径位置名字 /db
	 */
	public static final String ASSETS_NAME="callHomeDB.zip";
	/**
	 * 广告文件 路径位置名字 /ad
	 */
	public static final String ADVERTISEMENT_NAME="/ad/";
	/**
	 * 数据库位置
	 */
	public static String DB_PATH_ = "db_path";
	// 手机SD卡的路径
	public static final String SDCARD = Environment
			.getExternalStorageDirectory().getPath();

	public static final String PHOTO_PATH=SDCARD + "/callba/";
	public static final String LATITUDE="latitude";
	public static final String LONGITUDE="longitude";
	public static final String USER_AVATAR="user_avatar";
	public static final String NICKNAME="nickname";
	public static final String GOLD="gold";
	public static final String COMMISSION="commission";
	public static final String ADDRESS="address";
	public static final String SIGNATURE="signature";
}
