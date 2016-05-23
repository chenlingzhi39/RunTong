package com.callba.phone.activity;


import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.MyApplication;
import com.callba.phone.activity.contact.ContactActivity;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.cfg.Constant;
import com.callba.phone.util.ActivityUtil;

/**
 * 主界面
 * @author zxf
 *
 */
@SuppressWarnings("deprecation")
public class MainTabActivity extends TabActivity {
	private TabHost mTabhost;

	private String mTabTextArray[] = null;
	
	@SuppressWarnings("rawtypes")
	private Class[] mTabClassArray = {MainCallActivity.class,
			ContactActivity.class,HomeActivity.class,
			MessageActivity.class, UserActivity.class};

	private int[] mTabImageArray = {R.drawable.menu1_selector,
			R.drawable.menu2_selector, R.drawable.menu3_selector,
			R.drawable.menu4_selector, R.drawable.menu5_selector};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_tab);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
			);
			window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

			window.setStatusBarColor(Color.TRANSPARENT);
		}
		MyApplication.activities.add(this);

		if(savedInstanceState != null) {
			//恢复保存到数据
			CalldaGlobalConfig.getInstance().restoreGlobalCfg(savedInstanceState);
		}
		
		//关闭登录模块的页面
		ActivityUtil.finishLoginPages();
		
		mTabhost = this.getTabHost();

		mTabTextArray = getResources().getStringArray(R.array.maintab_texts);
		
		//放入底部状态栏数据
		for (int i = 0; i < mTabClassArray.length; i++) {
			TabSpec tabSpec = mTabhost.newTabSpec(mTabTextArray[i])
					.setIndicator(getTabItemView(i))
					.setContent(getTabItemIntent(i));
			mTabhost.addTab(tabSpec);
			mTabhost.getTabWidget().getChildAt(i);
		}
		
		//获取第一个tabwidget
		final View view = mTabhost.getTabWidget().getChildAt(0);
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mTabhost.getCurrentTab() == 0) {
					Intent intent = new Intent("com.runtong.phone.diallayout.show");
					ImageView iv = (ImageView) view
							.findViewById(R.id.iv_maintab_icon);
					if (BaseActivity.flag) {
						iv.setBackgroundDrawable(getResources().getDrawable(
								R.drawable.call_menu_up));
						intent.putExtra("action", "hide");
					} else {
						iv.setBackgroundDrawable(getResources().getDrawable(
								R.drawable.call_menu_downs));
						intent.putExtra("action", "show");
					}
					
					MainTabActivity.this.sendBroadcast(intent);
					BaseActivity.flag = !BaseActivity.flag;
				}
				mTabhost.setCurrentTab(0);
			}
		});
		
		mTabhost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				if(!tabId.equals(getString(R.string.call))) {
					ImageView iv = (ImageView) view
							.findViewById(R.id.iv_maintab_icon);
					iv.setBackgroundResource(R.drawable.menu1_selector);
				}else {
					ImageView iv = (ImageView) view
							.findViewById(R.id.iv_maintab_icon);
					iv.setBackgroundDrawable(getResources().getDrawable(
							R.drawable.call_menu_downs));
					Intent intent = new Intent("com.runtong.phone.diallayout.show");
					intent.putExtra("action", "show");
					MainTabActivity.this.sendBroadcast(intent);
					BaseActivity.flag = true;
				}
			}
		});
		
		//异常启动，跳转到第一个页签
		if(savedInstanceState != null) {
			try {
				String frompage = getIntent().getStringExtra("frompage");
				if(!TextUtils.isEmpty(frompage)
						&& frompage.equals("WelcomeActivity")) {
					savedInstanceState.remove("currentTab");
				}
			} catch (Exception e) {}
		}
	}
	
	private View getTabItemView(int index) {
		View view = View.inflate(this, R.layout.maintab_item, null);
		ImageView imageView = (ImageView) view
				.findViewById(R.id.iv_maintab_icon);
		TextView textview = (TextView) view.findViewById(R.id.tv_maintab_text);
		imageView.setBackgroundResource(mTabImageArray[index]);
		textview.setText(mTabTextArray[index]);
		return view;
	}
	
	private Intent getTabItemIntent(int index) {
		Intent intent = new Intent(this, mTabClassArray[index]);
		intent.putExtra("frompage", "all");
		return intent;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		//保存全局参数
		CalldaGlobalConfig.getInstance().saveGlobalCfg(outState);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		//延迟发送广播（让新来电更新数据库）
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent();
				intent.setAction(Constant.ACTION_TAB_ONRESUME);
				sendBroadcast(intent);
			}
		}, 300);
	}
	
	@Override
	protected void onDestroy() {
		MyApplication.activities.remove(this);
		super.onDestroy();
	}

}
