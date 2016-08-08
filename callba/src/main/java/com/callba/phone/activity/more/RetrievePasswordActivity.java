package com.callba.phone.activity.more;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Task;
import com.callba.phone.bean.UserDao;
import com.callba.phone.cfg.Constant;
import com.callba.phone.manager.UserManager;
import com.callba.phone.service.MainService;
import com.callba.phone.util.DesUtil;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.NumberAddressService;
import com.callba.phone.view.MyProgressDialog;
@ActivityFragmentInject(
		contentViewId = R.layout.more_retrievepass,
		toolbarTitle = R.string.find_password,
		navigationId = R.drawable.press_back
)
public class RetrievePasswordActivity extends BaseActivity implements OnClickListener {

	private Button  bn_submit;
	private EditText et_phoneNum;
	
	private MyProgressDialog progressDialog;
	UserDao userDao;
	private String language="";
	private final MyHandler mHandler = new MyHandler(this);
	private static class MyHandler extends Handler {
		private final WeakReference<RetrievePasswordActivity> mActivity;
        private TimeCount time;
		public MyHandler(RetrievePasswordActivity activity) {
			mActivity = new WeakReference<RetrievePasswordActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			RetrievePasswordActivity activity = mActivity.get();
			if (activity != null) {
				// ...
				switch (msg.what){
					case Interfaces.GET_KEY_START:
						activity.bn_submit.setClickable(false);
						break;
					case Interfaces.GET_KEY_FAILURE:
						activity.toast((String)msg.obj);
						activity.bn_submit.setClickable(true);
						break;
					case Interfaces.GET_KEY_SUCCESS:
						//activity.toast((String)msg.obj);
						break;
					case Interfaces.GET_CODE_START:
						activity.toast("发送短信请求");
						break;
					case Interfaces.GET_CODE_FAILURE:
						activity.toast((String)msg.obj);
						activity.bn_submit.setClickable(true);
						break;
					case Interfaces.GET_CODE_SUCCESS:
						activity.toast((String)msg.obj);
						time =new TimeCount(60000, 1000);
						time.start();
						break;

				}
			}}
		class TimeCount extends CountDownTimer {
			RetrievePasswordActivity activity;
			public TimeCount(long millisInFuture, long countDownInterval) {
				super(millisInFuture, countDownInterval);
				activity = mActivity.get();
			}

			@Override
			public void onFinish() {// 计时完毕
				activity.bn_submit.setBackgroundColor(activity.getResources().getColor(R.color.orange));
				activity.bn_submit.setText(activity.getString(R.string.send_yzm));
				activity.bn_submit.setClickable(true);
			}

			@Override
			public void onTick(long millisUntilFinished) {// 计时过程
				activity.bn_submit.setClickable(false);//防止重复点击
				activity.bn_submit.setBackgroundColor(activity.getResources().getColor(R.color.light_black));
				activity.bn_submit.setText(millisUntilFinished / 1000 + "秒后重新发送");
			}
		}

	}
	public void init() {
		Locale locale = getResources().getConfiguration().locale;
		 language = locale.getCountry();
		bn_submit = (Button) this.findViewById(R.id.bn_retrieve_pass);
		bn_submit.setOnClickListener(this);
		et_phoneNum = (EditText) this.findViewById(R.id.et_retrpass_phone);
		et_phoneNum.requestFocus();
		et_phoneNum.setText(getUsername());
		Timer timer = new Timer(); //设置定时器
		timer.schedule(new TimerTask() {
			@Override
			public void run() { //弹出软键盘的代码
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInputFromWindow(et_phoneNum.getWindowToken(), 0, InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}, 300); //设置300毫秒的时长
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
		userDao=new UserDao(this,mHandler);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bn_retrieve_pass:
			InputMethodManager imm = (InputMethodManager) this
				.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(bn_submit.getWindowToken(), 0);
			String num=et_phoneNum.getText().toString().trim();
			if (num.equals("")) {
				toast( "手机号不能为空!");
				return;
			}
			if ( num.length()>10) {
			/*	String address = NumberAddressService.getAddress(
						num, Constant.DB_PATH,
						RetrievePasswordActivity.this);
				if(!address.equals(""))
				{*/
					userDao.getFindKey(num);
				/*}else {toast("请输入正确的手机号!");
					return;
				}*/}else {toast("请输入正确的手机号!");
				return;
			}

			//getSMSCode();
			break;
			
		default:
			break;
		}
	}

}
