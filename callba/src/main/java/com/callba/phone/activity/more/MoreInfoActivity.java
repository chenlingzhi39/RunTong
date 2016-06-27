package com.callba.phone.activity.more;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.SimpleAdapter;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.activity.calling.CallbackDisplayActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Task;
import com.callba.phone.cfg.Constant;
import com.callba.phone.service.MainService;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.AppVersionChecker;
import com.callba.phone.util.AppVersionChecker.AppVersionBean;
import com.callba.phone.util.SharedPreferenceUtil;
import com.callba.phone.view.CalldaToast;
import com.callba.phone.view.CornerListView;
import com.callba.phone.view.MyDialog;
@ActivityFragmentInject(
		contentViewId = R.layout.tab_more
)
public class MoreInfoActivity extends BaseActivity implements OnClickListener {
//	private CornerListView list_acount;
//	private CornerListView  list_savemoney;
	private CornerListView list_query;
//	private CornerListView list_setting;
//	private CornerListView list_help;
	
	private Button bn_callService;
	private SharedPreferenceUtil mPreferenceUtil;
	private boolean myDialogFlag=false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}


	public void init() {
		// 账户
//		list_acount = (CornerListView) this.findViewById(R.id.more_list_accout);
//		list_acount.setAdapter(new SimpleAdapter(this, getData1(),
//				R.layout.more_list_item, new String[] { "icon", "text" },
//				new int[] { R.id.iv_more_item_icon, R.id.tv_more_item_text }));
//		list_acount.setOnItemClickListener(new MyListViewListener());
		// 赚话费
//		list_savemoney = (CornerListView) this
//				.findViewById(R.id.more_list_savemoney);
//		list_savemoney.setAdapter(new SimpleAdapter(this, getData2(),
//				R.layout.more_list_item, new String[] { "icon", "text" },
//				new int[] { R.id.iv_more_item_icon, R.id.tv_more_item_text }));
//		list_savemoney.setOnItemClickListener(new MyListViewListener());
		// 查询
		list_query = (CornerListView) this.findViewById(R.id.more_list_query);
		list_query.setAdapter(new SimpleAdapter(this, getData3(),
				R.layout.more_list_item, new String[] { "icon", "text" },
				new int[] { R.id.iv_more_item_icon, R.id.tv_more_item_text }));
		list_query.setOnItemClickListener(new MyListViewListener());
		// 设置
//		list_setting = (CornerListView) this
//				.findViewById(R.id.more_list_setting);
//		list_setting.setAdapter(new SimpleAdapter(this, getData4(),
//				R.layout.more_list_item, new String[] { "icon", "text" },
//				new int[] { R.id.iv_more_item_icon, R.id.tv_more_item_text }));
//		list_setting.setOnItemClickListener(new MyListViewListener());
//		// 帮助
//		list_help = (CornerListView) this.findViewById(R.id.more_list_help);
//		list_help.setAdapter(new SimpleAdapter(this, getData5(),
//				R.layout.more_list_item, new String[] { "icon", "text" },
//				new int[] { R.id.iv_more_item_icon, R.id.tv_more_item_text }));
//		list_help.setOnItemClickListener(new MyListViewListener());

		bn_callService = (Button) findViewById(R.id.bn_more_callservice);
		bn_callService.setOnClickListener(this);

		mPreferenceUtil = SharedPreferenceUtil.getInstance(this);
	}

	@Override
	public void refresh(Object... params) {
		
		// 处理返回数据
		Message verionMessage = (Message) params[0];
		
		AppVersionBean appVersionBean = AppVersionChecker.parseVersionInfo(this, verionMessage);
		check2Upgrade(appVersionBean);
		
		//保存secret key
//		CalldaGlobalConfig.getInstance().setSecretKey(appVersionBean.getSecretKey());
		if(mPreferenceUtil != null) {
			String secretKey = appVersionBean.getSecretKey();
			if(!TextUtils.isEmpty(secretKey)) {
				mPreferenceUtil.putString(Constant.SECRET_KEY, secretKey, true);
			}
		}
	}

	/**
	 * 为cornerlistview填充数据
	 * 
	 * @return
	 */
	private List<? extends Map<String, ?>> getData1() {
		List<Map<String, Object>> lists = new ArrayList<Map<String, Object>>();
		Map<String, Object> map1 = new HashMap<String, Object>();
		map1.put("icon", R.drawable.more_zhsz);
		map1.put("text", getString(R.string.more_zhsz));
		lists.add(map1);

		map1 = new HashMap<String, Object>();
		map1.put("icon", R.drawable.more_xgmm);
		map1.put("text", getString(R.string.more_xgmm));
		lists.add(map1);

		map1 = new HashMap<String, Object>();
		map1.put("icon", R.drawable.more_zhmm);
		map1.put("text", getString(R.string.more_zhmm));
		lists.add(map1);

		map1 = new HashMap<String, Object>();
		map1.put("icon", R.drawable.more_txltb);
		map1.put("text", getString(R.string.more_txltb));
		lists.add(map1);

		// map1 = new HashMap<String, Object>();
		// map1.put("icon", R.drawable.more_zzhgl);
		// map1.put("text", getString(R.string.more_zzhgl));
		// lists.add(map1);

		return lists;
	}

	private List<? extends Map<String, ?>> getData2() {
		List<Map<String, Object>> lists = new ArrayList<Map<String, Object>>();
		Map<String, Object> map1 = new HashMap<String, Object>();
		map1.put("icon", R.drawable.more_fxyl);
		map1.put("text", getString(R.string.more_fxyl));
		lists.add(map1);

		map1 = new HashMap<String, Object>();
		map1.put("icon", R.drawable.more_dxyq);
		map1.put("text", getString(R.string.more_dxyq));
		lists.add(map1);

		map1 = new HashMap<String, Object>();
		map1.put("icon", R.drawable.more_mrqd);
		map1.put("text", getString(R.string.more_mrqd));
		lists.add(map1);

		return lists;
	}

	private List<? extends Map<String, ?>> getData3() {
		List<Map<String, Object>> lists = new ArrayList<Map<String, Object>>();
		Map<String, Object> map1 = new HashMap<String, Object>();
		map1.put("icon", R.drawable.more_cxye);
		map1.put("text", getString(R.string.more_cxye));
		lists.add(map1);

//		map1 = new HashMap<String, Object>();
//		map1.put("icon", R.drawable.more_cxhd);
//		map1.put("text", getString(R.string.more_cxhd));
//		lists.add(map1);
//
//		map1 = new HashMap<String, Object>();
//		map1.put("icon", R.drawable.more_cxfl);
//		map1.put("text", getString(R.string.more_cxfl));
//		lists.add(map1);

		map1 = new HashMap<String, Object>();
		map1.put("icon", R.drawable.more_ggyh);
		map1.put("text", getString(R.string.more_ggyh));
		lists.add(map1);
		
		map1 = new HashMap<String, Object>();
		map1.put("icon", R.drawable.more_exit);
		map1.put("text", getString(R.string.more_zxtc));
		lists.add(map1);

		return lists;
	}

	private List<? extends Map<String, ?>> getData4() {
		List<Map<String, Object>> lists = new ArrayList<Map<String, Object>>();
		Map<String, Object> map1 = new HashMap<String, Object>();
//		map1.put("icon", R.drawable.more_bdsz);
//		map1.put("text", getString(R.string.more_bdsz));
//		lists.add(map1);
//
//		map1 = new HashMap<String, Object>();
		map1.put("icon", R.drawable.more_xhsz);
		map1.put("text", getString(R.string.more_xhsz));
		lists.add(map1);

		map1 = new HashMap<String, Object>();
		map1.put("icon", R.drawable.more_qhsz);
		map1.put("text", getString(R.string.more_qhsz));
		lists.add(map1);

		map1 = new HashMap<String, Object>();
		map1.put("icon", R.drawable.more_dlsz);
		map1.put("text", getString(R.string.more_dlsz));
		lists.add(map1);

		map1 = new HashMap<String, Object>();
		map1.put("icon", R.drawable.more_bdsz);
		map1.put("text", getString(R.string.more_zdjt));
		lists.add(map1);
		
		map1 = new HashMap<String, Object>();
		map1.put("icon", R.drawable.keyboard_setting_icon);
		map1.put("text", getString(R.string.more_jpysz));
		lists.add(map1);
		
		map1 = new HashMap<String, Object>();
		map1.put("icon", R.drawable.more_sys_dialer);
		map1.put("text", getString(R.string.more_xtbhp));
		lists.add(map1);

		map1 = new HashMap<String, Object>();
		map1.put("icon", R.drawable.more_exit);
		map1.put("text", getString(R.string.more_zxtc));
		lists.add(map1);

		return lists;
	}

	private List<? extends Map<String, ?>> getData5() {
		List<Map<String, Object>> lists = new ArrayList<Map<String, Object>>();
		Map<String, Object> map1 = new HashMap<String, Object>();
		map1.put("icon", R.drawable.more_help);
		map1.put("text", getString(R.string.more_xtbzzf));
		lists.add(map1);

		map1 = new HashMap<String, Object>();
		map1.put("icon", R.drawable.more_rjsj);
		map1.put("text", getString(R.string.more_rjsj));
		lists.add(map1);

		map1 = new HashMap<String, Object>();
		map1.put("icon", R.drawable.more_gywm);
		map1.put("text", getString(R.string.more_gywm));
		lists.add(map1);

		map1 = new HashMap<String, Object>();
		map1.put("icon", R.drawable.more_bug);
		map1.put("text", getString(R.string.more_bug));
		lists.add(map1);

		return lists;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.bn_more_callservice:
			dialCallback();
			break;

		default:
			break;
		}
	}

	class MyListViewListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
//			if (arg0 == list_acount) {
//				switch (arg2) {
//				case 0:
//					// 账户设置
//					goToActivity(AccountSettingActivity.class);
//					break;
//				case 1:
//					// 修改密码
//					goToActivity(ChangePasswordActivity.class);
//					break;
//				case 2:
//					// 找回密码
//					goToActivity(RetrievePasswordActivity.class);
//					break;
//				case 3:
//					// 通讯录同步
//					goToActivity(ContactBackupActivity.class);
//					break;
//				// case 4:
//				// 子账户管理
//				// goToActivity(SubAccountActivity.class);
//				// break;
//				default:
//					break;
//				}
//			} 
//			else if (arg0 == list_savemoney) {
//				switch (arg2) {
//				case 0:
//					// 分享有礼
//					goToActivity(ShareActivity.class);
//					break;
//				case 1:
//					// 短信邀请
//					goToActivity(InviteActivity.class);
//					break;
//				case 2:
//					// 每日签到
//					goToActivity(SignActivity.class);
//					break;
//				default:
//					break;
//				}
//			} else
			 if (arg0 == list_query) {
				switch (arg2) {
				case 0:
					// 查询余额
					goToActivity(QueryMealActivity.class);
					break;
//				case 1:
//					// 查询话单
//					goToActivity(QueryCalllogActivity.class);
//					break;
//				case 2:
//					// 查询费率
//					goToActivity(FeeQueryActivity.class);
//					break;
				case 1:
					// 公告及优惠
					goToActivity(PreferentialActivity.class);
					break;
				case 2:
					// 注销退出
					goToActivity(ExitActivity.class);
					break;
				default:
					break;
				}
			} 
//			else if (arg0 == list_setting) {
//				switch (arg2) {
//				case 0:
//					// 显号设置
//					goToActivity(ShowNumberActivity.class);
//					break;
//				case 1:
//					// 区号设置
//					goToActivity(QuHaoActivity.class);
//					break;
//				case 2:
//					// 登录设置
//					goToActivity(LoginSettingActivity.class);
//					break;
//				case 3:
//					// 自动接听
//					goToActivity(AutoAnswerActivity.class);
//					break;
//				case 4:
//					// 键盘音设置
//					goToActivity(KeyboardSettingActivity.class);
//					break;
//				case 5:
//					// 监听系统拨号盘 
//					goToActivity(SystemDialSettingActivity.class);
//					break;
//				case 6:
//					// 注销退出
//					goToActivity(ExitActivity.class);
//					break;
//				default:
//					break;
//				}
//			} 
//			else if (arg0 == list_help) {
//				switch (arg2) {
//				case 0:
//					// 系统帮助、资费说明
//					goToActivity(HelpCenterActivity.class);
//					break;
//				case 1:
//					// 软件升级
//					if (!myDialogFlag) {
//						softwareUpgrade();
//					}
//					break;
//				case 2:
//					// 关于我们
//					goToActivity(AboutActivity.class);
//					break;
//				case 3:
//					// bug提交
//					FeedbackAgent agent = new FeedbackAgent(
//							MoreInfoActivity.this);
//					agent.startFeedbackActivity();
//					break;
//				default:
//					break;
//				}
//			}
		}
	}

	/**
	 * 开启新activity
	 * 
	 * @param clazz
	 */
	private void goToActivity(Class<?> clazz) {
		Intent intent = new Intent(MoreInfoActivity.this, clazz);
		MoreInfoActivity.this.startActivity(intent);
	}

	/**
	 * 软件升级
	 */
	public void softwareUpgrade() {
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
		taskParams.put("fromPage", "MoreInfoActivity");
		taskParams.put("lan", activityUtil.language(this));
		task.setTaskParams(taskParams);

		MainService.newTask(task);
	}

	/**
	 * 回拨 呼叫客服
	 */
	private void dialCallback() {
		Intent intent = new Intent();
		intent.setClass(this, CallbackDisplayActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("name", getString(R.string.service_call));
		bundle.putString("number", getString(R.string.service_callnum));
		bundle.putString("callsetting", Constant.CALL_SETTING_HUI_BO);
		intent.putExtras(bundle);
		
		startActivity(intent);
	}
	
	/**
	 * 检查升级
	 */
	private void check2Upgrade(final AppVersionBean appVersionBean) {
		if (appVersionBean.isForceUpgrade()) {
			// 强制升级
			myDialogFlag=true;
			MyDialog.showDialog(this, getString(R.string.sjtsxx),
					new OnClickListener() {
						@Override
						public void onClick(View v) {
							switch (v.getId()) {
							case R.id.bn_ok:
								// 确定升级
								Uri uri = Uri.parse(appVersionBean.getDownloadUrl());
								Intent intent = new Intent(Intent.ACTION_VIEW,
										uri);
								startActivity(intent);
								
								mPreferenceUtil.putBoolean(
										Constant.IS_NOTICE_UPGRADE, false, true);
								break;
							case R.id.bn_cancel:
								
								break;
							default:
								break;
							}
							myDialogFlag=false;
							MyDialog.dismissDialog();
						}
					});
		} else {
			// 提示升级
			if(appVersionBean.isHasNewVersion()) {
				//有新版本
				myDialogFlag = true;
				MyDialog.showDialog(this, getString(R.string.more_xbbgx),
						new OnClickListener() {
							@Override
							public void onClick(View v) {
								switch (v.getId()) {
								case R.id.bn_ok:
									// 确定升级
									try {
										Uri uri = Uri.parse(appVersionBean.getDownloadUrl());
										Intent intent = new Intent(Intent.ACTION_VIEW,
												uri);
										startActivity(intent);
									} catch (Exception e) {
										e.printStackTrace();

										CalldaToast calldaToast = new CalldaToast();
										calldaToast.showToast(getApplicationContext(), R.string.upgrade_openfailed);
									}

									mPreferenceUtil.putBoolean(
											Constant.IS_NOTICE_UPGRADE, false, true);
									break;
								case R.id.bn_cancel:
									mPreferenceUtil.putBoolean(
											Constant.IS_NOTICE_UPGRADE, true, true);
									
									break;
								default:
									break;
								}
								myDialogFlag=false;
								MyDialog.dismissDialog();
							}
						});
			} else {
				//无新版本
				String showMsg = getString(R.string.more_zxbbsjnum) + appVersionBean.getLocalVersionCode() + "\n"
						+ getString(R.string.more_zxbbsj);
				
				CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(), showMsg);
			}
		} 
	}

	/**
	 * 重写onkeyDown 捕捉返回键
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			//转到后台运行
			ActivityUtil.moveAllActivityToBack();
			return true;
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.getMenuInflater().inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_exit) {
			super.exitApp();
		}
		return true;
	}
}
