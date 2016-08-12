package com.callba.phone.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;





import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat.Builder;

import android.widget.RemoteViews;
import android.widget.Toast;

import com.callba.R;
import com.callba.phone.util.Logger;

/***
 * 更新版本
 * 
 * @author zhangjia
 * 
 */
public class UpdateService extends Service {
	private static final int TIMEOUT = 10 * 1000;// 超时
	private  String down_url = "";
	private static final int DOWN_OK = 1;
	private static final int DOWN_ERROR = 0;
    public static boolean is_downloading=false;
	private String app_name;
    private String version_code;
	private NotificationManager notificationManager;

     private Builder builder;
	private Intent updateIntent;
	private PendingIntent pendingIntent;
	private Notification notification;
	private int notification_id = 0;
	public  File updateDir = null;
	public  File updateFile = null;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        down_url=intent.getStringExtra("url");
		version_code=intent.getStringExtra("version_code");
		app_name=getResources().getString(
				R.string.app_name);
		// 创建文件
		createFile(app_name+version_code);
		createNotification();
		createThread();
		return super.onStartCommand(intent, flags, startId);

	}

	
	@Override
	public void onCreate() {
		// TODO 自动生成的方法存根

		super.onCreate();
		
	}


	/***
	 * 开线程下载
	 */
	public void createThread() {
		/***
		 * 更新UI
		 */
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case DOWN_OK:
					// 下载完成，点击安装
					System.out.println("success");
					Uri uri = Uri.fromFile(updateFile);
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(uri,
							"application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
					pendingIntent = PendingIntent.getActivity(
							UpdateService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
					/*notification = new Notification(android.R.drawable.stat_sys_download_done, "下载成功", System.currentTimeMillis());
					notification.flags = Notification.FLAG_AUTO_CANCEL;
					notification.setLatestEventInfo(UpdateService.this,
							app_name, "下载成功，点击安装", pendingIntent);
*/
					builder=new Builder(UpdateService.this);
					builder.setSmallIcon(android.R.drawable.stat_sys_download_done);
					builder.setDefaults(0);
					builder.setAutoCancel(true);
					builder.setContentTitle(app_name);
					builder.setContentText("下载成功");
					builder.setTicker("下载成功");
					builder.setContentIntent(pendingIntent);
					notificationManager.notify(notification_id,builder.build() );
                    is_downloading=false;
					startActivity(intent);
					stopSelf();
					break;
				case DOWN_ERROR:
					/*notification = new Notification(android.R.drawable.stat_notify_error, "下载失败", System.currentTimeMillis());
					notification.flags = Notification.FLAG_AUTO_CANCEL;
					notification.setLatestEventInfo(UpdateService.this,
							app_name, "下载失败", pendingIntent);*/
					builder=new Builder(UpdateService.this);
					builder.setSmallIcon(android.R.drawable.stat_notify_error);
					builder.setDefaults(0);
					builder.setAutoCancel(true);
					builder.setContentTitle(app_name);
					builder.setContentText("下载失败");
					builder.setTicker("下载失败");
					notificationManager.notify(notification_id,builder.build() );
					is_downloading=false;
					stopSelf();
					break;
				default:
					//stopSelf();
					break;
				}

			}

		};

		final Message message = new Message();

		new Thread(new Runnable() {
			@Override
			public void run() {

				try {
					long downloadSize = downloadUpdateFile(down_url,
							updateFile.toString());
					if (downloadSize > 0) {
						// 下载成功
						message.what = DOWN_OK;
						handler.sendMessage(message);
					}

				} catch (Exception e) {
					e.printStackTrace();
					message.what = DOWN_ERROR;
					handler.sendMessage(message);
				}

			}
		}).start();
	}

	/***
	 * 创建通知栏
	 */
	RemoteViews contentView;

	public void createNotification() {
		
	
		//notification = new Notification();
		
		// notification.icon = R.drawable.ic_launcher;
		// // 这个参数是通知提示闪出来的值.
		// notification.tickerText = "开始下载";
		//
		// updateIntent = new Intent(this, MainActivity.class);
		// pendingIntent = PendingIntent.getActivity(this, 0, updateIntent, 0);
		//
		// // 这里面的参数是通知栏view显示的内容
		// notification.setLatestEventInfo(this, app_name, "下载：0%",
		// pendingIntent);
		//
		// notificationManager.notify(notification_id, notification);

		/***
		 * 在这里我们用自定的view来显示Notification
		 */
		int icon = android.R.drawable.stat_sys_download;
		CharSequence tickerText = "开始下载";
		long when = System.currentTimeMillis();
		 builder=new Builder(this);
		builder.setSmallIcon(icon);
		builder.setDefaults(0);
		builder.setAutoCancel(false);
		builder.setContentTitle("正在下载");
		builder.setContentText("0%");
		builder.setTicker("开始下载");
		builder.setProgress(100, 0, false);
	    builder.setPriority(Integer.MAX_VALUE);
		//notification = new Notification(icon, tickerText, when);
		//notification.flags = Notification.FLAG_ONGOING_EVENT;
		/*contentView = new RemoteViews(getPackageName(),
				R.layout.notification_item);
		contentView.setTextViewText(R.id.notificationTitle, "正在下载");
		contentView.setTextViewText(R.id.notificationPercent, "0%");
		contentView.setProgressBar(R.id.notificationProgress, 100, 0, false);*/

		//notification.contentView = contentView;
		notification = builder.build();
		notification.flags=Notification.FLAG_ONGOING_EVENT;
		notificationManager.notify(notification_id, notification);
	
        is_downloading=true;
		

		/*int icon = android.R.drawable.stat_sys_upload;
		CharSequence tickerText = "开始上传";
		long when = System.currentTimeMillis();
		notification = new Notification(icon, tickerText, when);

		// 放置在"正在运行"栏目中
		notification.flags = Notification.FLAG_ONGOING_EVENT;

		RemoteViews contentView = new RemoteViews(getPackageName(),
				R.layout.notification_item);

		
		// 指定个性化视图
		notification.contentView = contentView;
		 
		Intent intnt = new Intent(this, MainActivity.class);
		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intnt, PendingIntent.FLAG_UPDATE_CURRENT);
		// 指定内容意图
		notification.contentIntent = contentIntent;

		notificationManager.notify(0,notification);*/
	
	
	}

	/***
	 * 下载文件
	 * 
	 * @return
	 * @throws MalformedURLException
	 */
	public long downloadUpdateFile(String down_url, String file)
			throws Exception {
		int down_step = 1;// 提示step
		int totalSize;// 文件总大小
		int downloadCount = 0;// 已经下载好的大小
		int updateCount = 0;// 已经上传的文件大小
		InputStream inputStream;
		OutputStream outputStream;

		URL url = new URL(down_url);
		HttpURLConnection httpURLConnection = (HttpURLConnection) url
				.openConnection();
		httpURLConnection.setConnectTimeout(TIMEOUT);
		httpURLConnection.setReadTimeout(TIMEOUT);
		// 获取下载文件的size
		totalSize = httpURLConnection.getContentLength();
		if (httpURLConnection.getResponseCode() == 404) {
			builder=new Builder(this);
			builder.setSmallIcon(android.R.drawable.stat_notify_error);
			builder.setDefaults(0);
			builder.setAutoCancel(true);
			builder.setContentTitle(app_name);
			builder.setContentText("下载失败");
			builder.setTicker("下载失败");
			notificationManager.notify(notification_id,builder.build() );
			/*notification = new Notification(android.R.drawable.stat_notify_error, "下载失败", System.currentTimeMillis());
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			notification.setLatestEventInfo(UpdateService.this,
					app_name, "下载失败", pendingIntent);*/
			is_downloading=false;
			stopSelf();
			throw new Exception("fail!");
		}
		inputStream = httpURLConnection.getInputStream();
		outputStream = new FileOutputStream(file, false);// 文件存在则覆盖掉
		byte buffer[] = new byte[1024];
		int readsize = 0;
		while ((readsize = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, readsize);
			downloadCount += readsize;// 时时获取下载到的大小
			/**
			 * 每次增张5%
			 */
			if (updateCount == 0
					|| (downloadCount * 100 / totalSize - down_step) >= updateCount) {
				updateCount += down_step;
				// 改变通知栏
				// notification.setLatestEventInfo(this, "正在下载...", updateCount
				// + "%" + "", pendingIntent);
				/*contentView.setTextViewText(R.id.notificationPercent,
						updateCount + "%");
				contentView.setProgressBar(R.id.notificationProgress, 100,
						updateCount, false);*/
				// show_view
				builder.setContentText(updateCount+"%");
				builder.setProgress(100, updateCount, false);
				notification=builder.build();
				notification.flags=Notification.FLAG_ONGOING_EVENT;
				notificationManager.notify(notification_id,notification );

			}

		}
		if (httpURLConnection != null) {
			httpURLConnection.disconnect();
		}
		inputStream.close();
		outputStream.close();

		return downloadCount;

	}
	public  void createFile(String name) {
		if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment
				.getExternalStorageState())) {
			updateDir = new File(Environment.getExternalStorageDirectory()
					+ "/" + "Download");
			updateFile = new File(updateDir.getPath() + File.separator + name + ".apk");

			if (!updateDir.exists()) {
				updateDir.mkdirs();
			}
			if (!updateFile.exists()) {
				try {
					updateFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}
}
