package com.callba.phone.adapter;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.bean.Mood;
import com.callba.phone.util.StringUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by PC-20160514 on 2016/6/7.
 */
public class MoodViewHolder extends BaseViewHolder<Mood> {

    @InjectView(R.id.user_head)
    CircleImageView userHead;
    @InjectView(R.id.user_name)
    TextView userName;
    @InjectView(R.id.content)
    TextView content;
    @InjectView(R.id.images)
    RecyclerView images;
    @InjectView(R.id.time)
    TextView time;
    @InjectView(R.id.praise)
    TextView praise;
    @InjectView(R.id.comment)
    TextView comment;
    ImageAdapter imageAdapter;
    public MoodViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_mood);
        ButterKnife.inject(this,itemView);
    }

    @Override
    public void setData(Mood data) {
        images.setLayoutManager(new GridLayoutManager(getContext(),3));
        if(data.getImgUrls()!=null)
        {imageAdapter=new ImageAdapter(getContext());
        imageAdapter.addAll(data.getImgUrls().split(","));
        images.setAdapter(imageAdapter);
        content.setText(data.getContent());}
        if(data.getInTime()!=null)
        time.setHint(StringUtils.friendly_time(data.getInTime()));
    }
}
