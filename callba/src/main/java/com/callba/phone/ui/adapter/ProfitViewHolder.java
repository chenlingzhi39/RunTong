package com.callba.phone.ui.adapter;

import android.view.ViewGroup;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.bean.Profit;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by PC-20160514 on 2016/7/18.
 */
public class ProfitViewHolder extends BaseViewHolder<Profit> {
    @BindView(R.id.money)
    TextView money;
    @BindView(R.id.time)
    TextView time;

    public ProfitViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_profit);
        ButterKnife.bind(this,itemView);
    }

    @Override
    public void setData(Profit data) {
        money.setText(data.getMoney()+"元");
        time.setHint(data.getInTime());
    }
}
