package com.callba.phone.ui;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.Toast;

import com.callba.R;
import com.callba.phone.MyApplication;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.cfg.Constant;
import com.callba.phone.logic.login.LoginController;
import com.callba.phone.manager.UserManager;
import com.callba.phone.service.MainService;
import com.callba.phone.util.AppVersionChecker;
import com.callba.phone.util.AppVersionChecker.AppVersionBean;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.util.SPUtils;
import com.callba.phone.util.SimpleHandler;
import com.callba.phone.util.ZipUtil;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionNo;
import com.yanzhenjie.permission.PermissionYes;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import okhttp3.Call;


public class WelcomeActivity extends BaseActivity {
    public static final String TAG = "WelcomeActivity";
    private boolean isNetworkAvail = false; // 当前是否有可用网络
    // 记录当前获取key的次数
    private int currentGetVersionTime = 0;
    ProgressDialog progressDialog;
    // private PushAgent mPushAgent;
    private RationaleListener rationaleListener = new RationaleListener() {
        @Override
        public void showRequestPermissionRationale(int requestCode, final Rationale rationale) {
            new android.support.v7.app.AlertDialog.Builder(WelcomeActivity.this)
                    .setTitle("温馨提醒")
                    .setMessage("您已拒绝过某项权限，没有此权限将导致应用无法正常工作，是否重新请求权限？")
                    .setPositiveButton("好", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            rationale.resume();
                        }
                    })
                    .setNegativeButton("我拒绝", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            rationale.cancel();
                        }
                    }).show();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndPermission.with(this)
                .requestCode(100)
                .permission(Manifest.permission.READ_CONTACTS
                ,Manifest.permission.ACCESS_FINE_LOCATION
                ,Manifest.permission.READ_PHONE_STATE
                ,Manifest.permission.READ_SMS
                ,Manifest.permission.READ_EXTERNAL_STORAGE
                ,Manifest.permission.CAMERA
                ,Manifest.permission.RECORD_AUDIO)
                .rationale(rationaleListener)
                .send();

    }

    /**
     * 启动服务
     */
    private void asyncInitLoginEnvironment() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                startZip();
            }
        }).start();
    }

    /**
     * 开始复制
     */
    private void startZip() {
        try {
            Logger.i(TAG, "start zip");
            if (!new File((String)SPUtils.get(this,Constant.PACKAGE_NAME,Constant.DB_PATH_,"")).exists())
                ZipUtil.copyBigDataBase(WelcomeActivity.this);
            else
                Constant.DB_PATH =(String)SPUtils.get(this,Constant.PACKAGE_NAME,Constant.DB_PATH_,"");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void init() {
        //insertDummyContactWrapper();
        // 设置用户的登录状态

        LoginController.getInstance().setUserLoginState(false);
        startService(new Intent(WelcomeActivity.this, MainService.class));
        // 启动服务
        asyncInitLoginEnvironment();
                initEnvironment();

		/*rootView=(LinearLayout) findViewById(R.id.root);
		AlphaAnimation alphaAnimation=new AlphaAnimation(0.0f,1.0f);
		alphaAnimation.setDuration(2000);
		alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {


			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});
		rootView.startAnimation(alphaAnimation);*/

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // 只需要调用这一句，剩下的AndPermission自动完成。
        AndPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @PermissionYes(100)
    private void getLocationYes() {
        init();
    }

    @PermissionNo(100)
    private void getLocationNo() {
        finish();
    }
    private void initEnvironment() {
        String s = getResources().getConfiguration().locale.getCountry();
        Logger.v("语言环境", s);
        Locale.setDefault(new Locale("zh"));
        isNetworkAvail = MyApplication.getInstance().detect();
        alertNetWork(isNetworkAvail);
        if (isNetworkAvail) {
            currentGetVersionTime = 0;
            // 获取版本信息
            sendGetVersionTask();
        }

    }

    /**
     * 发送获取版本信息任务
     *
     * @author zhw
     */
    private void sendGetVersionTask() {
        currentGetVersionTime+=1;
        Logger.i("retry_time",System.currentTimeMillis()+"");
        OkHttpUtils.post().url(Interfaces.Version)
                .tag(this)
                .addParams("softType", "android")
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                e.printStackTrace();
                AppVersionBean appVersionBean = new AppVersionBean();
                checkLoginKey(appVersionBean);
            }
            @Override
            public void onResponse(String response, int id) {
                AppVersionBean appVersionBean = AppVersionChecker.parseVersionInfo(WelcomeActivity.this, response);
                GlobalConfig.getInstance().setAppVersionBean(appVersionBean);
                checkLoginKey(appVersionBean);
            }
        });
    }

    /**
     * 检查是否成功获取加密的key
     *
     * @author zhw
     */
    private void checkLoginKey(AppVersionBean appVersionBean) {
        // Logger.i(TAG, "getSecretKey() : " +
        // GlobalConfig.getInstance().getSecretKey());
        Logger.i(TAG, "currentGetVersionTime : " + currentGetVersionTime);

        if (!TextUtils.isEmpty(appVersionBean.getSecretKey())) {
            UserManager.putSecretKey(WelcomeActivity.this, appVersionBean.getSecretKey());
            // 成功获取key
            //check2Upgrade(appVersionBean);
            gotoActivity();
        } else if (currentGetVersionTime <= Constant.GETVERSION_RETRY_TIMES) {
            //OkHttpUtils.getInstance().cancelTag(this);
			// 再次发送获取任务
                    sendGetVersionTask();

		} else {
            // 统计获取版本失败次数
            //MobclickAgent.onEvent(this, "version_timeout");
            String secretKey = UserManager.getSecretKey(this);
            if (TextUtils.isEmpty(secretKey)) {
                // Toast.makeText(this, R.string.getversionfailed,
                // Toast.LENGTH_SHORT).show();
                // 提示用户获取失败
                alertUserGetVersionFailed();
            } else {
                //check2Upgrade(appVersionBean);
                        gotoActivity();
            }
        }
    }

    /**
     * 提示用户获取版本失败
     *
     * @author zhw
     */
    private void alertUserGetVersionFailed() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_title);
        builder.setMessage(R.string.net_error_getdata_fail);
        builder.setPositiveButton(R.string.retry,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendGetVersionTask();
                        dialog.dismiss();
                    }
                });
        builder.setNegativeButton(R.string.exit,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    /**
     * 页面跳转
     *
     * @param getVersion 获取版本是否成功
     */
    private void gotoActivity() {
        // 判断是不是第一次使用
        boolean isFirstStart =(boolean)SPUtils.get(this,Constant.PACKAGE_NAME,Constant.ISFRISTSTART,true);

        if (isFirstStart) {
            // 第一次启动，跳转到新功能介绍页面
            Intent intent = new Intent(WelcomeActivity.this,
                    TutorialActivity.class);
            WelcomeActivity.this.startActivity(intent);
            SPUtils.put(this,Constant.PACKAGE_NAME,Constant.ISFRISTSTART, false);
        } else {
            String username = getUsername();
            if (TextUtils.isEmpty(username)) {
                Intent intent = new Intent(WelcomeActivity.this,
                        GuideActivity.class);
                startActivity(intent);
                finish();
                return;
            }
            if (TextUtils.isEmpty(getPassword())) {
                Intent intent = new Intent(WelcomeActivity.this,
                        LoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
            // 自动登陆
            Intent intent = new Intent(WelcomeActivity.this,
                    MainTabActivity.class);
            //intent.putExtra("frompage", "WelcomeActivity");
            startActivity(intent);
            //OkHttpUtils.getInstance().cancelTag(this);
        } /*else {
            // 手动登陆
            Intent intent = new Intent(WelcomeActivity.this,
                    LoginActivity.class);
            WelcomeActivity.this.startActivity(intent);
        }*/
        finish();
    }

    /**
     * 判断网络连接
     */
    private void alertNetWork(boolean isAvail) {
        if (!isAvail) {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.networksetup)
                    .setMessage(R.string.networksetupinfo)
                    .setPositiveButton(R.string.setup,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    try {
                                        startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                                        dialog.dismiss();
                                    } catch (Exception e) {
                                        // TODO: handle exception
                                    }
                                }
                            })
                    .setNegativeButton(R.string.exit,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    WelcomeActivity.this.finish();
                                    // gotoActivity();
                                }
                            }).create();

            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setCancelable(false);
            try {
                alertDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(WelcomeActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ActivityCompat.checkSelfPermission(WelcomeActivity.this,permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(WelcomeActivity.this,permission))
                return false;
        }
        return true;
    }
 /*   @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 124:
            {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_CONTACTS, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_CONTACTS, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted

                } else {
                    // Permission Denied
                   toast( "Some Permission is Denied");
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED && requestCode == 0) {

            SimpleHandler.getInstance().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isNetworkAvail = MyApplication.getInstance().detect();
                    alertNetWork(isNetworkAvail);
                    if (isNetworkAvail) {
                        currentGetVersionTime = 0;
                        // 获取版本信息
                        sendGetVersionTask();
                    }
                }
            },2000);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
