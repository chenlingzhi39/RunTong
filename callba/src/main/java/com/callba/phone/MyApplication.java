package com.callba.phone;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.CountDownTimer;
import android.support.multidex.MultiDex;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.MemoryCategory;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.callba.BuildConfig;
import com.callba.phone.ui.WelcomeActivity;
import com.callba.phone.util.Logger;
import com.callba.phone.util.StorageUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import de.greenrobot.dao.DaoMaster;
import de.greenrobot.dao.DaoSession;
import de.greenrobot.dao.query.QueryBuilder;
import okhttp3.OkHttpClient;


public class MyApplication extends Application {
    /**
     * 保存所有打开的Activity
     */
    private static MyApplication myApplication;
    public static MyApplication getInstance(){
        return myApplication;
    }
    public static List<Activity> activities = new ArrayList<>();
//	private PushAgent mPushAgent;
    private long lastRestartTimeMillis = System.currentTimeMillis();
    //实现ConnectionListener接口
    private DaoSession mDaoSession;
    private SQLiteDatabase db;
    private  ApplicationComponent applicationComponent;
    ConnectivityManager manager;
   /* TimeCount timeCount;
    private boolean aBoolean=true;
    class TimeCount extends CountDownTimer {

        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);

        }

        @Override
        public void onFinish() {// 计时完毕
            aBoolean=true;
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程
            Logger.i("time",millisUntilFinished+"");
          aBoolean=false;
        }
    }

    public boolean isaBoolean() {
        return aBoolean;
    }

    public void setaBoolean(boolean aBoolean) {
        this.aBoolean = aBoolean;
    }

    public void startCount(){
        if(aBoolean){
            timeCount=new TimeCount(10000,1000);
            timeCount.start();
        }
    }*/
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
    @Override
    public void onCreate() {
       // MultiDex.install(this);
        super.onCreate();
       // timeCount=new TimeCount(10000,1000);
        manager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        myApplication = this;
        //CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/STXIHEI.TTF").setFontAttrId(R.attr.fontPath).build());
       /* EMOptions options = new EMOptions();
       // 默认添加好友时，是不需要验证的，改成需要验证
        options.setAcceptInvitationAlways(false);
        options.setAutoLogin(false);
        EaseUI.getInstance().init(this,options);*/
        //CrashHandler crashHandler = CrashHandler.getInstance();
       //crashHandler.init(this);
       /* if((boolean)SPUtils.get(this, Constant.SETTINGS,Constant.LOG_KEY,false))
        LogcatHelper.getInstance(this).start();*/
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(6000, TimeUnit.MILLISECONDS)
                .readTimeout(20000, TimeUnit.MILLISECONDS)
                .writeTimeout(6000,TimeUnit.MILLISECONDS)
                .build();
        OkHttpUtils.initClient(okHttpClient);
        //applicationComponent=ApplicationComponent.AppInitialize.init(this);
        DemoHelper.getInstance().init(this);
        GlideBuilder builder = new GlideBuilder(this);
        builder.setMemoryCache(new LruResourceCache(5 * 1024 * 1024));
        Glide.get(this).setMemoryCategory(MemoryCategory.NORMAL);
        builder.setDiskCache(
                new ExternalCacheDiskCacheFactory(this, StorageUtils.getCacheDirectory(getApplicationContext()).getPath(), 10 * 1024 * 1024));
        initUmengAnalytics();
        String language = Locale.getDefault().getLanguage();
        Logger.i("language",language);
        setupDatabase();
    }
    private void setupDatabase() {
        // // 官方推荐将获取 DaoMaster 对象的方法放到 Application 层，这样将避免多次创建生成 Session 对象
        // 通过 DaoMaster 的内部类 DevOpenHelper，你可以得到一个便利的 SQLiteOpenHelper 对象。
        // 可能你已经注意到了，你并不需要去编写「CREATE TABLE」这样的 SQL 语句，因为 greenDAO 已经帮你做了。
        // 注意：默认的 DaoMaster.DevOpenHelper 会在数据库升级时，删除所有的表，意味着这将导致数据的丢失。
        // 所以，在正式的项目中，你还应该做一层封装，来实现数据库的安全升级。
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "callba_db", null);
        db = helper.getWritableDatabase();
        // 注意：该数据库连接属于 DaoMaster，所以多个 Session 指的是相同的数据库连接。
        DaoMaster daoMaster = new DaoMaster(db);
        mDaoSession = daoMaster.newSession();
        // 在 QueryBuilder 类中内置两个 Flag 用于方便输出执行的 SQL 语句与传递参数的值
        QueryBuilder.LOG_SQL = BuildConfig.DEBUG;
        QueryBuilder.LOG_VALUES = BuildConfig.DEBUG;
    }
    public DaoSession getDaoSession() {
        return mDaoSession;
    }

    public SQLiteDatabase getDb() {
        return db;
    }
    /**
     * 重启应用程序
     *
     * @author zhw
     */
    private void restartApplication() {
        Intent intent = new Intent();
        intent.setClass(this, WelcomeActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, Intent.FLAG_ACTIVITY_NEW_TASK);

        //100毫秒后重启应用
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 100,
                pendingIntent);
    }



    public void onTerminate() {
       /* if((boolean)SPUtils.get(this, Constant.SETTINGS,Constant.LOG_KEY,false))
        LogcatHelper.getInstance(this).stop();*/
        super.onTerminate();
    }

    /**
     * 初始化友盟统计参数设置
     */
    private void initUmengAnalytics() {
       /* MobclickAgent.updateOnlineConfig(this);
        //设置时间间隔为5分钟
        MobclickAgent.setSessionContinueMillis(5 * 60 * 1000);*/
    }

    public static ApplicationComponent getApplicationComponent() {
        return ((MyApplication)myApplication.getApplicationContext()).applicationComponent;
    }
    public boolean detect(){

        if(manager == null){
            return false;
        }
        try {
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if(networkInfo == null || !networkInfo.isAvailable()){
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
