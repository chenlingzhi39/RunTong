package com.callba.phone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.adapter.MoodAdapter;
import com.callba.phone.adapter.RecyclerArrayAdapter;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Mood;
import com.callba.phone.bean.UserDao;
import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.manager.UserManager;
import com.callba.phone.util.Logger;
import com.callba.phone.widget.DividerItemDecoration;
import com.callba.phone.widget.refreshlayout.EasyRecyclerView;
import com.callba.phone.widget.refreshlayout.RefreshLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by PC-20160514 on 2016/5/25.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.community,
        toolbarTitle = R.string.community,
        navigationId = R.drawable.press_back,
        menuId = R.menu.menu_community
)
public class CommunityActivity extends BaseActivity implements UserDao.PostListener,RefreshLayout.OnRefreshListener,RecyclerArrayAdapter.OnLoadMoreListener{

    @InjectView(R.id.list)
    EasyRecyclerView moodList;
    UserDao userDao;
    int page,pageSize=30;
    private ArrayList<Mood> moods;
    private Gson gson;
    private String[] result;
    private MoodAdapter moodAdapter;
    private View headerView;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.camera:
                Intent intent = new Intent(CommunityActivity.this, PostActivity.class);
                startActivityForResult(intent, 0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.inject(this);
        initRefreshLayout();
        moodList.setLayoutManager(new LinearLayoutManager(this));
        moodList.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL_LIST));
        moodList.setFooterEnabled(false);
        gson=new Gson();
        userDao=new UserDao(this,this);
        moodAdapter=new MoodAdapter(this);
        headerView= getLayoutInflater().inflate(R.layout.header,null);
        moodAdapter.addHeader(new RecyclerArrayAdapter.ItemView() {
            @Override
            public View onCreateView(ViewGroup parent) {
                return headerView;
            }

            @Override
            public void onBindView(View headerView) {
                CircleImageView head=(CircleImageView) headerView.findViewById(R.id.head);
                TextView number=(TextView) headerView.findViewById(R.id.number);
                if(!UserManager.getUserAvatar(CommunityActivity.this).equals(""))
                Glide.with(CommunityActivity.this).load(UserManager.getUserAvatar(CommunityActivity.this)).into(head);
                number.setHint(getUsername());
            }
        });
        moodAdapter.setError(R.layout.view_more_error).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               moodAdapter.resumeMore();
            }
        });
        moodAdapter.setMore(R.layout.view_more, this);
        moodAdapter.setNoMore(R.layout.view_nomore);
        moodList.setAdapter(moodAdapter);
        moodList.showRecycler();
        userDao.getMoods(getUsername(),getPassword(),page+"",pageSize+"");
    }

    @Override
    public void onLoadMore() {
        userDao.getMoods(getUsername(), getPassword(),page+"",pageSize+"");
    }

    @Override
    public void onFooterRefresh() {

    }

    @Override
    public void onHeaderRefresh() {
        userDao.getMoods(getUsername(), getPassword(),"0",pageSize+"");
    }

    @Override
    public void failure(String msg) {
        moodAdapter.pauseMore();
        moodList.setHeaderRefreshing(false);
    }

    @Override
    public void start() {

    }

    @Override
    public void success(String msg) {
        moodList.setHeaderRefreshing(false);
        result=msg.split("\\|");
        if(result[0].equals("0"))
        {moods = new ArrayList<>();
            try {
                moods = gson.fromJson(result[1], new TypeToken<ArrayList<Mood>>() {
                }.getType());
            } catch (Exception e) {

            }
            Logger.i("size",moods.size()+"");
            if(moods.size()==0)
                moodAdapter.stopMore();
            if(moods.size()>0&&moodAdapter.getData().size()==0)
            {moodAdapter.addAll(moods);
                moodAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {

                    }
                });}
            if(moods.size()>0&&moodAdapter.getData().size()>0){
                moodAdapter.addAll(moods);
            }
            }else {
            moodAdapter.stopMore();
        }
    }
    public void initRefreshLayout() {
        moodList.setRefreshListener(this);
        moodList.setHeaderRefreshingColorResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        moodList.setFooterRefreshingColorResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 0) {
            userDao.getMoods(getUsername(), getPassword(),"0",pageSize+"");
        }
    }
}
