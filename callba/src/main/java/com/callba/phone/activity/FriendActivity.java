package com.callba.phone.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.activity.contact.ContactDetailActivity;
import com.callba.phone.adapter.NearByUserAdapter;
import com.callba.phone.adapter.RecyclerArrayAdapter;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Advertisement;
import com.callba.phone.bean.NearByUser;
import com.callba.phone.bean.UserDao;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.view.AlwaysMarqueeTextView;
import com.callba.phone.view.BannerLayout;
import com.callba.phone.widget.DividerItemDecoration;
import com.callba.phone.widget.refreshlayout.EasyRecyclerView;
import com.callba.phone.widget.refreshlayout.RefreshLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.umeng.socialize.utils.Log;


import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PC-20160514 on 2016/5/21.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.friend,
        toolbarTitle = R.string.friend,
        navigationId = R.drawable.press_back
)
public class FriendActivity extends BaseActivity implements UserDao.PostListener,RefreshLayout.OnRefreshListener {
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.mToolbarContainer)
    AppBarLayout mToolbarContainer;
    @InjectView(R.id.location)
    AlwaysMarqueeTextView location;
    @InjectView(R.id.list)
    EasyRecyclerView userList;
    private BannerLayout banner;
    private UserDao userDao;
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    private NearByUserAdapter nearByUserAdapter;
    private Gson gson;
    List<NearByUser> list;
    private String[] result;
    private ArrayList<Integer> localImages = new ArrayList<Integer>();
    private ArrayList<String> webImages=new ArrayList<>();
    List<Advertisement> advertisements;
    ADReceiver receiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        gson = new Gson();
        location.setTextColor(getResources().getColor(R.color.black_2f));
        location.setText(CalldaGlobalConfig.getInstance().getAddress());
        userDao=new UserDao(this,this);
        userDao.getNearBy(CalldaGlobalConfig.getInstance().getUsername(),CalldaGlobalConfig.getInstance().getPassword(),CalldaGlobalConfig.getInstance().getLatitude(),CalldaGlobalConfig.getInstance().getLongitude(),100000);
        initRefreshLayout();
        userList.setRefreshEnabled(true);
        userList.setFooterEnabled(false);
        userList.setLayoutManager(new LinearLayoutManager(this));
        userList.getEmptyView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userDao.getNearBy(CalldaGlobalConfig.getInstance().getUsername(),CalldaGlobalConfig.getInstance().getPassword(),CalldaGlobalConfig.getInstance().getLatitude(),CalldaGlobalConfig.getInstance().getLongitude(),100000);
                userList.showProgress();
            }
        });
        userList.getErrorView().setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.i("error","click");
                userDao.getNearBy(CalldaGlobalConfig.getInstance().getUsername(),CalldaGlobalConfig.getInstance().getPassword(),CalldaGlobalConfig.getInstance().getLatitude(),CalldaGlobalConfig.getInstance().getLongitude(),100000);
                userList.showProgress();
            }
        });
        final View view=getLayoutInflater().inflate(R.layout.banner,null);
        final View view1=getLayoutInflater().inflate(R.layout.ad,null);
        banner=(BannerLayout) view.findViewById(R.id.banner);
        for (int position = 1; position <= 3; position++)
            localImages.add(getResId("ad" + position, R.drawable.class));

        banner.setViewRes(localImages);
        if(CalldaGlobalConfig.getInstance().getAdvertisements()!=null)
            initAdvertisement();
        nearByUserAdapter=new NearByUserAdapter(this);
        nearByUserAdapter.addHeader(new RecyclerArrayAdapter.ItemView() {
            @Override
            public View onCreateView(ViewGroup parent) {
                return view1;
            }

            @Override
            public void onBindView(View headerView) {

            }
        });
        userList.showProgress();

        userList.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL_LIST));
        IntentFilter filter = new IntentFilter(
                "com.callba.getad");
        receiver = new ADReceiver();
        registerReceiver(receiver, filter);
    }

    @Override
    public void onHeaderRefresh() {
        nearByUserAdapter.clear();
        userDao.getNearBy(CalldaGlobalConfig.getInstance().getUsername(),CalldaGlobalConfig.getInstance().getPassword(),CalldaGlobalConfig.getInstance().getLatitude(),CalldaGlobalConfig.getInstance().getLongitude(),100000);
    }

    @Override
    public void onFooterRefresh() {

    }

    @Override
    public void failure(String msg) {
            userList.showError();
        userList.setHeaderRefreshing(false);
    }

    @Override
    public void start() {

    }
    @Override
    public void success(String msg) {
        userList.setHeaderRefreshing(false);
        result=msg.split("\\|");
        Log.i("friend_result",msg);
        if(result[0].equals("0"))
        { list = new ArrayList<>();
        try {
            list = gson.fromJson(result[1], new TypeToken<List<NearByUser>>() {
            }.getType());
        } catch (Exception e) {

        }
            Log.i("size",list.size()+"");
            if(list.size()==0)
        userList.showEmpty();
        else{
        nearByUserAdapter.addAll(list);
        userList.setAdapter(nearByUserAdapter);
        userList.showRecycler();
            nearByUserAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    Intent intent=new Intent(FriendActivity.this, ContactDetailActivity.class);

                }
            });
            }}else userList.showEmpty();
    }

    @Override
    public void init() {

    }

    @Override
    public void refresh(Object... params) {

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    public void initRefreshLayout() {
        userList.setRefreshListener(this);
        userList.setHeaderRefreshingColorResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }
    class ADReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("com.callba.getad"))
                initAdvertisement();
        }
    }
    public void initAdvertisement(){
        advertisements=CalldaGlobalConfig.getInstance().getAdvertisements();
        for(Advertisement advertisement : advertisements){
            webImages.add(advertisement.getImage());
        }
        banner.setViewUrls(webImages);
        banner.setOnBannerItemClickListener(new BannerLayout.OnBannerItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent1 = new Intent(Intent.ACTION_VIEW);
                intent1.setData(Uri.parse(advertisements.get(position).getAdurl()));
                startActivity(intent1);
            }
        });
    }
}
