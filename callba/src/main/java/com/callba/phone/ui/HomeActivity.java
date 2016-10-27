package com.callba.phone.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.MyApplication;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Advertisement;
import com.callba.phone.bean.Campaign;
import com.callba.phone.bean.ContactData;
import com.callba.phone.bean.HomeItem;
import com.callba.phone.bean.SystemNumber;
import com.callba.phone.cfg.Constant;
import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.logic.login.LoginController;
import com.callba.phone.manager.ContactsManager;
import com.callba.phone.manager.UserManager;
import com.callba.phone.ui.adapter.CampaignAdapter;
import com.callba.phone.ui.adapter.HomeAdapter;
import com.callba.phone.ui.adapter.RecyclerArrayAdapter;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.AppVersionChecker;
import com.callba.phone.util.ContactsAccessPublic;
import com.callba.phone.util.DesUtil;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.util.RxBus;
import com.callba.phone.util.SPUtils;
import com.callba.phone.view.BannerLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import butterknife.OnClick;
import de.greenrobot.dao.Mark;
import de.greenrobot.dao.MarkDao;
import okhttp3.Call;
import okhttp3.Request;
import rx.Observable;
import rx.functions.Action1;


/**
 * Created by Administrator on 2016/5/14.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.tab_home,
        toolbarTitle = R.string.home

)
public class HomeActivity extends BaseActivity {
    BannerLayout banner;
    @BindView(R.id.list)
    RecyclerView homeList;
    private String yue;
    private ArrayList<Integer> localImages = new ArrayList<Integer>();
    private ArrayList<String> webImages = new ArrayList<>();
    private ProgressDialog progressDialog;
    private String username;
    private String password;
    private String date;
    private Gson gson;
    private String[] result;
    List<SystemNumber> list;
    private MarkDao markDao;
    private ArrayList<Campaign> campaigns;
    private CampaignAdapter campaignAdapter;
    private ArrayList<HomeItem> homeItems;
    private HomeAdapter homeAdapter;
    private View headerView;
    private boolean has_image;
    private String img_url;
    private Observable<Boolean> mRefreshAdObservable;
    private boolean is_first=true,no_connection=false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        homeItems = new ArrayList<>();
        homeItems.add(new HomeItem(getString(R.string.recharge), R.drawable.recharge));
        homeItems.add(new HomeItem(getString(R.string.flow), R.drawable.flow));
        homeItems.add(new HomeItem(getString(R.string.discount), R.drawable.discount));
        homeItems.add(new HomeItem(getString(R.string.family), R.drawable.family));
        homeItems.add(new HomeItem(getString(R.string.mall), R.drawable.mall));
        homeItems.add(new HomeItem(getString(R.string.service), R.drawable.service));
        homeItems.add(new HomeItem(getString(R.string.game), R.drawable.game));
        homeItems.add(new HomeItem(getString(R.string.sign_in), R.drawable.signin));
        homeAdapter = new HomeAdapter(this);
        homeAdapter.addAll(homeItems);
        headerView = getLayoutInflater().inflate(R.layout.home_header, null);
        banner = (BannerLayout) headerView.findViewById(R.id.banner);
        homeAdapter.addHeader(new RecyclerArrayAdapter.ItemView() {
            @Override
            public View onCreateView(ViewGroup parent) {
                return headerView;
            }

            @Override
            public void onBindView(View headerView) {

            }
        });
        homeAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent;
                switch (position){
                    case 0:
                        intent = new Intent(HomeActivity.this, RechargeActivity.class);
                        startActivity(intent);
                        break;
                    case 1:
                        startActivity(new Intent(HomeActivity.this, FlowActivity.class));
                        break;
                    case 2:
                        startActivity(new Intent(HomeActivity.this, CouponActivity.class));
                        break;
                    case 3:
                        startActivity(new Intent(HomeActivity.this, FamilyActivity.class));
                        break;
                    case 4:
                        toast("暂未开放");
                        break;
                    case 5:
                        toast("暂未开放");
                        break;
                    case 6:
                        toast("暂未开放");
                        break;
                    case 7:
                        intent = new Intent(HomeActivity.this, SignInActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        });
        homeList.setLayoutManager(new GridLayoutManager(this, 4));
        homeList.setAdapter(homeAdapter);
        markDao = MyApplication.getInstance().getDaoSession().getMarkDao();
        gson = new Gson();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");//获取当前时间
        date = formatter.format(new Date(System.currentTimeMillis()));
        localImages.add(R.drawable.ad4);
        localImages.add(R.drawable.ad5);
        localImages.add(R.drawable.ad6);
        banner.setViewRes(localImages);
        mRefreshAdObservable= RxBus.get().register("refresh_ad", Boolean.class);
        mRefreshAdObservable.subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                getAd();
            }
        });
        if(MyApplication.getInstance().detect())
        getKey();
        else no_connection=true;
        //autoLogin();
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

    // 跳转到起始页
    private void gotoWelcomePage() {
        Intent intent = new Intent();
        intent.setClass(HomeActivity.this, WelcomeActivity.class);
        startActivity(intent);
        // 关闭主tab页面
        ActivityUtil.finishMainTabPages();
    }

    public class YueDialogHelper implements DialogInterface.OnDismissListener {
        @BindView(R.id.recharge)
        Button recharge;
        @BindView(R.id.exchange)
        Button exchange;
        @BindView(R.id.number)
        TextView number;
        @BindView(R.id.yue)
        TextView tv_yue;
        @BindView(R.id.gold)
        TextView gold;
        private View mView;
        private Dialog mDialog;

        public YueDialogHelper() {
            mView = getLayoutInflater().inflate(R.layout.dialog_yue, null);
            ButterKnife.bind(this, mView);
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
        OkHttpUtils.post().url(Interfaces.Login)
                .addParams("loginSign", sign)
                .addParams("loginType", "1")
                .addParams("softType", "android")
                .addParams("callType", "all")
                .build().execute(new StringCallback() {
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
                        getSystemPhoneNumber(ContactsAccessPublic.hasName(HomeActivity.this, "Call吧电话"));
                        LoginController.parseLoginSuccessResult(HomeActivity.this, username, password, resultInfo);
                        LoginController.getInstance().setUserLoginState(true);
                        if ((boolean) SPUtils.get(HomeActivity.this, "settings", "sign_key", true)&&!getDate().equals(SPUtils.get(HomeActivity.this, Constant.PACKAGE_NAME, "sign_date", "")))
                            signIn();
                        if (GlobalConfig.getInstance().getAppVersionBean() != null) {
                            if (GlobalConfig.getInstance().getAppVersionBean().isForceUpgrade() || (boolean) SPUtils.get(HomeActivity.this, "settings", "update_key", true))
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
        RxBus.get().unregister("refresh_ad", mRefreshAdObservable);
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
                                SPUtils.put(HomeActivity.this, Constant.PACKAGE_NAME, "sign_date", getDate());
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
                    Logger.i("campaign_result",response);
                    String[] result = response.split("\\|");
                    if (result[0].equals("0")) {
                        campaigns = gson.fromJson(result[1], new TypeToken<ArrayList<Campaign>>() {
                        }.getType());
                        for(Campaign campaign:campaigns){
                            if(Integer.parseInt(campaign.getType())<homeAdapter.getData().size())
                            homeAdapter.getData().get(Integer.parseInt(campaign.getType())).setIs_discount(true);
                            if(!has_image&&campaign.getType().equals("20")){
                                img_url=campaign.getImgUrl();
                                campaigns.remove(campaign);
                                has_image=true;
                            }
                        }
                        homeAdapter.notifyDataSetChanged();
                       if (!getDate().equals(SPUtils.get(HomeActivity.this, Constant.PACKAGE_NAME, "activity_date", "")))
                        { SPUtils.put(HomeActivity.this, Constant.PACKAGE_NAME, "activity_date", getDate());
                            if(has_image){
                            Intent intent=new Intent(HomeActivity.this,CampaignActivity.class);
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

public void getKey(){
    OkHttpUtils.post().url(Interfaces.Version)
            .tag(this)
            .addParams("softType", "android")
            .build().execute(new StringCallback() {
        @Override
        public void onBefore(Request request, int id) {
            progressDialog = ProgressDialog.show(HomeActivity.this, null,
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
            AppVersionChecker.AppVersionBean appVersionBean = AppVersionChecker.parseVersionInfo(HomeActivity.this, response);
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
    private void checkLoginKey(AppVersionChecker.AppVersionBean appVersionBean) {
        // Logger.i(TAG, "getSecretKey() : " +
        // GlobalConfig.getInstance().getSecretKey());
        if (!TextUtils.isEmpty(appVersionBean.getSecretKey())) {
            UserManager.putSecretKey(HomeActivity.this, appVersionBean.getSecretKey());
            // 成功获取key
            //check2Upgrade(appVersionBean);
           autoLogin();
        } else{
            //OkHttpUtils.getInstance().cancelTag(this);
            // 再次发送获取任务
            switchManualLogin();

        }
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

    public String getDate() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

    @Override
    public void showActivity() {

            getActivity();
    }
    public void getAd(){
        OkHttpUtils.post().url(Interfaces.GET_ADVERTICEMENT1)
                .addParams("loginName", getUsername())
                .addParams("loginPwd", getPassword())
                .addParams("softType", "android")
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(String response, int id) {
                try {
                    Logger.i("ad_result", response);
                    String[] result = response.split("\\|");
                    if (result[0].equals("0")) {
                        final ArrayList<Advertisement> list;
                        list = gson.fromJson(result[1], new TypeToken<ArrayList<Advertisement>>() {
                        }.getType());
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
                } catch (Exception e) {
                    e.printStackTrace();
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
}

