package com.callba.phone.service;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.text.TextUtils;

import com.callba.phone.bean.CalldaCalllogBean;
import com.callba.phone.bean.CalllogDetailBean;
import com.callba.phone.util.DBopenHelper;
import com.callba.phone.util.Logger;
import com.callba.phone.util.TimeFormatUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

public class CalllogService {
	private static final int ASYNC_QUERY_COMPLETE = 0x30; // 查询完毕
	private static final int ASYNC_DELETE_SINGLE_COMPLETE = 0x31; // 删除单条
	private static final int ASYNC_DELETE_SAMENUMBER_COMPLETE = 0x32; // 删除相同号码
	private static final int ASYNC_DELETE_ALL_COMPLETE = 0x33; // 删除全部

	private static final int DEFAULT_LOCAL_CALLLOG_COUNT = 30;


	private static final String TAG = CalllogService.class.getCanonicalName();

	private Context mContext;
	private DBopenHelper mDBopenHelper;
	private CalldaCalllogListener calldaCalllogListener;

	private int localCalllogCount = DEFAULT_LOCAL_CALLLOG_COUNT;

	public CalllogService(Context context,
			CalldaCalllogListener calldaCalllogListener) {
		if (mDBopenHelper == null) {
			mDBopenHelper = new DBopenHelper(context, DBopenHelper.BACK_CALL_DB_NAME,DBopenHelper.BACK_CALL_TABLE_NAME);
		}

		this.mContext = context;
		this.calldaCalllogListener = calldaCalllogListener;
	}

	final Handler mHandler = new Handler() {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			if (msg.what == ASYNC_QUERY_COMPLETE) {
				// 查询完毕
				if (calldaCalllogListener == null)
					throw new IllegalArgumentException(
							"calldaCalllogListener为空");

				calldaCalllogListener
						.onQueryCompleted((List<CalldaCalllogBean>) msg.obj);

			} else if (msg.what == ASYNC_DELETE_SINGLE_COMPLETE) {
				// 删除单条
				calldaCalllogListener.onDeleteCompleted();

			} else if (msg.what == ASYNC_DELETE_SAMENUMBER_COMPLETE) {
				// 删除相同号码
				calldaCalllogListener.onDeleteCompleted();

			} else if (msg.what == ASYNC_DELETE_ALL_COMPLETE) {
				// 删除全部通话记录
				calldaCalllogListener.onDeleteCompleted();

			}
		}
	};

	/**
	 * 设置查询的本地通话记录条数
	 * 
	 * @param calllogCount
	 */
	public void setQueryLocalCalllogCount(int calllogCount) {
		this.localCalllogCount = calllogCount;
	}

	/**
	 * 开启通话记录查询
	 */
	public void startQueryCallLog(final boolean hasBack) {
		final List<CalldaCalllogBean> totalCallLogBeans = new ArrayList<CalldaCalllogBean>();

		new Thread(new Runnable() {
			@Override
			public void run() {
				if(hasBack)
				totalCallLogBeans.addAll(queryBackCalllog());
				totalCallLogBeans.addAll(queryLocalCalllog());

				Collections.sort(totalCallLogBeans, new MyComparator());

				Message message = mHandler.obtainMessage();
				message.what = ASYNC_QUERY_COMPLETE;
				message.obj = totalCallLogBeans;
				mHandler.sendMessage(message);
			}
		}).start();
	}

	/**
	 * 删除单条通话记录
	 * 
	 * @param calllogBean
	 */
	public void deleteSingleCallLog(final CalllogDetailBean calllogBean) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				int callLogMIME = calllogBean.getCalllogBean().get(0).getCallLogMIME();
				switch (callLogMIME) {
				case CalldaCalllogBean.LOCAL_CALLLOG:
					// 本地
					mContext.getContentResolver()
							.delete(CallLog.Calls.CONTENT_URI,
									CallLog.Calls._ID + "=?",
									new String[] { String.valueOf(calllogBean
											.getCalllogBean().get(0).getId()) });
					break;
				
				case CalldaCalllogBean.BACK_CALLLOG:
					// 回拨
					synchronized (mDBopenHelper) {
						SQLiteDatabase database = mDBopenHelper
								.getWritableDatabase();
						database.delete(DBopenHelper.BACK_CALL_TABLE_NAME, "_id=?",
								new String[] { String.valueOf(calllogBean
										.getCalllogBean().get(0).getId()) });
						database.close();
					}
					break;
				default:
					break;
				}

				Message message = mHandler.obtainMessage();
				message.what = ASYNC_DELETE_SINGLE_COMPLETE;
				mHandler.sendMessage(message);
			}
		}).start();
	}

	/**
	 * 删除相同号码的通话记录
	 * 
	 * @param phoneNumber
	 *            号码
	 */
	public void deleteSomeCallLog(final String phoneNumber) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// 回拨
				synchronized (mDBopenHelper) {
					SQLiteDatabase database = mDBopenHelper
							.getWritableDatabase();
					database.delete(DBopenHelper.BACK_CALL_TABLE_NAME, "phoneNumber=?",
							new String[] { phoneNumber });
					database.close();
				}
				// 本地
				mContext.getContentResolver().delete(CallLog.Calls.CONTENT_URI,
						CallLog.Calls.NUMBER + "=?",
						new String[] { phoneNumber });
				
				Message message = mHandler.obtainMessage();
				message.what = ASYNC_DELETE_SAMENUMBER_COMPLETE;
				mHandler.sendMessage(message);
			}
		}).start();
	}

	/**
	 * 删除所有通话记录
	 */
	public void deleteAllCallLog() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// 回拨
				synchronized (mDBopenHelper) {
					SQLiteDatabase database = mDBopenHelper
							.getWritableDatabase();
					database.delete(DBopenHelper.BACK_CALL_TABLE_NAME, null, null);
					database.close();
				}
				// 本地
				mContext.getContentResolver().delete(CallLog.Calls.CONTENT_URI,
						null, null);

				Message message = mHandler.obtainMessage();
				message.what = ASYNC_DELETE_ALL_COMPLETE;
				mHandler.sendMessage(message);
			}
		}).start();
	}

	/**
	 * 获取本地手机通话记录
	 * 
	 * @return
	 */
	private List<CalldaCalllogBean> queryLocalCalllog() {
		List<CalldaCalllogBean> calldaCalllogBeans = new ArrayList<CalldaCalllogBean>();
		try {
			CalldaCalllogBean calldaCalllogBean;

			String[] selection = new String[] { CallLog.Calls.NUMBER,
					CallLog.Calls.CACHED_NAME, CallLog.Calls.TYPE,
					CallLog.Calls.DATE, CallLog.Calls.DURATION, CallLog.Calls._ID };

			Cursor cursor = mContext.getContentResolver().query(
					CallLog.Calls.CONTENT_URI, selection, null, null,
					CallLog.Calls.DATE + " DESC LIMIT 0," + localCalllogCount);

			while (cursor.moveToNext()) {
				int id = cursor.getInt(cursor.getColumnIndex(CallLog.Calls._ID));
				String name = cursor.getString(cursor
						.getColumnIndex(CallLog.Calls.CACHED_NAME));
				String number = cursor.getString(cursor
						.getColumnIndex(CallLog.Calls.NUMBER));
				int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
				long calltime = cursor.getLong(cursor
						.getColumnIndex(CallLog.Calls.DATE));
				long callduration = cursor.getLong(cursor
						.getColumnIndex(CallLog.Calls.DURATION));

				// 过滤未知号码
				if (TextUtils.isEmpty(number) || number.startsWith("-")
						|| number.length() < 3) {
					continue;
				}
                Logger.i("display_name",name+" ");
				calldaCalllogBean = new CalldaCalllogBean();
				calldaCalllogBean.setCallLogMIME(CalldaCalllogBean.LOCAL_CALLLOG);
				calldaCalllogBean.setId(id);
				calldaCalllogBean.setDisplayName(name);
				calldaCalllogBean.setCallLogNumber(number);
				calldaCalllogBean.setCallLogTime(calltime);
				calldaCalllogBean
						.setCalllogDuration(callduration);
				switch (type) {
				case Calls.INCOMING_TYPE:
					calldaCalllogBean
							.setCallLogType(CalldaCalllogBean.INCOMING_CALL);
					break;
				case Calls.OUTGOING_TYPE:
					calldaCalllogBean
							.setCallLogType(CalldaCalllogBean.OUTGOING_CALL);
					break;
				case Calls.MISSED_TYPE:
					calldaCalllogBean.setCallLogType(CalldaCalllogBean.MISSED_CALL);
					break;
				default:
					calldaCalllogBean.setCallLogType(CalldaCalllogBean.MISSED_CALL);
					break;
				}

				calldaCalllogBeans.add(calldaCalllogBean);
			}
			
			if(cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return calldaCalllogBeans;
	}

	/**
	 * 获取软件通话记录
	 * 
	 * @return
	 */

	/**
	 * 查询回拨通话记录
	 * 
	 * @return
	 */
	private List<CalldaCalllogBean> queryBackCalllog() {
		List<CalldaCalllogBean> calldaCalllogBeans = new ArrayList<CalldaCalllogBean>();
		CalldaCalllogBean calldaCalllogBean;

		synchronized (mDBopenHelper) {
			SQLiteDatabase database = mDBopenHelper.getReadableDatabase();
			Cursor cursor = database.query(DBopenHelper.BACK_CALL_TABLE_NAME, new String[] {
					"_id", "phoneNumber", "displayName", "TimeStamp" }, null,
					null, null, null, null);

			while (cursor.moveToNext()) {
				calldaCalllogBean = new CalldaCalllogBean();
				calldaCalllogBean
						.setCallLogMIME(CalldaCalllogBean.BACK_CALLLOG);
				calldaCalllogBean.setId(cursor.getInt(cursor
						.getColumnIndex("_id")));
				calldaCalllogBean.setCallLogNumber(cursor.getString(cursor
						.getColumnIndex("phoneNumber")));
				calldaCalllogBean.setDisplayName(cursor.getString(cursor
						.getColumnIndex("displayName")));
				calldaCalllogBean.setCallLogTime(Long.parseLong(cursor
						.getString(cursor.getColumnIndex("TimeStamp"))));
				calldaCalllogBean
						.setCallLogType(CalldaCalllogBean.OUTGOING_CALL);

				calldaCalllogBeans.add(calldaCalllogBean);
			}

			cursor.close();
			database.close();
		}

		return calldaCalllogBeans;
	}

	/**
	 * 格式化通话时长
	 * 
	 * @param callDuration
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	private String formatedCallDuration(long callDuration) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(callDuration);

		return simpleDateFormat.format(calendar.getTime());
	}

	/**
	 * 保存回拨呼叫记录
	 * 
	 * @param displayName
	 *            显示的姓名
	 * @param phoneNumber
	 *            呼叫的号码
	 */
	public void saveBackCallLog(String displayName, String phoneNumber) {
		if (TextUtils.isEmpty(phoneNumber)) {
			throw new IllegalArgumentException("电话号码为空");
		}

		long currentTime = Calendar.getInstance().getTimeInMillis();

		// insert into db
		ContentValues values = new ContentValues();
		values.put("phoneNumber", phoneNumber);
		values.put("displayName", displayName);
		values.put("TimeStamp", String.valueOf(currentTime));

		synchronized (mDBopenHelper) {
			SQLiteDatabase database = mDBopenHelper.getWritableDatabase();
			database.insert(DBopenHelper.BACK_CALL_TABLE_NAME, null, values);
			database.close();
		}
	}

	/**
	 * 通话记录排序规则
	 * 
	 * @author zhw
	 */
	class MyComparator implements Comparator<CalldaCalllogBean> {
		@Override
		public int compare(CalldaCalllogBean lhs, CalldaCalllogBean rhs) {
			if (lhs.getCallLogTime() > rhs.getCallLogTime()) {
				return -1;
			} else if (lhs.getCallLogTime() < rhs.getCallLogTime()) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	/**
	 * 通话记录监听器
	 * 
	 * @author zhw
	 */
	public interface CalldaCalllogListener {
		void onQueryCompleted(List<CalldaCalllogBean> calldaCalllogBeans);

		void onDeleteCompleted();
	}

	/**
	 * 查询相同号码的所有通讯记录
	 * 
	 * @param number
	 * @return
	 */
	public List<CalldaCalllogBean> QuerySameNumCalllog(
			List<CalldaCalllogBean> allBeans, String number) {
		List<CalldaCalllogBean> callLogBeans = new ArrayList<CalldaCalllogBean>();
		try {
			for (CalldaCalllogBean calldaCalllogBean : allBeans) {
				if (number.equals(calldaCalllogBean.getCallLogNumber())) {
					callLogBeans.add(calldaCalllogBean);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return callLogBeans;
	}

	/**
	 * 查询相同姓名的所有通讯记录
	 * 
	 * @param number
	 * @return
	 */
	public List<CalldaCalllogBean> QuerySameNameCalllog(
			List<CalldaCalllogBean> allBeans, String name) {
		List<CalldaCalllogBean> callLogBeans = new ArrayList<CalldaCalllogBean>();
		try {
			for (CalldaCalllogBean calldaCalllogBean : allBeans) {
				if (name.equals(calldaCalllogBean.getDisplayName())) {
					callLogBeans.add(calldaCalllogBean);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return callLogBeans;
	}

	/**
	 * 根据电话号码查询合并相邻的电话
	 * 
	 * @param allCalllogBeans
	 * @return
	 */
	public List<CalldaCalllogBean> QueryContinueCalllog(
			List<CalldaCalllogBean> allCalllogBeans) {
		// 新生成的list
		List<CalldaCalllogBean> myallCalllogBeans = new ArrayList<CalldaCalllogBean>();
		if (allCalllogBeans.size() <= 0) {
			return myallCalllogBeans;
		}
		CalldaCalllogBean currentBean = allCalllogBeans.get(0);// 当前作为对比基数的bean
		String callnumber = currentBean.getCallLogNumber();// 当前作为对比基数的bean的电话号码
		String callnumbernext = "";// 下一个bean的电话号码
		int number = 1;
		currentBean.setIndex(0);
		if (allCalllogBeans.size() == 1) {
			currentBean.setOccurrenceNumber(1);
			myallCalllogBeans.add(currentBean);
		} else if (allCalllogBeans.size() > 1) {

			for (int i = 1; i < allCalllogBeans.size(); i++) {
				callnumbernext = allCalllogBeans.get(i).getCallLogNumber();
				if (callnumber.equals(callnumbernext)) {
					number++;
					currentBean.setOccurrenceNumber(number);
				} else {
					currentBean.setOccurrenceNumber(number);
					myallCalllogBeans.add(currentBean);
					currentBean = allCalllogBeans.get(i);
					currentBean.setIndex(i);
					callnumber = currentBean.getCallLogNumber();
					number = 1;
				}
			}
		}
		// Logger.v("service 查询结果", myallCalllogBeans+"");
		return myallCalllogBeans;
	}

	/**
	 * 相邻号码的列表
	 * 
	 * @param allCalllogBeans
	 * @param bean
	 * @return
	 */
	public List<CalldaCalllogBean> QueryContinueCalllogDetail(
			List<CalldaCalllogBean> allCalllogBeans, CalldaCalllogBean bean) {
		int times = bean.getOccurrenceNumber();
		int index = bean.getIndex();
		// 新生成的list
		List<CalldaCalllogBean> myallCalllogBeans = new ArrayList<CalldaCalllogBean>();
		for (int i = index; i <= times + index - 1; i++) {
			myallCalllogBeans.add(allCalllogBeans.get(i));
		}
		Logger.v("索引", myallCalllogBeans + "\n位置：" + index);
		return myallCalllogBeans;
	}

	/**
	 * 根据电话号码查询合并一天中的相邻的电话
	 * 
	 * @param allCalllogBeans
	 * @return
	 */
	public List<CalllogDetailBean> QueryContinueDayCalllog(
			List<CalldaCalllogBean> allCalllogBeans) {
		TimeFormatUtil timeFormatUtil = new TimeFormatUtil();
		// 新生成的list
		List<CalllogDetailBean> mycalllogDetailBeans = new ArrayList<CalllogDetailBean>();
		CalllogDetailBean calllogDetailBean = new CalllogDetailBean();
		List<CalldaCalllogBean> calllogBeans = new ArrayList<CalldaCalllogBean>();//嵌套的list bean

		if (allCalllogBeans.size() <= 0) {
			return mycalllogDetailBeans;
		}
		CalldaCalllogBean currentBean = allCalllogBeans.get(0);// 第一条bean
		String callnumbernext = "";// 下一个bean的电话号码
		String date = timeFormatUtil
				.formatdayTime(currentBean.getCallLogTime());
		String datenext;
		int number = 1;
		//第一个数据插入
		calllogBeans.add(currentBean);
		calllogDetailBean.setCalllogBean(calllogBeans);
		calllogDetailBean.setCallLogNumber(currentBean.getCallLogNumber());
		calllogDetailBean.setDate(date);
		calllogDetailBean.setOccurrenceNumber(number);
		mycalllogDetailBeans.add(calllogDetailBean);
		
		if (allCalllogBeans.size() > 1) {
			boolean flag=true;
            //从第二个开始遍历
			for (int i = 1; i < allCalllogBeans.size(); i++) {
				flag=true;
				datenext = timeFormatUtil.formatdayTime(allCalllogBeans.get(i)
						.getCallLogTime());
				callnumbernext = allCalllogBeans.get(i).getCallLogNumber();
				
				Iterator<CalllogDetailBean> ite = mycalllogDetailBeans.iterator();
				while (ite.hasNext()) {
					CalllogDetailBean calllogDetailBeanNow = (CalllogDetailBean) ite
							.next();//当前的数据
					if (datenext.equals(calllogDetailBeanNow.getDate())
							&& callnumbernext.equals(calllogDetailBeanNow
									.getCallLogNumber())) {
						List<CalldaCalllogBean> havacalllogBeans = new ArrayList<CalldaCalllogBean>();
						havacalllogBeans=calllogDetailBeanNow.getCalllogBean();
						number=calllogDetailBeanNow.getOccurrenceNumber();
						number++;
						calllogDetailBeanNow.setOccurrenceNumber(number);
						havacalllogBeans.add(allCalllogBeans.get(i));
						calllogDetailBeanNow.setCalllogBean(havacalllogBeans);
						flag=false;
					}
				}
				if(flag){
					List<CalldaCalllogBean> nocalllogBeans = new ArrayList<CalldaCalllogBean>();
					CalllogDetailBean nocalllogDetailBean = new CalllogDetailBean();
					nocalllogBeans.add(allCalllogBeans.get(i));
					nocalllogDetailBean.setCalllogBean(nocalllogBeans);
					nocalllogDetailBean.setOccurrenceNumber(1);
					nocalllogDetailBean.setCallLogNumber(callnumbernext);
					nocalllogDetailBean.setDate(datenext);
					mycalllogDetailBeans.add(nocalllogDetailBean);
				}
			}
		}
		
		return mycalllogDetailBeans;
	}

	/**
	 * 相邻号码的列表
	 * 
	 * @param allCalllogBeans
	 * @param bean
	 * @return
	 */
	public List<CalldaCalllogBean> QueryContinueDayCalllogDetail(
			List<CalldaCalllogBean> allCalllogBeans, CalldaCalllogBean bean) {
		int times = bean.getOccurrenceNumber();
		int index = bean.getIndex();
		// 新生成的list
		List<CalldaCalllogBean> myallCalllogBeans = new ArrayList<CalldaCalllogBean>();
		for (int i = index; i <= times + index - 1; i++) {
			myallCalllogBeans.add(allCalllogBeans.get(i));
		}
		Logger.v("索引", myallCalllogBeans + "\n位置：" + index);
		return myallCalllogBeans;
	}
}
