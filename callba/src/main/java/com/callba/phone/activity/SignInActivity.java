package com.callba.phone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.UserDao;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.util.SharedPreferenceUtil;
import com.callba.phone.view.CircleTextView;
import com.callba.phone.widget.signcalendar.SignCalendar;
import com.callba.phone.widget.signcalendar.sqlit;
import com.umeng.socialize.utils.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

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
    private String date = null;// 设置默认选中的日期  格式为 “2014-04-05” 标准DATE格式
    private List<String> list = new ArrayList<String>(); //设置标记列表
    // DBManager dbManager;
    boolean isinput = false;
    private String date1 = null;//单天日期
    private UserDao userDao, userDao1;
    private int constant = 0;
    Calendar cal;
    Date today;
    ArrayList<Integer> monthList = new ArrayList<>();
    SimpleDateFormat df;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        // 初始化DBManager
        // dbManager = new DBManager(this);
        gold.setText(getString(R.string.gold)+":"+CalldaGlobalConfig.getInstance().getGold());
        list = new ArrayList<>();
        cal = Calendar.getInstance();
        today = calendar.getThisday();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");//获取当前时间
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

            }

            @Override
            public void success(String msg) {
                String[] result = msg.split("\\|");
                monthList.add(calendar.getCalendarMonth());
                if (result[0].equals("0")) {
                    String[] dates = result[1].split(",");
                    for (String date : dates) {
                        date = date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8);
                        Log.i("date", date);
                        if (date1.equals(date)) {
                            isinput = true;
                            if (calendar.getCalendarMonth() == cal.get(Calendar.MONTH) + 1)
                                constant = 1;
                            btn_signIn.setText("今日已签，明日继续");
                            btn_signIn.setBackgroundResource(R.drawable.button_gray);
                            btn_signIn.setEnabled(false);
                        } else {
                            if (calendar.getCalendarMonth() == cal.get(Calendar.MONTH) + 1)
                                constant = 0;
                        }
                        list.add(date);
                    }

                    if (calendar.getCalendarMonth() == cal.get(Calendar.MONTH) + 1)
                        if (dates.length > 0) {
                            try {
                                if (today.getTime() - df.parse(dates[dates.length - 1]).getTime() < 2 * 24 * 60 * 60 * 1000)
                                    constant = 1;
                                for (int i = dates.length - 1; i > 0; i--) {
                                    Date newDate = df.parse(dates[i]);
                                    Date oldDate = df.parse(dates[i - 1]);
                                    long l = newDate.getTime() - oldDate.getTime();
                                    Log.i("newDate", newDate.getTime() + "");
                                    Log.i("oldDate", oldDate.getTime() + "");
                                    if (l == 24 * 60 * 60 * 1000)
                                        constant += 1;
                                    else break;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    calendar.addMarks(list, 0);
                } else toast(result[1]);
                circle.setConstant(constant);
            }

            @Override
            public void failure(String msg) {

            }
        });
        query();

        btn_signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userDao.getSign(CalldaGlobalConfig.getInstance().getUsername(), CalldaGlobalConfig.getInstance().getPassword());

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

    }

    @Override
    public void success(String msg) {
        toast(msg);
        SharedPreferenceUtil.getInstance(this).putBoolean(date, true, true);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
           /* calendar.removeAllMarks();
           list.add(df.format(today));
           calendar.addMarks(list, 0);*/
        //将当前日期标示出来
        //add(df.format(today));
        calendar.addMark(today, 0);
        //query();
        HashMap<String, Integer> bg = new HashMap<String, Integer>();
        CalldaGlobalConfig.getInstance().setGold(CalldaGlobalConfig.getInstance().getGold()+3);
        gold.setText(getString(R.string.gold)+":"+CalldaGlobalConfig.getInstance().getGold());
        calendar.setCalendarDayBgColor(today, R.drawable.bg_sign_today);
        btn_signIn.setText("今日已签，明日继续");
        btn_signIn.setBackgroundResource(R.drawable.button_gray);
        btn_signIn.setEnabled(false);
        try {
            if (today.getTime() - df.parse(list.get(list.size() - 1)).getTime() < 2 * 24 * 60 * 60 * 1000) {
                constant += 1;
                circle.setConstant(constant);
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void failure(String msg) {
        toast(msg);
    }

    @Override
    public void init() {

    }

    @Override
    public void refresh(Object... params) {

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
        if (!monthList.contains(calendar.getCalendarMonth()) && calendar.getCalendarMonth() <= cal.get(Calendar.MONTH) + 1) {
            Log.i("year", calendar.getCalendarYear() + "");
            Log.i("month", calendar.getCalendarMonth() + "");
            String year = calendar.getCalendarYear() + "";
            String month = calendar.getCalendarMonth() + "";
            if (month.length() == 1)
                month = "0" + month;
            userDao1.getMarks(CalldaGlobalConfig.getInstance().getUsername(), CalldaGlobalConfig.getInstance().getPassword(), year + month);
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
            case R.id.share:
                Intent intent = new Intent(Intent.ACTION_SEND); // 启动分享发送的属性
                intent.setType("text/plain"); // 分享发送的数据类型
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Callba分享"); // 分享的主题
                intent.putExtra(Intent.EXTRA_TEXT, "我正在使用CALL吧！ CALL吧“0月租”“0漫游”“通话不计分钟”，赶快加入我们吧！"); // 分享的内容
                startActivity(Intent.createChooser(intent, "选择分享"));// 目标应用选择对话框的标题*/
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
