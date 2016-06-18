package com.callba.phone.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.bumptech.glide.Glide;
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
import com.callba.phone.util.Logger;
import com.callba.phone.view.AlwaysMarqueeTextView;
import com.callba.phone.view.BannerLayout;
import com.callba.phone.widget.DividerItemDecoration;
import com.callba.phone.widget.refreshlayout.RefreshLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.umeng.socialize.utils.Log;

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
public class FriendActivity extends BaseActivity implements UserDao.PostListener {
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.mToolbarContainer)
    AppBarLayout mToolbarContainer;
    @InjectView(R.id.location)
    AlwaysMarqueeTextView location;
    @InjectView(R.id.list)
    XRecyclerView userList;
    private BannerLayout banner;
    private UserDao userDao, userDao1;
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    private NearByUserAdapter nearByUserAdapter;
    private Gson gson;
    List<NearByUser> list;
    private String[] result;
    private ArrayList<Integer> localImages = new ArrayList<Integer>();
    private ArrayList<String> webImages = new ArrayList<>();
    private ImageView imageView;
    private View footer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        gson = new Gson();
        location.setTextColor(getResources().getColor(R.color.black_2f));
        location.setText(CalldaGlobalConfig.getInstance().getAddress());
        userDao = new UserDao(this, this);

        initRefreshLayout();
        userList.setLayoutManager(new LinearLayoutManager(this));
        userList.setLoadingMoreEnabled(false);
        final View view = getLayoutInflater().inflate(R.layout.banner, null);
        final View view1 = getLayoutInflater().inflate(R.layout.ad, null);
        imageView=(ImageView) view1.findViewById(R.id.image);
        banner = (BannerLayout) view.findViewById(R.id.banner);
        for (int position = 1; position <= 3; position++)
            localImages.add(getResId("ad" + position, R.drawable.class));

        banner.setViewRes(localImages);
        userDao1 = new UserDao(this, new UserDao.PostListener() {
            @Override
            public void failure(String msg) {
                toast(msg);
            }

            @Override
            public void start() {

            }

            @Override
            public void success(String msg) {
                final ArrayList<Advertisement> list;
                list = gson.fromJson(msg, new TypeToken<List<Advertisement>>() {
                }.getType());
                Glide.with(FriendActivity.this).load(list.get(0).getImage()).into(imageView);
               CalldaGlobalConfig.getInstance().setAdvertisements2(list);
              /*  for (Advertisement advertisement : list) {
                    webImages.add(advertisement.getImage());
                }
                banner.setViewUrls(webImages);
                banner.setOnBannerItemClickListener(new BannerLayout.OnBannerItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        Intent intent1 = new Intent(Intent.ACTION_VIEW);
                        intent1.setData(Uri.parse(list.get(position).getAdurl()));
                        startActivity(intent1);
                    }
                });*/
            }
        });


       /* nearByUserAdapter.addHeader(new RecyclerArrayAdapter.ItemView() {
            @Override
            public View onCreateView(ViewGroup parent) {
                return view1;
            }

            @Override
            public void onBindView(View headerView) {

            }
        });*/
        nearByUserAdapter = new NearByUserAdapter(this);
        userList.addHeaderView(view1);
        footer=new View(this);
        userList.addFootView(footer);
       userList.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                //refresh data here
                userDao.getNearBy(CalldaGlobalConfig.getInstance().getUsername(), CalldaGlobalConfig.getInstance().getPassword(), CalldaGlobalConfig.getInstance().getLatitude(), CalldaGlobalConfig.getInstance().getLongitude(), 100000);
            }

            @Override
            public void onLoadMore() {
                // load more data here
            }
        });
        userList.setAdapter(new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return null;
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            }

            @Override
            public int getItemCount() {
                return 0;
            }
        });
        userList.setAdapter(nearByUserAdapter);
        userList.setRefreshing(true);
    }



    @Override
    public void failure(String msg) {
        userList.refreshComplete();
        toast(msg);
    }

    @Override
    public void start() {

    }

    @Override
    public void success(String msg) {
        userList.refreshComplete();
        result = msg.split("\\|");
        Log.i("friend_result", msg);
        if (result[0].equals("0")) {
            list = new ArrayList<>();
            try {
                list = gson.fromJson(result[1], new TypeToken<List<NearByUser>>() {
                }.getType());
            } catch (Exception e) {

            }
            Log.i("size", list.size() + "");
            if (list.size() == 0) {
            } else {
               nearByUserAdapter.clear();
                nearByUserAdapter.addAll(list);
                nearByUserAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        Intent intent = new Intent(FriendActivity.this, ContactDetailActivity.class);

                    }
                });
            }
        } else {
        }
        ;
    }

    @Override
    protected void onResume() {
        if(CalldaGlobalConfig.getInstance().getAdvertisements2()!=null)
        {Logger.i("ad_image",CalldaGlobalConfig.getInstance().getAdvertisements2().get(0).getImage());
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    Glide.with(FriendActivity.this).load(CalldaGlobalConfig.getInstance().getAdvertisements2().get(0).getImage()).into(imageView);
                }
            });

        }
        else  userDao1.getAd(2, CalldaGlobalConfig.getInstance().getUsername(), CalldaGlobalConfig.getInstance().getPassword());
        super.onResume();
    }

    @Override
    public void init() {

    }

    @Override
    public void refresh(Object... params) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void initRefreshLayout() {

    }

}
