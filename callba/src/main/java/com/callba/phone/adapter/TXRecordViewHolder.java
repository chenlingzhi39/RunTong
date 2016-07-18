package com.callba.phone.adapter;

import android.view.ViewGroup;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.bean.TxRecord;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PC-20160514 on 2016/7/18.
 */
public class TXRecordViewHolder extends BaseViewHolder<TxRecord> {
    @InjectView(R.id.money)
    TextView money;
    @InjectView(R.id.time)
    TextView time;
    @InjectView(R.id.state)
    TextView state;
    private String[] arrays={"成功","失败","审核中"};
    public TXRecordViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_tx_record);
        ButterKnife.inject(this.itemView);
    }

    @Override
    public void setData(TxRecord data) {
        money.setText(data.getMoney()+"元");
        time.setHint(data.getInTime());
        state.setText(arrays[data.getState()]);
    }
}
