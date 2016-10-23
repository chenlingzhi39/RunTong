package com.callba.phone.service;

import android.os.Handler;
import android.text.TextUtils;

import com.callba.phone.bean.CalldaCalllogBean;
import com.callba.phone.bean.QuickQueryContactBean;
import com.callba.phone.cfg.Constant;
import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.logic.contact.ContactPersonEntity;
import com.callba.phone.util.Logger;
import com.callba.phone.util.PinYinUtil;
import com.callba.phone.util.QuickSearchContactFromComparator;
import com.callba.phone.util.QuickSearchContactIndexComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * 九宫格键盘快速检索联系人功能
 * 
 * @Author zhw
 * @Version V1.0
 * @Createtime：2014年5月13日 下午6:03:14
 */
public class NineKeyboardQuickSearch {
	protected static final String TAG = NineKeyboardQuickSearch.class
			.getCanonicalName();
	/**
	 * 存储检索的字母列表
	 */
	private static final Queue<String> searchKeyQueue = new LinkedList<String>();

	private Handler mHandler;

	// 检索线程运行标记
	private boolean isSearchThreadRun = false;
	// 记录是否第一次检索
	private boolean isFristSearchContact = true;

	// 检索线程
	private Thread mSearchThread;
	// 搜索监听器
	private NineKeyboardQuickSearchListener mSearchListener;

	// 通话记录列表
	private List<CalldaCalllogBean> mCalldaCalllogBeans;
	// 整合通话记录之后的联系人列表
	private List<QuickQueryContactBean> merageCalllogContacts;
	// 保存查询过的联系人列表
	private Map<String, List<QuickQueryContactBean>> searchedContactMap;
	// 最后查询到的联系人列表
	private List<QuickQueryContactBean> lastSearchedContact;
	// 保存查询过的字母组合列表
	private Map<String, List<String>> searchedLetterGroup;
	// 最后查询用到的字母组合
	private List<String> lastSearchedLetterGroup;

	/**
	 * 快速检索联系人监听器
	 * 
	 * @author zhw
	 */
	public interface NineKeyboardQuickSearchListener {
		/**
		 * 检索完毕
		 * 
		 * @param searchedContactBeanMap
		 *            检索到的联系人Map
		 */
		void onSearchCompleted(
				Map<String, List<QuickQueryContactBean>> searchedContactBeanMap);
	}

	public NineKeyboardQuickSearch(
			NineKeyboardQuickSearchListener searchListener) {
		mSearchListener = searchListener;
		mHandler = new Handler();
	}

	/**
	 * 开始检索
	 * 
	 * @param isFristQuery
	 *            是否第一次开始检索
	 */
	public void startQuery(List<CalldaCalllogBean> calldaCalllogBeans) {
		this.mCalldaCalllogBeans = calldaCalllogBeans;

		isFristSearchContact = true;
		isSearchThreadRun = true;
		mSearchThread = new Thread(new AsyncQueryRunnable());
		mSearchThread.start();

		Logger.i(TAG, "====== startQuery..");
	}

	/**
	 * 停止检索
	 */
	public void stopQuery() {
		isSearchThreadRun = false;
		synchronized (searchKeyQueue) {
			searchKeyQueue.notify();
		}
		mSearchThread = null;

		// 释放查询的联系人
		if (merageCalllogContacts != null) {
			merageCalllogContacts.clear();
			merageCalllogContacts = null;

			Logger.i(TAG, "Stop search_yue.. 释放merageCalllogContacts");
		}
		if (lastSearchedContact != null) {
			lastSearchedContact.clear();
			lastSearchedContact = null;

			Logger.i(TAG, "Stop search_yue.. 释放lastSearchedContact");
		}
		if (searchedContactMap != null) {
			searchedContactMap.clear();
			searchedContactMap = null;

			Logger.i(TAG, "Stop search_yue.. 释放searchedContactMap");
		}
		if (lastSearchedLetterGroup != null) {
			lastSearchedLetterGroup.clear();
			lastSearchedLetterGroup = null;

			Logger.i(TAG, "Stop search_yue.. 释放lastSearchLetterGroup");
		}
		if (searchedLetterGroup != null) {
			searchedLetterGroup.clear();
			searchedLetterGroup = null;

			Logger.i(TAG, "Stop search_yue.. 释放searchedLetterGroup");
		}
	}

	/**
	 * 设置检索的号码
	 * 
	 * @param inputNumber
	 */
	public void setSearchNumber(String inputNumber) {
		if (TextUtils.isEmpty(inputNumber)) {
			throw new IllegalArgumentException("检索的字母为空!");
		}

		Logger.d(TAG, "setSearchNumber -> " + inputNumber);
		synchronized (searchKeyQueue) {
			searchKeyQueue.offer(inputNumber);
			searchKeyQueue.notify();
		}
	}

	/**
	 * 自动裁剪设置检索的号码
	 * 
	 * @param pastedNumber
	 */
	public void setPastedSearchNumber(String pastedNumber) {
		if (TextUtils.isEmpty(pastedNumber)) {
			return;
		}

		for (int i = 1; i <= pastedNumber.length(); i++) {
			String tempNumber = pastedNumber.substring(0, i);
			synchronized (searchKeyQueue) {
				searchKeyQueue.offer(tempNumber);
				searchKeyQueue.notify();
			}
		}
	}

	class AsyncQueryRunnable implements Runnable {
		@Override
		public void run() {
			Logger.d(TAG, "NineKeyboardQuickSearch asyncQuery thread run...");

			while (isSearchThreadRun) {
				String currentSearchKey = null;
				synchronized (searchKeyQueue) {
					if (searchKeyQueue.size() > 0) {
						currentSearchKey = searchKeyQueue.poll();
					} else {
						try {
							Logger.d(TAG,
									"NineKeyboardQuickSearch asyncQuery thread wait...");
							searchKeyQueue.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}

				if (currentSearchKey != null) {
					Logger.d(TAG,
							"NineKeyboardQuickSearch asyncQuery thread current search_yue key is : "
									+ currentSearchKey);
					try {
						// when stop search_yue while searching is not completed may
						// case NullPointorException
						searchContactsByDialNumber(currentSearchKey);
					} catch (Exception e) {
						e.printStackTrace();
					}

					currentSearchKey = null;
				}

			}

			Logger.w(TAG, "NineKeyboardQuickSearch asyncQuery thread stop...");
		}
	}

	/**
	 * 根据拼音、数字异步快速检索联系人
	 */
	private void searchContactsByDialNumber(String currentSearchNumber) {
		List<QuickQueryContactBean> currSearchContactList = null; // 当前查找使用的好友列表
		List<String> currSearchPinYinGroup = null; // 当前的使用的拼音组合
		Logger.i(TAG, "===========  Start search_yue..");

		if (isFristSearchContact) {
			isFristSearchContact = false;
			Logger.i(TAG, "===========  first search_yue.. currentSearchNumber:"+currentSearchNumber);

			// 初始化合并的查询列表
			merageCalllogContacts = new ArrayList<QuickQueryContactBean>();

			// 整合通话记录
			if (mCalldaCalllogBeans != null) {
				for (CalldaCalllogBean calllogBean : mCalldaCalllogBeans) {
					boolean isExist = false;
					for (ContactPersonEntity bean : merageCalllogContacts) {
						if (bean.getPhoneNumber().equals(
								calllogBean.getCallLogNumber())) {
							isExist = true;
							break;
						}
					}

					if (!isExist) {
						QuickQueryContactBean contactBean = new QuickQueryContactBean();
						contactBean
								.setDisplayName(calllogBean.getDisplayName());
						contactBean.setPhoneNumber(calllogBean
								.getCallLogNumber());
						contactBean.setSearchSortKeyBean(calllogBean
								.getSearchSortKeyBean());
						contactBean
								.setQuickSearchFrom(QuickQueryContactBean.FROM_CALLLOG);

						merageCalllogContacts.add(contactBean);
					}

					if (merageCalllogContacts.size() >= Constant.NINEPAD_QUERY_CALLLOG_COUNT) {
						break;
					}
				}
			}

			// 整合通讯录数据
			if (GlobalConfig.getInstance().getContactBeans() != null) {
				for (ContactPersonEntity contactBean : GlobalConfig
						.getInstance().getContactBeans()) {
					boolean isExist = false;
					for (ContactPersonEntity bean : merageCalllogContacts) {
						if (bean.getPhoneNumber().equals(
								contactBean.getPhoneNumber())) {
							isExist = true;
							break;
						}
					}

					if (!isExist) {
						QuickQueryContactBean queryContactBean = new QuickQueryContactBean(
								contactBean);
						queryContactBean.setSearchSortKeyBean(contactBean
								.getSearchSortKeyBean());
						queryContactBean
								.setQuickSearchFrom(QuickQueryContactBean.FROM_LOCAL);
						merageCalllogContacts.add(queryContactBean);
					}
				}
			}

			// 最后一次查询到的联系人
			lastSearchedContact = new ArrayList<QuickQueryContactBean>();
			// 保存每次查询到的列表
			searchedContactMap = new HashMap<String, List<QuickQueryContactBean>>();
			// 最后一次使用的字母组合
			lastSearchedLetterGroup = new ArrayList<String>();
			// 保存历史使用过得字母组合
			searchedLetterGroup = new HashMap<String, List<String>>();

			// 设置首次查找的列表
			currSearchContactList = merageCalllogContacts;
			// 设置首次查找使用的拼音组合
			currSearchPinYinGroup = PinYinUtil.convertNumber2PinYinGroup(
					currentSearchNumber, null);

		} else {
			// 非首次查询

			// 按输入号码在map中查找
			currSearchContactList = searchedContactMap.get(currentSearchNumber);
			currSearchPinYinGroup = searchedLetterGroup
					.get(currentSearchNumber);

			// 按输入号码的少一位在map中查找
			if (currSearchContactList == null) {
				currSearchContactList = searchedContactMap
						.get(currentSearchNumber.substring(0,
								currentSearchNumber.length() - 1));
			}
			if (currSearchPinYinGroup == null) {
				currSearchPinYinGroup = searchedLetterGroup
						.get(currentSearchNumber.substring(0,
								currentSearchNumber.length() - 1));
				currSearchPinYinGroup = PinYinUtil.convertNumber2PinYinGroup(
						currentSearchNumber, currSearchPinYinGroup);
			}

			// 如果在搜索map中 匹配不到，则从整个合并的列表中开始查找
			if (currSearchContactList == null) {
				currSearchContactList = merageCalllogContacts;
			}
			if (currSearchPinYinGroup == null) {
				currSearchPinYinGroup = PinYinUtil
						.convertNumber2FullPinYinGroup(currentSearchNumber);
			}
		}

		lastSearchedContact.clear();
		lastSearchedLetterGroup.clear();

		if (currSearchContactList.isEmpty()) {
			// 当前查找列表为空,直接返回
			List<QuickQueryContactBean> contactBeans = new ArrayList<QuickQueryContactBean>();
			contactBeans.addAll(lastSearchedContact);
			searchedContactMap.put(currentSearchNumber, contactBeans);

			List<String> letterGroups = new ArrayList<String>();
			letterGroups.add("[~!@#$%^&*()]"); // 标记一种没有的拼音组合);
			searchedLetterGroup.put(currentSearchNumber, letterGroups);

			// 设置查询完毕的回调
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mSearchListener.onSearchCompleted(searchedContactMap);
				}
			});
			return;
		}

		for (QuickQueryContactBean bean : currSearchContactList) {
			if(bean.getSearchSortKeyBean()==null)
				continue;
			// 手机号码
			String phoneNum = bean.getPhoneNumber();
			// 姓名简称
			String simplePinYin = bean.getSearchSortKeyBean()
					.getShortPinYinString();
			// 姓名全拼
			String fullPinYin = bean.getSearchSortKeyBean()
					.getFullPinYinString();

			if (!phoneNum.contains(currentSearchNumber)) {
				// 检索拼音组合
				boolean isContactSearched = false;
				for (String str : currSearchPinYinGroup) {
					// 记录联系人是否已添加

					if (TextUtils.isEmpty(simplePinYin)) {
						continue;
					}

					if (simplePinYin.toLowerCase().contains(str)) { // 简拼
						// 添加该拼音组合
						boolean isLetterFlagExist = false;
						for (String str1 : lastSearchedLetterGroup) {
							if (str1.equals(str)) {
								isLetterFlagExist = true;
								break;
							}
						}
						if (!isLetterFlagExist) {
							lastSearchedLetterGroup.add(str);
						}

						if (!isContactSearched) {
							String formattedPinYinStr = bean
									.getSearchSortKeyBean()
									.getFormattedFullPinYinByShortPY(str);
							// String formattedNameStr =
							// bean.getSearchSortKeyBean().getFormattedChineseNameByShortPY(str);
							bean.setShowSortPinYin(formattedPinYinStr);
							// bean.setShowDisplayName(formattedNameStr);
							bean.setShowPhoneNumber(phoneNum);
							bean.setQuickSearchIndex(QuickQueryContactBean.SEARCH_BY_SHORT_PY);

							lastSearchedContact.add(bean);

							// 记录已添加
							isContactSearched = true;
						}

					} else if (fullPinYin.toLowerCase().contains(str)) { // 全拼
						// 添加该拼音组合
						boolean isLetterFlagExist = false;
						for (String str1 : lastSearchedLetterGroup) {
							if (str1.equals(str)) {
								isLetterFlagExist = true;
								break;
							}
						}
						if (!isLetterFlagExist) {
							lastSearchedLetterGroup.add(str);
						}

						if (!isContactSearched) {
							String formattedFullPinYin = bean
									.getSearchSortKeyBean()
									.getFormattedFullPinYinByFullPY(str);
							String formattedNameStr = bean
									.getSearchSortKeyBean()
									.getFormattedChineseNameByFullPY(str);

							bean.setShowSortPinYin(formattedFullPinYin);
							bean.setShowDisplayName(formattedNameStr);
							bean.setShowPhoneNumber(phoneNum);
							bean.setQuickSearchIndex(QuickQueryContactBean.SEARCH_BY_FULL_PY);

							lastSearchedContact.add(bean);

							isContactSearched = true;
						}
					}
				}

			} else {
				bean.setShowPhoneNumber(phoneNum.replaceFirst(
						currentSearchNumber, "<font color='#fa4407'>"
								+ currentSearchNumber + "</font>")); // #0ECCFE
				bean.setShowSortPinYin(fullPinYin);
				bean.setQuickSearchIndex(QuickQueryContactBean.SEARCH_BY_NUMBER);

				lastSearchedContact.add(bean);

			}
		}

		// 对查询结果排序
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		try {
			Collections.sort(lastSearchedContact,
					new QuickSearchContactFromComparator());
			Collections.sort(lastSearchedContact,
					new QuickSearchContactIndexComparator());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 将查找结果保存到map
		List<QuickQueryContactBean> contactBeans = new ArrayList<QuickQueryContactBean>();
		contactBeans.addAll(lastSearchedContact);
		searchedContactMap.put(currentSearchNumber, contactBeans);

		// 将最后使用的拼音组合保存到map
		List<String> letterGroups = new ArrayList<String>();
		if (lastSearchedLetterGroup.isEmpty()) {
			lastSearchedLetterGroup.add("[~!@#$%^&*()]"); // 标记一种没有的拼音组合
		}
		letterGroups.addAll(lastSearchedLetterGroup);
		searchedLetterGroup.put(currentSearchNumber, letterGroups);

		// 设置查询完毕的回调
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mSearchListener.onSearchCompleted(searchedContactMap);
			}
		});
		Logger.i(TAG, "===========  Search completed..");
	}
}
