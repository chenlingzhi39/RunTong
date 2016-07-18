package com.callba.phone.adapter;

import android.view.ViewGroup;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.bean.Profit;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PC-20160514 on 2016/7/18.
 */
public class ProfitViewHolder extends BaseViewHolder<Profit> {
    @InjectView(R.id.money)
    TextView money;
    @InjectView(R.id.time)
    TextView time;

    public ProfitViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_profit);
        ButterKnife.inject(this,itemView);
    }

    @Override
    public void setData(Profit data) {
        money.setText(data.getMoney()+"元");
        time.setHint(data.getInTime());
    }
}
