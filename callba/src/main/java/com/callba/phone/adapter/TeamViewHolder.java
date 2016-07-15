package com.callba.phone.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.callba.R;
import com.callba.phone.bean.Team;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PC-20160514 on 2016/7/15.
 */
public class TeamViewHolder extends BaseViewHolder<Team> {
    @InjectView(R.id.image)
    ImageView image;
    @InjectView(R.id.name)
    TextView name;
    @InjectView(R.id.time)
    TextView time;
    @InjectView(R.id.meal)
    TextView meal;

    public TeamViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_team);
        ButterKnife.inject(this,itemView);
    }

    @Override
    public void setData(Team data) {
        Glide.with(getContext()).load(data.getUrl_head()).placeholder(R.drawable.logo).into(image);
        name.setText(data.getNickname());
        time.setHint(data.getInTime());
        meal.setHint(data.getTitle());
        if(data.getTitle()==null)
            meal.setVisibility(View.GONE);
        else meal.setVisibility(View.VISIBLE);
    }
}
