package com.callba.phone.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseFragment;
import com.callba.phone.adapter.RecyclerArrayAdapter;
import com.callba.phone.adapter.TeamAdapter;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Team;
import com.callba.phone.widget.DividerItemDecoration;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PC-20160514 on 2016/7/15.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.fragment_order
)
public class TeamFragment extends BaseFragment {
    @InjectView(R.id.list)
    RecyclerView list;
    @InjectView(R.id.hint)
    TextView hint;
    ArrayList<Team> teams;
    TeamAdapter teamAdapter;

    @Override
    protected void initView(View fragmentRootView) {
        ButterKnife.inject(this,fragmentRootView);
        teamAdapter = new TeamAdapter(getActivity());
        teams = (ArrayList<Team>) getArguments().getParcelableArrayList("list").get(0);
        list.addItemDecoration(new DividerItemDecoration(
                getActivity(), DividerItemDecoration.VERTICAL_LIST));
            teamAdapter.addAll(teams);
            list.setAdapter(teamAdapter);
        if(teams.size()==0)
         hint.setVisibility(View.VISIBLE);
        teamAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

            }
        });
    }

    @Override
    protected void lazyLoad() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
