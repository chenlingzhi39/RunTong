package com.callba.phone.ui.adapter;

import android.graphics.Bitmap;
import android.view.ViewGroup;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.logic.contact.ContactPersonEntity;
import com.callba.phone.manager.ContactsManager;

import butterknife.ButterKnife;
import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by PC-20160514 on 2016/10/14.
 */

public class ContactViewHolder extends BaseViewHolder<ContactPersonEntity> {
    @BindView(R.id.avatar)
    CircleImageView avatar;
    @BindView(R.id.name)
    TextView name;

    public ContactViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_contact);
        ButterKnife.bind(this,itemView);
    }

    @Override
    public void setData(ContactPersonEntity data) {
        if(data.getDisplayName().equals(""))
           name.setText("未知");
        else name.setText(data.getDisplayName());
        Bitmap bitmap= ContactsManager.getAvatar(getContext(),data.get_id(),false);
        if(bitmap!=null)
           avatar.setImageBitmap(bitmap);
        else avatar.setImageResource(R.drawable.head_portrait);
    }
}
