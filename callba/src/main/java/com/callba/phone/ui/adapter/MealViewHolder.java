package com.callba.phone.ui.adapter;

import android.view.ViewGroup;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.bean.Meal;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by PC-20160514 on 2016/6/10.
 */
public class MealViewHolder extends BaseViewHolder<Meal> {
    @BindView(R.id.meal_name)
    TextView mealName;
    @BindView(R.id.meal_time)
    TextView mealTime;
    @BindView(R.id.rest_time)
    TextView restTime;
    @BindView(R.id.max_time)
    TextView maxTime;

    public MealViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_meal);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void setData(Meal data) {
        mealName.setText("套餐名称："+data.getName());
        mealTime.setText("到期时间："+data.getTime());
        maxTime.setText("每月上限："+data.getMax());
        restTime.setText("剩余时间："+data.getRest());
    }
}
