package com.callba.phone.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.callba.R;
import com.callba.phone.DemoHelper;
import com.callba.phone.activity.HomeActivity;
import com.callba.phone.activity.UserActivity;
import com.callba.phone.activity.WelcomeActivity;
import com.callba.phone.activity.contact.ContactBackupActivity;
import com.callba.phone.activity.contact.RemoteContactsActvity;
import com.callba.phone.activity.login.OnekeyRegisterAcitvity;
import com.callba.phone.activity.login.RegisterActivity;
import com.callba.phone.activity.more.AddSubAccountActivity;
import com.callba.phone.activity.more.ChangePasswordActivity;
import com.callba.phone.activity.more.FeeQueryActivity;
import com.callba.phone.activity.more.MoreInfoActivity;
import com.callba.phone.activity.more.PreferentialActivity;
import com.callba.phone.activity.more.QueryCalllogActivity;
import com.callba.phone.activity.more.QueryMealActivity;
import com.callba.phone.activity.more.RetrievePasswordActivity;
import com.callba.phone.activity.more.ShowNumberActivity;
import com.callba.phone.activity.more.SignActivity;
import com.callba.phone.activity.more.SubAccountActivity;
import com.callba.phone.activity.recharge.RechargeActivity;
import com.callba.phone.activity.recharge.RechargeMealActivity;
import com.callba.phone.bean.Task;
import com.callba.phone.bean.UserDao;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.cfg.Constant;
import com.callba.phone.logic.contact.ContactPersonEntity;
import com.callba.phone.logic.contact.QueryContactCallback;
import com.callba.phone.logic.contact.QueryContacts;
import com.callba.phone.logic.login.LoginController;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.HttpUtils;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.util.NetworkDetector;
import com.callba.phone.util.SharedPreferenceUtil;
import com.callba.phone.view.CalldaToast;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;

/**
 * 程序主服务，处理后台任务
 * @author Zhang
 */
public class MainService extends Service implements Runnable{
	private static final String TAG = MainService.class.getCanonicalName(); 
	/**
	 * 线程池最大线程数
	 */
	private static final int DEFAULT_MAXIMUM_POOL_SIZE = 2;
	private Thread mThread;
	private boolean isRun = true;// 线程开关
	private static ExecutorService fixedThreadPool;	//线程池
	/**
	 * 存储任务集合
	 */
	private static LinkedList<Task> calldaTaskList = new LinkedList<Task>(); // 存储要处理的所有任务
	private AMapLocationClient locationClient = null;
	private AMapLocationClientOption locationOption = null;
	UserDao userDao;
	LocationReceiver receiver;
	//监听联系人数据的监听对象
	private  ContentObserver mObserver = new ContentObserver(
			new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			// 当联系人表发生变化时进行相应的操作
			new QueryContacts(new QueryContactCallback() {
				@Override
				public void queryCompleted(List<ContactPersonEntity> contacts) {
					sendBroadcast(new Intent("com.callba.contact"));
				}
			}).loadContact(MainService.this);

		}
	};

	public static  ExecutorService getFixedThreadPool() {
		return fixedThreadPool;
	}

	/**
	 * 新建任务
	 * 
	 * @param task
	 */
	public static void newTask(Task task) {
		synchronized (calldaTaskList) {
			calldaTaskList.add(task);
			calldaTaskList.notify();
			Logger.i(TAG, "MainService newTask -> " + task);
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mThread = new Thread(this);
		mThread.start();
		fixedThreadPool = Executors.newFixedThreadPool(DEFAULT_MAXIMUM_POOL_SIZE);
		userDao=new UserDao();
		Log.i("service","oncreate");
		IntentFilter filter = new IntentFilter(
				"com.callba.location");
		receiver = new LocationReceiver();
		registerReceiver(receiver, filter);
		getContentResolver().registerContentObserver(
				ContactsContract.Contacts.CONTENT_URI, true, mObserver);

	}

	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case Task.TASK_GET_VERSION:// 获取版本
				Bundle bundle = (Bundle) msg.obj;
				String fromPage = bundle.getString("fromPage");
				if("UserActivity".equals(fromPage)){
					
					UserActivity userActivity= (UserActivity) ActivityUtil.getActivityByName("UserActivity");
					if (userActivity != null)
						userActivity.refresh(msg);
				}else if("WelcomeActivity".equals(fromPage)){
					WelcomeActivity wa = (WelcomeActivity) ActivityUtil.getActivityByName("WelcomeActivity");
					if (wa != null)
						wa.refresh(msg);
				}
				break;

//			case Task.TASK_LOGIN: // 登录
//				Bundle bundle = (Bundle) msg.obj;
//				String fromPage = bundle.getString("fromPage");
//				if ("login".equals(fromPage)) {
//					// 来自login页面
//					LoginActivity la = (LoginActivity) ActivityUtil.getActivityByName("LoginActivity");
//					if (la != null)
//						la.refresh(msg);
//				} else if ("main".equals(fromPage)) {
//					// 来自Main页面
//					MainCallActivity mca = (MainCallActivity) ActivityUtil.getActivityByName("MainCallActivity");
//					if (mca != null)
//						mca.refresh(msg);
//				} else if ("switchAccount".equals(fromPage)) {
//					// 来自切换账号页面
//					AccountSettingActivity asa = (AccountSettingActivity) ActivityUtil.getActivityByName("AccountSettingActivity");
//					if (asa != null)
//						asa.refresh(msg);
//				}else if("onekeyregisteracitvity".equals(fromPage)){
//					OnekeyRegisterAcitvity ora = (OnekeyRegisterAcitvity) ActivityUtil.getActivityByName("OnekeyRegisterAcitvity");
//					if (ora != null)
//						ora.refresh(msg);
//				}
//				break;

			case Task.TASK_GET_USER_BALANCE: // 获取余额
				HomeActivity homeActivity=(HomeActivity) ActivityUtil.getActivityByName("HomeActivity");
				if (homeActivity!=null)
					homeActivity.refresh(msg);

			/*	String balancefromPage = null;
				try {
					balancefromPage = balanceBundle.getString("frompage");
				} catch (Exception e) {
				}
				
				msg.obj = balanceBundle.getString("result");
				if(balancefromPage != null && balancefromPage.equals("QueryMealActivity")) {
					QueryMealActivity qma = (QueryMealActivity) ActivityUtil.getActivityByName("QueryMealActivity");
					if(qma != null) {
						qma.refresh(msg);
					}
					
				} else if(balancefromPage != null && balancefromPage.equals("MainCallActivity")) {
					MainCallActivity mca = (MainCallActivity) ActivityUtil.getActivityByName("MainCallActivity");
					if (mca != null)
						mca.refresh(msg);
				} else {
					try {
						String[] balanceResult = ((String) msg.obj).split("\\|");
						if("0".equals(balanceResult[0])) {
							String balance = balanceResult[1];
							CalldaGlobalConfig.getInstance().setAccountBalance(balance);
						}
					} catch (Exception e) {}
				}*/
				break;

			case Task.TASK_GET_SMS_KEY: // 获取短信key
				Bundle bundle1 = (Bundle) msg.obj;
				String fromPage1 = bundle1.getString("fromPage");
				if ("RegisterActivity".equals(fromPage1)) {
					RegisterActivity ra = (RegisterActivity) ActivityUtil.getActivityByName("RegisterActivity");
					if (ra != null)
						ra.refresh(msg);
				} else if ("RetrievePasswordActivity".equals(fromPage1)) {
					RetrievePasswordActivity rpa = (RetrievePasswordActivity) ActivityUtil.getActivityByName("RetrievePasswordActivity");
					if (rpa != null)
						rpa.refresh(msg);
				}
				break;

			case Task.TASK_GET_VERFICA_CODE:// 获取验证码
			case Task.TASK_REGISTER: // 注册
				RegisterActivity ra = (RegisterActivity) ActivityUtil.getActivityByName("RegisterActivity");
				if (ra != null)
					ra.refresh(msg);
				break;
				
			case Task.TASK_VERIFY_USER_EXIST:	//验证用户是否已注册
				OnekeyRegisterAcitvity ora = (OnekeyRegisterAcitvity) ActivityUtil.getActivityByName("OnekeyRegisterAcitvity");
				if(ora != null) {
					ora.refresh(msg);
				}
				break;

			case Task.TASK_SEND_SMS: // 发送短信
				if (msg.arg1 == Task.TASK_FAILED) {
					CalldaToast calldaToast = new CalldaToast();
					calldaToast.showToast(MainService.this, R.string.send_failed);
				} else {
					try {
						String result = (String) msg.obj;
						if ("1".equals(result.split("\\|")[0])) {
							// 返回值1开头，发送失败
							CalldaToast calldaToast = new CalldaToast();
							calldaToast.showToast(MainService.this, result.split("\\|")[1]);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;

			case Task.TASK_CHANGE_PWD: // 更改密码
				ChangePasswordActivity cpa = (ChangePasswordActivity) ActivityUtil.getActivityByName("ChangePasswordActivity");
				if(cpa != null)
					cpa.refresh(msg);
				break;

			case Task.TASK_RETRIEVE_PWD: // 找回密码
				RetrievePasswordActivity rpa = (RetrievePasswordActivity) ActivityUtil.getActivityByName("RetrievePasswordActivity");
				if (rpa != null)
					rpa.refresh(msg);
				else {
					showMessage(msg);
				}
				break;

			case Task.TASK_SIGN: // 每日签到
				SignActivity sa = (SignActivity) ActivityUtil.getActivityByName("SignActivity");
				if (sa != null)
					sa.refresh(msg);
				else {
					showMessage(msg);
				}
				break;

			case Task.TASK_SHOWNUMBER_QUERY:// 显号查询
				ShowNumberActivity snq = (ShowNumberActivity) ActivityUtil.getActivityByName("ShowNumberActivity");
				if (snq != null)
					snq.refresh(msg);
			case Task.TASK_SHOWNUMBER_SET:// 显号设置
				ShowNumberActivity sna = (ShowNumberActivity) ActivityUtil.getActivityByName("ShowNumberActivity");
				if (sna != null)
					sna.refresh(msg);
				break;

			case Task.TASK_FEE_QUERY:
				FeeQueryActivity fqa = (FeeQueryActivity) ActivityUtil.getActivityByName("FeeQueryActivity");
				if (fqa != null)
					fqa.refresh(msg);
				break;

			case Task.TASK_GET_CONTACT_COUNT:
			case Task.TASK_BACKUP_CONTACT:
				ContactBackupActivity cba = (ContactBackupActivity) ActivityUtil.getActivityByName("ContactBackupActivity");
				if (cba != null)
					cba.refresh(msg);
				break;

			case Task.TASK_LOOK_REMOTE_CONTACT:
				RemoteContactsActvity rca = (RemoteContactsActvity) ActivityUtil.getActivityByName("RemoteContactsActvity");
				ContactBackupActivity cba1 = (ContactBackupActivity) ActivityUtil.getActivityByName("ContactBackupActivity");
				if (rca != null) {
					rca.refresh(msg);
				} else if (cba1 != null) {
					cba1.refresh(msg);
				}
				break;

			case Task.TASK_GET_RECHARGE_INFO:
				RechargeActivity ra1 = (RechargeActivity) ActivityUtil.getActivityByName("RechargeActivity");
				if (ra1 != null)
					ra1.refresh(msg);
				break;

			case Task.TASK_GET_RECHARGE_MEAL:
				RechargeMealActivity rma = (RechargeMealActivity) ActivityUtil.getActivityByName("RechargeMealActivity");
				if (rma != null)
					rma.refresh(msg);
				break;

			case Task.TASK_GET_PREFERENTIAL_INFO:
				PreferentialActivity pa = (PreferentialActivity) ActivityUtil.getActivityByName("PreferentialActivity");
				if (pa != null)
					pa.refresh(msg);
				break;

			case Task.TASK_QUERY_MEAL:
				QueryMealActivity qma = (QueryMealActivity) ActivityUtil.getActivityByName("QueryMealActivity");
				if (qma != null)
					qma.refresh(msg);
				break;

			case Task.TASK_QUERY_CALLLOG:
				QueryCalllogActivity qca = (QueryCalllogActivity) ActivityUtil.getActivityByName("QueryCalllogActivity");
				if (qca != null)
					qca.refresh(msg);
				break;

			case Task.TASK_GET_SUBACCOUNT_NUM:
			case Task.TASK_GET_SUBACCOUNT_LIST:
			case Task.TASK_DELETE_SUBACCOUNT:
			case Task.TASK_SUBACCOUNT_CHANGEPASS:

				SubAccountActivity saa = (SubAccountActivity) ActivityUtil.getActivityByName("SubAccountActivity");
				if (saa != null)
					saa.refresh(msg);
				break;

			case Task.TASK_GET_SUBACCOUNT_YZM:
			case Task.TASK_ADD_SUBACCOUNT:
			case Task.TASK_ADD_SUBACCOUNT_GH:

				AddSubAccountActivity asaa = (AddSubAccountActivity) ActivityUtil.getActivityByName("AddSubAccountActivity");
				if (asaa != null)
					asaa.refresh(msg);
				break;
			case Task.TASK_SHOW_USER:
				
//				AddSubAccountActivity asaa = (AddSubAccountActivity) ActivityUtil.getActivityByName("AddSubAccountActivity");
//				if (asaa != null)
//					asaa.refresh(msg);
				break;

			default:
				break;
			}
		}

		/**
		 * 当前页面不在，使用Toast弹出后台处理消息
		 * 
		 * @param msg
		 */
		private void showMessage(Message msg) {
			try {
				String message = ((String) msg.obj).split("\\|")[1];
				
				CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(MainService.this, message);
			} catch (Exception e) {
				e.printStackTrace();

			}
		}
	};

	@Override
	public void run() {
		while (isRun) {
			Logger.i(TAG, "MainService tasklist size is -> " + calldaTaskList.size());
			
			synchronized (calldaTaskList) {
				if (calldaTaskList.size() > 0) {
					// 接受任务
					final Task currentTask = calldaTaskList.poll();
					Logger.i(TAG, "MainService thread Get out task -> " + currentTask);
					
					if(currentTask != null) {
						Logger.i(TAG, "MainService thread do task -> " + currentTask.getTaskID());
						if(fixedThreadPool == null) {
							fixedThreadPool = Executors.newFixedThreadPool(DEFAULT_MAXIMUM_POOL_SIZE);
						}
						fixedThreadPool.execute(new Runnable() {
							@Override
							public void run() {
								//处理任务
								dotask(currentTask);
							}
						});
					}
					
				} else {
					Logger.i(TAG, "MainService thread No task .. wait.");
					try {
						calldaTaskList.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
			
		}
		
		//关闭线程池
		try {
			if(fixedThreadPool != null) {
				fixedThreadPool.shutdown();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Logger.w(TAG, "Mainservice thread stop..");
	}

	/**
	 * 处理任务
	 * 
	 * @param currentTask
	 */
	private void dotask(Task task) {
		if(task == null) {
			Logger.w(TAG, "MainService dotask() task is null, return.");
			return;
		}
		Logger.i(TAG, "MainService dotask() task is -> " + task.getTaskID());
		Message msg = mHandler.obtainMessage();

		switch (task.getTaskID()) {
		case Task.TASK_GET_VERSION: // 获取版本
			getAppVersion(task, msg);
			break;

//		case Task.TASK_LOGIN: // 登录
//			login(task, msg);
//			break;

		case Task.TASK_GET_SMS_KEY:// 获取短信发送key
			getSmsCode(task, msg);
			break;

		case Task.TASK_GET_VERFICA_CODE:// 获取验证码
			getVerificaCode(task, msg);
			break;
			
		case Task.TASK_VERIFY_USER_EXIST://验证注册的用户是否已存在
			verifyUserExist(task, msg);
			break;

		case Task.TASK_REGISTER:// 注册
			register(task, msg);
			break;

		case Task.TASK_SEND_SMS:	//发送短信
			sendSMS(task, msg);
			break;

		case Task.TASK_CHANGE_PWD:	//更改密码
			changePwd(task, msg);
			break;

		case Task.TASK_RETRIEVE_PWD:	//找回密码
			retrievePwd(task, msg);
			break;

		case Task.TASK_SIGN:
			userSign(task, msg);
			break;

		// case Task.TASK_INVITE_FRIEND:
		// inviteFriend(task, msg);
		// break;

		case Task.TASK_SHOWNUMBER_QUERY:
			queryShowNum(task, msg);
			break;

		case Task.TASK_SHOWNUMBER_SET:
			setShowNum(task, msg);
			break;

		case Task.TASK_FEE_QUERY:
			feeQuery(task, msg);
			break;

		case Task.TASK_GET_CONTACT_COUNT:
			getContactCount(task, msg);
			break;

		case Task.TASK_BACKUP_CONTACT:
			backupContacts(task, msg);
			break;

		case Task.TASK_LOOK_REMOTE_CONTACT:
			getRemoteContact(task, msg);
			break;

		case Task.TASK_GET_RECHARGE_INFO:
			getRechargeInfo(task, msg);
			break;

		case Task.TASK_GET_USER_BALANCE:
			getUserBalance(task, msg);
			break;

		case Task.TASK_GET_RECHARGE_MEAL:
			getRechargeMeal(task, msg);
			break;

		case Task.TASK_GET_PREFERENTIAL_INFO:
			getPreferentailInfo(task, msg);
			break;

		case Task.TASK_QUERY_MEAL: // 查询用户套餐
			queryMeal(task, msg);
			break;

		case Task.TASK_QUERY_CALLLOG: // 查询用户通话记录
			queryCalllog(task, msg);
			break;

		case Task.TASK_GET_SUBACCOUNT_NUM: // 获取子账户个数
			getSubAccountNum(task, msg);
			break;

		case Task.TASK_GET_SUBACCOUNT_LIST: // 获取子账户列表
			getSubAccountList(task, msg);
			break;

		case Task.TASK_GET_SUBACCOUNT_YZM: // 添加子账户 获取验证码
			getSubAccountYZM(task, msg);
			break;

		case Task.TASK_ADD_SUBACCOUNT: // 添加子账户
			addSubAccountPhone(task, msg);
			break;

		case Task.TASK_ADD_SUBACCOUNT_GH: // 添加子账户 固话
			addSubAccountGH(task, msg);
			break;

		case Task.TASK_DELETE_SUBACCOUNT: // 删除子账户
			deleteSubAccount(task, msg);
			break;

		case Task.TASK_SUBACCOUNT_CHANGEPASS: // 修改子账户密码
			changeSubAccountPass(task, msg);
			break;
		case Task.TASK_SHOW_USER: // 获取通讯录中注册用户
			changeSubAccountPass(task, msg);
			break;

		default:
			break;
		}
	}

	/*-----------------------------Task----------------------------------- */

	/**
	 * 获取版本信息
	 */
	protected void getAppVersion(Task task, Message msg) {
		Map<String, Object> taskParams = task.getTaskParams();
		String fromPage = (String) taskParams.get("fromPage");
		String lan = (String) taskParams.get("lan");
		Bundle bundle = new Bundle();
		bundle.putString("fromPage", fromPage);
		try {
			String versionName = (String) task.getTaskParams().get(
					"versionName");
			Map<String, String> params = new HashMap<String, String>();
			params.put("softType", Constant.SOFTWARE_TYPE);
			params.put("download_from", Constant.DOWNLOAD_FROM);
			params.put("v", versionName);
			params.put("lan", lan);
			String result = HttpUtils.getDatafFromPostConnClose(Interfaces.Version
					, params);
			Log.i("result",result);
			msg.arg1 = Task.TASK_SUCCESS;
			bundle.putString("result",
					result.replace("\n", "").replace("\r", ""));
		} catch (Exception e) {
			msg.arg1 = Task.TASK_FAILED;
//			msg.obj = e.getMessage();
			e.printStackTrace();
		} finally {
			msg.what = task.getTaskID();
			msg.obj = bundle;
			mHandler.sendMessage(msg);
		}
	}

	/**
	 * 获取短信发送key
	 * 
	 * @param task
	 * @param msg
	 */
	private void getSmsCode(Task task, Message msg) {
		Map<String, Object> taskParams = task.getTaskParams();
		String phoneNum = (String) taskParams.get("phoneNum");
		String fromPage = (String) taskParams.get("fromPage");
		String lan = (String) taskParams.get("lan");
		Bundle bundle = new Bundle();
		bundle.putString("fromPage", fromPage);
		try {
			String result = HttpUtils.getDataFromHttpGet(Interfaces.SMS_Key
					+ phoneNum+"&lan="+lan);
			msg.arg1 = Task.TASK_SUCCESS;
			Logger.v("TASK_GET_SMS_KEY  http:", Interfaces.SMS_Key
					+ phoneNum+"&lan="+lan);
			bundle.putString("result",
					result.replace("\n", "").replace("\r", ""));
		} catch (Exception e) {
			msg.arg1 = Task.TASK_FAILED;
			e.printStackTrace();
		} finally {
			msg.what = task.getTaskID();
			msg.obj = bundle;
			mHandler.sendMessage(msg);
		}
	}

	/**
	 * 获取短信验证码
	 * 
	 * @param task
	 * @param msg
	 */
	private void getVerificaCode(Task task, Message msg) {
		Map<String, Object> taskParams = task.getTaskParams();
		String phoneNumber = (String) taskParams.get("encryptedPhoneNum");
		String sign = (String) taskParams.get("verificaSign");
		String lan = (String) taskParams.get("lan");

		try {
			String urlstring = Interfaces.Verification_Code + "phoneNumber="
					+ phoneNumber + "&sign=" + sign +"&lan="+lan+ "&softType=android";
			Logger.i("注册获取验证码URL", urlstring);
			String result = HttpUtils.getDataFromHttpGet(urlstring);

			msg.arg1 = Task.TASK_SUCCESS;
			msg.obj = result.replace("\n", "").replace("\r", "");
		} catch (Exception e) {
			msg.arg1 = Task.TASK_FAILED;
			e.printStackTrace();
		} finally {
			msg.what = task.getTaskID();
			mHandler.sendMessage(msg);
		}
	}
	
	/**
	 * 验证用户是否已注册
	 * @param task
	 * @param msg
	 */
	private void verifyUserExist(Task task, Message msg) {
		Map<String, Object> taskParams = task.getTaskParams();
		String v = (String) taskParams.get("v");
		String lan = (String) taskParams.get("lan");
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("v", v);
		params.put("lan", lan);
		
		try {
			String result = HttpUtils.getDataFromHttpPost(Interfaces.VerifyRegistedUserExist, params);
			msg.obj = result;
			msg.arg1 = Task.TASK_SUCCESS;
		} catch (Exception e) {
			msg.arg1 = Task.TASK_FAILED;
			e.printStackTrace();
		} finally {
			msg.what = task.getTaskID();
			mHandler.sendMessage(msg);
		}
	}

	/**
	 * 用户注册
	 * 
	 * @param task
	 * @param msg
	 */
	private void register(Task task, Message msg) {
		Map<String, Object> taskParams = task.getTaskParams();
		String username = (String) taskParams.get("phoneNumber");
		String password = (String) taskParams.get("password");
		String verifiCode = (String) taskParams.get("code");
		String softType = (String) taskParams.get("softType");
		String countryCode = (String) taskParams.get("countryCode");

		Map<String, String> params = new HashMap<String, String>();
		params.put("phoneNumber", username);
		params.put("password", password);
		params.put("code", verifiCode);
		params.put("softType", softType);
		params.put("countryCode",countryCode);

		try {
			if (NetworkDetector.detect(this)) {
				String content = HttpUtils.getDataFromHttpPost(
						Interfaces.Register, params);
				Logger.i(TAG, Interfaces.Register+params);
				msg.arg1 = Task.TASK_SUCCESS;
				msg.obj = content.replace("\n", "").replace("\r", "");
			} else {
				msg.what = Task.TASK_NETWORK_ERROR;
			}
		} catch (Exception e) {
			msg.arg1 = Task.TASK_FAILED;
			e.printStackTrace();
		} finally {
			msg.what = task.getTaskID();
			mHandler.sendMessage(msg);
		}
	}

	/**
	 * 用户登录
	 */
//	private void login(Task task, Message msg) {
//		Map<String, Object> taskParams = task.getTaskParams();
//		String loginSign = (String) taskParams.get("loginSign");
//		String loginType = (String) taskParams.get("loginType");
//		String fromPage = (String) taskParams.get("fromPage"); // 来自哪个页面的登录请求
//
//		Map<String, String> params = new HashMap<String, String>();
//		params.put("loginSign", loginSign);
//		params.put("loginType", loginType);
//		Bundle bundle = new Bundle();
//		bundle.putString("fromPage", fromPage);
//		try {
//			if (NetworkDetector.detect(this)) {
//				String content = HttpUtils.getDataFromHttpPost(
//						Interfaces.Login, params);
//
//				msg.arg1 = Task.TASK_SUCCESS;
//				
//				bundle.putString("result",
//						content.replace("\n", "").replace("\r", ""));
//				
//			} else {
//				msg.what = Task.TASK_NETWORK_ERROR;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			msg.arg1 = Task.TASK_FAILED;
//		} finally {
//			msg.what = task.getTaskID();
//			msg.obj = bundle;
//			mHandler.sendMessage(msg);
//		}
//	}

	/**
	 * 发送短信
	 * 
	 * @param task
	 * @param msg
	 */
	private void sendSMS(Task task, Message msg) {
		Map<String, Object> taskParams = task.getTaskParams();
		String loginName = (String) taskParams.get("loginName");
		String loginPwd = (String) taskParams.get("loginPwd");
		String softType = (String) taskParams.get("softType");
		String phoneNumber = (String) taskParams.get("phoneNumber");
		String mesg = (String) taskParams.get("msg");
		String lan = (String) taskParams.get("lan");

		Map<String, String> params = new HashMap<String, String>();
		params.put("loginName", loginName);
		params.put("loginPwd", loginPwd);
		params.put("softType", softType);
		params.put("phoneNumber", phoneNumber);
		params.put("msg", mesg);
		params.put("lan", lan);

		handleDataByPost(Interfaces.Send_SMS, task, msg, params);
	}

	/**
	 * 修改密码
	 * 
	 * @param task
	 * @param msg
	 */
	private void changePwd(Task task, Message msg) {
		Map<String, Object> taskParams = task.getTaskParams();
		String loginName = (String) taskParams.get("loginName");
		String loginPwd = (String) taskParams.get("loginPwd");
		String softType = (String) taskParams.get("softType");
		String oldPwd = (String) taskParams.get("oldPwd");
		String newPwd = (String) taskParams.get("newPwd");
		String lan = (String) taskParams.get("lan");

		Map<String, String> params = new HashMap<String, String>();
		params.put("loginName", loginName);
		params.put("loginPwd", loginPwd);
		params.put("softType", softType);
		params.put("oldPwd", oldPwd);
		params.put("newPwd", newPwd);
		params.put("lan", lan);

		handleDataByPost(Interfaces.Change_Pass, task, msg, params);
	}

	/**
	 * 找回密码
	 * 
	 * @param task
	 * @param msg
	 */
	private void retrievePwd(Task task, Message msg) {
		Map<String, Object> taskParams = task.getTaskParams();
		String phoneNuber = (String) taskParams.get("phoneNuber");
		String sign = (String) taskParams.get("sign");
		String lan = (String) taskParams.get("lan");

		try {
			String urlstring = Interfaces.Retrieve_Pass + "phoneNumber="
					+ phoneNuber + "&sign=" + sign+"&lan="+lan;
			Logger.i(TAG + "找回密码", urlstring);
			String result = HttpUtils.getDataFromHttpGet(urlstring);
			Logger.i(TAG + "找回密码", result);
			msg.arg1 = Task.TASK_SUCCESS;
			msg.obj = result.replace("\n", "").replace("\r", "");
		} catch (Exception e) {
			msg.arg1 = Task.TASK_FAILED;
			e.printStackTrace();
		} finally {
			msg.what = task.getTaskID();
			mHandler.sendMessage(msg);
		}
	}

	/**
	 * 用户签到
	 * 
	 * @param task
	 * @param msg
	 */
	private void userSign(Task task, Message msg) {
		Map<String, Object> taskParams = task.getTaskParams();
		String loginName = (String) taskParams.get("loginName");
		String loginPwd = (String) taskParams.get("loginPwd");
		String softType = (String) taskParams.get("softType");
		String lan = (String) taskParams.get("lan");

		Map<String, String> params = new HashMap<String, String>();
		params.put("loginName", loginName);
		params.put("loginPwd", loginPwd);
		params.put("softType", softType);
		params.put("lan", lan);
		handleDataByPost(Interfaces.Sign, task, msg, params);
	}

	/**
	 * 邀请好友
	 * 
	 * @param task
	 * @param msg
	 */
	// private void inviteFriend(Task task, Message msg) {
	// Map<String, Object> taskParams = task.getTaskParams();
	// String loginName = (String) taskParams.get("loginName");
	// String loginPwd = (String) taskParams.get("loginPwd");
	// String softType = (String) taskParams.get("softType");
	//
	// Map<String, String> params = new HashMap<String, String>();
	// params.put("loginName", loginName);
	// params.put("loginPwd", loginPwd);
	// params.put("softType", softType);
	//
	// handleDataByPost(Interfaces.Sign, task, msg, params);
	// }

	/**
	 * 查询显号设置
	 * 
	 * @param task
	 * @param msg
	 */
	private void queryShowNum(Task task, Message msg) {
		Map<String, Object> taskParams = task.getTaskParams();
		String loginName = (String) taskParams.get("loginName");
		String loginPwd = (String) taskParams.get("loginPwd");
		String softType = (String) taskParams.get("softType");
		String lan = (String) taskParams.get("lan");

		Map<String, String> params = new HashMap<String, String>();
		params.put("loginName", loginName);
		params.put("loginPwd", loginPwd);
		params.put("softType", softType);
		params.put("lan", lan);

		handleDataByPost(Interfaces.Query_ShowNumber, task, msg, params);
	}

	/**
	 * 设置显号状态
	 * 
	 * @param task
	 * @param msg
	 */
	private void setShowNum(Task task, Message msg) {
		Map<String, Object> taskParams = task.getTaskParams();
		String loginName = (String) taskParams.get("loginName");
		String loginPwd = (String) taskParams.get("loginPwd");
		String type = (String) taskParams.get("type");
		String callType = (String) taskParams.get("callType");
		String softType = (String) taskParams.get("softType");
		String lan = (String) taskParams.get("lan");

		Map<String, String> params = new HashMap<String, String>();
		params.put("loginName", loginName);
		params.put("loginPwd", loginPwd);
		params.put("type", type);
		params.put("callType", callType);
		params.put("softType", softType);
		params.put("lan", lan);

		handleDataByPost(Interfaces.Set_ShowNumber, task, msg, params);
	}

	/**
	 * 费率查询
	 * 
	 * @param task
	 * @param msg
	 */
	private void feeQuery(Task task, Message msg) {
		Map<String, Object> taskParams = task.getTaskParams();
		String searchText = (String) taskParams.get("searchText");
		String softType = (String) taskParams.get("softType");
		String lan = (String) taskParams.get("lan");

		Map<String, String> params = new HashMap<String, String>();
		params.put("searchText", searchText);
		params.put("softType", softType);
		params.put("lan", lan);
		handleDataByPost(Interfaces.Query_fee, task, msg, params);
	}

	/**
	 * 备份联系人
	 * 
	 * @param task
	 * @param msg
	 */
	private void backupContacts(Task task, Message msg) {
		Map<String, Object> taskParams = task.getTaskParams();
		String loginName = (String) taskParams.get("loginName");
		String loginPwd = (String) taskParams.get("loginPwd");
		String name_callee = (String) taskParams.get("name_callee");
		String softType = (String) taskParams.get("softType");
		String lan = (String) taskParams.get("lan");

		Map<String, String> params = new HashMap<String, String>();
		params.put("loginName", loginName);
		params.put("loginPwd", loginPwd);
		params.put("name_callee", name_callee);
		params.put("softType", softType);
		params.put("lan", lan);
		
		handleDataByPost(Interfaces.BackupContacts, task, msg, params);
	}

	/**
	 * 获取服务端联系人个数
	 * 
	 * @param task
	 * @param msg
	 */
	private void getContactCount(Task task, Message msg) {
		Map<String, Object> taskParams = task.getTaskParams();
		String loginName = (String) taskParams.get("loginName");
		String loginPwd = (String) taskParams.get("loginPwd");
		String softType = (String) taskParams.get("softType");
		String lan = (String) taskParams.get("lan");

		Map<String, String> params = new HashMap<String, String>();
		params.put("loginName", loginName);
		params.put("loginPwd", loginPwd);
		params.put("softType", softType);
		params.put("lan", lan);

		handleDataByPost(Interfaces.GetContactCount, task, msg, params);
	}

	/**
	 * 查看云端联系人
	 */
	private void getRemoteContact(Task task, Message msg) {
		Map<String, Object> taskParams = task.getTaskParams();
		String loginName = (String) taskParams.get("loginName");
		String loginPwd = (String) taskParams.get("loginPwd");
		String type = taskParams.get("type") + "";
		String softType = (String) taskParams.get("softType");
		String lan = (String) taskParams.get("lan");

		Map<String, String> params = new HashMap<String, String>();
		params.put("loginName", loginName);
		params.put("loginPwd", loginPwd);
		params.put("type", type);
		params.put("softType", softType);
		params.put("lan", lan);

		handleDataByPost(Interfaces.GetContactContent, task, msg, params);
	}

	/**
	 * 充值 获取充值优惠信息
	 * 
	 * @param task
	 * @param msg
	 */
	private void getRechargeInfo(Task task, Message msg) {
		Map<String, Object> taskParams = task.getTaskParams();
		String loginName = (String) taskParams.get("loginName");
		String loginPwd = (String) taskParams.get("loginPwd");
		String softType = (String) taskParams.get("softType");
		String lan = (String) taskParams.get("lan");

		Map<String, String> params = new HashMap<String, String>();
		params.put("loginName", loginName);
		params.put("loginPwd", loginPwd);
		params.put("softType", softType);
		params.put("lan", lan);

		handleDataByPost(Interfaces.Recharge_ZS, task, msg, params);
	}

	/**
	 * 查询余额
	 * 
	 * @param task
	 * @param msg
	 */
	private void getUserBalance(Task task, Message msg) {
		Map<String, Object> taskParams = task.getTaskParams();
		String loginName = (String) taskParams.get("loginName");
		String loginPwd = (String) taskParams.get("loginPwd");
		String softType = (String) taskParams.get("softType");
		String fromPage = (String) taskParams.get("frompage");
		String lan = (String) taskParams.get("lan");

		Map<String, String> params = new HashMap<String, String>();
		params.put("loginName", loginName);
		params.put("loginPwd", loginPwd);
		params.put("softType", softType);
		//params.put("lan", lan);
		try {
			String content = HttpUtils.getDataFromHttpPost(Interfaces.Query_Balance, params);

			msg.arg1 = Task.TASK_SUCCESS;
			msg.obj = content.replace("\n", "").replace("\r", "");
		} catch (Exception e) {
			e.printStackTrace();
			msg.arg1 = Task.TASK_FAILED;
		} finally {
			msg.what = task.getTaskID();
			mHandler.sendMessage(msg);
		}
	}

	/**
	 * 获取充值套餐
	 * 
	 * @param task
	 * @param msg
	 */
	private void getRechargeMeal(Task task, Message msg) {
		Map<String, Object> taskParams = task.getTaskParams();
		String loginName = (String) taskParams.get("loginName");
		String loginPwd = (String) taskParams.get("loginPwd");
		String softType = (String) taskParams.get("softType");
		String lan = (String) taskParams.get("lan");

		Map<String, String> params = new HashMap<String, String>();
		params.put("loginName", loginName);
		params.put("loginPwd", loginPwd);
		params.put("softType", softType);
		params.put("lan", lan);

		handleDataByPost(Interfaces.GET_RECHARGE_MEAL, task, msg, params);
	}

	/**
	 * 获取公告、优惠信息
	 * 
	 * @param task
	 * @param msg
	 */
	private void getPreferentailInfo(Task task, Message msg) {
		try {
			String content = HttpUtils
					.getDataFromHttpGet(Interfaces.Preferential_Push);

			msg.arg1 = Task.TASK_SUCCESS;
			msg.obj = content.replace("\n", "").replace("\r", "");
		} catch (Exception e) {
			e.printStackTrace();
			msg.arg1 = Task.TASK_FAILED;
		} finally {
			msg.what = task.getTaskID();
			mHandler.sendMessage(msg);
		}
	}

	/**
	 * 查询套餐
	 * 
	 * @param task
	 * @param msg
	 */
	private void queryMeal(Task task, Message msg) {
		Map<String, Object> taskParams = task.getTaskParams();
		String loginName = (String) taskParams.get("loginName");
		String loginPwd = (String) taskParams.get("loginPwd");
		String softType = (String) taskParams.get("softType");
		String lan = (String) taskParams.get("lan");

		Map<String, String> params = new HashMap<String, String>();
		params.put("loginName", loginName);
		params.put("loginPwd", loginPwd);
		params.put("softType", softType);
		params.put("lan", lan);

		handleDataByPost(Interfaces.QUERY_MEAL, task, msg, params);
	}

	/**
	 * 查询通话记录
	 * 
	 * @param task
	 * @param msg
	 */
	private void queryCalllog(Task task, Message msg) {
		Map<String, Object> taskParams = task.getTaskParams();
		String loginName = (String) taskParams.get("loginName");
		String loginPwd = (String) taskParams.get("loginPwd");
	/*	String softType = (String) taskParams.get("softType");
		String lan = (String) taskParams.get("lan");
		String year = (String) taskParams.get("year");
		String month = (String) taskParams.get("month");
		String type = (String) taskParams.get("type");
		String currentPage = (String) taskParams.get("currentPage");
		String pageNum = (String) taskParams.get("pageNum");*/
        String date=(String)taskParams.get("date");
		Map<String, String> params = new HashMap<String, String>();
		params.put("loginName", loginName);
		params.put("loginPwd", loginPwd);
	/*	params.put("softType", softType);
		params.put("year", year);
		params.put("month", month);
		params.put("type", type);
		params.put("currentPage", currentPage);
		params.put("pageNum", pageNum);
		params.put("lan", lan);*/
		params.put("date",date);
		Logger.i("查询通话记录", params.toString());
		handleDataByPost(Interfaces.QUERY_CALLLOG, task, msg, params);
	}

	/**
	 * 获取子账户个数
	 * 
	 * @param task
	 * @param msg
	 */
	private void getSubAccountNum(Task task, Message msg) {
		Map<String, Object> taskParams = task.getTaskParams();
		String loginName = (String) taskParams.get("loginName");
		String loginPwd = (String) taskParams.get("loginPwd");
		String softType = (String) taskParams.get("softType");

		Map<String, String> params = new HashMap<String, String>();
		params.put("loginName", loginName);
		params.put("loginPwd", loginPwd);
		params.put("softType", softType);

		handleDataByPost(Interfaces.GET_SUBACCOUNT_NUM, task, msg, params);
	}

	/**
	 * 获取子账户列表
	 * 
	 * @param task
	 * @param msg
	 */
	private void getSubAccountList(Task task, Message msg) {
		Map<String, Object> taskParams = task.getTaskParams();
		String loginName = (String) taskParams.get("loginName");
		String loginPwd = (String) taskParams.get("loginPwd");
		String softType = (String) taskParams.get("softType");

		Map<String, String> params = new HashMap<String, String>();
		params.put("loginName", loginName);
		params.put("loginPwd", loginPwd);
		params.put("softType", softType);

		handleDataByPost(Interfaces.GET_SUBACCOUNT_LIST, task, msg, params);
	}

	/**
	 * 获取子账户验证码
	 * 
	 * @param task
	 * @param msg
	 */
	private void getSubAccountYZM(Task task, Message msg) {
		Map<String, Object> taskParams = task.getTaskParams();
		String loginName = (String) taskParams.get("loginName");
		String loginPwd = (String) taskParams.get("loginPwd");
		String softType = (String) taskParams.get("softType");
		String childPhoneNumber = (String) taskParams.get("childPhoneNumber");

		Map<String, String> params = new HashMap<String, String>();
		params.put("loginName", loginName);
		params.put("loginPwd", loginPwd);
		params.put("softType", softType);
		params.put("childPhoneNumber", childPhoneNumber);

		handleDataByPost(Interfaces.GET_SUBACCOUNT_YZM, task, msg, params);
	}

	/**
	 * 添加手机子账户
	 * 
	 * @param task
	 * @param msg
	 */
	private void addSubAccountPhone(Task task, Message msg) {
		Map<String, Object> taskParams = task.getTaskParams();
		String loginName = (String) taskParams.get("loginName");
		String loginPwd = (String) taskParams.get("loginPwd");
		String softType = (String) taskParams.get("softType");
		String childPhoneNumber = (String) taskParams.get("childPhoneNumber");
		String type = (String) taskParams.get("type");
		String childPwd = (String) taskParams.get("childPwd");
		String childCode = (String) taskParams.get("childCode");

		Map<String, String> params = new HashMap<String, String>();
		params.put("loginName", loginName);
		params.put("loginPwd", loginPwd);
		params.put("softType", softType);
		params.put("childPhoneNumber", childPhoneNumber);
		params.put("type", type);
		params.put("childPwd", childPwd);
		params.put("childCode", childCode);

		handleDataByPost(Interfaces.ADD_SUBACCOUNT, task, msg, params);
	}

	/**
	 * 添加固话子账户
	 * 
	 * @param task
	 * @param msg
	 */
	private void addSubAccountGH(Task task, Message msg) {
		Map<String, Object> taskParams = task.getTaskParams();
		String loginName = (String) taskParams.get("loginName");
		String loginPwd = (String) taskParams.get("loginPwd");
		String softType = (String) taskParams.get("softType");
		String childPhoneNumber = (String) taskParams.get("childPhoneNumber");
		String type = (String) taskParams.get("type");

		Map<String, String> params = new HashMap<String, String>();
		params.put("loginName", loginName);
		params.put("loginPwd", loginPwd);
		params.put("softType", softType);
		params.put("childPhoneNumber", childPhoneNumber);
		params.put("type", type);

		handleDataByPost(Interfaces.ADD_SUBACCOUNT, task, msg, params);
	}

	/**
	 * 删除子账号
	 * 
	 * @param task
	 * @param msg
	 */
	private void deleteSubAccount(Task task, Message msg) {
		Map<String, Object> taskParams = task.getTaskParams();
		String loginName = (String) taskParams.get("loginName");
		String loginPwd = (String) taskParams.get("loginPwd");
		String childPhoneNumber = (String) taskParams.get("childPhoneNumber");

		Map<String, String> params = new HashMap<String, String>();
		params.put("loginName", loginName);
		params.put("loginPwd", loginPwd);
		params.put("childPhoneNumber", childPhoneNumber);

		handleDataByPost(Interfaces.DELETE_SUBACCOUNT, task, msg, params);
	}

	/**
	 * 修改子账户密码
	 * 
	 * @param task
	 * @param msg
	 */
	private void changeSubAccountPass(Task task, Message msg) {
		Map<String, Object> taskParams = task.getTaskParams();
		String loginName = (String) taskParams.get("loginName");
		String loginPwd = (String) taskParams.get("loginPwd");
		String childPhoneNumber = (String) taskParams.get("childPhoneNumber");
		String childNewPwd = (String) taskParams.get("childNewPwd");

		Map<String, String> params = new HashMap<String, String>();
		params.put("loginName", loginName);
		params.put("loginPwd", loginPwd);
		params.put("childPhoneNumber", childPhoneNumber);
		params.put("childNewPwd", childNewPwd);

		handleDataByPost(Interfaces.CHANGE_SUBACCOUNT_PASS, task, msg, params);
	}

	/**
	 * 通过HttpPost 处理dotask数据
	 * 
	 * @param task
	 * @param msg
	 * @param params
	 */
	private void handleDataByPost(String url, Task task, Message msg,
			Map<String, String> params) {
		try {
			String content = HttpUtils.getDataFromHttpPost(url, params);
			Logger.v(TAG, content);

			msg.arg1 = Task.TASK_SUCCESS;
			msg.obj = content.replace("\n", "").replace("\r", "");
			
//			Logger.d(TAG, "Tast id:" + task.getTaskID() + " params: " + task.getTaskParams());
			Logger.d(TAG, "Tast url:" + url+params);
			Logger.d(TAG, "Tast result:" + msg.obj.toString());
		} catch (Exception e) {
			e.printStackTrace();
			msg.arg1 = Task.TASK_FAILED;
		} finally {
			msg.what = task.getTaskID();
			mHandler.sendMessage(msg);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		isRun = false;
		synchronized (calldaTaskList) {
			calldaTaskList.notify();
		}
		getContentResolver().unregisterContentObserver(mObserver);
		unregisterReceiver(receiver);
		if (null != locationClient) {
			/**
			 * 如果AMapLocationClient是在当前Activity实例化的，
			 * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
			 */
			locationClient.stopLocation();
			locationClient.onDestroy();
			locationClient = null;
			locationOption = null;
		}
		//关闭通知
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
		super.onDestroy();
		
//		PushAgent.getInstance(this).disable();
//		PushManager.getInstance().stopService(this.getApplicationContext());
		mThread = null;
		
		//服务退出时，清空activity栈
		ActivityUtil.finishAllActivity();
		
		//设置用户的登录状态
		LoginController.getInstance().setUserLoginState(false);
	}
	class LocationReceiver extends BroadcastReceiver implements AMapLocationListener {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("location",intent.getStringExtra("action"));
			if(intent.getStringExtra("action").equals("login")){
				EMClient.getInstance().login(SharedPreferenceUtil.getInstance(context).getString(Constant.LOGIN_USERNAME)+"-callba",SharedPreferenceUtil.getInstance(context).getString(Constant.LOGIN_PASSWORD),new EMCallBack() {//回调
					@Override
					public void onSuccess() {

						EMClient.getInstance().groupManager().loadAllGroups();
						EMClient.getInstance().chatManager().loadAllConversations();
						Log.d("main", "登录聊天服务器成功！");

						//DemoHelper.getInstance().getUserProfileManager().asyncGetCurrentUserInfo();
					}

					@Override
					public void onProgress(int progress, String status) {

					}

					@Override
					public void onError(int code, String message) {
						Log.d("main", "登录聊天服务器失败！");
					}
				});
				locationClient = new AMapLocationClient(context);
				locationOption = new AMapLocationClientOption();
				// 设置是否需要显示地址信息
				locationOption.setNeedAddress(true);
				/**
				 * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位
				 * 注意：只有在高精度模式下的单次定位有效，其他方式无效
				 */
				locationOption.setGpsFirst(false);
				// 设置定位模式为高精度模式
				locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
				locationOption.setInterval(CalldaGlobalConfig.getInstance().getInterval());
				// 设置定位监听
				locationClient.setLocationListener(this);
				//locationOption.setOnceLocation(true);
				locationClient.setLocationOption(locationOption);
				fixedThreadPool.execute(new Runnable() {
					@Override
					public void run() {
						//处理任务

						// 启动定位
						locationClient.startLocation();
					}
				});

			}else{
			/*	EMClient.getInstance().logout(false, new EMCallBack() {

					@Override
					public void onSuccess() {
						// TODO Auto-generated method stub
						Log.d("main", "退出聊天服务器成功！");
					}

					@Override
					public void onProgress(int progress, String status) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onError(int code, String message) {
						// TODO Auto-generated method stub
						Log.d("main", "退出聊天服务器失败！");
					}
				});*/
				DemoHelper.getInstance().logout(false,new EMCallBack() {

					@Override
					public void onSuccess() {
						// TODO Auto-generated method stub
						Log.d("main", "退出聊天服务器成功！");
					}

					@Override
					public void onProgress(int progress, String status) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onError(int code, String message) {
						// TODO Auto-generated method stub
						Log.d("main", "退出聊天服务器失败！");
					}
				});
				if (null != locationClient) {
					/**
					 * 如果AMapLocationClient是在当前Activity实例化的，
					 * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
					 */
					locationClient.stopLocation();
					locationClient.onDestroy();
					locationClient = null;
					locationOption = null;
				}
			}
		}
		@Override
		public void onLocationChanged(AMapLocation aMapLocation) {
			StringBuilder sb = new StringBuilder();
			if (aMapLocation.getErrorCode() == 0) {
				Logger.i("address", aMapLocation.getAddress());
				Logger.i("latitude",aMapLocation.getLatitude()+"");
				Logger.i("longitude",aMapLocation.getLongitude()+"");
				CalldaGlobalConfig.getInstance().setAddress(aMapLocation.getAddress());
				CalldaGlobalConfig.getInstance().setLatitude(aMapLocation.getLatitude());
				CalldaGlobalConfig.getInstance().setLongitude(aMapLocation.getLongitude());
				userDao.saveLocation(CalldaGlobalConfig.getInstance().getUsername(),CalldaGlobalConfig.getInstance().getPassword(),aMapLocation.getLatitude(),aMapLocation.getLongitude());
			} else {
				//定位失败
				sb.append("定位失败" + "\n");
				sb.append("错误码:" + aMapLocation.getErrorCode() + "\n");
				sb.append("错误信息:" + aMapLocation.getErrorInfo() + "\n");
				sb.append("错误描述:" + aMapLocation.getLocationDetail() + "\n");
				Logger.i("error", sb.toString());

			}
		}
	}
}
