package com.callba.phone.adapter;

import android.view.ViewGroup;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.bean.Help;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PC-20160514 on 2016/6/11.
 */
public class HelpViewHolder extends BaseViewHolder<Help> {
    @InjectView(R.id.ask)
    TextView ask;
    @InjectView(R.id.answer)
    TextView answer;

    public HelpViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_help);
        ButterKnife.inject(this,itemView);
    }

    @Override
    public void setData(Help data) {
    ask.setText(data.getAsk());
        answer.setHint(data.getAnswer());
    }
}
