package com.callba.phone.logic.contact;

import java.util.ArrayList;
import java.util.List;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.callba.phone.bean.SearchSortKeyBean;
import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.cfg.Constant;
import com.callba.phone.service.MainService;
import com.callba.phone.util.Logger;
import com.callba.phone.util.PinYinUtil;
import com.callba.phone.util.SharedPreferenceUtil;

/**
 * 查询系统联系人
 * @author Administrator
 */
public class QueryContacts {
	private Context mContext;
	private QueryContactCallback callback;
	
	public QueryContacts(QueryContactCallback queryContactCallback) {
		this.callback = queryContactCallback;
	}
	
	public synchronized void loadContact(Context context) {
		mContext = context;
		ContentResolver contentResolver = context.getContentResolver();
		
		final Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // 联系人的Uri
		final String[] projection = { ContactsContract.CommonDataKinds.Phone._ID,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.DATA1, "sort_key",
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
				ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
				ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY }; // 查询的列
		List<ContactPersonEntity> contactLists = new ArrayList<ContactPersonEntity>();
		final MyAsyncQueryHandler asyncQuery = new MyAsyncQueryHandler(contentResolver, contactLists);
		new Thread(new Runnable() {
			@Override
			public void run() {
				asyncQuery.startQuery(0, null, uri, projection, null, null,
						"sort_key COLLATE LOCALIZED asc"); // 按照sort_key升序查询
			}
		}).start();

	}
	
	/**
	 * 异步查询联系人
	 * @author Zhang
	 */
	private class MyAsyncQueryHandler extends AsyncQueryHandler {
		private List<ContactPersonEntity> contactLists;

		public MyAsyncQueryHandler(ContentResolver cr, List<ContactPersonEntity> contactLists) {
			super(cr);
			this.contactLists = contactLists;
		}

		/**
		 * 查询结束的回调函数
		 */
		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			if (cursor != null && cursor.getCount() > 0) {

				contactLists = new ArrayList<ContactPersonEntity>();
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					cursor.moveToPosition(i);

  			    //	String _id = cursor.getString(0);
					String name = cursor.getString(1);
					String number = cursor.getString(2);
//					String sortKey = cursor.getString(3);
 				    int contactId = cursor.getInt(4);
// 					Long photoId = cursor.getLong(5);
//					String lookUpKey = cursor.getString(6);
					if(TextUtils.isEmpty(number)) {
						continue;
					}
					Bitmap contactPhoto=null;
					ContactPersonEntity cb = new ContactPersonEntity();
					cb.set_id(contactId+"");
					cb.setDisplayName(name);
					if (number.startsWith("+86")) {// 去除多余的中国地区号码标志，对这个程序没有影响。
						cb.setPhoneNumber(number.substring(3));
					} else {
						cb.setPhoneNumber(number.replace(" ", "").replace("-",
								""));
					}

					if (name != null && !"".equals(name)) {
						
						SearchSortKeyBean searchSortKeyBean = PinYinUtil.converterPinYinToSearchBean(name.trim());
						//获取首字母英文
						String indexWorld = "";
						if(searchSortKeyBean.getShortPinYinArray() == null || searchSortKeyBean.getShortPinYinArray().isEmpty()) {
							indexWorld = "#";
						} else {
							indexWorld = String.valueOf(searchSortKeyBean.getShortPinYinArray().get(0));
							if(!Constant.CONTACT_SEARCH_INDEX.contains(indexWorld)) {
								indexWorld = "#";
							}
						}
						cb.setTypeName(indexWorld);
						
						cb.setSearchSortKeyBean(searchSortKeyBean);
						
					}
					contactLists.add(cb);
				}
				GlobalConfig.getInstance().setContactBeans(contactLists);
				Logger.i("contact_size",contactLists.size()+"");
				//保存本地联系人个数
				SharedPreferenceUtil mPreferenceUtil = SharedPreferenceUtil.getInstance(mContext);
				int contactsSize = contactLists.size(); 
				mPreferenceUtil.putString(Constant.CONTACTS_SIZE, String.valueOf(contactsSize), true);
				if (cursor!=null) {
					cursor.close();
				}
				if(callback != null) {
					callback.queryCompleted(contactLists);
				}
			}
		}
	}
}
