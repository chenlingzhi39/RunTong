package com.callba.phone.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.text.TextUtils;

import com.callba.R;
import com.callba.phone.bean.CalldaCalllogBean;
import com.callba.phone.bean.CalllogDetailBean;
import com.callba.phone.bean.SearchSortKeyBean;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.logic.contact.ContactPersonEntity;
import com.callba.phone.service.CalllogService;
import com.callba.phone.service.CalllogService.CalldaCalllogListener;

public class DataAnalysis {
	private TimeFormatUtil timeFormatUtil;
	public List<CalldaCalllogBean> allcalllists;
	private CalllogService calllogService;

	public List<? extends Map<String, ?>> getData(Context context,
			List<CalldaCalllogBean> allcalllists) {
		timeFormatUtil = new TimeFormatUtil();
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		Map<String, Object> map;
		String time="";
		Logger.i("size",allcalllists.size()+"");
		for (int i = 0; i < allcalllists.size(); i++) {
			map = new HashMap<String, Object>();
			Date date=new Date(allcalllists.get(i).getCallLogTime());
			SimpleDateFormat sf=new SimpleDateFormat("HH:mm");
			CalldaCalllogBean bean = allcalllists.get(i);
			// CallType
			switch (bean.getCallLogType()) {
			case CalldaCalllogBean.INCOMING_CALL:
				map.put("calltype", context.getString(R.string.callin));
				break;

			case CalldaCalllogBean.OUTGOING_CALL:
				map.put("calltype", context.getString(R.string.callout));
				break;

			case CalldaCalllogBean.MISSED_CALL:
				map.put("calltype", context.getString(R.string.callmiss));
				break;
			default:
				break;
			}
			// Call Name
			map.put("name", bean.getDisplayName());
			// Call PhoneNum
			map.put("phoneNum", bean.getCallLogNumber());
			// Call location
			map.put("phoneLocation", bean.getLocation());
			// Call time
			map.put("calltime",
					sf.format(date));

			data.add(map);
		}
		return data;
	}

	public List<Map<String, Object>> mygetData(Context context,
			List<CalllogDetailBean> allcalllists) {
		timeFormatUtil = new TimeFormatUtil();
		// List<CalldaCalllogBean> insideCalllogBean=new
		// ArrayList<CalldaCalllogBean>();
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		Map<String, Object> map;
		for (int i = 0; i < allcalllists.size(); i++) {
			map = new HashMap<String, Object>();
			List<CalldaCalllogBean> insideCalllogBean = allcalllists.get(i)
					.getCalllogBean();
			CalldaCalllogBean bean = insideCalllogBean.get(0);
			// CallType
			switch (bean.getCallLogType()) {
			case CalldaCalllogBean.INCOMING_CALL:
				map.put("calltype", R.drawable.ic_call_got_grey600_24dp);
				break;

			case CalldaCalllogBean.OUTGOING_CALL:
				map.put("calltype", R.drawable.ic_call_made_grey600_24dp);
				break;

			case CalldaCalllogBean.MISSED_CALL:
				map.put("calltype", R.drawable.ic_call_missed_grey600_24dp);
				break;
			default:
				break;
			}
			// Call Name
			map.put("name", bean.getDisplayName());
			// Call PhoneNum
			map.put("phoneNum", bean.getCallLogNumber());
			// Call location
			map.put("phoneLocation", bean.getLocation());
			// Call time
			map.put("calltime",
					timeFormatUtil.formatTime(bean.getCallLogTime()));
			// 连续出现的次数
			map.put("occurrencenumber", allcalllists.get(i)
					.getOccurrenceNumber());

			data.add(map);
		}
		return data;
	}

	/**
	 * 通话记录查询监听器
	 * 
	 * @author Administrator
	 */
	class Listenrer implements CalldaCalllogListener {

		@Override
		public void onQueryCompleted(List<CalldaCalllogBean> calldaCalllogBeans) {
			allcalllists.clear();
			if (0 < calldaCalllogBeans.size()) {
				for (CalldaCalllogBean bean : calldaCalllogBeans) {
					if (TextUtils.isEmpty(bean.getDisplayName())) {
						String tempPhoneNumber = bean.getCallLogNumber();
						if (!"".equals(tempPhoneNumber)
								&& tempPhoneNumber.length() >= 11
								&& tempPhoneNumber.startsWith("86")) {
							tempPhoneNumber = tempPhoneNumber.substring(2);
						}
						// bean.setDisplayName("未知");
						bean.setDisplayName(tempPhoneNumber);
						if (CalldaGlobalConfig.getInstance().getContactBeans() != null) {
							for (ContactPersonEntity contactBean : CalldaGlobalConfig
									.getInstance().getContactBeans()) {
								if (contactBean.getPhoneNumber().equals(
										tempPhoneNumber)) {
									// 通讯录中存在该记录
									bean.setDisplayName(contactBean
											.getDisplayName());

									bean.setSearchSortKeyBean(contactBean
											.getSearchSortKeyBean());
									break;
								}
							}
						}
					}

					if (bean.getSearchSortKeyBean() == null) {
						if (CalldaGlobalConfig.getInstance().getContactBeans() != null) {
							for (ContactPersonEntity contactBean : CalldaGlobalConfig
									.getInstance().getContactBeans()) {
								if (contactBean.getPhoneNumber().equals(
										bean.getCallLogNumber())) {

									bean.setSearchSortKeyBean(contactBean
											.getSearchSortKeyBean());
									break;
								}
							}
						}
					}
					// 如果非联系人 以上均过滤不到 则设个默认值
					if (bean.getSearchSortKeyBean() == null) {
						bean.setSearchSortKeyBean(new SearchSortKeyBean());
					}
				}
				allcalllists.addAll(calldaCalllogBeans);
			}
		}

		@Override
		public void onDeleteCompleted() {
			calllogService.startQueryCallLog();
		}
	}
}
