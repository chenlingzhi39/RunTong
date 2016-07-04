package com.callba.phone.cfg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;

import com.callba.phone.bean.Advertisement;
import com.callba.phone.logic.contact.ContactPersonEntity;

/** 
 * 存储程序全局变量
 * @Author  zhw
 * @Version V1.0  
 * @Createtime：2014年5月10日 下午5:19:25 
 */
public class CalldaGlobalConfig implements Serializable{
	private static CalldaGlobalConfig calldaGlobalConfig;
	private CalldaGlobalConfig(){}
	
	public static CalldaGlobalConfig getInstance() {
		if(calldaGlobalConfig == null) {
			calldaGlobalConfig = new CalldaGlobalConfig();
		}
		
		return calldaGlobalConfig;
	}
	
	
	private String username;	//登录的用户名
	private String password;	//登录的密码
	private String secretKey;	//版本号返回的密钥
	private String sipIP;		//sip服务器地址
	private String loginToken;	//登陆成功 返回的密钥
	private String accountBalance;//账户余额
	private String callSetting;	//拨打设置
	private boolean isCallBackAutoAnswer;	//回拨是否自动接听
	private boolean isAutoLogin;//是否自动登陆
	private boolean isSipRegistered;//记录sip服务是否成功注册
	private List<ContactPersonEntity> contactBeans;//本地联系人列表
	private boolean keyBoardSetting;//键盘音设置
	private long lastInterceptCallTime;	//最后拦截呼叫时间
	private String ivPath;//键盘广告路径
	private String ivPathBack;//回拨广告路径
	private ArrayList<Advertisement> advertisements1;
	private ArrayList<Advertisement> advertisements2;
	private ArrayList<Advertisement> advertisements3;
	private double latitude;
	private double longitude;
	private String address;
	private long interval=600000;
	private String userhead;
    private String nickname;
	private String signature;
    private int gold;
    private String commission;

	public String getCommission() {
		return commission;
	}

	public void setCommission(String commission) {
		this.commission = commission;
	}

	public int getGold() {
		return gold;
	}

	public void setGold(int gold) {
		this.gold = gold;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getUserhead() {
		return userhead;
	}

	public void setUserhead(String userhead) {
		this.userhead = userhead;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public ArrayList<Advertisement> getAdvertisements1() {
		return advertisements1;
	}

	public void setAdvertisements1(ArrayList<Advertisement> advertisements) {
		this.advertisements1 = advertisements1;
	}

	public ArrayList<Advertisement> getAdvertisements2() {
		return advertisements2;
	}

	public void setAdvertisements2(ArrayList<Advertisement> advertisements2) {
		this.advertisements2 = advertisements2;
	}

	public ArrayList<Advertisement> getAdvertisements3() {
		return advertisements3;
	}

	public void setAdvertisements3(ArrayList<Advertisement> advertisements3) {
		this.advertisements3 = advertisements3;
	}

	public boolean getKeyBoardSetting() {
		return keyBoardSetting;
	}

	public void setKeyBoardSetting(boolean keyBoardSetting) {
		this.keyBoardSetting = keyBoardSetting;
	}

	/**
	 * 获取当前登录的用户
	 * @return
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * 设置当前登录的用户
	 * @param username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * 获取当前登录用户的密码
	 * @return
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * 设置当前用户登录的密码
	 * @param password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * 获取版本号返回的密钥
	 * @return
	 */
	public String getSecretKey() {
		return secretKey;
	}

	/**
	 * 设置版本号返回的密钥
	 * @param secretKey
	 */
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	/**
	 * 获取sip服务器地址
	 * @return
	 */
	public String getSipIP() {
		return sipIP;
	}

	/**
	 * 设置sip服务器地址
	 * @param sipIP
	 */
	public void setSipIP(String sipIP) {
		this.sipIP = sipIP;
	}

	/**
	 * 获取登陆成功 返回的密钥
	 * @return
	 */
	public String getLoginToken() {
		return loginToken;
	}

	/**
	 * 设置登陆成功 返回的密钥
	 * @param loginToken
	 */
	public void setLoginToken(String loginToken) {
		this.loginToken = loginToken;
	}

	/**
	 * 获取账户余额
	 * @return
	 */
	public String getAccountBalance() {
		return accountBalance;
	}

	/**
	 * 设置账户余额
	 * @param accountBalance
	 */
	public void setAccountBalance(String accountBalance) {
		this.accountBalance = accountBalance;
	}

	/**
	 * 获取呼叫设置
	 * @return
	 */
	public String getCallSetting() {
		return callSetting;
	}

	/**
	 * 设置呼叫设置
	 * @param callSetting
	 */
	public void setCallSetting(String callSetting) {
		this.callSetting = callSetting;
	}

	/**
	 * 获取回拨是否自动接听
	 * @return
	 */
	public boolean isCallBackAutoAnswer() {
		return isCallBackAutoAnswer;
	}
	
	/**
	 * 设置回拨是否自动接听
	 * @param isCallBackAutoAnswer
	 */
	public void setCallBackAutoAnswer(boolean isCallBackAutoAnswer) {
		this.isCallBackAutoAnswer = isCallBackAutoAnswer;
	}

	/**
	 * 获取直拨sip服务注册状态
	 * @return
	 */
	public boolean isSipRegistered() {
		try {
//			LinphoneManager.getInstance();
		} catch (Exception e) {
			isSipRegistered = false;
			return false;
		}
		return isSipRegistered;
	}

	/**
	 * 设置sip服务注册状态
	 * @param isSipRegistered
	 */
	public void setSipRegistered(boolean isSipRegistered) {
		this.isSipRegistered = isSipRegistered;
	}

	/**
	 * 获取是否自动登陆
	 * @return
	 */
	public boolean isAutoLogin() {
		return isAutoLogin;
	}

	/**
	 * 设置是否自动登陆
	 * @param isAutoLogin
	 */
	public void setAutoLogin(boolean isAutoLogin) {
		this.isAutoLogin = isAutoLogin;
	}
	
	/**
	 * 获取本地联系人
	 * @return
	 */
	public List<ContactPersonEntity> getContactBeans() {
		if(contactBeans == null) {
			contactBeans = new ArrayList<ContactPersonEntity>();
		}
		return contactBeans;
	}

	/**
	 * 设置本地联系人
	 * @param contactBeans
	 */
	public void setContactBeans(List<ContactPersonEntity> contactBeans) {
		this.contactBeans = contactBeans;
	}

	/**
	 * 获取最后一次拦截呼叫的时间
	 * @return
	 */
	public long getLastInterceptCallTime() {
		return lastInterceptCallTime;
	}

	/**
	 * 设置最后一次拦截呼叫的时间
	 * @return
	 */
	public void setLastInterceptCallTime(long lastInterceptCallTime) {
		this.lastInterceptCallTime = lastInterceptCallTime;
	}

	public String getIvPath() {
		return ivPath;
	}

	public void setIvPath(String ivPath) {
		this.ivPath = ivPath;
	}

	public String getIvPathBack() {
		return ivPathBack;
	}

	public void setIvPathBack(String ivPathBack) {
		this.ivPathBack = ivPathBack;
	}

	/**
	 * 将全局参数保存到outState中
	 * @param outState
	 */
	@SuppressWarnings("unchecked")
	public void saveGlobalCfg(Bundle outState) {
		outState.putString("username", username);
		outState.putString("password", password);
		outState.putString("secretkey", secretKey);
		outState.putString("logintoken", loginToken);
		outState.putString("sipip", sipIP);
		outState.putString("balance", accountBalance);
		outState.putString("callSetting", callSetting);
		outState.putString("ivPath", ivPath);
		outState.putString("ivPathBack", ivPathBack);
		outState.putBoolean("isBackcallAutoAnswer", isCallBackAutoAnswer);
		outState.putBoolean("isAutoLogin", isAutoLogin);
		outState.putBoolean("keyBoardSetting", keyBoardSetting);
		outState.putDouble("longitude",longitude);
		outState.putDouble("latitude",latitude);
		outState.putString("address",address);
		outState.putLong("interval",interval);
		outState.putString("userhead",userhead);
		outState.putString("nickname",nickname);
		outState.putString("signature",signature);
		outState.putInt("gold",gold);
		outState.putString("commission",commission);
		ArrayList list1 = new ArrayList();
		list1.add(advertisements1);
		outState.putParcelableArrayList("advertisements1",list1);
		ArrayList list2 = new ArrayList();
		list2.add(advertisements1);
		outState.putParcelableArrayList("advertisements2",list2);
		ArrayList list3 = new ArrayList();
		list3.add(advertisements1);
		outState.putParcelableArrayList("advertisements3",list3);
		@SuppressWarnings("rawtypes")
		ArrayList list = new ArrayList();
		list.add(contactBeans);
		outState.putParcelableArrayList("contact", list);
	}

	/**
	 * 从保存的savedInstanceState还原全局参数
	 * @param savedInstanceState
	 */
	@SuppressWarnings("unchecked")
	public void restoreGlobalCfg(Bundle savedInstanceState) {
		username = savedInstanceState.getString("username");
		password = savedInstanceState.getString("password");
		secretKey = savedInstanceState.getString("secretkey");
		loginToken = savedInstanceState.getString("logintoken");
		sipIP = savedInstanceState.getString("sipip");
		accountBalance = savedInstanceState.getString("balance");
		isCallBackAutoAnswer = savedInstanceState.getBoolean("isBackcallAutoAnswer");
		isAutoLogin = savedInstanceState.getBoolean("isAutoLogin");
		callSetting = savedInstanceState.getString("callSetting");
		ivPath = savedInstanceState.getString("ivPath");
		ivPathBack = savedInstanceState.getString("ivPathBack");
		keyBoardSetting = savedInstanceState.getBoolean("keyBoardSetting");
		contactBeans = (List<ContactPersonEntity>) savedInstanceState.getParcelableArrayList("contact").get(0);
		longitude=savedInstanceState.getDouble("longitude",longitude);
		latitude=savedInstanceState.getDouble("latitude",latitude);
		address=savedInstanceState.getString("address",address);
		interval=savedInstanceState.getLong("interval",interval);
		userhead=savedInstanceState.getString("userhead",userhead);
		nickname=savedInstanceState.getString("nickname",nickname);
		signature=savedInstanceState.getString("signature",signature);
		gold=savedInstanceState.getInt("gold",gold);
		commission=savedInstanceState.getString("commission",commission);
		advertisements1=(ArrayList<Advertisement>)savedInstanceState.getParcelableArrayList("advertisements1").get(0);
		advertisements2=(ArrayList<Advertisement>)savedInstanceState.getParcelableArrayList("advertisements2").get(0);
		advertisements3=(ArrayList<Advertisement>)savedInstanceState.getParcelableArrayList("advertisements3").get(0);
	}
}
 