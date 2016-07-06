package com.callba.phone.adapter;

import android.view.ViewGroup;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.bean.CalldaCalllogBean;
import com.callba.phone.util.StringUtils;

import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Administrator on 2016/7/6.
 */
public class TimeViewHolder extends BaseViewHolder<CalldaCalllogBean> {
    @InjectView(R.id.time)
    TextView time;

    public TimeViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_time);
        ButterKnife.inject(this,itemView);
    }

    @Override
    public void setData(CalldaCalllogBean data) {
        time.setHint(StringUtils.friendly_time(new Date(data.getCallLogTime())));
    }
}
