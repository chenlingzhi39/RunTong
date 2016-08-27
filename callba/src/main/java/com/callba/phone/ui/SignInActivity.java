package com.callba.phone.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.MyApplication;
import com.callba.phone.SocializeConfigDemo;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.UserDao;
import com.callba.phone.manager.UserManager;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.util.SharedPreferenceUtil;
import com.callba.phone.view.CircleTextView;
import com.callba.phone.widget.signcalendar.SignCalendar;
import com.callba.phone.widget.signcalendar.sqlit;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.RequestType;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.*;
import com.umeng.socialize.media.CircleShareContent;
import com.umeng.socialize.media.MailShareContent;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.QZoneShareContent;
import com.umeng.socialize.media.RenrenShareContent;
import com.umeng.socialize.media.SinaShareContent;
import com.umeng.socialize.media.TencentWbShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMVideo;
import com.umeng.socialize.media.UMusic;
import com.umeng.socialize.media.WeiXinShareContent;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.TencentWBSsoHandler;
import com.umeng.socialize.sso.UMWXHandler;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.dao.Mark;
import de.greenrobot.dao.MarkDao;
import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by PC-20160514 on 2016/5/24.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.sign_in,
        toolbarTitle = R.string.sign_in,
        navigationId = R.drawable.press_back,
        menuId = R.menu.menu_sign
)
public class SignInActivity extends BaseActivity implements UserDao.PostListener {
    @InjectView(R.id.circle)
    CircleTextView circle;
    @InjectView(R.id.popupwindow_calendar)
    SignCalendar calendar;
    @InjectView(R.id.popupwindow_calendar_month)
    TextView popupwindow_calendar_month;
    @InjectView(R.id.btn_signIn)
    Button btn_signIn;
    @InjectView(R.id.gold)
    TextView gold;
    @InjectView(R.id.to_play)
    Button toPlay;
    @InjectView(R.id.to_recharge)
    Button toRecharge;
    @InjectView(R.id.to_share)
    Button toShare;
    private String date = null;// 设置默认选中的日期  格式为 “2014-04-05” 标准DATE格式
    private List<String> list = new ArrayList<String>(); //设置标记列表
    // DBManager dbManager;
    boolean isinput = false;
    private String date1 = null;//单天日期
    private UserDao userDao, userDao1;
    private int constant = 0;
    Calendar cal;
    ArrayList<Integer> monthList = new ArrayList<>();
    SimpleDateFormat df, formatter;
    private MarkDao markDao;
    private Cursor cursor;
    private ProgressDialog progressDialog;
    // 整个平台的Controller, 负责管理整个SDK的配置、操作等处理
    private UMSocialService mController = UMServiceFactory.getUMSocialService(
            SocializeConfigDemo.DESCRIPTOR, RequestType.SOCIAL);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        // 初始化DBManager
        // dbManager = new DBManager(this);
        configSso();
        markDao = MyApplication.getInstance().getDaoSession().getMarkDao();
        gold.setText(getString(R.string.gold) + ":" + UserManager.getGold(this));
        list = new ArrayList<>();
        cal = Calendar.getInstance();
        formatter = new SimpleDateFormat("yyyy-MM-dd");//获取当前时间
        date1 = formatter.format(calendar.getThisday());
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int mDensity = metrics.densityDpi;
        circle.setDensity(mDensity);
        popupwindow_calendar_month.setText(calendar.getCalendarYear() + "年"
                + calendar.getCalendarMonth() + "月");
      /*  if (null != date) {
            int years = Integer.parseInt(date.substring(0,
                    date.indexOf("-")));
            int month = Integer.parseInt(date.substring(
                    date.indexOf("-") + 1, date.lastIndexOf("-")));
            popupwindow_calendar_month.setText(years + "年" + month + "月");

            calendar.showCalendar(years, month);
            calendar.setCalendarDayBgColor(date,
                    R.drawable.calendar_date_focused);
        }*/
        df = new SimpleDateFormat("yyyyMMdd");
        date = df.format(new Date(System.currentTimeMillis()));
        /*add("2015-11-10");
        add("2015-11-02");
        add("2015-12-02");*/
        userDao1 = new UserDao(this, new UserDao.PostListener() {
            @Override
            public void start() {
                progressDialog = ProgressDialog.show(SignInActivity.this, null,
                        "正在获取签到信息");
                btn_signIn.setEnabled(false);
                circle.setEnabled(false);
            }

            @Override
            public void success(String msg) {
                progressDialog.dismiss();
                if (calendar.getCalendarMonth() == cal.get(Calendar.MONTH) + 1) {
                    btn_signIn.setEnabled(true);
                    circle.setEnabled(true);
                }
                try {
                String[] result = msg.split("\\|");
                monthList.add(calendar.getCalendarMonth());
                if (result[0].equals("0")) {
                    String[] dates = result[1].split(",");
                    for (String date : dates) {
                        Mark mark = new Mark();
                        try {
                            mark.setDate(df.parse(date));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mark.setUsername(getUsername());
                        mark.setMonth(calendar.getCalendarMonth());
                        markDao.insert(mark);
                        date = date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8);
                        Logger.i("date", date);
                        if (calendar.getCalendarMonth() == cal.get(Calendar.MONTH) + 1)
                            list.add(date);
                        calendar.addMark(date,0);
                    }
                    if (dates.length > 0 && calendar.getCalendarMonth() == cal.get(Calendar.MONTH) + 1) {
                        try {
                            if (formatter.parse(date1).getTime() - df.parse(dates[dates.length - 1]).getTime() <= 24 * 60 * 60 * 1000) {
                                constant = 1;
                                if (formatter.parse(date1).getTime() == df.parse(dates[dates.length - 1]).getTime()) {
                                    isinput = true;
                                    btn_signIn.setText("今日已签，明日继续");
                                    btn_signIn.setBackgroundResource(R.drawable.button_gray);
                                    btn_signIn.setEnabled(false);
                                    circle.setIs_sign(true);
                                    circle.setEnabled(false);
                                }
                                if (dates.length >= 2)
                                    for (int i = dates.length - 1; i > 0; i--) {
                                        Date newDate = df.parse(dates[i]);
                                        Date oldDate = df.parse(dates[i - 1]);
                                        long l = newDate.getTime() - oldDate.getTime();
                                        Logger.i("newDate", newDate.getTime() + "");
                                        Logger.i("oldDate", oldDate.getTime() + "");
                                        if (l == 24 * 60 * 60 * 1000)
                                            constant += 1;
                                        else break;
                                    }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                } else toast(result[1]);
                }catch(Exception e){
                    toast(R.string.getserverdata_exception);
                }
                circle.setConstant(constant);
            }

            @Override
            public void failure(String msg) {
                progressDialog.dismiss();
                toast(msg);
                if (!circle.is_sign()) {
                    btn_signIn.setEnabled(true);
                    circle.setEnabled(true);
                }
            }
        });
        query();

        btn_signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userDao.getSign(getUsername(), getPassword());

            }
        });
        circle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userDao.getSign(getUsername(), getPassword());

            }
        });
        //监听所选中的日期
//		calendar.setOnCalendarClickListener(new OnCalendarClickListener() {
//
//			public void onCalendarClick(int row, int col, String dateFormat) {
//				int month = Integer.parseInt(dateFormat.substring(
//						dateFormat.indexOf("-") + 1,
//						dateFormat.lastIndexOf("-")));
//
//				if (calendar.getCalendarMonth() - month == 1//跨年跳转
//						|| calendar.getCalendarMonth() - month == -11) {
//					calendar.lastMonth();
//
//				} else if (month - calendar.getCalendarMonth() == 1 //跨年跳转
//						|| month - calendar.getCalendarMonth() == -11) {
//					calendar.nextMonth();
//
//				} else {
//					list.add(dateFormat);
//					calendar.addMarks(list, 0);
//					calendar.removeAllBgColor();
//					calendar.setCalendarDayBgColor(dateFormat,
//							R.drawable.calendar_date_focused);
//					date = dateFormat;//最后返回给全局 date
//				}
//			}
//		});

        //监听当前月份
        calendar.setOnCalendarDateChangedListener(new SignCalendar.OnCalendarDateChangedListener() {
            public void onCalendarDateChanged(int year, int month) {
                popupwindow_calendar_month
                        .setText(year + "年" + month + "月");

                query();
            }
        });
        userDao = new UserDao(this, this);

    }

    @Override
    public void start() {
        circle.setEnabled(false);
        btn_signIn.setEnabled(false);
    }

    @Override
    public void success(String msg) {
        toast(msg);
        //SharedPreferenceUtil.getInstance(this).putString(getUsername(), date, true);
           /* calendar.removeAllMarks();
           list.add(df.format(today));
           calendar.addMarks(list, 0);*/
        //将当前日期标示出来
        //add(df.format(today));
        Mark mark = new Mark();
        mark.setUsername(getUsername());
        mark.setMonth(cal.get(Calendar.MONTH) + 1);
        try {
            mark.setDate(formatter.parse(date1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        markDao.insert(mark);
        calendar.addMark(date1, 0);
        //query();
        HashMap<String, Integer> bg = new HashMap<String, Integer>();
        UserManager.putGold(this,UserManager.getGold(this) + 3);
        gold.setText(getString(R.string.gold) + ":" + UserManager.getGold(this));
        calendar.setCalendarDayBgColor(date1, R.drawable.bg_sign_today);
        btn_signIn.setText("今日已签，明日继续");
        btn_signIn.setBackgroundResource(R.drawable.button_gray);
        btn_signIn.setEnabled(false);
        circle.setIs_sign(true);
        circle.setEnabled(false);
      /*  try {
            if (list.size() != 0)
            {if (formatter.parse(date1).getTime() - formatter.parse(list.get(list.size() - 1)).getTime() ==  24 * 60 * 60 * 1000) {
                constant += 1;
            }} else constant += 1;

            circle.setConstant(constant);
        } catch (Exception e) {

        }*/
        constant += 1;
        circle.setConstant(constant);
    }

    @Override
    public void failure(String msg) {
        toast(msg);
        btn_signIn.setEnabled(true);
        circle.setEnabled(true);
    }

    public void add(String date) {
        ArrayList<sqlit> persons = new ArrayList<sqlit>();

        sqlit person1 = new sqlit(date, "true");

        persons.add(person1);

        //dbManager.add(persons);
    }

    public void query() {
      /*  List<sqlit> persons = dbManager.query();
        for (sqlit person : persons) {
            list.add(person.date);
            if (date1.equals(person.getDate())) {
                isinput = true;
            }
        }*/
        if (!monthList.contains(calendar.getCalendarMonth()) && calendar.getCalendarMonth() <= cal.get(Calendar.MONTH) + 1)
            if (!getLocalMarks(calendar.getCalendarMonth())) {
                String year = calendar.getCalendarYear() + "";
                String month = calendar.getCalendarMonth() + "";
                if (month.length() == 1)
                    month = "0" + month;
                userDao1.getMarks(getUsername(),getPassword(), year + month);
            }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //dbManager.closeDB();// 释放数据库资源
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                monthList.clear();
                list.clear();
                markDao.deleteAll();
                query();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean getLocalMarks(int month) {
        String where = MarkDao.Properties.Month.columnName + " = " + month + " and " + MarkDao.Properties.Username.columnName + " = '" + getUsername() + "'";
        String orderBy = MarkDao.Properties.Date.columnName + " DESC";
        cursor = MyApplication.getInstance().getDb().query(markDao.getTablename(), markDao.getAllColumns(), where, null, null, null, orderBy);
        if (cursor.getCount() == 0 || cursor == null)
            return false;
        ArrayList<Long> millis = new ArrayList<>();
        monthList.add(month);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            Mark mark = new Mark();
            markDao.readEntity(cursor, mark, 0);
            calendar.addMark(mark.getDate(), 0);
            Logger.i("date", formatter.format(mark.getDate()));
            millis.add(mark.getDate().getTime());
            if (calendar.getCalendarMonth() == cal.get(Calendar.MONTH) + 1)
                list.add(formatter.format(mark.getDate()));
        }
        if (calendar.getCalendarMonth() == cal.get(Calendar.MONTH) + 1)
            Collections.reverse(list);
        if (calendar.getCalendarMonth() == cal.get(Calendar.MONTH) + 1) {
            try {
                if (formatter.parse(date1).getTime() - millis.get(0) <= 24 * 60 * 60 * 1000) {
                    constant = 1;
                    if (formatter.parse(date1).getTime() == millis.get(0)) {
                        btn_signIn.setText("今日已签，明日继续");
                        btn_signIn.setBackgroundResource(R.drawable.button_gray);
                        btn_signIn.setEnabled(false);
                        circle.setIs_sign(true);
                        circle.setEnabled(false);
                    }
                    if (millis.size() >= 2)
                        for (int i = 0; i < millis.size() - 1; i++) {
                            if (millis.get(i) - millis.get(i + 1) == 24 * 60 * 60 * 1000)
                                constant += 1;
                            else break;
                        }
                }


            } catch (Exception e) {
            }
            circle.setConstant(constant);
        }
        Logger.i("mark_size", millis.size() + "");
        cursor.close();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.i("result_code", resultCode + "");
        super.onActivityResult(requestCode, resultCode, data);
        //UMShareAPI.get( this ).onActivityResult( requestCode, resultCode, data);
    }

    @OnClick({R.id.to_play, R.id.to_recharge, R.id.to_share, R.id.previous, R.id.next})
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.to_play:
                toast("暂未开放");
                break;
            case R.id.to_recharge:
                intent = new Intent(SignInActivity.this, RechargeActivity.class);
                startActivity(intent);
                break;
            case R.id.to_share:
              /*  intent = new Intent(Intent.ACTION_SEND); // 启动分享发送的属性
                intent.setType("text/plain"); // 分享发送的数据类型
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Callba分享"); // 分享的主题
                intent.putExtra(Intent.EXTRA_TEXT, "我正在使用CALL吧！ CALL吧“0月租”“0漫游”“通话不计分钟”，赶快加入我们吧！"); // 分享的内容
                startActivityForResult(Intent.createChooser(intent, "选择分享"), 0);*/
                //startActivity(new Intent(SignInActivity.this, ShareActivity.class));
                // 首先在您的Activity中添加如下成员变量
                addWXPlatform();
                mController.setShareContent("我正在使用CALL吧！ CALL吧“0月租”“0漫游”“通话不计分钟”，赶快加入我们吧！");
                mController.openShare(this, false);
                break;
            case R.id.previous:
                calendar.lastMonth();
                break;
            case R.id.next:
                calendar.nextMonth();
                break;
        }
    }

    /*   private UMShareListener umShareListener = new UMShareListener() {
           @Override
           public void onResult(SHARE_MEDIA platform) {
               Log.d("plat","platform"+platform);
               if(platform.name().equals("WEIXIN_FAVORITE")){
                   Toast.makeText(SignInActivity.this,platform + " 收藏成功啦",Toast.LENGTH_SHORT).show();
               }else{
                   Toast.makeText(SignInActivity.this, platform + " 分享成功啦", Toast.LENGTH_SHORT).show();
               }
           }

           @Override
           public void onError(SHARE_MEDIA platform, Throwable t) {
               Toast.makeText(SignInActivity.this,platform + " 分享失败啦", Toast.LENGTH_SHORT).show();
           }

           @Override
           public void onCancel(SHARE_MEDIA platform) {
               Toast.makeText(SignInActivity.this,platform + " 分享取消了", Toast.LENGTH_SHORT).show();
           }
       };
   */
    private void setPlatformOrder() {
        mController.getConfig().setPlatformOrder(SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.SINA, SHARE_MEDIA.TENCENT, SHARE_MEDIA.QZONE, SHARE_MEDIA.SMS);
//		mController.getConfig().setPlatforms(SHARE_MEDIA.WEIXIN,SHARE_MEDIA.WEIXIN_CIRCLE,SHARE_MEDIA.SINA,SHARE_MEDIA.TENCENT,SHARE_MEDIA.QZONE,SHARE_MEDIA.SMS);
    }

    /**
     * @return void
     * @throws
     * @Title: configSso
     * @Description: 配置sso授权Handler
     */
    private void configSso() {
        // 配置SSO
        mController.getConfig().setSsoHandler(new SinaSsoHandler());
        mController.getConfig().setSsoHandler(new TencentWBSsoHandler());
        mController.getConfig().supportQQPlatform(this, "100424468", "c7394704798a158208a74ab60104f0ba",
                "http://weixin.boboit.cn/download/download.jsp");
        mController.getConfig().setSsoHandler(
                new QZoneSsoHandler(this, "100424468", "c7394704798a158208a74ab60104f0ba"));
        mController.setShareContent("我正在使用CALL吧！ CALL吧“0月租”“0漫游”“通话不计分钟”，赶快加入我们吧！");
        setPlatformOrder();

        // 设置微信分享内容
        // UMImage mUMImgBitmap = new UMImage(getActivity(),
        // "http://www.umeng.com/images/pic/banner_module_social.png");
        // UMImage mUMImgBitmap = new UMImage(getActivity(),
        // "/mnt/sdcard/test.jpg");

        // UMImage mUMImgBitmap = new UMImage(getActivity(),
        // R.drawable.bigimage);

        // UMImage mUMImgBitmap = new UMImage(getActivity(),
        // new File("/mnt/sdcard/testjpg.jpg"));

        UMImage localImage = new UMImage(this, R.drawable.logo);
        UMImage urlImage = new UMImage(this,
                "http://www.umeng.com/images/pic/banner_module_social.png");
        UMImage resImage = new UMImage(this, R.drawable.logo);

        // 视频分享
        UMVideo video = new UMVideo(
                "http://v.youku.com/v_show/id_XNTc0ODM4OTM2.html");
        // vedio.setThumb("http://www.umeng.com/images/pic/banner_module_social.png");
        video.setTitle("友盟社会化组件视频");
        video.setThumb(urlImage);

        UMusic uMusic = new UMusic(
                "http://music.huoxing.com/upload/20130330/1364651263157_1085.mp3");
        uMusic.setAuthor("umeng");
        uMusic.setTitle("天籁之音");
        uMusic.setThumb(urlImage);
        // uMusic.setThumb("http://www.umeng.com/images/pic/social/chart_1.png");

        WeiXinShareContent weixinContent = new WeiXinShareContent();
        weixinContent.setShareContent("我正在使用CALL吧！ CALL吧“0月租”“0漫游”“通话不计分钟”，赶快加入我们吧！");
        weixinContent.setTitle("Call吧分享");
        weixinContent.setTargetUrl("http://weixin.boboit.cn/download/download.jsp");
        weixinContent.setShareImage(new UMImage(this, "http://www.umeng.com/images/pic/banner_module_social.png"));
        mController.setShareMedia(weixinContent);

        // 设置朋友圈分享的内容
        CircleShareContent circleMedia = new CircleShareContent();
        circleMedia.setShareContent("我正在使用CALL吧！ CALL吧“0月租”“0漫游”“通话不计分钟”，赶快加入我们吧！");
        circleMedia.setTitle("Call吧分享");
        circleMedia.setShareImage(localImage);
        // circleMedia.setShareMusic(uMusic);
//		circleMedia.setShareVideo(video);
        mController.setShareMedia(circleMedia);

        // 设置新浪分享内容
//		mController.setShareMedia(new SinaShareContent(new UMImage(
//				getActivity(),
//				"http://www.umeng.com/images/pic/social/integrated_3.png")));

        // 设置renren分享内容
        RenrenShareContent renrenShareContent = new RenrenShareContent();
        renrenShareContent.setShareContent("我正在使用CALL吧！ CALL吧“0月租”“0漫游”“通话不计分钟”，赶快加入我们吧！");
        UMImage image = new UMImage(this,
                BitmapFactory.decodeResource(getResources(), R.drawable.logo));
        image.setTitle("Call吧分享");
        image.setThumb("http://www.umeng.com/images/pic/social/integrated_3.png");
        renrenShareContent.setShareImage(image);
        renrenShareContent.setAppWebSite("http://www.umeng.com/social");
        mController.setShareMedia(renrenShareContent);

        UMImage qzoneImage = new UMImage(this,
                "http://www.umeng.com/images/pic/social/integrated_3.png");
        qzoneImage
                .setTargetUrl("http://www.umeng.com/images/pic/social/integrated_3.png");

        UMImage mx2Image = new UMImage(
                this,
                /* new File("/mnt/sdcard/bigimage.jpg") */"http://www.umeng.com/images/pic/social/integrated_3.png");

        // 设置QQ空间分享内容
        QZoneShareContent qzone = new QZoneShareContent();
        qzone.setShareContent("我正在使用CALL吧！ CALL吧“0月租”“0漫游”“通话不计分钟”，赶快加入我们吧！");
        qzone.setTargetUrl("http://weixin.boboit.cn/download/download.jsp");
        qzone.setTitle("Call吧分享");
        qzone.setShareImage(localImage);
        mController.setShareMedia(qzone);

        video.setThumb(new UMImage(this, BitmapFactory.decodeResource(
                getResources(), R.drawable.logo)));

        QQShareContent qqShareContent = new QQShareContent();
        qqShareContent.setShareContent("我正在使用CALL吧！ CALL吧“0月租”“0漫游”“通话不计分钟”，赶快加入我们吧！");
        qqShareContent.setTitle("Call吧分享");
        qqShareContent
                .setShareImage(new UMImage(this, R.drawable.logo));
        qqShareContent.setTargetUrl("http://weixin.boboit.cn/download/download.jsp");
        mController.setShareMedia(qqShareContent);

        // UMusic uMusic = new
        // UMusic("http://sns.whalecloud.com/test_music.mp3");
        // uMusic.setAuthor("umeng");
        // uMusic.setTitle("天籁之音");
        // uMusic.setThumb(mUMImgBitmap);
        // // 设置tencent分享内容
        // mController
        // .setShareMedia(new TencentWbShareContent(uMusic));

        // 视频分享
        UMVideo umVideo = new UMVideo(
                "http://v.youku.com/v_show/id_XNTc0ODM4OTM2.html");
        umVideo.setThumb("http://www.umeng.com/images/pic/banner_module_social.png");
        umVideo.setTitle("友盟社会化组件视频");

        TencentWbShareContent tencent = new TencentWbShareContent();
        tencent.setShareContent("我正在使用CALL吧！ CALL吧“0月租”“0漫游”“通话不计分钟”，赶快加入我们吧！");
        // 设置tencent分享内容
        mController.setShareMedia(tencent);

        // 设置邮件分享内容， 如果需要分享图片则只支持本地图片
        MailShareContent mail = new MailShareContent(localImage);
        mail.setTitle("Call吧分享");
        mail.setShareContent("我正在使用CALL吧！ CALL吧“0月租”“0漫游”“通话不计分钟”，赶快加入我们吧！");
        // 设置tencent分享内容
        mController.setShareMedia(mail);

        // 设置短信分享内容
        // SmsShareContent sms = new SmsShareContent(new UMImage(getActivity(),
        // new File(
        // "/mnt/sdcard/bigimage.jpg")));
        // SmsShareContent sms = new SmsShareContent();
        // sms.setShareImage(new UMImage(getActivity(), R.drawable.device));
        // sms.setShareContent("来自友盟社会化组件（SDK）让移动应用快速整合社交分享功能，短信");
        // sms.setShareImage(urlImage);
        // mController.setShareMedia(sms);

        SinaShareContent sinaContent = new SinaShareContent(localImage);
        sinaContent.setShareContent("我正在使用CALL吧！ CALL吧“0月租”“0漫游”“通话不计分钟”，赶快加入我们吧！");
//		mController.setShareMedia(sinaContent);

        mController.setShareMedia(new UMImage(this, R.drawable.logo));

        mController.getConfig().closeSinaSSo();
        // addInstagram();

        // addWXPlatform();
        // addYXPlatform();

    }

    private void addWXPlatform() {

        // wx967daebe835fbeac是你在微信开发平台注册应用的AppID, 这里需要替换成你注册的AppID
        String appId = "wxd258acce3916c63a";
        String appSecret="c17f1da2803cc4b58544cc9f81f989f8";
        // 微信图文分享,音乐必须设置一个url
        String contentUrl = "http://www.umeng.com/social";
        // 添加微信平台
        UMWXHandler wxHandler = mController.getConfig().supportWXPlatform(
                this, appId,appSecret);
        wxHandler.setWXTitle("友盟社会化组件还不错-WXHandler...");

        UMImage mUMImgBitmap = new UMImage(this,
                "http://www.umeng.com/images/pic/banner_module_social.png");

        UMusic uMusic = new UMusic("http://sns.whalecloud.com/test_music.mp3");
        uMusic.setAuthor("zhangliyong");
        uMusic.setTitle("天籁之音");
        // uMusic.setThumb("http://www.umeng.com/images/pic/banner_module_social.png");
        // 非url类型的缩略图需要传递一个UMImage的对象
        uMusic.setThumb(mUMImgBitmap);
        //
        // 视频分享
        UMVideo umVedio = new UMVideo(
                "http://v.youku.com/v_show/id_XNTc0ODM4OTM2.html");
        umVedio.setTitle("友盟社会化组件视频");
        // umVedio.setThumb("http://www.umeng.com/images/pic/banner_module_social.png");
        umVedio.setThumb(mUMImgBitmap);
        // 设置分享文字内容
        mController
                .setShareContent("我正在使用CALL吧！ CALL吧“0月租”“0漫游”“通话不计分钟”，赶快加入我们吧！");
        // mController.setShareContent(null);
        // 设置分享图片
        // mController.setShareMedia(mUMImgBitmap);
        // 支持微信朋友圈
        UMWXHandler circleHandler = mController.getConfig()
                .supportWXCirclePlatform(this, appId, appSecret);
        circleHandler.setCircleTitle("友盟社会化组件还不错-CircleHandler...");

        //
        mController.getConfig().registerListener(new SnsPostListener() {

            @Override
            public void onStart() {
          /*   Toast.makeText(SignInActivity.this, "weixin -- xxxx onStart", 0)
                     .show();*/
            }

            @Override
            public void onComplete(SHARE_MEDIA platform, int eCode,
                                   SocializeEntity entity) {
                Logger.i("eCode",eCode+"");
               /* Toast.makeText(SignInActivity.this, platform + " code = " + eCode, 0)
                        .show();*/
                if(eCode==200){
                    OkHttpUtils.post().url(Interfaces.GET_GOLD_FROM_SHARE)
                            .addParams("loginName",getUsername())
                            .addParams("loginPwd",getPassword())
                            .build().execute(new StringCallback() {

                        @Override
                        public void onAfter(int id) {
                           progressDialog.dismiss();
                        }

                        @Override
                        public void onBefore(Request request, int id) {
                         progressDialog=ProgressDialog.show(SignInActivity.this,"","获取金币中");
                        }

                        @Override
                        public void onError(Call call, Exception e, int id) {
                            if(e instanceof UnknownHostException){
                                toast(R.string.conn_failed);
                            }else toast(R.string.network_error);
                        }

                        @Override
                        public void onResponse(String response, int id) {
                        try {
                            String[] result=response.split("\\|");
                            if(result[0].equals("0"))
                            {
                                toast(result[1]);
                                UserManager.putGold(SignInActivity.this,UserManager.getGold(SignInActivity.this)+5);
                            }else{
                                toast(result[1]);
                            }
                        }catch (Exception e){
                            toast(R.string.getserverdata_exception);
                        }
                        }
                    });
                }
            }
        });

    }
}
