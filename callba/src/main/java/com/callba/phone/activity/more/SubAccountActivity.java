package com.callba.phone.activity.more;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.bean.Task;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.service.MainService;
import com.callba.phone.view.CalldaToast;

public class SubAccountActivity extends BaseActivity implements OnClickListener {
	private Button bn_back, bn_add;
	private TextView tv_account, tv_accNum;
	private ListView lv_subAccount;
	private LinearLayout ll_loading;

	private String language="";
	private SubAccountListAdapter adapter;	//ListView adapter
	
	@Override
	public void init() {
		Locale locale = getResources().getConfiguration().locale;
		 language = locale.getCountry();
		bn_add = (Button) findViewById(R.id.bn_subaccount_add);
		bn_back = (Button) findViewById(R.id.bn_subaccount_back);
		bn_add.setOnClickListener(this);
		bn_back.setOnClickListener(this);

		tv_account = (TextView) findViewById(R.id.tv_subaccount_account);
		tv_accNum = (TextView) findViewById(R.id.tv_subaccount_num);
		tv_account.setText(CalldaGlobalConfig.getInstance().getUsername());

		lv_subAccount = (ListView) findViewById(R.id.lv_subaccount);
		
		ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
	}

	@Override
	public void refresh(Object... params) {
		Message msg = (Message) params[0];

		if (msg.what == Task.TASK_GET_SUBACCOUNT_NUM) {
			if (msg.arg1 == Task.TASK_SUCCESS) {
				String content = (String) msg.obj;
				try {
					String[] result = content.split("\\|");
					if ("0".equals(result[0])) {
						// 成功fanhui数据
						parseData(result);
					} else if ("1".equals(result[0])) {
						CalldaToast calldaToast = new CalldaToast();
						calldaToast.showToast(getApplicationContext(), result[1]);
					}
				} catch (Exception e) {
					e.printStackTrace();
					
					CalldaToast calldaToast = new CalldaToast();
					calldaToast.showToast(getApplicationContext(), R.string.getserverdata_exception);
				}
			} else {
				CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(), R.string.getserverdata_failed);
			}
		} else if (msg.what == Task.TASK_GET_SUBACCOUNT_LIST) {
			if (msg.arg1 == Task.TASK_SUCCESS) {
				String content = (String) msg.obj;
				try {
					String[] result = content.split("\\|");
					if ("0".equals(result[0])) {
						// 成功fanhui数据
						parseData1(result);
					} else if ("1".equals(result[0])) {
						ll_loading.setVisibility(View.GONE);
						
						CalldaToast calldaToast = new CalldaToast();
						calldaToast.showToast(getApplicationContext(), result[1]);
					}
				} catch (Exception e) {
					e.printStackTrace();
					ll_loading.setVisibility(View.GONE);
					
					CalldaToast calldaToast = new CalldaToast();
					calldaToast.showToast(getApplicationContext(), R.string.getserverdata_exception);
				}
			} else {
				ll_loading.setVisibility(View.GONE);
				
				CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(), R.string.getserverdata_failed);
			}
		}else if(msg.what == Task.TASK_DELETE_SUBACCOUNT) {
			//删除子账号
			adapter.dismissProDialog();
			if (msg.arg1 == Task.TASK_SUCCESS) {
				String content = (String) msg.obj;
				try {
					String[] result = content.split("\\|");
					if ("0".equals(result[0])) {
						// 成功fanhui数据
						CalldaToast calldaToast = new CalldaToast();
						calldaToast.showToast(getApplicationContext(), result[1]);
						
						getSubAccountList();
					} else if ("1".equals(result[0])) {
						CalldaToast calldaToast = new CalldaToast();
						calldaToast.showToast(getApplicationContext(), result[1]);
					}
				} catch (Exception e) {
					e.printStackTrace();
					
					CalldaToast calldaToast = new CalldaToast();
					calldaToast.showToast(getApplicationContext(), R.string.getserverdata_exception);
				}
			} else {
				CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(), R.string.getserverdata_failed);
			}
		}else if(msg.what == Task.TASK_SUBACCOUNT_CHANGEPASS) {
			//修改子账号密码
			adapter.dismissProDialog();
			if (msg.arg1 == Task.TASK_SUCCESS) {
				String content = (String) msg.obj;
				try {
					String[] result = content.split("\\|");
					if ("0".equals(result[0])) {
						// 成功fanhui数据
						CalldaToast calldaToast = new CalldaToast();
						calldaToast.showToast(getApplicationContext(), result[1]);
						
						getSubAccountList();
					} else if ("1".equals(result[0])) {
						CalldaToast calldaToast = new CalldaToast();
						calldaToast.showToast(getApplicationContext(), result[1]);
					}
				} catch (Exception e) {
					e.printStackTrace();
					
					CalldaToast calldaToast = new CalldaToast();
					calldaToast.showToast(getApplicationContext(), R.string.getserverdata_exception);
				}
			} else {
				CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(), R.string.getserverdata_failed);
			}
		}
	}

	/**
	 * 处理子账户列表 数据
	 * 
	 * @param result
	 * 
	 * 0|2|15716193160:112233,15716193157:112233
	 */
	private void parseData1(String[] result) {
		if(result.length < 3) {
			ll_loading.setVisibility(View.GONE);
			
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.sacc_zzhk);
			return;
		}
		String subAccounts = result[2];
		
		String[] accountStrs = subAccounts.split(",");

		List<SubAccountBean> accountInfos = new ArrayList<SubAccountBean>();
		for (int i = 0; i < accountStrs.length; i++) {
			SubAccountBean bean = new SubAccountBean();
			bean.setuName(accountStrs[i].split(":")[0]);
			bean.setuPass(accountStrs[i].split(":")[1]);
			
			accountInfos.add(bean);
		}

		adapter = new SubAccountListAdapter(this, accountInfos);
		lv_subAccount.setAdapter(adapter);
		
		ll_loading.setVisibility(View.GONE);
	}

	/**
	 * 处理子账户个数 数据
	 * 
	 * @param result
	 */
	private void parseData(String[] result) {
		String currSubAccount = result[1];
		String availSubAccount = result[2];

		tv_accNum.setText(currSubAccount + "/" + availSubAccount);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.more_subaccount);
		super.onCreate(savedInstanceState);

		getSubAccountNum();
		getSubAccountList();
	}

	/**
	 * 获取子账户数量
	 */
	private void getSubAccountNum() {
		Task task = new Task(Task.TASK_GET_SUBACCOUNT_NUM);
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put("loginName", CalldaGlobalConfig.getInstance().getUsername());
		taskParams.put("loginPwd", CalldaGlobalConfig.getInstance().getPassword());
		taskParams.put("softType", "android");
		taskParams.put("lan", language);
		task.setTaskParams(taskParams);

		MainService.newTask(task);
	}

	/**
	 * 获取子账户列表
	 */
	private void getSubAccountList() {
		Task task = new Task(Task.TASK_GET_SUBACCOUNT_LIST);
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put("loginName", CalldaGlobalConfig.getInstance().getUsername());
		taskParams.put("loginPwd", CalldaGlobalConfig.getInstance().getPassword());
		taskParams.put("softType", "android");
		taskParams.put("lan", language);
		task.setTaskParams(taskParams);

		MainService.newTask(task);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bn_subaccount_back:
			finish();
			break;

		case R.id.bn_subaccount_add:
			addSubAccount();
			break;

		default:
			break;
		}
	}

	/**
	 * 添加子账户
	 */
	private void addSubAccount() {
		Intent intent = new Intent(this, AddSubAccountActivity.class);
		startActivityForResult(intent, 20);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 20 && resultCode == RESULT_OK) {
			//成功添加手机子账户
			//刷新子账户
			getSubAccountList();
		}
	}
	
	/**
	 * 子账户列表 JavaBean
	 * @author Administrator
	 */
	class SubAccountBean {
		private String uName;
		private String uPass;

		public String getuName() {
			return uName;
		}

		public void setuName(String uName) {
			this.uName = uName;
		}

		public String getuPass() {
			return uPass;
		}

		public void setuPass(String uPass) {
			this.uPass = uPass;
		}
	}
}
