package com.callba.phone.ui.adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.callba.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by PC-20160514 on 2016/7/6.
 */
public class ContactNumberViewHolder extends BaseViewHolder<String> {

    @BindView(R.id.tv_phonelist_number)
    TextView tvPhonelistNumber;
    @BindView(R.id.send_message)
    Button sendMessage;


    public ContactNumberViewHolder(ViewGroup parent) {
        super(parent, R.layout.contact_detail_lv_item);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void setData(final String data) {
        tvPhonelistNumber.setText(data);
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri smsToUri = Uri.parse("smsto://" +data);
                Intent mIntent = new Intent(Intent.ACTION_SENDTO, smsToUri);
                getContext().startActivity(mIntent);
            }
        });
    }

}
