package com.callba.phone.logic.login;
/** 
 * 用户登录监听器
 * @Author  zhw
 * @Version V1.0  
 * @Createtime：2014年5月22日 上午10:22:18 
 */
public interface UserLoginListener {
	/**
	 * 登录成功
	 */
	void loginSuccess(String[] resultInfo);
	/**
	 * 本地登录失败
	 * @param errorMsg 失败信息
	 */
	void localLoginFailed(UserLoginErrorMsg errorMsg);
	/**
	 * 接口返回失败
	 * @param info
	 */
	void serverLoginFailed(String errorInfo);
}