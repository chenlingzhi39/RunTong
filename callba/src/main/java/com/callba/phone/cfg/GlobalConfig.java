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

	public static synchronized GlobalConfig getInstance() {
		if(globalConfig == null) {
			globalConfig = new GlobalConfig();
		}
		
		return globalConfig;
	}

	private List<ContactPersonEntity> contactBeans;//本地联系人列表
	private long lastInterceptCallTime;	//最后拦截呼叫时间
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
	/**
	 * 获取本地联系人
	 * @return
	 */
	public List<ContactPersonEntity> getContactBeans() {
		if(contactBeans == null) {
			contactBeans = new ArrayList<>();
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

	/**
	 * 将全局参数保存到outState中
	 * @param outState
	 */
	@SuppressWarnings("unchecked")
	public void saveGlobalCfg(Bundle outState) {
	    outState.putParcelable("message",message);
	    outState.putSerializable("appVersion",appVersionBean);
	/*	ArrayList list = new ArrayList();
		list.add(contactBeans);
		outState.putParcelableArrayList("contact", list);*/
	}

	/**
	 * 从保存的savedInstanceState还原全局参数
	 * @param savedInstanceState
	 */
	@SuppressWarnings("unchecked")
	public void restoreGlobalCfg(Bundle savedInstanceState) {
         message=savedInstanceState.getParcelable("message");
		appVersionBean=(AppVersionChecker.AppVersionBean) savedInstanceState.getSerializable("appVersion");
		//contactBeans = (ArrayList<ContactPersonEntity>) savedInstanceState.getParcelableArrayList("contact").get(0);

	}
}
 