package com.callba.phone.activity.calling;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.conn.ConnectTimeoutException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.bitmap.BitmapCommonUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.bitmap.callback.DefaultBitmapLoadCallBack;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Task;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.service.AutoAnswerReceiver;
import com.callba.phone.service.CalllogService;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.BitmapHelp;
import com.callba.phone.util.HttpUtils;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.util.NetworkDetector;
import com.callba.phone.util.TimeFormatUtil;

@ActivityFragmentInject(
		contentViewId = R.layout.callback_display)
public class CallbackDisplayActivity extends BaseActivity {
	private String name;
	private String number;
	private TextView tv_name;
	private TextView tv_num;
	private TextView tv_status;
	private CalllogService calllogService;
	@ViewInject(R.id.iv_call_bg)
	private ImageView iv_ad;
	private BitmapUtils bitmapUtils;

	private BitmapDisplayConfig bigPicDisplayConfig;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//loadADImage();
		super.onCreate(savedInstanceState);
	}
	
	/**
	 * 统计回拨失败数据
	 * @author zhw
	 *
	 * @param errorMsg
	 */
	private void countCallbackFailedData(String errorMsg) {
		String errorTime = TimeFormatUtil.formatTimeRange();
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put("errorMsg", errorMsg);
		paramsMap.put("errorTime", errorTime);
	//	MobclickAgent.onEvent(this, "callback_failed", paramsMap);
	}

	private void callback() {
		
		final Handler mHandler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				if (msg.what == Task.TASK_SUCCESS) {
					try {
						String result = (String) msg.obj;
						String[] content = result.split("\\|");
						Log.i("dial_result",result);
						if("0".equals(content[0])) {
							//回拨成功，开启自动接听
							AutoAnswerReceiver.answerPhone(CallbackDisplayActivity.this);
							calllogService.saveBackCallLog("", number);
						} else {
							//统计回拨失败数据
							countCallbackFailedData(content[1]);
							
							delayFinish();
						}
						tv_status.setText(content[1]);
					} catch (Exception e) {
						tv_status.setText(R.string.server_error);
						//统计回拨失败数据
						countCallbackFailedData(getString(R.string.server_error));
						
						delayFinish();
					}
				} else if (msg.what == Task.TASK_NETWORK_ERROR) {
					tv_status.setText(R.string.network_error);
					//统计回拨失败数据
					countCallbackFailedData(getString(R.string.network_error));
					
					delayFinish();
				} else if(msg.what == Task.TASK_TIMEOUT) {
					tv_status.setText(R.string.callback_timeout);
					//统计回拨失败数据
					countCallbackFailedData(getString(R.string.callback_timeout));
					
					delayFinish();
				} else {
					tv_status.setText(R.string.unknownerror);
					//统计回拨失败数据
					countCallbackFailedData(getString(R.string.unknownerror));
					
					delayFinish();
				}
			}
			
			/**
			 * 延迟关闭当前页面
			 * @author zhw
			 */
			private void delayFinish() {
				//呼叫失败，延迟关闭
				this.postDelayed(new Runnable() {
					@Override
					public void run() {
						finish();
					}
				}, 2000);
			}
		};

		new Thread() {
			public void run() {
				ActivityUtil activityUtil=new ActivityUtil();
				String lan=activityUtil.language(CallbackDisplayActivity.this);
				Map<String, String> params = new HashMap<String, String>();
				params.put("loginName", CalldaGlobalConfig.getInstance()
						.getUsername());
				params.put("loginPwd", CalldaGlobalConfig.getInstance()
						.getPassword());
				params.put("softType", "android");
				params.put("caller", CalldaGlobalConfig.getInstance()
						.getUsername());
				params.put("callee", number);
				//params.put("lan", lan);

				Message msg = mHandler.obtainMessage();

				try {
					Logger.v("", params+"");
					if (NetworkDetector.detect(CallbackDisplayActivity.this.getApplicationContext())) {
						String result = HttpUtils.getDataFromHttpPost(
								Interfaces.DIAL_CALLBACK, params);
						msg.what = Task.TASK_SUCCESS;
						msg.obj = result.replace("\n", "").replace("\r", "");
					} else {
						// 无网络连接
						msg.what = Task.TASK_NETWORK_ERROR;
					}
				} catch(ConnectTimeoutException cte) {
					cte.printStackTrace();
					msg.what = Task.TASK_TIMEOUT;
				} catch (Exception e) {
					e.printStackTrace();
					msg.what = Task.TASK_FAILED;
				} finally {
					mHandler.sendMessage(msg);
				}
			}
		}.start();
		
	}

	@Override
	public void init() {
		tv_name = (TextView) findViewById(R.id.tv_name);
		tv_num = (TextView) findViewById(R.id.tv_num);
		tv_status = (TextView) findViewById(R.id.tv_status);
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		name = bundle.getString("name");
		number = bundle.getString("number");
		tv_name.setText(name);
		tv_num.setText(number);
		// tv_status.setText(number);
		calllogService = new CalllogService(this, null);
		callback();
	}

	@Override
	public void refresh(Object... params) {
	}

	@Override
	protected void onResume() {
		setSendNotification(false);
		super.onResume();
	}
	
	private void loadADImage() {
		ViewUtils.inject(this);
		if (bitmapUtils == null) {
			bitmapUtils = BitmapHelp.getBitmapUtils(this
					.getApplicationContext());
		}
		String imgUrl = CalldaGlobalConfig.getInstance().getIvPathBack();
		if (imgUrl==null) {
			return;
		}
		bigPicDisplayConfig = new BitmapDisplayConfig();
		// bigPicDisplayConfig.setShowOriginal(true); // 显示原始图片,不压缩, 尽量不要使用,
		// 图片太大时容易OOM。
		bigPicDisplayConfig.setBitmapConfig(Bitmap.Config.RGB_565);
		bigPicDisplayConfig.setBitmapMaxSize(BitmapCommonUtils
				.getScreenSize(this));

		BitmapLoadCallBack<ImageView> callback = new DefaultBitmapLoadCallBack<ImageView>() {
			@Override
			public void onLoadStarted(ImageView container, String uri,
					BitmapDisplayConfig config) {
				super.onLoadStarted(container, uri, config);
			}

			@Override
			public void onLoadCompleted(ImageView container, String uri,
					Bitmap bitmap, BitmapDisplayConfig config,
					BitmapLoadFrom from) {
				super.onLoadCompleted(container, uri, bitmap, config, from);
			}
		};
		bitmapUtils.display(iv_ad, imgUrl, bigPicDisplayConfig, callback);
	}
}
