package com.callba.phone.cfg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;

import com.callba.phone.bean.Advertisement;
import com.callba.phone.bean.Contact;
import com.callba.phone.bean.DialAd;
import com.callba.phone.logic.contact.ContactEntity;
import com.callba.phone.logic.contact.ContactPersonEntity;
import com.callba.phone.util.AppVersionChecker;
import com.hyphenate.chat.EMMessage;

/** 
 * 存储程序全局变量
 * @Author  zhw
 * @Version V1.0  
 * @Createtime：2014年5月10日 下午5:19:25 
 */
public class GlobalConfig implements Serializable{
	private static GlobalConfig globalConfig;
	private GlobalConfig(){}
	private Context context;
	public void init(Context context){
		this.context=context;
	}

	public static synchronized GlobalConfig getInstance() {
		if(globalConfig == null) {
			globalConfig = new GlobalConfig();
		}
		
		return globalConfig;
	}

	private String sipIP;		//sip服务器地址
	private boolean isSipRegistered;//记录sip服务是否成功注册
	private List<ContactPersonEntity> contactBeans;//本地联系人列表
	private long lastInterceptCallTime;	//最后拦截呼叫时间
	private String ivPath;//键盘广告路径
	private String ivPathBack;//回拨广告路径
	private ArrayList<Advertisement> advertisements1;
	private ArrayList<Advertisement> advertisements2;
	private ArrayList<Advertisement> advertisements3;
	private DialAd dialAd;
	private long interval=600000;
    private AppVersionChecker.AppVersionBean appVersionBean;
    private EMMessage message;

	public EMMessage getMessage() {
		return message;
	}

	public void setMessage(EMMessage message) {
		this.message = message;
	}

	public AppVersionChecker.AppVersionBean getAppVersionBean() {
		return appVersionBean;
	}

	public void setAppVersionBean(AppVersionChecker.AppVersionBean appVersionBean) {
		this.appVersionBean = appVersionBean;
	}

	public DialAd getDialAd() {
		return dialAd;
	}

	public void setDialAd(DialAd dialAd) {
		this.dialAd = dialAd;
	}


	public ArrayList<Advertisement> getAdvertisements1() {
		return advertisements1;
	}

	public void setAdvertisements1(ArrayList<Advertisement> advertisements1) {
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
		outState.putString("sipip", sipIP);
		outState.putString("ivPath", ivPath);
		outState.putString("ivPathBack", ivPathBack);
		outState.putLong("interval",interval);
		outState.putSerializable("dialAd",dialAd);
		outState.putSerializable("appVersion",appVersionBean);
		ArrayList list1 = new ArrayList();
		list1.add(advertisements1);
		outState.putParcelableArrayList("advertisements1",list1);
		ArrayList list2 = new ArrayList();
		list2.add(advertisements2);
		outState.putParcelableArrayList("advertisements2",list2);
		ArrayList list3 = new ArrayList();
		list3.add(advertisements3);
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
		sipIP = savedInstanceState.getString("sipip");
		ivPath = savedInstanceState.getString("ivPath");
		ivPathBack = savedInstanceState.getString("ivPathBack");
		contactBeans = (ArrayList<ContactPersonEntity>) savedInstanceState.getParcelableArrayList("contact").get(0);
		interval=savedInstanceState.getLong("interval",interval);
		dialAd=(DialAd) savedInstanceState.getSerializable("dialAd");
		appVersionBean=(AppVersionChecker.AppVersionBean) savedInstanceState.getSerializable("appVersion");
		advertisements1=(ArrayList<Advertisement>)savedInstanceState.getParcelableArrayList("advertisements1").get(0);
		advertisements2=(ArrayList<Advertisement>)savedInstanceState.getParcelableArrayList("advertisements2").get(0);
		advertisements3=(ArrayList<Advertisement>)savedInstanceState.getParcelableArrayList("advertisements3").get(0);
	}
}
 