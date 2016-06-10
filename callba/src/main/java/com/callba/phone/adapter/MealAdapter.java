package com.callba.phone.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.callba.phone.bean.Meal;

/**
 * Created by PC-20160514 on 2016/6/10.
 */
public class MealAdapter extends RecyclerArrayAdapter<Meal>{
    public MealAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new MealViewHolder(parent);
    }
}
