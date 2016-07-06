package com.callba.phone.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.callba.phone.bean.CalldaCalllogBean;

/**
 * Created by Administrator on 2016/7/6.
 */
public class CalllogAdapter extends RecyclerArrayAdapter<CalldaCalllogBean>{
    public CalllogAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case 1:return new TimeViewHolder(parent);
            case 0:return new CalllogViewHolder(parent);
        }
      return null;
    }

    @Override
    public int getViewType(int position) {
        CalldaCalllogBean bean=getItem(position);
      return bean.getIndex();

    }
}
