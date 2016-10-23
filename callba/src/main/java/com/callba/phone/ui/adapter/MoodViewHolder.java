package com.callba.phone.ui.adapter;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.bean.Mood;
import com.callba.phone.util.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by PC-20160514 on 2016/6/7.
 */
public class MoodViewHolder extends BaseViewHolder<Mood> {

    @BindView(R.id.user_head)
    CircleImageView userHead;
    @BindView(R.id.user_name)
    TextView userName;
    @BindView(R.id.content)
    TextView content;
    @BindView(R.id.images)
    RecyclerView images;
    @BindView(R.id.time)
    TextView time;
    @BindView(R.id.praise)
    TextView praise;
    @BindView(R.id.comment)
    TextView comment;
    ImageAdapter imageAdapter;
    public MoodViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_mood);
        ButterKnife.bind(this,itemView);
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
