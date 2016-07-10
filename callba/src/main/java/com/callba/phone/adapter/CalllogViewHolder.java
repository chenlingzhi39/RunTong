package com.callba.phone.adapter;

import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.bean.CalldaCalllogBean;
import com.callba.phone.cfg.CalldaGlobalConfig;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Administrator on 2016/7/6.
 */
public class CalllogViewHolder extends BaseViewHolder<CalldaCalllogBean> {
    @InjectView(R.id.image_state)
    ImageView imageState;
    @InjectView(R.id.time)
    TextView time;
    @InjectView(R.id.state)
    TextView state;
    @InjectView(R.id.number)
    TextView number;

    public CalllogViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_calllog);
        ButterKnife.inject(this, itemView);
    }

    @Override
    public void setData(CalldaCalllogBean data) {
        number.setHint(data.getCallLogNumber()+"");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        time.setText(simpleDateFormat.format(new Date(data.getCallLogTime())));
        if(data.getCalllogDuration()==0)
            state.setHint("未接通");
        else{
            state.setHint(getTime(data.getCalllogDuration()));
        }
        switch (data.getCallLogType()) {
            case CalldaCalllogBean.INCOMING_CALL:
                imageState.setImageResource(R.drawable.ic_call_got_grey600_24dp);
                break;

            case CalldaCalllogBean.OUTGOING_CALL:
                imageState.setImageResource(R.drawable.ic_call_made_grey600_24dp);
                break;

            case CalldaCalllogBean.MISSED_CALL:
                imageState.setImageResource(R.drawable.ic_call_missed_grey600_24dp);
                break;
            default:
                break;
        }

    }
    public String getTime(long time){
        if(time<60)return time+"秒";
        else if(time<3600)return time/60+"分"+time%60+"秒";
        else if(time<3600*24)return time/3600+"小时"+(time%3600)/60+"分"+time%3600%60+"秒";
        return time+"秒";
    }
}
