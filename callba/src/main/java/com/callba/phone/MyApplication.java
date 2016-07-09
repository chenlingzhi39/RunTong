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
import android.os.Process;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.MemoryCategory;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.callba.BuildConfig;
import com.callba.R;
import com.callba.phone.activity.WelcomeActivity;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.util.Logger;
import com.callba.phone.util.StorageUtils;
import com.zhy.http.okhttp.OkHttpUtils;

import de.greenrobot.dao.DaoMaster;
import de.greenrobot.dao.DaoSession;
import de.greenrobot.dao.query.QueryBuilder;
import okhttp3.OkHttpClient;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class MyApplication extends Application {
    /**
     * 保存所有打开的Activity
     */
    private static MyApplication myApplication;
    public static MyApplication getInstance(){
        return myApplication;
    }
    public static List<Activity> activities = new ArrayList<Activity>();
//	private PushAgent mPushAgent;

    private long lastRestartTimeMillis = System.currentTimeMillis();
    //实现ConnectionListener接口
    private DaoSession mDaoSession;
    private SQLiteDatabase db;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
    @Override
    public void onCreate() {
        MultiDex.install(this);
        super.onCreate();
        myApplication = this;
        //CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/STXIHEI.TTF").setFontAttrId(R.attr.fontPath).build());
       /* EMOptions options = new EMOptions();
       // 默认添加好友时，是不需要验证的，改成需要验证
        options.setAcceptInvitationAlways(false);
        options.setAutoLogin(false);
        EaseUI.getInstance().init(this,options);*/
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .addInterceptor(new LoggerInterceptor("TAG"))
                .connectTimeout(20000L, TimeUnit.MILLISECONDS)
                .readTimeout(20000L, TimeUnit.MILLISECONDS)
                //其他配置
                .build();

        OkHttpUtils.initClient(okHttpClient);
        DemoHelper.getInstance().init(myApplication);
        GlideBuilder builder = new GlideBuilder(myApplication);
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
    private void restartAppcation() {
        Intent intent = new Intent();
        intent.setClass(this, WelcomeActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, Intent.FLAG_ACTIVITY_NEW_TASK);

        //100毫秒后重启应用
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 100,
                pendingIntent);
    }

//	private void initUmengPush() {
//		mPushAgent = PushAgent.getInstance(this);
//		mPushAgent.setDebugMode(true);
//		/**
//		 * 该Handler是在BroadcastReceiver中被调用，故
//		 * 如果需启动Activity，需添加Intent.FLAG_ACTIVITY_NEW_TASK
//		 * */
//		UmengNotificationClickHandler notificationClickHandler = new UmengNotificationClickHandler(){
//			@Override
//			public void dealWithCustomAction(Context context, UMessage msg) {
////				Intent intent =new Intent();
////				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////				intent.setClass(context, DialogActivity.class);
////				startActivity(intent);
//				
//				CalldaToast calldaToast = new CalldaToast();
//				calldaToast.showToast(context, msg.custom);
//			}
//		};
//		mPushAgent.setNotificationClickHandler(notificationClickHandler);
//	}

    public void addActivity(Activity activity) {
        activities.add(activity);
    }

    public void onTerminate() {
        super.onTerminate();
        for (Activity activity : activities) {
            activity.finish();
        }
        System.exit(0);
    }

    /**
     * 初始化友盟统计参数设置
     */
    private void initUmengAnalytics() {
       /* MobclickAgent.updateOnlineConfig(this);
        //设置时间间隔为5分钟
        MobclickAgent.setSessionContinueMillis(5 * 60 * 1000);*/
    }

}
