package com.callba.phone.util.download;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;

import com.callba.phone.bean.AdvertisementBean;
import com.callba.phone.util.DBopenHelper;

public class AdvertisementUtil {
	
	private static final int ASYNC_QUERY_COMPLETE = 0x30; // 查询完毕
	private static final String TAG = AdvertisementUtil.class
			.getCanonicalName();

	private Context mContext;
	private DBopenHelper mDBopenHelper;
	private AdvertisementListener listener;

	public interface AdvertisementListener {
		void onQueryCompleted(List<AdvertisementBean> adBeans);
	}

	public AdvertisementUtil(Context mContext,
			AdvertisementListener adListener) {
		super();
		this.mContext = mContext;
		this.listener = adListener;
		if (mDBopenHelper == null) {
			mDBopenHelper = new DBopenHelper(mContext,
					DBopenHelper.BACK_CALL_DB_NAME,
					DBopenHelper.ADVERTISEMENT_TABLE_NAME);
		}
	}

	public void startQueryCallLog() {

		final List<AdvertisementBean> totaladBeans = new ArrayList<AdvertisementBean>();
		final List<AdvertisementBean> allcalllists = new ArrayList<AdvertisementBean>();

		new Thread(new Runnable() {
			@Override
			public void run() {
				totaladBeans.addAll(queryADs());
				// totalCallLogBeans.addAll(queryPushCalllog());
				allcalllists.addAll(totaladBeans);

				Message message = mHandler.obtainMessage();
				message.what = ASYNC_QUERY_COMPLETE;
				message.obj = allcalllists;
				mHandler.sendMessage(message);
			}
		}).start();

	}

	protected List<AdvertisementBean> queryADs() {
		List<AdvertisementBean> advertisementBeans = new ArrayList<AdvertisementBean>();
		AdvertisementBean advertisementBean;

		synchronized (mDBopenHelper) {
			SQLiteDatabase database = mDBopenHelper.getReadableDatabase();
			Cursor cursor = database.query(
					DBopenHelper.ADVERTISEMENT_TABLE_NAME, null, null, null,
					null, null, null);

			while (cursor.moveToNext()) {
				advertisementBean = new AdvertisementBean();
				advertisementBean.setId(cursor.getInt(cursor
						.getColumnIndex("_id")));
				advertisementBean.setIvName(cursor.getString(cursor
						.getColumnIndex("pictureName")));
				advertisementBean.setIvPath(cursor.getString(cursor
						.getColumnIndex("pictureUrl")));
				advertisementBean.setVideoName(cursor.getString(cursor
						.getColumnIndex("videoName")));
				advertisementBean.setVideoPath(cursor.getString(cursor
						.getColumnIndex("videoUrl")));

				// Logger.v("calllog -----time",timeFormatUtil.formatTime(calllogBean.getCallLogTime()));
				advertisementBeans.add(advertisementBean);
			}
			if (cursor != null) {
				cursor.close();
			}
			database.close();
		}

		return advertisementBeans;
	}

	@SuppressLint("HandlerLeak")
	final Handler mHandler = new Handler() {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			if (msg.what == ASYNC_QUERY_COMPLETE) {
				// 查询完毕
				if (listener == null)
					throw new IllegalArgumentException(
							"AdvertisementListener为空");
				listener.onQueryCompleted((List<AdvertisementBean>) msg.obj);

			}
		}
	};
	
	/**
	 * 保存回拨呼叫记录
	 * 
	 * @param displayName
	 *            显示的姓名
	 * @param phoneNumber
	 *            呼叫的号码
	 */
	public void saveAD(AdvertisementBean bean) {
		// insert into db
		ContentValues values = new ContentValues();
		values.put("pictureName", bean.getIvName());
		values.put("pictureUrl", bean.getIvPath());
		values.put("videoName", bean.getVideoName());
		values.put("videoUrl",bean.getVideoPath());

		synchronized (mDBopenHelper) {
			SQLiteDatabase database = mDBopenHelper.getWritableDatabase();
			database.insert(DBopenHelper.ADVERTISEMENT_TABLE_NAME, null, values);
			database.close();
		}
	}
}
