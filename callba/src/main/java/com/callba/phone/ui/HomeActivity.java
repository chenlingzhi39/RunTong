package com.callba.phone.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.MyApplication;
import com.callba.phone.bean.Campaign;
import com.callba.phone.ui.adapter.CampaignAdapter;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Advertisement;
import com.callba.phone.bean.ContactData;
import com.callba.phone.bean.SystemNumber;
import com.callba.phone.bean.UserDao;
import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.cfg.Constant;
import com.callba.phone.logic.login.LoginController;
import com.callba.phone.manager.ContactsManager;
import com.callba.phone.manager.UserManager;
import com.callba.phone.service.MainService;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.ContactsAccessPublic;
import com.callba.phone.util.DesUtil;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.util.SPUtils;
import com.callba.phone.util.SharedPreferenceUtil;
import com.callba.phone.view.BannerLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.dao.Mark;
import de.greenrobot.dao.MarkDao;
import okhttp3.Call;
import okhttp3.Request;


/**
 * Created by Administrator on 2016/5/14.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.tab_home,
        toolbarTitle = R.string.home

)
public class HomeActivity extends BaseActivity {
    @InjectView(R.id.mall)
    TextView mall;
    @InjectView(R.id.game)
    TextView game;
    @InjectView(R.id.sign_in)
    TextView signIn;
    @InjectView(R.id.banner)
    BannerLayout banner;
    private String yue;
    private ArrayList<Integer> localImages = new ArrayList<Integer>();
    private ArrayList<String> webImages = new ArrayList<>();
    private SharedPreferenceUtil mPreferenceUtil;
    private ProgressDialog progressDialog;
    private String username;
    private String password;
    private UserDao userDao2;
    private String date;
    private Gson gson;
    private String[] result;
    List<SystemNumber> list;
    private MarkDao markDao;
    private ArrayList<Campaign> campaigns;
    private CampaignAdapter campaignAdapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        markDao = MyApplication.getInstance().getDaoSession().getMarkDao();
        gson = new Gson();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");//获取当前时间
        date = formatter.format(new Date(System.currentTimeMillis()));
        mPreferenceUtil = SharedPreferenceUtil.getInstance(this);
        mPreferenceUtil.putBoolean(Constant.IS_FROMGUIDE, false, true);
        mPreferenceUtil.commit();
        userDao2 = new UserDao(this, new UserDao.PostListener() {
            @Override
            public void failure(String msg) {
                //toast(msg);
            }

            @Override
            public void start() {

            }

            @Override
            public void success(String msg) {
                final ArrayList<Advertisement> list;
                list = gson.fromJson(msg, new TypeToken<ArrayList<Advertisement>>() {
                }.getType());
                GlobalConfig.getInstance().setAdvertisements1(list);
                webImages.clear();
                for (Advertisement advertisement : list) {
                    webImages.add(advertisement.getImage());
                }
                banner.setViewUrls(webImages);
                banner.setOnBannerItemClickListener(new BannerLayout.OnBannerItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        Intent intent1 = new Intent(Intent.ACTION_VIEW);
                        intent1.setData(Uri.parse(list.get(position).getAdurl()));
                        startActivity(intent1);
                    }
                });
            }
        });
        localImages.add(R.drawable.ad4);
        localImages.add(R.drawable.ad5);
        localImages.add(R.drawable.ad6);
        banner.setViewRes(localImages);
        // 判断是否自动启动
        if (savedInstanceState == null
                && (boolean) SPUtils.get(this, Constant.PACKAGE_NAME, Constant.Auto_Login, false)
                && !LoginController.getInstance().getUserLoginState()) {
            Log.i("MainCallActivity", "auto");
            Logger.i("MainCallActivity", "MainCallActivity  oncreate autoLogin");
            // 登录
            autoLogin();
            return;
        }/* else {

            // 检查内存数据是否正常
            String username = GlobalConfig.getInstance().getUsername();
            String password = GlobalConfig.getInstance().getPassword();
            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                // 重新打开
                gotoWelcomePage();
            }
            sendBroadcast(new Intent("com.callba.login"));
            Logger.i("date",date);
            Logger.i("save_date",mPreferenceUtil.getString(GlobalConfig.getInstance().getUsername()));
            if (!mPreferenceUtil.getString(GlobalConfig.getInstance().getUsername()).equals(date)) {
                String year = Calendar.getInstance().get(Calendar.YEAR) + "";
                String month = Calendar.getInstance().get(Calendar.MONTH) + 1 + "";
                if (month.length() == 1)
                    month = "0" + month;
                userDao.getMarks(GlobalConfig.getInstance().getUsername(), GlobalConfig.getInstance().getPassword(), year + month);
            }
            userDao1.getSystemPhoneNumber(GlobalConfig.getInstance().getUsername(), GlobalConfig.getInstance().getPassword(), ContactsAccessPublic.hasName(HomeActivity.this, "Call吧电话"));
            userDao2.getAd(1, GlobalConfig.getInstance().getUsername(), GlobalConfig.getInstance().getPassword());
        }*/
        //userDao1.getSystemPhoneNumber(getUsername(), getPassword(), ContactsAccessPublic.hasName(HomeActivity.this, "Call吧电话"));
        getSystemPhoneNumber(ContactsAccessPublic.hasName(HomeActivity.this, "Call吧电话"));
        userDao2.getAd(1, getUsername(), getPassword());
        if ((boolean) SPUtils.get(HomeActivity.this, "settings", "sign_key", true))
            signIn();
     /*   if (!mPreferenceUtil.getString(getUsername()).equals(date) && (boolean) SPUtils.get(HomeActivity.this, "settings", "sign_key", false)) {
            String year = Calendar.getInstance().get(Calendar.YEAR) + "";
            String month = Calendar.getInstance().get(Calendar.MONTH) + 1 + "";
            if (month.length() == 1)
                month = "0" + month;
            //userDao.getMarks(getUsername(), getPassword(), year + month);
            getMarks(year+month);
        }*/
        if((boolean)SPUtils.get(HomeActivity.this,Constant.PACKAGE_NAME,getDate(),true))
        getActivity();
        if (GlobalConfig.getInstance().getAppVersionBean() != null) {
            if (GlobalConfig.getInstance().getAppVersionBean().isForceUpgrade() || (boolean) SPUtils.get(this, "settings", "update_key", true))
                check2Upgrade(GlobalConfig.getInstance().getAppVersionBean(), false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (GlobalConfig.getInstance().getAdvertisements1() != null)
            if (GlobalConfig.getInstance().getAdvertisements1().size() == 0)
                userDao2.getAd(1, getUsername(), getPassword());
    }

    // 跳转到起始页
    private void gotoWelcomePage() {
        Intent intent = new Intent();
        intent.setClass(HomeActivity.this, WelcomeActivity.class);
        startActivity(intent);

        // 关闭主tab页面
        ActivityUtil.finishMainTabPages();
    }


    @OnClick({R.id.recharge, R.id.discount, R.id.sale, R.id.mall, R.id.flow, R.id.family, R.id.game, R.id.sign_in})
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.recharge:
                intent = new Intent(HomeActivity.this, RechargeActivity.class);
                startActivity(intent);
                break;
            case R.id.discount:
                Log.i("home", "search_yue");
                //queryUserBalance();
              /*  intent=new Intent(HomeActivity.this,AccountActivity.class);
                startActivity(intent);*/
                startActivity(new Intent(HomeActivity.this, CouponActivity.class));
                break;
            case R.id.sale:
                toast("暂未开放");
                break;
            case R.id.mall:
                toast("暂未开放");
                break;
            case R.id.flow:
                startActivity(new Intent(HomeActivity.this, FlowActivity.class));
                //toast("暂未开放");
                break;
            case R.id.family:
                startActivity(new Intent(HomeActivity.this, FamilyActivity.class));
                //toast("暂未开放");
                /*intent = new Intent(HomeActivity.this, CommunityActivity.class);
                startActivity(intent);*/
                break;
            case R.id.game:
                toast("暂未开放");
                break;
            case R.id.sign_in:
                intent = new Intent(HomeActivity.this, SignInActivity.class);
                startActivity(intent);
                break;
        }
    }


    public class YueDialogHelper implements DialogInterface.OnDismissListener {
        @InjectView(R.id.recharge)
        Button recharge;
        @InjectView(R.id.exchange)
        Button exchange;
        @InjectView(R.id.number)
        TextView number;
        @InjectView(R.id.yue)
        TextView tv_yue;
        @InjectView(R.id.gold)
        TextView gold;
        private View mView;
        private Dialog mDialog;

        public YueDialogHelper() {
            mView = getLayoutInflater().inflate(R.layout.dialog_yue, null);
            ButterKnife.inject(this, mView);
            number.setText(number.getText().toString() + "   " + getUsername());
            if (!yue.equals(""))
                tv_yue.setText(tv_yue.getText().toString() + "   " + yue);
            gold.setText(gold.getText().toString() + "   " + UserManager.getGold(HomeActivity.this) + "");
        }

        @OnClick(R.id.recharge)
        public void recharge() {
            Intent intent = new Intent(HomeActivity.this, RechargeActivity.class);
            startActivity(intent);
            mDialog.dismiss();
        }

        @OnClick(R.id.exchange)
        public void exchange() {
            mDialog.dismiss();
        }

        public View getView() {
            return mView;
        }

        public void setDialog(Dialog mDialog) {
            this.mDialog = mDialog;
        }

        @Override
        public void onDismiss(DialogInterface dialogInterface) {
            mDialog = null;
        }
    }

    private void showYueDialog() {
        YueDialogHelper helper = new YueDialogHelper();
        Dialog dialog = new AlertDialog.Builder(this, R.style.MyDialogStyle)
                .setView(helper.getView())
                .create();
        helper.setDialog(dialog);
        dialog.show();
    }

    /**
     * 自动登录
     */
    private void autoLogin() {
        username = mPreferenceUtil.getString(Constant.LOGIN_USERNAME);
        password = mPreferenceUtil.getString(Constant.LOGIN_PASSWORD);

        if ("".equals(UserManager.getSecretKey(this))) {
            Log.i("home", "nosecret");
            // 跳转到起始页
            gotoWelcomePage();
            finish();
            return;

        } else if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {

            // 保存的数据被清空，跳转到手动登录界面
            switchManualLogin();
            return;
        }

        // 加密，生成loginSign
        String source = username + "," + password;
        String sign = null;
        try {
            sign = DesUtil.encrypt(source, UserManager
                    .getSecretKey(this));
        } catch (Exception e) {
            e.printStackTrace();

            // 手动登录
            switchManualLogin();
            return;
        }
        OkHttpUtils.post().url(Interfaces.Login)
                .addParams("loginSign", sign)
                .addParams("loginType", "1")
                .addParams("softType", "android")
                .addParams("callType", "all")
                .build().execute(new StringCallback() {
            @Override
            public void onAfter(int id) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onBefore(Request request, int id) {
                progressDialog = ProgressDialog.show(HomeActivity.this, null,
                        getString(R.string.logining));
            }

            @Override
            public void onError(Call call, Exception e, int id) {
                if (e instanceof UnknownHostException) {
                    toast(R.string.conn_failed);
                } else {
                    toast(R.string.network_error);
                }
                switchManualLogin();
            }

            @Override
            public void onResponse(String response, int id) {
                try {
                    Logger.i("login_result", response);
                    String[] resultInfo = response.split("\\|");
                    if (resultInfo[0].equals("0")) { //处理登录成功返回信息
                        LoginController.parseLoginSuccessResult(HomeActivity.this, username, password, resultInfo);
                        LoginController.getInstance().setUserLoginState(true);
                       /* if (!mPreferenceUtil.getString(getUsername()).equals(date) && (boolean) SPUtils.get(HomeActivity.this, "settings", "sign_key", false)) {
                            String year = Calendar.getInstance().get(Calendar.YEAR) + "";
                            String month = Calendar.getInstance().get(Calendar.MONTH) + 1 + "";
                            if (month.length() == 1)
                                month = "0" + month;
                            //userDao.getMarks(getUsername(), getPassword(), year + month);
                            getMarks(year+month);
                        }*/
                        if ((boolean) SPUtils.get(HomeActivity.this, "settings", "sign_key", true))
                            signIn();
                        getSystemPhoneNumber(ContactsAccessPublic.hasName(HomeActivity.this, "Call吧电话"));
                        if((boolean)SPUtils.get(HomeActivity.this,Constant.PACKAGE_NAME,getDate(),true))
                        getActivity();
                        userDao2.getAd(1, getUsername(), getPassword());
                        if (GlobalConfig.getInstance().getAppVersionBean() != null) {
                            if (GlobalConfig.getInstance().getAppVersionBean().isForceUpgrade() || (boolean) SPUtils.get(HomeActivity.this, "settings", "update_key", true))
                                check2Upgrade(GlobalConfig.getInstance().getAppVersionBean(), false);
                        }
                        MobclickAgent.onProfileSignIn(getUsername());
                    } else {
                        toast(resultInfo[1]);
                        switchManualLogin();
                    }
                } catch (Exception e) {
                    toast(R.string.getserverdata_exception);
                    switchManualLogin();
                }
            }
        });
    }

    /**
     * 跳转到手动登陆界面
     */
    private void switchManualLogin() {
        Intent intent = new Intent();
        intent.setClass(HomeActivity.this, LoginActivity.class);
        startActivity(intent);

        // 关闭主tab页面
        ActivityUtil.finishMainTabPages();
    }


    @Override
    protected void onDestroy() {
        Logger.i("home", "destroy");
        super.onDestroy();
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

    public void getMarks(String time) {
        OkHttpUtils.post().url(Interfaces.GET_MARKS)
                .addParams("loginName", getUsername())
                .addParams("loginPwd", getPassword())
                .addParams("date", time)
                .build().execute(new StringCallback() {

            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(String response, int id) {
                try {
                    String[] result = response.split("\\|");
                    if (result[0].equals("0")) {
                        String[] dates = result[1].split(",");
                        if (date.equals(dates[dates.length - 1])) {
                            mPreferenceUtil.putString(getUsername(), date, true);

                        } else {
                            mPreferenceUtil.putString(getUsername(), dates[dates.length - 1], true);
                            Intent intent = new Intent(HomeActivity.this, SignInActivity.class);
                            startActivity(intent);
                        }
                    } else {
                        //toast(result[1]);
                        if (result[1].equals("没有签到记录")) {
                            Intent intent = new Intent(HomeActivity.this, SignInActivity.class);
                            startActivity(intent);
                        }
                    }
                } catch (Exception e) {
                    toast(R.string.getserverdata_exception);
                }
            }
        });
    }

    public void getSystemPhoneNumber(String count) {
        Logger.i("phoneNumberCount", count);
        OkHttpUtils.post().url(Interfaces.GET_SYSTEM_PHONE_NUMBER)
                .addParams("loginName", getUsername())
                .addParams("loginPwd", getPassword())
                .addParams("phoneNumberCount", count)
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(String response, int id) {
                try {
                    Log.i("system_result", response);
                    result = response.split("\\|");
                    if (result[0].equals("0")) {
                        list = new ArrayList<>();
                        try {
                            list = gson.fromJson(result[1], new TypeToken<ArrayList<SystemNumber>>() {
                            }.getType());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        final ArrayList<String> numbers = new ArrayList<>();
                        final ContactData contactData = new ContactData();
                        contactData.setContactName("Call吧电话");
                        for (SystemNumber user : list) {
                            numbers.add(user.getPhoneNumber());
                            Logger.i("phonenumber", user.getPhoneNumber());
                        }
                        MainService.system_contact = true;
                        if (ContactsAccessPublic.hasName(HomeActivity.this, "Call吧电话").equals("0"))
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    ContactsAccessPublic.insertPhoneContact(HomeActivity.this, contactData, numbers);
                                }
                            }).start();
                        else {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    ContactsAccessPublic.deleteContact(HomeActivity.this, new ContactsManager(getContentResolver()).getContactID("Call吧电话"));
                                    ContactsAccessPublic.insertPhoneContact(HomeActivity.this, contactData, numbers);
                                }
                            }).start();
                        }
                    } else {

                    }
                } catch (Exception e) {

                }
            }
        });
    }

    public void signIn() {
        OkHttpUtils.post().url(Interfaces.Sign)
                .addParams("loginName", getUsername())
                .addParams("loginPwd", getPassword())
                .addParams("softType", "android")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        try {
                            result = response.split("\\|");
                            Log.i("get_sign", response);
                            if (result[0].equals("0")) {
                                toast(result[1]);
                                Mark mark = new Mark();
                                mark.setUsername(getUsername());
                                mark.setMonth(Calendar.getInstance().get(Calendar.MONTH) + 1);
                                try {
                                    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                                    mark.setDate(formatter.parse(date));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                markDao.insert(mark);
                                UserManager.putGold(HomeActivity.this, UserManager.getGold(HomeActivity.this) + 3);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public void getActivity() {
        OkHttpUtils.post().url(Interfaces.ACTIVITY_INFO)
                .addParams("loginName", getUsername())
                .addParams("loginPwd", getPassword())
                .build().execute(new StringCallback() {

            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(String response, int id) {
                try {
                    String[] result = response.split("\\|");
                    if (result[0].equals("0")) {
                      campaigns=gson.fromJson(result[1],new TypeToken<ArrayList<Campaign>>() {
                      }.getType());
                         showDialog(campaigns);
                        SPUtils.put(HomeActivity.this,Constant.PACKAGE_NAME,getDate(),false);
                    }
                } catch (Exception e) {
                }
            }
        });
    }

    public void showActivityDialog(String activity) {
        // 存在新版本
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("活动信息");
        builder.setMessage(activity);
        builder.setPositiveButton(R.string.know,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        dialog.dismiss();
                    }
                });
        android.app.AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.setCancelable(true);
        alertDialog.show();

    }
    public class DialogHelper implements DialogInterface.OnDismissListener {
        private AlertDialog mDialog;
        private View mView;
        private RecyclerView mealList;

        public DialogHelper(ArrayList<Campaign> campaigns) {
            mView = getLayoutInflater().inflate(R.layout.dialog_list, null);
            mealList = (RecyclerView) mView.findViewById(R.id.list);
            campaignAdapter = new CampaignAdapter(HomeActivity.this);
            campaignAdapter.addAll(campaigns);
            mealList.setAdapter(campaignAdapter);
        }


        @Override
        public void onDismiss(DialogInterface dialogInterface) {
            mDialog = null;
        }

        public void setDialog(AlertDialog mDialog) {
            this.mDialog = mDialog;
        }

        public View getView() {
            return mView;
        }
    }

    public void showDialog(ArrayList<Campaign> campaigns) {
        final DialogHelper helper = new DialogHelper(campaigns);
        dialog = new AlertDialog.Builder(this)
                .setView(helper.getView()).setTitle("活动信息")
                .setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        helper.setDialog(dialog);
        dialog.show();
    }
    public String getDate(){
        Date date=new Date(System.currentTimeMillis());
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }
}

