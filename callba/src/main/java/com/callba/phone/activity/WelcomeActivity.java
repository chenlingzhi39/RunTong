package com.callba.phone.activity;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.activity.login.LoginActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Task;
import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.cfg.Constant;
import com.callba.phone.cfg.GlobalSetting;
import com.callba.phone.logic.contact.ContactPersonEntity;
import com.callba.phone.logic.contact.QueryContactCallback;
import com.callba.phone.logic.contact.QueryContacts;
import com.callba.phone.logic.login.LoginController;
import com.callba.phone.service.MainService;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.AppVersionChecker;
import com.callba.phone.util.AppVersionChecker.AppVersionBean;
import com.callba.phone.util.Logger;
import com.callba.phone.util.NetworkDetector;
import com.callba.phone.util.SharedPreferenceUtil;
import com.callba.phone.util.ZipUtil;


@ActivityFragmentInject(
		contentViewId = R.layout.welcome_page
)
public class WelcomeActivity extends BaseActivity {
	public static final String TAG = "WelcomeActivity";
	private SharedPreferenceUtil mSharedPreferenceUtil;
	private Handler mHandler;
	private boolean isNetworkAvail = false; // 当前是否有可用网络
    private LinearLayout rootView;
	// 记录当前获取key的次数
	private int currentGetVersionTime = 0;

	// private PushAgent mPushAgent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
		mSharedPreferenceUtil = SharedPreferenceUtil.getInstance(this);

	}

	/**
	 * 启动服务
	 */
	private void asyncInitLoginEnvironment() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				startZip();
				// mPushAgent = PushAgent.getInstance(WelcomeActivity.this);
				// mPushAgent.onAppStart();
				// mPushAgent.enable();
				// 初始化PushManager对象（初始化推送服务）

//				PushManager.getInstance().initialize(
//						WelcomeActivity.this.getApplicationContext());
//				String clientid = PushManager.getInstance().getClientid(
//						getApplicationContext());
//				Logger.i(TAG, "初始化PushManager对象（初始化推送服务）" + clientid);
				// 启动服务
				//startService(new Intent(WelcomeActivity.this, MainService.class));
				if (Constant.CALL_SETTING_HUI_BO.equals(GlobalConfig
						.getInstance().getCallSetting())) {
					return;
				}
			}
		}).start();
	}

	/**
	 * 开始复制
	 */
	private void startZip() {
		try {
			Logger.i(TAG, "start zip");
			if(!new File(SharedPreferenceUtil.getInstance(this).getString(Constant.DB_PATH_)).exists())
			ZipUtil.copyBigDataBase(WelcomeActivity.this);
			else Constant.DB_PATH=SharedPreferenceUtil.getInstance(this).getString(Constant.DB_PATH_);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void init() {
		startService(new Intent(WelcomeActivity.this, MainService.class));
		mHandler = new Handler();
		rootView=(LinearLayout) findViewById(R.id.root);
		AlphaAnimation alphaAnimation=new AlphaAnimation(0.0f,1.0f);
		alphaAnimation.setDuration(2000);
		alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						initEnvironment();

						// 设置用户的登录状态
						LoginController.getInstance().setUserLoginState(false);

						// 启动服务
						asyncInitLoginEnvironment();
					}
				}, 500);

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});
		rootView.startAnimation(alphaAnimation);

	}

	@Override
	protected void onResume() {
		super.onResume();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			Window window = getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
			);
			window.getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_FULLSCREEN
					| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}
		}


	private void initEnvironment() {
		String s = getResources().getConfiguration().locale.getCountry();
		Logger.v("语言环境", s);
        Locale.setDefault(new Locale("zh"));
		// 查询联系人
		new QueryContacts(new QueryContactCallback() {
			@Override
			public void queryCompleted(List<ContactPersonEntity> contacts) {
						// 检查网络
						isNetworkAvail = NetworkDetector.detect(WelcomeActivity.this);
						alertNetWork(isNetworkAvail);
						if (isNetworkAvail) {
							currentGetVersionTime = 0;
							// 获取版本信息
							sendGetVersionTask();
						}
			}
		}).loadContact(this);

		// 初始化环境参数
		GlobalSetting.initEnvirment(this);
	}

	/**
	 * 发送获取版本信息任务
	 * 
	 * @author zhw
	 */
	private void sendGetVersionTask() {
		PackageManager pm = this.getPackageManager();
		String localVersion = "";
		try {
			PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);
			localVersion = packageInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		ActivityUtil activityUtil = new ActivityUtil();
		Task task = new Task(Task.TASK_GET_VERSION);
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put("versionName", localVersion);
		taskParams.put("fromPage", "WelcomeActivity");
		taskParams.put("lan", activityUtil.language(this));
		task.setTaskParams(taskParams);

		MainService.newTask(task);

		currentGetVersionTime++;
	}

	@Override
	public void refresh(Object... params) {
		Message verionMessage = (Message) params[0];
		// 解析版本返回数据
		AppVersionBean appVersionBean = AppVersionChecker.parseVersionInfo(
				this, verionMessage);

		String secretKey = appVersionBean.getSecretKey();
		if (!TextUtils.isEmpty(secretKey)) {
			GlobalConfig.getInstance().setSecretKey(secretKey);
			mSharedPreferenceUtil.putString(Constant.SECRET_KEY, secretKey,
					true);
		}
        GlobalConfig.getInstance().setAppVersionBean(appVersionBean);
		// 检查是否成功获取加密Key
		checkLoginKey(appVersionBean);
	}

	/**
	 * 检查是否成功获取加密的key
	 * 
	 * @author zhw
	 */
	private void checkLoginKey(AppVersionBean appVersionBean) {
		// Logger.i(TAG, "getSecretKey() : " +
		// GlobalConfig.getInstance().getSecretKey());
		Logger.i(TAG, "currentGetVersionTime : " + currentGetVersionTime);

		if (!TextUtils.isEmpty(appVersionBean.getSecretKey())) {

			// 成功获取key
			//check2Upgrade(appVersionBean);
			gotoActivity();
		} else if (currentGetVersionTime <= Constant.GETVERSION_RETRY_TIMES) {

			// 再次发送获取任务
			sendGetVersionTask();

		} else {
			// 统计获取版本失败次数
			//MobclickAgent.onEvent(this, "version_timeout");
			String secretKey = mSharedPreferenceUtil
					.getString(Constant.SECRET_KEY);
			GlobalConfig.getInstance().setSecretKey(secretKey);

			if (TextUtils.isEmpty(secretKey)) {
				// Toast.makeText(this, R.string.getversionfailed,
				// Toast.LENGTH_SHORT).show();
				// 提示用户获取失败
				alertUserGetVersionFailed();
			} else {
				//check2Upgrade(appVersionBean);
				gotoActivity();
			}
		}
	}

	/**
	 * 提示用户获取版本失败
	 * 
	 * @author zhw
	 */
	private void alertUserGetVersionFailed() {

		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.dialog_title);
		builder.setMessage(R.string.net_error_getdata_fail);
		builder.setPositiveButton(R.string.retry,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						PackageManager pm =WelcomeActivity.this.getPackageManager();
						String localVersion = "";
						try {
							PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);
							localVersion = packageInfo.versionName;
						} catch (NameNotFoundException e) {
							e.printStackTrace();
						}
						ActivityUtil activityUtil = new ActivityUtil();
						Task task = new Task(Task.TASK_GET_VERSION);
						Map<String, Object> taskParams = new HashMap<String, Object>();
						taskParams.put("versionName", localVersion);
						taskParams.put("fromPage", "WelcomeActivity");
						taskParams.put("lan", activityUtil.language(WelcomeActivity.this));
						task.setTaskParams(taskParams);
						MainService.newTask(task);
						/*Intent intent = new Intent();
						intent.setAction(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
						startActivity(intent);*/
						dialog.dismiss();
					}
				});
		builder.setNegativeButton(R.string.exit,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});
		AlertDialog alertDialog=builder.create();
		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.setCancelable(false);
		alertDialog.show();
	}

	/**
	 * 页面跳转
	 * 
	 * @param getVersion
	 *            获取版本是否成功
	 */
	private void gotoActivity() {
		// 判断是不是第一次使用
		boolean isFirstStart = mSharedPreferenceUtil.getBoolean(
				Constant.ISFRISTSTART, true);

		if (isFirstStart) {
			// 第一次启动，跳转到新功能介绍页面
			Intent intent = new Intent(WelcomeActivity.this,
					FunIntroduceActivity.class);
			WelcomeActivity.this.startActivity(intent);

			mSharedPreferenceUtil.putBoolean(Constant.ISFRISTSTART, false);
			mSharedPreferenceUtil.putBoolean(Constant.Auto_Login, true); // 默认自动启动
			mSharedPreferenceUtil.commit();
		} else if (GlobalConfig.getInstance().isAutoLogin()) {
			String username = mSharedPreferenceUtil
					.getString(Constant.LOGIN_USERNAME);
			if (TextUtils.isEmpty(username)) {
				Intent intent = new Intent(WelcomeActivity.this,
						GuideActivity.class);
				WelcomeActivity.this.startActivity(intent);

				finish();
				return;
			}
			// 自动登陆
			Intent intent = new Intent(WelcomeActivity.this,
					MainTabActivity.class);
			intent.putExtra("frompage", "WelcomeActivity");
			WelcomeActivity.this.startActivity(intent);
		} else {
			// 手动登陆
			Intent intent = new Intent(WelcomeActivity.this,
					LoginActivity.class);
			WelcomeActivity.this.startActivity(intent);
		}
		finish();
	}

	/**
	 * 判断网络连接
	 */
	private void alertNetWork(boolean isAvail) {
		if (!isAvail) {
			AlertDialog alertDialog = new AlertDialog.Builder(this)
					.setTitle(R.string.networksetup)
					.setMessage(R.string.networksetupinfo)
					.setPositiveButton(R.string.setup,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									try {
										startActivityForResult(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS), 0);
									} catch (Exception e) {
										// TODO: handle exception
									}
								}
							})
					.setNegativeButton(R.string.exit,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									WelcomeActivity.this.finish();
									// gotoActivity();
								}
							}).create();

			alertDialog.setCanceledOnTouchOutside(false);
			alertDialog.setCancelable(false);
			try {
				alertDialog.show();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_CANCELED && requestCode == 0) {
			isNetworkAvail = NetworkDetector.detect(WelcomeActivity.this);
			alertNetWork(isNetworkAvail);
			if (isNetworkAvail) {
				currentGetVersionTime = 0;
				// 获取版本信息
				sendGetVersionTask();
			}
		}

	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
