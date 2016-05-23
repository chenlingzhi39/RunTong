package com.callba.phone.bean;

import com.callba.phone.logic.contact.ContactPersonEntity;

/**
 * 拨号键盘 快速查询联系人实体类
 * 
 * @author zhw
 */
public class QuickQueryContactBean extends ContactPersonEntity {
	private static final long serialVersionUID = 112L;
	
	public static final int FROM_CALLLOG = 0x10;
	public static final int FROM_LOCAL	 = 0x11;

	public static final int SEARCH_BY_SHORT_PY = 0x20;
	public static final int SEARCH_BY_FULL_PY	= 0x21;
	public static final int SEARCH_BY_NUMBER	= 0x22;

	public QuickQueryContactBean(){}
	
	public QuickQueryContactBean(ContactPersonEntity bean) {
		super.set_id(bean.get_id());
		super.setDisplayName(bean.getDisplayName());
		super.setPhoneNumber(bean.getPhoneNumber());
		super.setShowPhoneNumber(bean.getShowPhoneNumber());
		super.setShowSortPinYin(bean.getShowSortPinYin());
		super.setShowDisplayName(bean.getShowDisplayName());
		super.setSearchSortKeyBean(bean.getSearchSortKeyBean());
		super.setType(bean.getType());
		super.setTypeName(bean.getTypeName());
	}
	
	private int quickSearchFrom; // 联系人查找自
	private int quickSearchIndex;// 联系人搜索索引
	
	public int getQuickSearchFrom() {
		return quickSearchFrom;
	}
	
	public void setQuickSearchFrom(int quickSearchFrom) {
		this.quickSearchFrom = quickSearchFrom;
	}
	
	public int getQuickSearchIndex() {
		return quickSearchIndex;
	}
	
	public void setQuickSearchIndex(int quickSearchIndex) {
		this.quickSearchIndex = quickSearchIndex;
	}

	
}
