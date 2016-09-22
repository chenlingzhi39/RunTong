package com.callba.phone.logic.login;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.conn.ConnectTimeoutException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.callba.R;
import com.callba.phone.DemoHelper;
import com.callba.phone.MyApplication;
import com.callba.phone.bean.Task;
import com.callba.phone.cfg.Constant;
import com.callba.phone.manager.UserManager;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.DesUtil;
import com.callba.phone.util.HttpUtils;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;


/** 
 * 登录控制器
 * @Author  zhwO
 * @Version V1.0  
 * @Createtime：2014年5月21日 下午12:02:24 
 */
public class LoginController {
	private static final String TAG = LoginController.class.getCanonicalName();
	
	//用户登录状态
	private boolean loginState = false;
	//当前登录超时重试次数
	private int currentLoginTime;
	
	private String lan;
	
	private static LoginController mLoginController;
	private LoginController(){}
	
	public synchronized static LoginController getInstance() {
		if(mLoginController == null) {
			mLoginController = new LoginController();
		}
		
		return mLoginController;
	}
	
	/**
	 * 获取当前用户登录状态
	 * @return
	 */
	public boolean getUserLoginState() {
		Logger.d(TAG, "getUserLoginState loginState -> " + loginState);
		if(loginState) {
			String username = UserManager.getUsername(MyApplication.getInstance());
			String password = UserManager.getPassword(MyApplication.getInstance());
			if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
				Logger.w(TAG, "getUserLoginState username or password is null.");
				return false;
			}
			return true;
		}
		return false;
	}
	
	/**
	 * 设置用户的登录状态
	 * @param loginState
	 */
	public void setUserLoginState(boolean loginState) {
		this.loginState = loginState;
	}
	
	/**
	 * 用户登录
	 * @param context
	 * @param task
	 * @param loginListener
	 */
	@SuppressLint("HandlerLeak") 
	public synchronized void userLogin(final Context context, final Task task, final UserLoginListener loginListener) {
		Logger.d(TAG, "LoginController startLogin.");
		//设置超时重试次数为0
		currentLoginTime = 3;
		
		if(loginListener == null) {
			throw new IllegalArgumentException("UserLoginListener 为空");
		}
		
		if(loginState) {
			loginListener.loginSuccess(null);
			return;
		} 
		
		if(!MyApplication.getInstance().detect()) {
			//无网络连接
			Logger.w(TAG, "LoginController no network conn.");
			
			loginListener.localLoginFailed(UserLoginErrorMsg.NO_NETWORK);
			return;
		}
		
		Handler mHandler = new Handler() {
			public void handleMessage(Message msg) {
				
				boolean loginTimeout = handleLoginResult(context, loginListener, msg);
				
				if(loginTimeout) {
					//重新登录
					currentLoginTime++;
					Logger.i(TAG, "UserLogin timeout Retry login. time is " +  currentLoginTime);
					asyncUserLogin(this, task);
				}
			}
		};
		ActivityUtil activityUtil=new ActivityUtil();
		lan=activityUtil.language(context);
		//登录
		asyncUserLogin(mHandler, task);
	}
	
	/**
	 * 处理登录成功信息
	 * @param resultInfo
	 */
	public static void parseLoginSuccessResult(Context context, String username, String password, String[] resultInfo) {
		if(resultInfo != null) {
			DemoHelper.getInstance().setCurrentUserName(username);
			DemoHelper.getInstance().getUserProfileManager().setCurrentUserAvatar(resultInfo[6]);
			DemoHelper.getInstance().getUserProfileManager().setCurrentUserNick(resultInfo[7]);
			try {
				String encryptPwd = DesUtil.encrypt(password,
						resultInfo[2]);
				UserManager.putPassword(context,encryptPwd);
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(context,context.getString(R.string.result_data_error),Toast.LENGTH_SHORT).show();
			/*	CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(context, R.string.result_data_error);*/
			}
			UserManager.putLoginToken(context,resultInfo[2]);
			UserManager.putSignature(context,resultInfo[8]);
			UserManager.putNickname(context,resultInfo[7]);
			if(!resultInfo[9].equals(""))
			UserManager.putGold(context,Integer.parseInt(resultInfo[9]));
			UserManager.putUserAvatar(context,resultInfo[6]);
			UserManager.putCommission(context,resultInfo[10]);
			UserManager.putCreateTime(context,resultInfo[11]);
			UserManager.putProportion(context,Integer.parseInt(resultInfo[12]));
			UserManager.putUsername(context,username);
			UserManager.putOriginalPassword(context,password);
//			SharedPreferenceUtil mPreferenceUtil = SharedPreferenceUtil.getInstance(context);
//			mPreferenceUtil.putString(Constant.LOGIN_USERNAME, username);
//			mPreferenceUtil.putString(Constant.LOGIN_PASSWORD, password);
//			mPreferenceUtil.putString(Constant.LOGIN_ENCODED_PASSWORD, GlobalConfig.getInstance().getPassword());
//			mPreferenceUtil.commit();
			Intent intent=new Intent("com.callba.location");
			intent.putExtra("action","login");
			context.sendBroadcast(intent);
			context.sendBroadcast(new Intent("com.callba.login"));
		}
		
	/*	String callSetting = GlobalConfig.getInstance().getCallSetting();
		Task task = new Task(Task.TASK_DOWNLOAD_AD);
		Map<String, Object> taskParams = new HashMap<String, Object>();
		task.setTaskParams(taskParams);
		DownLoadAdvertisement.getInstance().downloadAD(context, task, null);*/
	}
	
	/**
	 * 解析登录失败信息
	 * @param context
	 */
	public static void parseLocalLoginFaildInfo(Context context, UserLoginErrorMsg errorMsg) {

		
		switch (errorMsg) {
			case UNKNOWN:
				//calldaToast.showToast(context, R.string.unknownerror);
				Toast.makeText(context,context.getString(R.string.unknownerror),Toast.LENGTH_SHORT).show();
				break;
			case TIMEOUT:
				//calldaToast.showToast(context, R.string.login_timeout);
				Toast.makeText(context,context.getString(R.string.login_timeout),Toast.LENGTH_SHORT).show();
				break;
			case SERVER_ERROR:
				//calldaToast.showToast(context, R.string.server_error);
				Toast.makeText(context,context.getString(R.string.server_error),Toast.LENGTH_SHORT).show();
				break;
			case NO_NETWORK:
				//calldaToast.showToast(context, R.string.network_error);
				Toast.makeText(context,context.getString(R.string.network_error),Toast.LENGTH_SHORT).show();
				break;
			case CONN_NETWORK_FAILED:
				//calldaToast.showToast(context, R.string.conn_failed);
				Toast.makeText(context,context.getString(R.string.conn_failed),Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
		}
	}
	
	/**
	 * 处理登录返回结果
	 * @param loginListener
	 * @param msg
	 * @return 是否登录超时
	 */
	protected boolean handleLoginResult(Context context, UserLoginListener loginListener, Message msg) {
		if(msg.what == Task.TASK_LOGIN) {
			int taskResultCode = msg.arg1;
			
			switch (taskResultCode) {
			case Task.TASK_SUCCESS:
				try {
					String[] result = ((String)msg.obj).split("\\|");
					Log.i("result",(String)msg.obj);
					if("0".equals(result[0])) {
						loginState = true;
						loginListener.loginSuccess(result);
					} else {
						loginState = false;
						loginListener.serverLoginFailed(result[1]);
					}
				} catch (Exception e) {
					e.printStackTrace();
					if(currentLoginTime < Constant.LOGIN_RETRY_TIMES) {
						return true;
					}
					loginState = false;
					loginListener.localLoginFailed(UserLoginErrorMsg.TIMEOUT);
				}
				break;
				
			case Task.TASK_TIMEOUT:
				if(currentLoginTime < Constant.LOGIN_RETRY_TIMES) {
					return true;
				} else {
					//统计登录超时
					//MobclickAgent.onEvent(context, "login_timeout");
					
					loginState = false;
					loginListener.localLoginFailed(UserLoginErrorMsg.TIMEOUT);
				}
				break;
				
			case Task.TASK_FAILED:
				loginState = false;
				loginListener.localLoginFailed(UserLoginErrorMsg.SERVER_ERROR);
				break;
				
			case Task.TASK_NETWORK_ERROR:
				loginState = false;
				loginListener.localLoginFailed(UserLoginErrorMsg.CONN_NETWORK_FAILED);
				break;
				
			default:
				break;
			}
		} else {
			loginState = false;
			loginListener.localLoginFailed(UserLoginErrorMsg.UNKNOWN);
		}
		
		return false;
	}

	/**
	 * 处理异步登录任务
	 * @param mHandler
	 * @param task
	 */
	private void asyncUserLogin(final Handler mHandler, final Task task) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				login(mHandler, task);
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();
	}
	
	private void login(Handler handler, Task task) {
		
		Map<String, Object> taskParams = task.getTaskParams();
		String loginSign = (String) taskParams.get("loginSign");
		String loginType = (String) taskParams.get("loginType");
		
		Logger.d(TAG, "LoginControll do login -> " + task);
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("loginSign", loginSign);
		params.put("loginType", loginType);
		params.put("softType","android");
		params.put("callType", "all");
		Message msg = handler.obtainMessage();
		String results = null;
		try {
			String content = HttpUtils.getDatafFromPostConnClose(
					Interfaces.Login, params);
			Logger.v(TAG, Interfaces.Login+ params);
			results = content.replace("\n", "").replace("\r", "");

			Logger.d(TAG, "LoginControll login result : " + results);
			
			msg.arg1 = Task.TASK_SUCCESS;

		} catch (ConnectTimeoutException e1) {
			e1.printStackTrace();
			msg.arg1 = Task.TASK_TIMEOUT;
		/*	if(i!=4)
			continue;*/
		} catch (UnknownHostException e2) {
			e2.printStackTrace();
			msg.arg1 = Task.TASK_NETWORK_ERROR;
		} catch (Exception e) {
			e.printStackTrace();
			msg.arg1 = Task.TASK_TIMEOUT;
		} finally {
			msg.what = task.getTaskID();
			msg.obj = results;
			handler.sendMessage(msg);
        }
	}
}