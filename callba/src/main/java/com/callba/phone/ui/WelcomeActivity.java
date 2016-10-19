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
        // 设置用户的登录状态
        LoginController.getInstance().setUserLoginState(false);
        startService(new Intent(WelcomeActivity.this, MainService.class));
        // 启动服务
        asyncInitLoginEnvironment();
                initEnvironment();
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
        gotoActivity();
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
            if (TextUtils.isEmpty(getUsername())) {
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

}
