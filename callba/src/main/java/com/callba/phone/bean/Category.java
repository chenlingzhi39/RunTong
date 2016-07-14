package com.callba.phone.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by PC-20160514 on 2016/7/14.
 */
public class Category<T> {
    private String mCategoryName;
    private List<T> mCategoryItem = new ArrayList<T>();

    public Category(String mCategroyName) {
        mCategoryName = mCategroyName;
    }

    public String getmCategoryName() {
        return mCategoryName;
    }

    public void addItem(T pItemName) {
        mCategoryItem.add(pItemName);
    }

    /**
     *  获取Item内容
     *
     * @param pPosition
     * @return
     */
    public Object getItem(int pPosition) {
        // Category排在第一位
        if (pPosition == 0) {
            return mCategoryName;
        } else {
            return mCategoryItem.get(pPosition - 1);
        }
    }

    /**
     * 当前类别Item总数。Category也需要占用一个Item
     * @return
     */
    public int getItemCount() {
        return mCategoryItem.size() + 1;
    }
}
