package com.callba.phone.ui.adapter;

import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.callba.R;
import com.callba.phone.bean.NearByUser;
import com.callba.phone.util.StringUtils;

import butterknife.ButterKnife;
import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by PC-20160514 on 2016/5/24.
 */
public class NearByUserViewHolder extends BaseViewHolder<NearByUser> {
    @BindView(R.id.user_head)
    CircleImageView userHead;
    @BindView(R.id.user_name)
    TextView userName;
    @BindView(R.id.distance)
    TextView distance;
    @BindView(R.id.sign)
    TextView sign;
    @BindView(R.id.time)
    TextView time;

    public NearByUserViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_nearby_user);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void setData(NearByUser data) {
        userName.setText(data.getNickname());
        if (!data.getUrl_head().equals(""))
            Glide.with(getContext()).load(data.getUrl_head()).into(userHead);
        else
            userHead.setImageResource(R.drawable.ease_default_avatar);
        sign.setText(data.getSign());
        int away = Integer.parseInt(data.getDistance().substring(0, data.getDistance().lastIndexOf(".")));
        if (away < 1000)
            distance.setHint(away + "米以内");
        else distance.setHint((float) (Math.round(away / 10)) / 100 + "公里以内");
        time.setHint(StringUtils.friendly_time(data.getInTime()));
    }
}
