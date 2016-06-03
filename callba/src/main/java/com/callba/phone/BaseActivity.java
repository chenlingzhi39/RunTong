package com.callba.phone;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.callba.R;
import com.callba.phone.activity.FunIntroduceActivity;
import com.callba.phone.activity.GuideActivity;
import com.callba.phone.activity.HomeActivity;
import com.callba.phone.activity.MainCallActivity;
import com.callba.phone.activity.MainTabActivity;
import com.callba.phone.activity.MessageActivity;
import com.callba.phone.activity.UserActivity;
import com.callba.phone.activity.WelcomeActivity;
import com.callba.phone.activity.contact.ContactActivity;
import com.callba.phone.activity.login.LoginActivity;
import com.callba.phone.activity.login.OnekeyRegisterAcitvity;
import com.callba.phone.activity.login.RegisterActivity;
import com.callba.phone.activity.more.RetrievePasswordActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.service.MainService;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.view.MyDialog;

import java.lang.reflect.Field;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public abstract class BaseActivity extends AppCompatActivity {
	public static Boolean flag = true;
	/**
	 * 布局的id
	 */
	protected int mContentViewId;
	/**
	 * 返回键图片id
	 */
	private int navigationId;
	/**
	 * 菜单的id
	 */
	private int mMenuId;
	/**
	 * Toolbar标题
	 */
	private int mToolbarTitle;
	/**
	 *  当前页面是否发送通知
	 */
	private boolean isSendNotification = true;

	/**
	 * 初始化界面
	 */
	public abstract void init();

	/**
	 * 后台数据处理完毕，回调刷新界面
	 * 
	 * @param params
	 */
	public abstract void refresh(Object... params);

	private boolean myDialogFlag = false;
   public Toolbar toolbar;
	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
	}
	@SuppressLint("InlinedApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getClass().isAnnotationPresent(ActivityFragmentInject.class)) {
			ActivityFragmentInject annotation = getClass()
					.getAnnotation(ActivityFragmentInject.class);
			mContentViewId = annotation.contentViewId();
			navigationId=annotation.navigationId();
			mMenuId = annotation.menuId();
			mToolbarTitle = annotation.toolbarTitle();
		} else {
			throw new RuntimeException(
					"Class must add annotations of ActivityFragmentInitParams.class");
		}
		setContentView(mContentViewId);
		initToolbar();
		if(mToolbarTitle!=-1)
			setToolbarTitle(mToolbarTitle);

		MyApplication.activities.add(this);

	/*	if (this.getClass() != MainCallActivity.class
				&& this.getClass() != WelcomeActivity.class
				&& this.getClass() != FunIntroduceActivity.class
				&& this.getClass() != GuideActivity.class
				&& this.getClass() != LoginActivity.class
				&& this.getClass() != RegisterActivity.class
				&& this.getClass() != OnekeyRegisterAcitvity.class
				&& this.getClass() != RetrievePasswordActivity.class) {
			// 检查内存数据是否正常
			String username = CalldaGlobalConfig.getInstance().getUsername();
			String password = CalldaGlobalConfig.getInstance().getPassword();
			if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
				// 重新打开
				Intent intent = new Intent();
				intent.setClass(this, WelcomeActivity.class);
				startActivity(intent);

				// 关闭主tab页面
				finish();
				ActivityUtil.finishMainTabPages();
			}
		}*/
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
			if(this.getClass()!= UserActivity.class)
			{getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
			Log.i(this.getClass().getName(),"light");
				if(Build.MANUFACTURER.equals("Xiaomi"))
					ActivityUtil.MIUISetStatusBarLightMode(getWindow(),true);
				if(Build.MANUFACTURER.equals("Meizu"))
					ActivityUtil.FlymeSetStatusBarLightMode(getWindow(),true);
			}
		}if(Build.VERSION.SDK_INT==Build.VERSION_CODES.LOLLIPOP||Build.VERSION.SDK_INT==Build.VERSION_CODES.LOLLIPOP_MR1){
			if(this.getClass()==MainCallActivity.class||
					this.getClass()==ContactActivity.class||
					this.getClass()==HomeActivity.class||
					this.getClass()==MessageActivity.class)
			{SystemBarTintManager systemBarTintManager=new SystemBarTintManager(this);
			systemBarTintManager.setStatusBarTintEnabled(true);
			systemBarTintManager.setStatusBarTintResource(R.color.gray_status);}
		}
		Log.i("manufacturer",Build.MANUFACTURER);

		init();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		MyApplication.activities.remove(this);

		if (MyApplication.activities.size() < 1) {
			cancelNotification();

			// 停止服务
			try {
				stopService(new Intent(this, MainService.class));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	private void initToolbar() {
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (toolbar != null) {
			setSupportActionBar(toolbar);
			if(navigationId!=-1) {
				getSupportActionBar().setDisplayHomeAsUpEnabled(true);
				toolbar.setNavigationIcon(navigationId);
			}
			getSupportActionBar().setTitle("");


	}
	}
	protected void setToolbarTitle(int strId) {
		if (getSupportActionBar() != null) {
			//AssetManager mgr=getAssets();//得到AssetManager
			//Typeface tf= Typeface.createFromAsset(mgr, "fonts/STXIHEI.TTF");//根据路径得到Typeface
			TextView title=(TextView)findViewById(R.id.title);
			//title.setTypeface(tf);
			title.setText(getResources().getString(mToolbarTitle));



		}
	}
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		if(mMenuId!=-1)
			getMenuInflater().inflate(mMenuId, menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				Log.i("base", "finish");
				finish();
				break;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	/**
	 * 退出所有打开的activity
	 */
	protected void exitApp() {
		if (!myDialogFlag) {
			myDialogFlag = true;
			MyDialog.showDialog(this, getString(R.string.exit_confirm),
					new OnClickListener() {

						@Override
						public void onClick(View v) {
							switch (v.getId()) {
							case R.id.bn_ok:
								// 清空activity栈
								ActivityUtil.finishAllActivity();

								stopService(new Intent(BaseActivity.this,
										MainService.class));
								break;

							default:
								break;
							}
							MyDialog.dismissDialog();
							myDialogFlag = false;
						}
					});

		}
	}

	/**
	 * 发送通知栏广播
	 * 
	 * @author zhw
	 */
	private void showBackRunNotification() {
		Class<?> clazz = this.getClass();
		if (clazz.getName()
				.equals("com.callba.phone.activity.MainCallActivity")
				|| clazz.getName().equals(
						"com.callba.phone.activity.contact.ContactActivity")
				|| clazz.getName()
						.equals("com.callba.phone.activity.HomeActivity")
				|| clazz.getName().equals(
						"com.callba.phone.activity.MessageActivity")
				|| clazz.getName().equals(
				"com.callba.phone.activity.UserActivity")) {

			 clazz = MainTabActivity.class;
//			clazz = OpenTabHelperActivity.class;
		}

		sendNotification1(clazz);
	}

	/**
	 * 设置通知默认打开页面为 ManinTab
	 * 
	 * @author zhw
	 */
	public void restoreNotificationOpenPage() {
		sendNotification1(MainTabActivity.class);
	}

	// /**
	// * 发送通知
	// * @author zhw
	// * @param clazz
	// */
	// @SuppressLint("NewApi")
	// private void sendNotification(Class<?> clazz) {
	// Notification notification = new Notification(R.drawable.logo_notication,
	// "", System.currentTimeMillis());
	// NotificationManager notificationManager = (NotificationManager)
	// getSystemService(NOTIFICATION_SERVICE);
	//
	// Context context = getApplicationContext();
	// CharSequence contentTitle = getString(R.string.contenttitle);
	// CharSequence contentText = getString(R.string.contenttext);
	// notification.flags = Notification.FLAG_ONGOING_EVENT;//设置常驻
	// Intent notificationIntent = new Intent(context, clazz);
	// PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
	// notificationIntent, 0);
	// notification.setLatestEventInfo(context, contentTitle, contentText,
	// contentIntent);
	// if(Build.VERSION.SDK_INT >= 11) {
	// notification.largeIcon = BitmapFactory.decodeResource(getResources(),
	// R.drawable.logo);
	// }
	//
	// notificationManager.notify(10, notification);
	// }

	/**
	 * 发送通知
	 * 
	 * @author zhw
	 * @param clazz
	 */
	private void sendNotification1(Class<?> clazz) {
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				getApplicationContext())
				.setSmallIcon(R.drawable.logo_notification)
				.setLargeIcon(
						BitmapFactory.decodeResource(getResources(),
								R.drawable.logo))
				.setContentTitle(getString(R.string.contenttitle))
				.setContentText(getString(R.string.contenttext));

		Intent notificationIntent = new Intent(getApplicationContext(), clazz);

		// TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// stackBuilder.addParentStack(clazz);
		// stackBuilder.addNextIntent(notificationIntent);
		// PendingIntent resultPendingIntent =
		// stackBuilder.getPendingIntent(
		// 0,
		// PendingIntent.FLAG_UPDATE_CURRENT
		// );
		PendingIntent contentIntent = PendingIntent.getActivity(
				getApplicationContext(), 0, notificationIntent, 0);
		mBuilder.setContentIntent(contentIntent);

		Notification notification = mBuilder.build();
		notification.flags = Notification.FLAG_ONGOING_EVENT;

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(10, notification);
	}
	/**
	 * Android判断Intent是否存在，是否可用
	 * @param context
	 * @param intent
	 * @return
	 */
	public static boolean isIntentAvailable(Context context, Intent intent) {
		final PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.GET_ACTIVITIES);
		return list.size() > 0;
	}
	/**
	 * 关闭所有通知
	 */
	private void cancelNotification() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
	}

	@Override
	protected void onResume() {
		if (isSendNotification) {
			showBackRunNotification();
		}
	//	MobclickAgent.onResume(this);
		super.onResume();
	}

	@Override
	protected void onPause() {
	//	MobclickAgent.onPause(this);
		super.onPause();
	}

	public boolean isSendNotification() {
		return isSendNotification;
	}

	public void setSendNotification(boolean isSendNotification) {
		this.isSendNotification = isSendNotification;
	}
	public void toast(String msg){
		Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
	}
	public static int getResId(String variableName, Class<?> c) {
		try {
			Field idField = c.getDeclaredField(variableName);
			return idField.getInt(idField);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
  public void toast(int id){
	  Toast.makeText(this,getString(id),Toast.LENGTH_SHORT).show();
  }

}
