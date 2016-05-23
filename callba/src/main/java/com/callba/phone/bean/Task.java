package com.callba.phone.bean;

import java.util.Map;

/**
 * 封装任务实体类
 * @author Zhang
 */
public class Task {
	public static final int TASK_SUCCESS = 200;
	public static final int TASK_FAILED = -1;
	public static final int TASK_TIMEOUT = -200;
	public static final int TASK_NETWORK_ERROR = -100;
	public static final int TASK_UNKNOWN_HOST = -300;
	
	public static final int TASK_GET_VERSION = 0;
	public static final int TASK_LOGIN = 1;
	public static final int TASK_REGISTER = 2;
	public static final int TASK_GET_SMS_KEY = 3;	//获取短信密钥
	public static final int TASK_GET_VERFICA_CODE = 4;	//获取验证码
	public static final int TASK_SEND_SMS = 5;	//发送短信
	public static final int TASK_CHANGE_PWD = 6;	//修改密码
	public static final int TASK_RETRIEVE_PWD = 7;	//找回密码
	public static final int TASK_SIGN = 8;	//每日签到
	public static final int TASK_INVITE_FRIEND = 9;	//每日签到
	public static final int TASK_SHOWNUMBER_QUERY = 10;	//查询显号设置
	public static final int TASK_SHOWNUMBER_SET = 11;	//设置显号设置
	public static final int TASK_FEE_QUERY = 12;	//查询费率
	public static final int TASK_BACKUP_CONTACT = 13;	//备份联系人
	public static final int TASK_GET_CONTACT_COUNT = 14;	//获取云端联系人个数
	public static final int TASK_RESTORE_CONTACT = 15;	//下载联系人
	public static final int TASK_GET_RECHARGE_INFO = 16;	//查询充值优惠信息
	public static final int TASK_GET_USER_BALANCE = 17;	//查询余额
	public static final int TASK_GET_RECHARGE_MEAL = 18;	//查询充值套餐
	public static final int TASK_GET_PREFERENTIAL_INFO = 19;	//查询公告、优惠信息
	public static final int TASK_QUERY_MEAL = 20;	//查询用户套餐
	public static final int TASK_QUERY_CALLLOG = 21;	//查询用户通话记录
	public static final int TASK_GET_SUBACCOUNT_NUM = 22;	//获取子账户数量
	public static final int TASK_GET_SUBACCOUNT_LIST = 23;	//获取子账户列表
	public static final int TASK_GET_SUBACCOUNT_YZM = 24;	//添加子账户 获取验证码
	public static final int TASK_ADD_SUBACCOUNT = 25;	//注册子账户
	public static final int TASK_ADD_SUBACCOUNT_GH = 26;	//注册固话子账户
	public static final int TASK_DELETE_SUBACCOUNT = 27;	//删除子账户
	public static final int TASK_SUBACCOUNT_CHANGEPASS = 28;	//修改子账户密码
	public static final int TASK_LOOK_REMOTE_CONTACT = 29;	//查看远程联系人
	/**
	 *  易宝支付任务
	 */
	public static final int TASK_YEE_PAY = 30; 
	/**
	  *  闰通卡支付任务
	  */
	public static final int TASK_CALLDA_PAY = 31;
	/**
	 *  闰通余额支付任务
	 */
	public static final int TASK_CALLDA_YUE_PAY = 32;
	/**
	 * 验证当前注册用户是否已存在
	 */
	public static final int TASK_VERIFY_USER_EXIST = 33;	
	/**
	 * 获取联系人中的注册用户
	 */
	public static final int TASK_SHOW_USER = 34;	
	/**
	 * 下载广告图片
	 */
	public static final int TASK_DOWNLOAD_AD = 35;	
	
	

	/**
	 * 任务编号
	 */
	private int TaskID;
	/**
	 * 任务参数
	 */
	private Map<String, Object> taskParams;

	public Task() {}

	public Task(int TaskID) {
		this.TaskID = TaskID;
	}
	
	public int getTaskID() {
		return TaskID;
	}

	public void setTaskID(int taskID) {
		TaskID = taskID;
	}

	public Map<String, Object> getTaskParams() {
		return taskParams;
	}

	public void setTaskParams(Map<String, Object> taskParams) {
		this.taskParams = taskParams;
	}

	@Override
	public String toString() {
		return "Task [TaskID=" + TaskID + ", taskParams=" + taskParams + "]";
	}
}
