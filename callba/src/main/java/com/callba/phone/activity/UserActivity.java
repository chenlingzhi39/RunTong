package com.callba.phone.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.MyApplication;
import com.callba.phone.activity.login.LoginActivity;
import com.callba.phone.activity.more.RetrievePasswordActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Task;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.cfg.Constant;
import com.callba.phone.logic.login.LoginController;
import com.callba.phone.service.MainService;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.AppVersionChecker;
import com.callba.phone.util.SharedPreferenceUtil;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Administrator on 2016/5/15.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.tab_user,
        toolbarTitle = R.string.center,
        menuId = R.menu.menu_user
)
public class UserActivity extends BaseActivity {
    @InjectView(R.id.user_head)
    CircleImageView userHead;
    @InjectView(R.id.number)
    TextView number;
    @InjectView(R.id.word)
    TextView word;
    @InjectView(R.id.account)
    RelativeLayout account;
    @InjectView(R.id.change_info)
    RelativeLayout changeInfo;
    @InjectView(R.id.change_password)
    RelativeLayout changePassword;
    @InjectView(R.id.retrieve)
    RelativeLayout retrieve;
    @InjectView(R.id.about)
    RelativeLayout about;
    @InjectView(R.id.help)
    RelativeLayout help;
    @InjectView(R.id.update)
    RelativeLayout update;
    @InjectView(R.id.logout)
    RelativeLayout logout;
    private SharedPreferenceUtil mSharedPreferenceUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        number.setText(CalldaGlobalConfig.getInstance().getUsername());
        if (!CalldaGlobalConfig.getInstance().getUserhead().equals(""))
            Glide.with(this).load(CalldaGlobalConfig.getInstance().getUserhead()).into(userHead);
        Log.i("head", CalldaGlobalConfig.getInstance().getUserhead());
        if(!CalldaGlobalConfig.getInstance().getNickname().equals(""))
        {
            word.setText(CalldaGlobalConfig.getInstance().getSignature());
        }
        mSharedPreferenceUtil = SharedPreferenceUtil.getInstance(this);
    }

    @Override
    protected void onResume() {
        if (!CalldaGlobalConfig.getInstance().getUserhead().equals(""))
            Glide.with(this).load(CalldaGlobalConfig.getInstance().getUserhead()).into(userHead);
        if(!CalldaGlobalConfig.getInstance().getSignature().equals(""))
        {
            word.setText(CalldaGlobalConfig.getInstance().getSignature());
        }
        if(!CalldaGlobalConfig.getInstance().getNickname().equals(""))
         number.setText(CalldaGlobalConfig.getInstance().getUsername());
        super.onResume();
    }



    @OnClick({R.id.account, R.id.change_info, R.id.change_password, R.id.retrieve, R.id.logout, R.id.about, R.id.help, R.id.update})
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.account:
                intent = new Intent(UserActivity.this, AccountActivity.class);
                startActivity(intent);
                break;
            case R.id.change_info:
                intent = new Intent(UserActivity.this, ChangeInfoActivity.class);
                startActivity(intent);
                break;
            case R.id.change_password:
                intent = new Intent(UserActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
                break;
            case R.id.retrieve:
                intent = new Intent(UserActivity.this, RetrievePasswordActivity.class);
                startActivity(intent);
                break;
            case R.id.logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("确认退出登录？");
                builder.setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                CalldaGlobalConfig.getInstance().setUsername("");
                                CalldaGlobalConfig.getInstance().setPassword("");
                                CalldaGlobalConfig.getInstance().setIvPath("");
                                if (CalldaGlobalConfig.getInstance().getAdvertisements1()!=null)
                                    CalldaGlobalConfig.getInstance().getAdvertisements1().clear();
                                if (CalldaGlobalConfig.getInstance().getAdvertisements2()!=null)
                                    CalldaGlobalConfig.getInstance().getAdvertisements2().clear();
                                if (CalldaGlobalConfig.getInstance().getAdvertisements3()!=null)
                                    CalldaGlobalConfig.getInstance().getAdvertisements3().clear();
                                LoginController.getInstance().setUserLoginState(false);
                                SharedPreferenceUtil.getInstance(UserActivity.this).putString(Constant.LOGIN_PASSWORD, "", true);
                                Intent intent0 = new Intent("com.callba.location");
                                intent0.putExtra("action", "logout");
                                sendBroadcast(intent0);
                                for (Activity activity : MyApplication.activities) {
                                    activity.finish();
                                }
                                startActivity(new Intent(UserActivity.this,LoginActivity.class));
                                    }
                        });
                builder.setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {

                              dialog.dismiss();


                            }
                        });

                AlertDialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(true);
                alertDialog.setCancelable(true);
                alertDialog.show();

                break;
            case R.id.about:
                intent = new Intent(UserActivity.this, AboutActivity.class);
                startActivity(intent);
                break;
            case R.id.help:
                intent = new Intent(UserActivity.this, HelpActivity.class);
                startActivity(intent);
                break;
            case R.id.update:
                Log.i("click", "update");
                sendGetVersionTask();
                break;
        }
    }

    /**
     * 发送获取版本信息任务
     *
     * @author zhw
     */
    private void sendGetVersionTask() {
        PackageManager pm = this.getPackageManager();
        String localVersion = "";
        try {
            PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);
            localVersion = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        ActivityUtil activityUtil = new ActivityUtil();
        Task task = new Task(Task.TASK_GET_VERSION);
        Map<String, Object> taskParams = new HashMap<String, Object>();
        taskParams.put("versionName", localVersion);
        taskParams.put("fromPage", "UserActivity");
        taskParams.put("lan", activityUtil.language(this));
        task.setTaskParams(taskParams);
        MainService.newTask(task);
    }

    @Override
    public void refresh(Object... params) {
        Log.i("user", "refresh");
        Message verionMessage = (Message) params[0];
        // 解析版本返回数据
        AppVersionChecker.AppVersionBean appVersionBean = AppVersionChecker.parseVersionInfo(
                this, verionMessage);


        // 检查是否成功获取加密Key
        checkLoginKey(appVersionBean);
    }

    /**
     * 检查是否成功获取加密的key
     *
     * @author zhw
     */
    private void checkLoginKey(AppVersionChecker.AppVersionBean appVersionBean) {
        // Logger.i(TAG, "getSecretKey() : " +
        // CalldaGlobalConfig.getInstance().getSecretKey());
        //Logger.i(TAG, "currentGetVersionTime : " + currentGetVersionTime);

        if (!TextUtils.isEmpty(appVersionBean.getSecretKey())) {
            // 成功获取key
            check2Upgrade(appVersionBean);
        } else {
            // 统计获取版本失败次数
            //MobclickAgent.onEvent(this, "version_timeout");
            String secretKey = mSharedPreferenceUtil
                    .getString(Constant.SECRET_KEY);
            CalldaGlobalConfig.getInstance().setSecretKey(secretKey);

            if (TextUtils.isEmpty(secretKey)) {
                // Toast.makeText(this, R.string.getversionfailed,
                // Toast.LENGTH_SHORT).show();
                // 提示用户获取失败
                //alertUserGetVersionFailed();
                toast(R.string.net_error_getdata_fail);
            } else {
                check2Upgrade(appVersionBean);
            }
        }
    }

    /**
     * 检查升级
     */
    private void check2Upgrade(final AppVersionChecker.AppVersionBean appVersionBean) {
        if (appVersionBean.isForceUpgrade()) {
            // 强制升级
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.sjts);
            builder.setMessage(R.string.sjtsxx);
            builder.setPositiveButton(R.string.upgrade,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                Uri uri = Uri.parse(appVersionBean
                                        .getDownloadUrl());
                                Intent intent = new Intent(Intent.ACTION_VIEW,
                                        uri);
                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                e.printStackTrace();

							/*	CalldaToast calldaToast = new CalldaToast();
                                calldaToast.showToast(getApplicationContext(),
										R.string.upgrade_openfailed);*/
                                toast(getString(R.string.upgrade_openfailed));
                            }
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

        } else {
            // 是否已提示过升级
          /*  boolean noticedUpgrade = mSharedPreferenceUtil.getBoolean(
                    Constant.IS_NOTICE_UPGRADE, false);
            if (noticedUpgrade) {
                // 只提示一次
                return;
            }*/

            if (appVersionBean.isHasNewVersion()) {
                // 存在新版本
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.sjts);
                builder.setMessage(R.string.upgrade_findnewversion);
                builder.setPositiveButton(R.string.upgrade,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                try {
                                    Uri uri = Uri.parse(appVersionBean
                                            .getDownloadUrl());
                                    Intent intent = new Intent(
                                            Intent.ACTION_VIEW, uri);
                                    startActivity(intent);
                                } catch (ActivityNotFoundException e) {
                                    e.printStackTrace();
                                    /*CalldaToast calldaToast = new CalldaToast();
									calldaToast.showToast(
											getApplicationContext(),
											R.string.upgrade_openfailed);*/
                                    toast(getString(R.string.upgrade_openfailed));
                                }

                                mSharedPreferenceUtil
                                        .putBoolean(Constant.IS_NOTICE_UPGRADE,
                                                false, true);
                            }
                        });
                builder.setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {

                                mSharedPreferenceUtil.putBoolean(
                                        Constant.IS_NOTICE_UPGRADE, true, true);


                            }
                        });

                AlertDialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(true);
                alertDialog.setCancelable(true);
                alertDialog.show();

            } else {
                // 无新版本
                toast(R.string.upgrade_no);
            }
        }
    }
    /**
     * 重写onkeyDown 捕捉返回键
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 转到后台运行
            ActivityUtil.moveAllActivityToBack();
            return true;
        }
        return false;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
               /* Intent intent = new Intent(MessageActivity.this, PostMessageActivity.class);
                startActivity(intent);*/
                Intent intent = new Intent(UserActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
