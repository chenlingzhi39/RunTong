package com.callba.phone.pinyin;

import com.callba.phone.bean.SeparatedEMGroup;

import java.util.Comparator;

/**
 * Created by PC-20160514 on 2016/10/20.
 */

public class GroupComparator implements Comparator<SeparatedEMGroup> {
    @Override
    public int compare(SeparatedEMGroup o1, SeparatedEMGroup o2) {
        return o1.getType().compareTo(o2.getType());
    }
}
