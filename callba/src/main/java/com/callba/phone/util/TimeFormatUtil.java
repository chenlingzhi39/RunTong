package com.callba.phone.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;

import com.callba.R;

/** 
 * 时间格式化工具类
 * @Author  zhw
 * @Version V1.0  
 * @Createtime：2014年5月9日 下午6:24:09 
 */
public class TimeFormatUtil {
	/**
	 * 格式化当前时间为 yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public static String formatChineseTimeStr() {
		long currentTime = System.currentTimeMillis();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		return simpleDateFormat.format(currentTime);
	}
	
	/**
	 * 获取当前时间散落的区间
	 * @author zhw
	 *
	 * @return
	 */
	public static String formatTimeRange() {
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		
		return hour + "点 ~ " + (hour+1) + "点";
	}
	
	/**
	 * 获取当前星期几
	 * @author zhw
	 * 
	 * @return
	 */
	public static String formatWeekRange() {
		Calendar calendar = Calendar.getInstance();
		int week = calendar.get(Calendar.DAY_OF_WEEK);
		
		String currentWeek = "";
		switch (week) {
			case 1:
				currentWeek = "星期天";
				break;
			case 2:
				currentWeek = "星期一";
				break;
			case 3:
				currentWeek = "星期二";
				break;
			case 4:
				currentWeek = "星期三";
				break;
			case 5:
				currentWeek = "星期四";
				break;
			case 6:
				currentWeek = "星期五";
				break;
			case 7:
				currentWeek = "星期六";
				break;
			default:
				break;
		}
		
		return currentWeek;
	}
	
	/**
	 * 格式化时间
	 * MM月dd日 HH:mm
	 * @param timeMills
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public String formatTime(long timeMills) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM月dd日 HH:mm");
		Date date = new Date(timeMills);
		return dateFormat.format(date);
	}
	/**
	 * 格式化时间
	 * MM月dd日
	 * @param timeMills
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public String formatdayTime(long timeMills) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM月dd日");
		Date date = new Date(timeMills);
		return dateFormat.format(date);
	}
	/**
	 * 毫秒转分秒工具方法
	 * 
	 * @author Zhang
	 */
	@SuppressWarnings("unused")
	private String formatMillis(Context context,long millis) {
		int second = (int) (millis / 1000);
		if (second == 0 && millis != 0) {
			return "1" + context.getString(R.string.second);
		} else if (second == 0) {
			return context.getString(R.string.unconnected);
		} else if (second < 60) {
			return second + context.getString(R.string.second);
		} else {
			return second / 60 + context.getString(R.string.minute)
					+ (second - (second / 60) * 60)
					+ context.getString(R.string.second);
		}
	}
}
 