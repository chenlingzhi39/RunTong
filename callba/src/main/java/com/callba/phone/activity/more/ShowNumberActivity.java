package com.callba.phone.activity.more;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ToggleButton;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.bean.Task;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.service.MainService;
import com.callba.phone.util.Logger;
import com.callba.phone.view.CalldaToast;
import com.callba.phone.view.MyProgressDialog;

public class ShowNumberActivity extends BaseActivity {
	private Button bn_back, bn_refresh;
	private ToggleButton tb_zhibo, tb_huibo;
	private boolean tag_zhibo, tag_huibo;

	private MyProgressDialog progressDialog;

	private String changetype, tg_calltype,language;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.more_shownumber);
		super.onCreate(savedInstanceState);

		refreshall();
	}

	private void refreshall() {
		progressDialog = new MyProgressDialog(this,
				getString(R.string.snum_cxxh));
		progressDialog.show();

		Task task = new Task(Task.TASK_SHOWNUMBER_QUERY);
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put("loginName", CalldaGlobalConfig.getInstance().getUsername());
		taskParams.put("loginPwd", CalldaGlobalConfig.getInstance().getPassword());
		taskParams.put("softType", "android");
		taskParams.put("lan", language);
		task.setTaskParams(taskParams);

		MainService.newTask(task);
	}

	@Override
	public void init() {
		Locale locale = getResources().getConfiguration().locale;
		String language = locale.getCountry();
		
		bn_back = (Button) findViewById(R.id.bn_xh_back);
		bn_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		bn_refresh = (Button) findViewById(R.id.bn_xh_refresh);
		bn_refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshall();
			}
		});

		tb_huibo = (ToggleButton) findViewById(R.id.tb_xh_hb);
		tb_zhibo = (ToggleButton) findViewById(R.id.tb_xh_zb);

		tb_huibo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				changetype = "callback";
				if (tag_huibo) {
					tg_calltype = "hidden";
					setShowNumber(changetype, tg_calltype);
				} else {
					tg_calltype = "show";
					setShowNumber(changetype, tg_calltype);
				}
			}
		});
		tb_zhibo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				changetype = "sip";
				if (tag_zhibo) {
					tg_calltype = "hidden";
					setShowNumber(changetype, tg_calltype);
				} else {
					tg_calltype = "show";
					setShowNumber(changetype, tg_calltype);
				}
			}
		});
	}

	@Override
	public void refresh(Object... params) {
		if (progressDialog!=null&&progressDialog.isShowing()) {
			progressDialog.dismiss();
		}

		Message msg = (Message) params[0];
		if (msg.what == Task.TASK_SHOWNUMBER_QUERY) {
			// 查询
			if (msg.arg1 == Task.TASK_SUCCESS) {
				try {
					String result = (String) msg.obj;
					String[] content = result.split("\\|");
					Logger.i("查询显号设置结果", result);
					if ("0".equals(content[0])) {
						String callback = content[1].split(",")[0].split(":")[1];
						String sip = content[1].split(",")[1].split(":")[1];
						Logger.i("showNumberActivity", "callback：" + callback
								+ "    sip：" + sip);
						setButtonShow(callback, sip);
					} else {
						CalldaToast calldaToast = new CalldaToast();
						calldaToast.showToast(getApplicationContext(), content[1]);
					}
				} catch (Exception e) {
					CalldaToast calldaToast = new CalldaToast();
					calldaToast.showToast(getApplicationContext(), R.string.getserverdata_exception);
					
					e.printStackTrace();
				}
			} else {
				CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(), R.string.snum_cxsb);
			}
		} else if (msg.what == Task.TASK_SHOWNUMBER_SET) {
			// 设置

			if (msg.arg1 == Task.TASK_SUCCESS) {
				try {
					String result = (String) msg.obj;
					Logger.i("显号设置结果", result);
					String[] content = result.split("\\|");
					if ("0".equals(content[0])) {
						changeButtonShow();
						
						CalldaToast calldaToast = new CalldaToast();
						calldaToast.showToast(getApplicationContext(), content[1]);
					} else {
						CalldaToast calldaToast = new CalldaToast();
						calldaToast.showToast(getApplicationContext(), content[1]);
					}
				} catch (Exception e) {
					CalldaToast calldaToast = new CalldaToast();
					calldaToast.showToast(getApplicationContext(), R.string.getserverdata_exception);
					e.printStackTrace();
				}
			} else {
				CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(), R.string.snum_qqsb);
			}
		}
	}

	private void changeButtonShow() {
		// String type = (String) taskParams.get("type");
		// String callType = (String) taskParams.get("callType");

		if ("sip".equals(changetype)) {
			if ("hidden".equals(tg_calltype)) {
				tb_zhibo.setBackgroundResource(R.drawable.toggle_off);
				tag_zhibo = false;
			} else if ("show".equals(tg_calltype)) {
				tb_zhibo.setBackgroundResource(R.drawable.toggle_on);
				tag_zhibo = true;
			}
		} else if ("callback".equals(changetype)) {
			if ("hidden".equals(tg_calltype)) {
				tb_huibo.setBackgroundResource(R.drawable.toggle_off);
				tag_huibo = false;
			} else if ("show".equals(tg_calltype)) {
				tb_huibo.setBackgroundResource(R.drawable.toggle_on);
				tag_huibo = true;
			}
		}
	}

	// 界面显示
	private void setButtonShow(String callback, String sip) {
		if ("show".equals(callback)) {
			tb_huibo.setBackgroundResource(R.drawable.toggle_on);
			tag_huibo = true;
		} else if ("hidden".equals(callback)) {
			tb_huibo.setBackgroundResource(R.drawable.toggle_off);
			tag_huibo = false;
		}

		if ("show".equals(sip)) {
			tb_zhibo.setBackgroundResource(R.drawable.toggle_on);
			tag_zhibo = true;
		} else if ("hidden".equals(sip)) {
			tb_zhibo.setBackgroundResource(R.drawable.toggle_off);
			tag_zhibo = false;
		}
	}

	/**
	 * 设置显号状态
	 * 
	 * @param type
	 *            callback:回拨显示设置， sip:对sip示设置
	 * @param callType
	 *            show:显号 hidden 隐藏号码
	 */
	private void setShowNumber(String type, String callType) {
		Task task = new Task(Task.TASK_SHOWNUMBER_SET);
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put("loginName", CalldaGlobalConfig.getInstance().getUsername());
		taskParams.put("loginPwd", CalldaGlobalConfig.getInstance().getPassword());
		taskParams.put("type", type);
		taskParams.put("callType", callType);
		taskParams.put("softType", "android");
		taskParams.put("lan", language);
		task.setTaskParams(taskParams);
		Logger.i("显号设置发送", taskParams.toString());
		MainService.newTask(task);
	}
}
