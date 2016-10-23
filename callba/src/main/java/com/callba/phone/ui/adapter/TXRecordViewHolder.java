package com.callba.phone.ui.adapter;

import android.view.ViewGroup;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.bean.TxRecord;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by PC-20160514 on 2016/7/18.
 */
public class TXRecordViewHolder extends BaseViewHolder<TxRecord> {
    private String[] arrays={"成功","失败","审核中"};
    @BindView(R.id.money)
    TextView money;
    @BindView(R.id.time)
    TextView time;
    @BindView(R.id.state)
    TextView state;
    public TXRecordViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_tx_record);
        ButterKnife.bind(this.itemView);
        money=(TextView)itemView.findViewById(R.id.money);
        time=(TextView)itemView.findViewById(R.id.time);
        state=(TextView)itemView.findViewById(R.id.state);
    }

    @Override
    public void setData(TxRecord data) {
        money.setText(data.getMoney()+"元");
        time.setHint(data.getInTime());
        state.setHint(arrays[Integer.parseInt(data.getState())]);
    }
}
