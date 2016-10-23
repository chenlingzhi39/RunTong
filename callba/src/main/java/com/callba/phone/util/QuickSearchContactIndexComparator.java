package com.callba.phone.util;

import com.callba.phone.bean.QuickQueryContactBean;

import java.util.Comparator;

/**
 * 将快速查找联系人 按 <简拼> <全拼> <号码> 排序
 * 
 * @author zhw
 */
public class QuickSearchContactIndexComparator implements
		Comparator<QuickQueryContactBean> {

	@Override
	public int compare(QuickQueryContactBean lhs, QuickQueryContactBean rhs) {
		if (lhs.getQuickSearchFrom() < rhs.getQuickSearchFrom()) {

			return -1;
		} else if (lhs.getQuickSearchFrom() > rhs.getQuickSearchFrom()) {

			return 1;
		} else {
			if (lhs.getQuickSearchIndex() < rhs.getQuickSearchIndex()) {

				return -1;
			} else if (lhs.getQuickSearchIndex() > rhs.getQuickSearchIndex()) {

				return 1;
			} else {

				return 0;
			}
		}
	}

}
