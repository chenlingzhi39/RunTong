package com.callba.phone.service;

import java.util.List;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.callba.phone.DemoHelper;
import com.callba.phone.logic.contact.ContactPersonEntity;
import com.callba.phone.logic.contact.QueryContactCallback;
import com.callba.phone.logic.contact.QueryContacts;
import com.callba.phone.logic.login.LoginController;
import com.callba.phone.manager.UserManager;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import okhttp3.Call;

/**
 * 程序主服务，处理后台任务
 *
 * @author Zhang
 */
public class MainService extends Service {
    private static final String TAG = MainService.class.getCanonicalName();

    /**
     * 存储任务集合
     */
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
     LocationReceiver receiver;
    //监听联系人数据的监听对象
    private ContentObserver mObserver = new ContentObserver(
            new Handler()) {

        @Override
        public void onChange(boolean selfChange) {
            // 当联系人表发生变化时进行相应的操作
                new QueryContacts(new QueryContactCallback() {
                    @Override
                    public void queryCompleted(List<ContactPersonEntity> contacts) {
                       sendBroadcast(new Intent("com.callba.contact"));
                    }
                }).loadContact(getApplicationContext());

        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("service", "oncreate");
        IntentFilter filter = new IntentFilter(
                "com.callba.location");
        receiver = new LocationReceiver();
        registerReceiver(receiver, filter);
        getContentResolver().registerContentObserver(
                ContactsContract.Contacts.CONTENT_URI, true, mObserver);
        new QueryContacts(new QueryContactCallback() {
            @Override
            public void queryCompleted(List<ContactPersonEntity> contacts) {
                sendBroadcast(new Intent("com.callba.contact"));
            }
        }).loadContact(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        getContentResolver().unregisterContentObserver(mObserver);
        unregisterReceiver(receiver);
        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient.stopLocation();
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
        //关闭通知
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        super.onDestroy();

//		PushAgent.getInstance(this).disable();
//		PushManager.getInstance().stopService(this.getApplicationContext());

        //服务退出时，清空activity栈
        ActivityUtil.finishAllActivity();

        //设置用户的登录状态
        LoginController.getInstance().setUserLoginState(false);
    }

    class LocationReceiver extends BroadcastReceiver implements AMapLocationListener {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("location", intent.getStringExtra("action"));
            if (intent.getStringExtra("action").equals("login")) {
                EMClient.getInstance().login(UserManager.getUsername(context) + "-callba", UserManager.getOriginalPassword(context), new EMCallBack() {//回调
                    @Override
                    public void onSuccess() {

                        EMClient.getInstance().groupManager().loadAllGroups();
                        EMClient.getInstance().chatManager().loadAllConversations();
                        Log.d("main", "登录聊天服务器成功！");
                        sendBroadcast(new Intent(("message_num")));
                        //DemoHelper.getInstance().getUserProfileManager().asyncGetCurrentUserInfo();
                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }

                    @Override
                    public void onError(int code, String message) {
                        Log.d("main", "登录聊天服务器失败！");
                    }
                });
                locationClient = new AMapLocationClient(context);
                locationOption = new AMapLocationClientOption();
                // 设置是否需要显示地址信息
                locationOption.setNeedAddress(true);
                /**
                 * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位
                 * 注意：只有在高精度模式下的单次定位有效，其他方式无效
                 */
                locationOption.setGpsFirst(false);
                // 设置定位模式为高精度模式
                locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
                locationOption.setInterval(600000);
                // 设置定位监听
                locationClient.setLocationListener(this);
                //locationOption.setOnceLocation(true);
                locationClient.setLocationOption(locationOption);
                locationClient.startLocation();
            } else {
			/*	EMClient.getInstance().logout(false, new EMCallBack() {

					@Override
					public void onSuccess() {
						// TODO Auto-generated method stub
						Log.d("main", "退出聊天服务器成功！");
					}

					@Override
					public void onProgress(int progress, String status) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onError(int code, String message) {
						// TODO Auto-generated method stub
						Log.d("main", "退出聊天服务器失败！");
					}
				});*/
                DemoHelper.getInstance().logout(false, new EMCallBack() {

                    @Override
                    public void onSuccess() {
                        // TODO Auto-generated method stub
                        Log.d("main", "退出聊天服务器成功！");
                    }

                    @Override
                    public void onProgress(int progress, String status) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onError(int code, String message) {
                        // TODO Auto-generated method stub
                        Log.d("main", "退出聊天服务器失败！");
                    }
                });
                if (null != locationClient) {
                    /**
                     * 如果AMapLocationClient是在当前Activity实例化的，
                     * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
                     */
                    locationClient.stopLocation();
                    locationClient.onDestroy();
                    locationClient = null;
                    locationOption = null;
                }
            }
        }

        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            StringBuilder sb = new StringBuilder();
            if (aMapLocation.getErrorCode() == 0) {
                Logger.i("address", aMapLocation.getAddress());
                Logger.i("latitude", aMapLocation.getLatitude() + "");
                Logger.i("longitude", aMapLocation.getLongitude() + "");
                UserManager.putAddress(MainService.this,aMapLocation.getAddress());
                UserManager.putLatitude(MainService.this,aMapLocation.getLatitude()+"");
                UserManager.putLongitude(MainService.this,aMapLocation.getLongitude()+"");
                //userDao.saveLocation(UserManager.getUsername(MainService.this), UserManager.getPassword(MainService.this), aMapLocation.getLatitude(), aMapLocation.getLongitude());
                OkHttpUtils.post().url(Interfaces.SAVE_LOCATION)
                        .addParams("loginName",UserManager.getUsername(MainService.this))
                        .addParams("loginPwd",UserManager.getPassword(MainService.this))
                        .addParams("latitude",aMapLocation.getLatitude()+"")
                        .addParams("longitude",aMapLocation.getLongitude()+"")
                        .build().execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {

                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.i("save_success",response);
                    }
                });
            } else {
                //定位失败
                sb.append("定位失败" + "\n");
                sb.append("错误码:" + aMapLocation.getErrorCode() + "\n");
                sb.append("错误信息:" + aMapLocation.getErrorInfo() + "\n");
                sb.append("错误描述:" + aMapLocation.getLocationDetail() + "\n");
                Logger.i("error", sb.toString());
            }
        }
    }

}
