package com.callba.phone.adapter;

import android.view.ViewGroup;
import android.widget.TextView;

import com.callba.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PC-20160514 on 2016/7/6.
 */
public class ContactNumberViewHolder extends BaseViewHolder<String> {

    @InjectView(R.id.tv_phonelist_number)
    TextView tvPhonelistNumber;

    public ContactNumberViewHolder(ViewGroup parent) {
        super(parent, R.layout.contact_detail_lv_item);
        ButterKnife.inject(this,itemView);
    }

    @Override
    public void setData(String data) {
        tvPhonelistNumber.setText(data);
    }
}
