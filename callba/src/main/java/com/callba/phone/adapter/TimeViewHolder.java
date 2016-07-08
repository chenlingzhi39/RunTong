package com.callba.phone.adapter;

import android.view.ViewGroup;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.bean.CalldaCalllogBean;
import com.callba.phone.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
        time.setHint(getTime(new Date(data.getCallLogTime())));
    }
    public String getTime(Date time){
        SimpleDateFormat dateFormat1=new SimpleDateFormat("MM-dd");
        SimpleDateFormat dateFormat2=new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        long lt = time.getTime()/86400000;
        long ct = cal.getTimeInMillis()/86400000;
        int days = (int)(ct - lt);
        if(days==0){
            return "今天";
        }
        else if(days == 1){
            return  "昨天";
        }
        else if(days == 2){
            return  "前天";
        }
        long ly=time.getTime()/(86400000*365);
        long cy=cal.getTimeInMillis()/(86400000*365);
        int years=(int)(cy - ly);
      if(years==0)
          return dateFormat1.format(time);
      else
        return dateFormat2.format(time);
    }
}
