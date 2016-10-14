package com.callba.phone.pinyin;


import com.callba.phone.bean.ContactMultiNumBean;

import java.util.Comparator;

/**
 * 
 * @author xiaanming
 *
 */
public class PinyinComparator implements Comparator<ContactMultiNumBean> {

	public int compare(ContactMultiNumBean o1, ContactMultiNumBean o2) {
		if (o1.getTypeName().equals("@")
				|| o2.getTypeName().equals("#")) {
			return -1;
		} else if (o1.getTypeName().equals("#")
				|| o2.getTypeName().equals("@")) {
			return 1;
		} else {
			return o1.getTypeName().compareTo(o2.getTypeName());
		}
	}

}
