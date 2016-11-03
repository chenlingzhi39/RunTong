package com.callba.phone.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.callba.R;
import com.callba.phone.MyApplication;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Campaign;
import com.callba.phone.bean.ContactData;
import com.callba.phone.bean.SystemNumber;
import com.callba.phone.bean.Tab;
import com.callba.phone.cfg.Constant;
import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.logic.login.LoginController;
import com.callba.phone.manager.ContactsManager;
import com.callba.phone.manager.UserManager;
import com.callba.phone.ui.adapter.CampaignAdapter;
import com.callba.phone.ui.adapter.RecyclerArrayAdapter;
import com.callba.phone.ui.adapter.TabAdapter;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.util.AppVersionChecker;
import com.callba.phone.util.ContactsAccessPublic;
import com.callba.phone.util.DesUtil;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.util.RxBus;
import com.callba.phone.util.SPUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.dao.Mark;
import de.greenrobot.dao.MarkDao;
import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by Administrator on 2016/10/30.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.activity_main
)
public class MainActivity extends BaseActivity {

    @BindView(R.id.viewpager)
    ViewPager viewpager;
    @BindView(R.id.tab_list)
    RecyclerView tabList;
    TabAdapter tabAdapter;
    private String[] result;
    List<SystemNumber> list;
    private Gson gson=new Gson();
    private ProgressDialog progressDialog;
    private String username,password;
    private String date;
    private ArrayList<Campaign> campaigns;
    private CampaignAdapter campaignAdapter;
    private boolean has_image;
    private MarkDao markDao;
    private String img_url;
    private boolean is_first=true,no_connection=false;
    private boolean isConflictDialogShow;
    private boolean isAccountRemovedDialogShow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
        tabAdapter=new TabAdapter(this);
        tabAdapter.add(new Tab(R.drawable.menu1_selector,R.string.tel));
        tabAdapter.add(new Tab(R.drawable.menu2_selector,R.string.list));
        tabAdapter.add(new Tab(R.drawable.menu3_selector,R.string.home));
        tabAdapter.add(new Tab(R.drawable.menu4_selector,R.string.message));
        tabAdapter.add(new Tab(R.drawable.menu5_selector,R.string.center));
        tabList.setAdapter(tabAdapter);
        tabList.setLayoutManager(new GridLayoutManager(this,5));
        viewpager.setAdapter(new SimpleFragmentPagerAdapter(getSupportFragmentManager(),this));
        viewpager.setCurrentItem(tabAdapter.getmSelectedItem());
        viewpager.canScrollHorizontally(0);
        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
               tabAdapter.setmSelectedItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tabAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Logger.i("position",position+"");
                viewpager.setCurrentItem(position,true);
            }
        });
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");//获取当前时间
        date = formatter.format(new Date(System.currentTimeMillis()));
        markDao = MyApplication.getInstance().getDaoSession().getMarkDao();
        if(MyApplication.getInstance().detect())
            getKey();
        else no_connection=true;
    }
    @Override
    public void onNetworkChanged(boolean isAvailable) {
        if(isAvailable&&is_first&&no_connection)
        {getKey();
            is_first=false;
            no_connection=false;
        }
        else if(!isAvailable)is_first=true;
    }
    public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {

        final int PAGE_COUNT = 5;

        private Context context;

        public SimpleFragmentPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;

        }

        @Override
        public Fragment getItem(int position) {
         return HomeFragment.newInstance();

        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

    }
    public void getKey(){
        addRequestCall(OkHttpUtils.post().url(Interfaces.Version)
                .tag(this)
                .addParams("softType", "android")
                .build()).execute(new StringCallback() {
            @Override
            public void onBefore(Request request, int id) {
                progressDialog = ProgressDialog.show(MainActivity.this, null,
                        getString(R.string.logining));
            }

            @Override
            public void onError(Call call, Exception e, int id) {
                e.printStackTrace();
                showException(e);
                AppVersionChecker.AppVersionBean appVersionBean = new AppVersionChecker.AppVersionBean();
                checkLoginKey(appVersionBean);
            }
            @Override
            public void onResponse(String response, int id) {
                AppVersionChecker.AppVersionBean appVersionBean = AppVersionChecker.parseVersionInfo(MainActivity.this, response);
                GlobalConfig.getInstance().setAppVersionBean(appVersionBean);
                checkLoginKey(appVersionBean);
            }
        });
    }
    private void checkLoginKey(AppVersionChecker.AppVersionBean appVersionBean) {
        // Logger.i(TAG, "getSecretKey() : " +
        // GlobalConfig.getInstance().getSecretKey());
        if (!TextUtils.isEmpty(appVersionBean.getSecretKey())) {
            UserManager.putSecretKey(MainActivity.this, appVersionBean.getSecretKey());
            // 成功获取key
            //check2Upgrade(appVersionBean);
            autoLogin();
        } else{
            //OkHttpUtils.getInstance().cancelTag(this);
            // 再次发送获取任务
            switchManualLogin();

        }
    }
    /**
     * 自动登录
     */
    private void autoLogin() {
        username = getUsername();
        password = UserManager.getOriginalPassword(this);

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
        addRequestCall(OkHttpUtils.post().url(Interfaces.Login)
                .addParams("loginSign", sign)
                .addParams("loginType", "1")
                .addParams("softType", "android")
                .addParams("callType", "all")
                .build()).execute(new StringCallback() {
            @Override
            public void onAfter(int id) {
                progressDialog.dismiss();
            }

            @Override
            public void onBefore(Request request, int id) {

            }

            @Override
            public void onError(Call call, Exception e, int id) {
                showException(e);
                switchManualLogin();
            }

            @Override
            public void onResponse(String response, int id) {
                try {
                    Logger.i("login_result", response);
                    String[] resultInfo = response.split("\\|");
                    if (resultInfo[0].equals("0")) { //处理登录成功返回信息
                        getSystemPhoneNumber(ContactsAccessPublic.hasName(MainActivity.this, "Call吧电话"));
                        LoginController.parseLoginSuccessResult(MainActivity.this, username, password, resultInfo);
                        LoginController.getInstance().setUserLoginState(true);
                        if ((boolean) SPUtils.get(MainActivity.this, "settings", "sign_key", true)&&!getDate().equals(SPUtils.get(MainActivity.this, Constant.PACKAGE_NAME, "sign_date", "")))
                            signIn();
                        if (GlobalConfig.getInstance().getAppVersionBean() != null) {
                            if (GlobalConfig.getInstance().getAppVersionBean().isForceUpgrade() || (boolean) SPUtils.get(MainActivity.this, "settings", "update_key", true))
                                check2Upgrade(GlobalConfig.getInstance().getAppVersionBean(), false);
                            else getActivity();
                        }else getActivity();
                        RxBus.get().post("refresh_ad",true);
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
    public void getSystemPhoneNumber(String count) {
        Logger.i("phoneNumberCount", count);
        addRequestCall(OkHttpUtils.post().url(Interfaces.GET_SYSTEM_PHONE_NUMBER)
                .addParams("loginName", getUsername())
                .addParams("loginPwd", getPassword())
                .addParams("phoneNumberCount", count)
                .build()).execute(new StringCallback() {
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
                        if (ContactsAccessPublic.hasName(MainActivity.this, "Call吧电话").equals("0"))
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    ContactsAccessPublic.insertPhoneContact(MainActivity.this, contactData, numbers);
                                }
                            }).start();
                        else {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    ContactsAccessPublic.deleteContact(MainActivity.this, new ContactsManager(getContentResolver()).getContactID("Call吧电话"));
                                    ContactsAccessPublic.insertPhoneContact(MainActivity.this, contactData, numbers);
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
    /**
     * 跳转到手动登陆界面
     */
    private void switchManualLogin() {
        Intent intent = new Intent();
        intent.setClass(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    // 跳转到起始页
    private void gotoWelcomePage() {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, WelcomeActivity.class);
        startActivity(intent);
        finish();
    }
    public String getDate() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }
    @Override
    public void showActivity() {

        getActivity();
    }
    public void getActivity() {
        addRequestCall(OkHttpUtils.post().url(Interfaces.ACTIVITY_INFO)
                .addParams("loginName", getUsername())
                .addParams("loginPwd", getPassword())
                .build()).execute(new StringCallback() {

            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(String response, int id) {
                try {
                    Logger.i("campaign_result",response);
                    String[] result = response.split("\\|");
                    if (result[0].equals("0")) {
                        campaigns = gson.fromJson(result[1], new TypeToken<ArrayList<Campaign>>() {
                        }.getType());
                        for(Campaign campaign:campaigns){
                           /* if(Integer.parseInt(campaign.getType())<homeAdapter.getData().size())
                                homeAdapter.getData().get(Integer.parseInt(campaign.getType())).setIs_discount(true);*/
                            if(!has_image&&campaign.getType().equals("20")){
                                img_url=campaign.getImgUrl();
                                campaigns.remove(campaign);
                                has_image=true;
                            }
                        }
                        //homeAdapter.notifyDataSetChanged();
                        if (!getDate().equals(SPUtils.get(MainActivity.this, Constant.PACKAGE_NAME, "activity_date", "")))
                        { SPUtils.put(MainActivity.this, Constant.PACKAGE_NAME, "activity_date", getDate());
                            if(has_image){
                                Intent intent=new Intent(MainActivity.this,CampaignActivity.class);
                                intent.putExtra("image",img_url);
                                startActivity(intent);
                            }else
                            {showDialog(campaigns);
                            }}
                    }
                } catch (Exception e) {
                }
            }
        });
    }

    public void signIn() {
        addRequestCall(OkHttpUtils.post().url(Interfaces.Sign)
                .addParams("loginName", getUsername())
                .addParams("loginPwd", getPassword())
                .addParams("softType", "android")
                .build())
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
                                UserManager.putGold(MainActivity.this, UserManager.getGold(MainActivity.this) + 3);
                                SPUtils.put(MainActivity.this, Constant.PACKAGE_NAME, "sign_date", getDate());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

    }
    public class DialogHelper implements DialogInterface.OnDismissListener {
        private AlertDialog mDialog;
        private View mView;
        private RecyclerView mealList;

        public DialogHelper(ArrayList<Campaign> campaigns) {
            mView = getLayoutInflater().inflate(R.layout.dialog_list, null);
            mealList = (RecyclerView) mView.findViewById(R.id.list);
            campaignAdapter = new CampaignAdapter(MainActivity.this);
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
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra(com.callba.phone.Constant.ACCOUNT_CONFLICT, false) && !isConflictDialogShow) {
            showConflictDialog();
        } else if (intent.getBooleanExtra(com.callba.phone.Constant.ACCOUNT_REMOVED, false) && !isAccountRemovedDialogShow) {
            showAccountRemovedDialog();
        }
    }
    /**
     * 显示帐号在别处登录dialog
     */
    private void showConflictDialog() {
        isConflictDialogShow = true;
        logout();
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UserManager.putOriginalPassword(MainActivity.this,"");
                UserManager.putPassword(MainActivity.this,"");
                LoginController.getInstance().setUserLoginState(false);
                Intent intent0 = new Intent("com.callba.location");
                intent0.putExtra("action", "logout");
                sendBroadcast(intent0);
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, LoginActivity.class);
            /*    for (Activity activity : MyApplication.activities) {
                    activity.finish();
                }*/
                finish();
                dialog.dismiss();
                startActivity(intent);
            }
        });
        builder.setTitle(getString(R.string.Logoff_notification));
        builder.setMessage(getString(R.string.connect_conflict));
        builder.setCancelable(false);
        builder.create().show();
    }

    /**
     * 帐号被移除的dialog
     */
    private void showAccountRemovedDialog() {
        isAccountRemovedDialogShow = true;
        logout();
        Dialog dialog = new android.support.v7.app.AlertDialog.Builder(this).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UserManager.putUsername(MainActivity.this,"");
                UserManager.putPassword(MainActivity.this,"");
                LoginController.getInstance().setUserLoginState(false);
                UserManager.putOriginalPassword(MainActivity.this, "");
                Intent intent0 = new Intent("com.callba.location");
                intent0.putExtra("action", "logout");
                sendBroadcast(intent0);
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, LoginActivity.class);
                for (Activity activity : MyApplication.activities) {
                    activity.finish();
                }
                startActivity(intent);
            }
        }).create();
        dialog.setCancelable(false);
        dialog.show();

    }

    public void logout() {
        EMClient.getInstance().logout(false, new EMCallBack() {

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
    }
}
