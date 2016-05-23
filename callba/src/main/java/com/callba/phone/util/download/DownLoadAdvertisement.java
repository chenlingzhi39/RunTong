package com.callba.phone.util.download;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.callba.phone.activity.MainCallActivity;
import com.callba.phone.bean.AdvertisementBean;
import com.callba.phone.bean.Task;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.cfg.Constant;
import com.callba.phone.logic.login.UserLoginErrorMsg;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.HttpUtils;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.util.NetWorkUtil;
import com.callba.phone.util.download.AdvertisementUtil.AdvertisementListener;

public class DownLoadAdvertisement {
	private static final String TAG = DownLoadAdvertisement.class
			.getCanonicalName();
	private static String lan;
	private static DownLoadAdvertisement loadAD;
	private static Context mContext;

	private DownLoadAdvertisement() {
	}

	// 当前登录超时重试次数
	private int currentLoginTime;

	public synchronized static DownLoadAdvertisement getInstance() {
		if (loadAD == null) {
			loadAD = new DownLoadAdvertisement();
		}
		return loadAD;
	}

	@SuppressLint("HandlerLeak")
	public synchronized void downloadAD(final Context context, final Task task,
			final DownloadADListener loadADListener) {
		// 设置超时重试次数为0
		currentLoginTime = 0;
		this.mContext = context;
		final Handler mHandler = new Handler() {
			public void handleMessage(Message msg) {
				/*boolean timeout = handleResult(mContext, loadADListener, msg);

				if (timeout) {
					// 重新登录
					currentLoginTime++;
					Logger.i("", "DownloadAD timeout Retry login. time is "
							+ currentLoginTime);
					asyncDownloadAD(this, task);
				}*/
			}
		};
		ActivityUtil activityUtil = new ActivityUtil();
		lan = activityUtil.language(context);

		asyncDownloadAD(mHandler, task);
	}

	protected void asyncDownloadAD(final Handler handler, final Task task) {

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				download(handler, task);
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();

	}

	protected void download(Handler handler, Task task) {
		Logger.i(TAG, "download");

		Map<String, String> taskParams = new HashMap<String, String>();
		taskParams.put("loginName", CalldaGlobalConfig.getInstance()
				.getUsername());
		taskParams.put("loginPwd", CalldaGlobalConfig.getInstance()
				.getPassword());
		taskParams.put("softType", "android");
		// taskParams.put("softType", "android");
		taskParams.put("lan", lan);
		Message msg = handler.obtainMessage();
//		String results = null;
		try {
			if (NetWorkUtil.detect(mContext.getApplicationContext())) {
				String result = HttpUtils.getDataFromHttpPost(
						Interfaces.GET_ADVERTICEMENT, taskParams);
//				Logger.i(TAG, result);
				msg.arg1 = Task.TASK_SUCCESS;
				Log.i("ad",result);
				msg.obj = result.replace("\n", "").replace("\r", "");
				String[] result1=result.split("\\|");
				CalldaGlobalConfig.getInstance().setAdvertisements(result1[1].split(","));
			} else {
				// 无网络连接
				msg.what = Task.TASK_NETWORK_ERROR;
			}
		} catch (Exception e) {
			e.printStackTrace();
			msg.arg1 = Task.TASK_FAILED;
		} finally {
			msg.what = task.getTaskID();
			// msg.obj = results;
			handler.sendMessage(msg);
		}
	}

	protected boolean handleResult(Context context,
			DownloadADListener loadADListener, Message msg) {
		if (loadADListener == null) {
			// throw new IllegalArgumentException("UserLoginListener 为空");
			if (msg.arg1 == Task.TASK_SUCCESS) {
				Logger.v("", (String) msg.obj + "----handleResult");
				String[] result = ((String) msg.obj).split("\\|");
				if ("0".equals(result[0])) {
					// 成功
					analyzeDate(context, result[1]);
				}
			}
			return false;
		}
		if (msg.what == Task.TASK_DOWNLOAD_AD) {
			int taskResultCode = msg.arg1;

			switch (taskResultCode) {
			case Task.TASK_SUCCESS:
				try {
					String[] result = ((String) msg.obj).split("\\|");
					if ("0".equals(result[0])) {
						String urls = "";
						if (result.length > 1) {
							urls = result[1];
							analyzeDate(context, result[1]);
						}
						loadADListener.onSuccess(urls);
					} else if ("1".equals(result[0])) {
						loadADListener.onFailed(result[1]);
					}
				} catch (Exception e) {
					e.printStackTrace();
					loadADListener.onFailed(UserLoginErrorMsg.SERVER_ERROR);
				}
				break;

			case Task.TASK_TIMEOUT:
				if (currentLoginTime < Constant.LOGIN_RETRY_TIMES) {
					return true;
				} else {
					// 统计登录超时
					// MobclickAgent.onEvent(context, "login_timeout");

					loadADListener.onFailed(UserLoginErrorMsg.TIMEOUT);
				}
				break;

			case Task.TASK_FAILED:
				loadADListener.onFailed(UserLoginErrorMsg.SERVER_ERROR);
				break;

			case Task.TASK_NETWORK_ERROR:
				loadADListener.onFailed(UserLoginErrorMsg.CONN_NETWORK_FAILED);
				break;
			case Task.TASK_UNKNOWN_HOST:
				loadADListener.onFailed(UserLoginErrorMsg.CONN_NETWORK_FAILED);
				break;

			default:
				break;
			}
		} else {
			loadADListener.onFailed(UserLoginErrorMsg.UNKNOWN);
		}
		return false;
	}

	private static void analyzeDate(final Context context, String result) {
		final String[] content;
		final AdvertisementBean advertisementBean = new AdvertisementBean();
		try {
			Logger.v(TAG, "analyzeDate");
			content = result.split(",");
			final String imageUrl1 = content[0];// 键盘广告
			final String imageName1 = imageUrl1.substring(imageUrl1.lastIndexOf("/") + 1, imageUrl1.length());
			advertisementBean.setIvName(imageName1);
			advertisementBean.setIvPath(imageUrl1);
			CalldaGlobalConfig.getInstance().setIvPath(imageUrl1);
			Logger.i(TAG, "imageName=" + imageName1 );
//			if (content.length<=1) {
//				Intent msgIntent = new Intent(MainCallActivity.KEYBOARD_MESSAGE_RECEIVED_ACTION);
//				Logger.v(TAG, "发送已获取到图片广播");
//				LocalBroadcastManager.getInstance(context).sendBroadcast(msgIntent);
//				return;
//			}

			String imageUrlback = content[1];// 回拨广告
			final String  imageNameback = imageUrl1.substring(imageUrl1.lastIndexOf("/") + 1, imageUrl1.length());
			advertisementBean.setVideoName(imageNameback);
			advertisementBean.setVideoPath(imageUrlback);
			CalldaGlobalConfig.getInstance().setIvPathBack(imageUrlback);
			final AdvertisementUtil advertisementUtil = new AdvertisementUtil(
					context, new AdvertisementListener() {
						@Override
						public void onQueryCompleted(
								List<AdvertisementBean> adBeans) {
							boolean isDownload = false;
							if (adBeans != null && adBeans.size() > 0) {

								for (AdvertisementBean adBean : adBeans) {
									if (adBean.getVideoName().equals(imageNameback)) {
										advertisementBean.setVideoPath(adBean
												.getVideoPath());
										Logger.i(TAG, "video has downloaded");
										Logger.i(TAG, "video path:"+adBean.getVideoPath());
										isDownload = true;
										break;
									}
								}
							} else {
								Logger.i(TAG, "video has not downloaded");
								isDownload = false;
							}
							if (!isDownload) {
								new AdvertisementUtil(context, null).saveAD(advertisementBean);
							}
							Intent msgIntent = new Intent(MainCallActivity.KEYBOARD_MESSAGE_RECEIVED_ACTION);
							Logger.v(TAG, "发送已获取到图片广播");
							LocalBroadcastManager.getInstance(context).sendBroadcast(msgIntent);
						}
					});
			advertisementUtil.startQueryCallLog();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
