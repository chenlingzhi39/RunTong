package com.callba.phone.util;
import java.util.Comparator;

import com.callba.phone.bean.QuickQueryContactBean;

/**
 * 将快速查找联系人 按 <通话记录> <通讯录> 排序
 * @author zhw
 */
public class QuickSearchContactFromComparator implements Comparator<QuickQueryContactBean> {

	@Override
	public int compare(QuickQueryContactBean lhs, QuickQueryContactBean rhs) {
		if(lhs.getQuickSearchFrom() < rhs.getQuickSearchFrom()) {
			return -1;
		} else if(lhs.getQuickSearchFrom() > rhs.getQuickSearchFrom()) {
			
			return 1;
		} else {
			return 0;
		}
	}
	
}
