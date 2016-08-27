package com.callba.phone.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.bumptech.glide.Glide;
import com.callba.R;
import com.callba.phone.MyApplication;
import com.callba.phone.bean.ApiService;
import com.callba.phone.manager.RetrofitManager;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.Constant;
import com.callba.phone.DemoHelper;
import com.callba.phone.ui.adapter.NearByUserAdapter;
import com.callba.phone.ui.adapter.RecyclerArrayAdapter;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Advertisement;
import com.callba.phone.bean.EaseUser;
import com.callba.phone.bean.NearByUser;
import com.callba.phone.bean.UserDao;
import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.manager.UserManager;
import com.callba.phone.util.EaseCommonUtils;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.util.SimpleHandler;
import com.callba.phone.view.AlwaysMarqueeTextView;
import com.callba.phone.view.BannerLayout;
import com.callba.phone.widget.EaseAlertDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hyphenate.chat.EMClient;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import okhttp3.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

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
    @InjectView(R.id.progressBar)
    ProgressBar progressBar;
    private BannerLayout banner;
    private UserDao userDao, userDao1, userDao2;
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
    private int page = 1;
    private boolean is_refresh = false;
    /*@Inject
    public ApiService apiService;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        //MyApplication.getApplicationComponent().inject(this);
        gson = new Gson();
        location.setTextColor(getResources().getColor(R.color.black_2f));
        location.setText(UserManager.getAddress(this));
        userDao = new UserDao(this, this);
        userList.setLoadingMoreEnabled(false);
        final View view = getLayoutInflater().inflate(R.layout.banner, null);
        final View view1 = getLayoutInflater().inflate(R.layout.ad, null);
        imageView = (ImageView) view1.findViewById(R.id.image);
        banner = (BannerLayout) view.findViewById(R.id.banner);
        for (int position = 1; position <= 3; position++)
            localImages.add(getResId("ad" + position, R.drawable.class));
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalConfig.getInstance().getAdvertisements2() != null && GlobalConfig.getInstance().getAdvertisements2().size() > 0) {
                    Intent intent1 = new Intent(Intent.ACTION_VIEW);
                    intent1.setData(Uri.parse(GlobalConfig.getInstance().getAdvertisements2().get(0).getAdurl()));
                    startActivity(intent1);
                }
            }
        });
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
                list = gson.fromJson(msg, new TypeToken<ArrayList<Advertisement>>() {
                }.getType());
                SimpleHandler.getInstance().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(getApplicationContext()).load(list.get(0).getImage()).into(imageView);
                    }
                }, 500);
                GlobalConfig.getInstance().setAdvertisements2(list);
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
        userDao2 = new UserDao();
        nearByUserAdapter = new NearByUserAdapter(this);
        nearByUserAdapter.setError(R.layout.view_more_error).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                is_refresh = false;
                nearByUserAdapter.resumeMore();
            }
        });
        nearByUserAdapter.setMore(R.layout.view_more, new RecyclerArrayAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                is_refresh = false;
                getNearBy(true);
                //userDao.getNearBy(getUsername(), getPassword(), UserManager.getLatitude(FriendActivity.this), UserManager.getLongitude(FriendActivity.this), 1000,page+1);
            }
        });
        nearByUserAdapter.setNoMore(R.layout.view_nomore);
        userList.addHeaderView(view1);
        footer = new View(this);
        userList.addFootView(footer);
        userList.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                //refresh data here
                is_refresh = true;
                getNearBy(false);
                //userDao.getNearBy(getUsername(), getPassword(), UserManager.getLatitude(FriendActivity.this), UserManager.getLongitude(FriendActivity.this), 1000,page);
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
        locationClient = new AMapLocationClient(this);
        locationOption = new AMapLocationClientOption();
        // 设置是否需要显示地址信息
        locationOption.setNeedAddress(true);
        /**
         * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位
         * 注意：只有在高精度模式下的单次定位有效，其他方式无效
         */
        locationOption.setGpsFirst(false);
        // 设置定位模式为高精度模式
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        // locationOption.setInterval(GlobalConfig.getInstance().getInterval());
        locationOption.setOnceLocation(true);
        locationClient.setLocationOption(locationOption);
        // 设置定位监听
        locationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                progressBar.setVisibility(View.GONE);
                location.setVisibility(View.VISIBLE);
                StringBuilder sb = new StringBuilder();
                if (aMapLocation.getErrorCode() == 0) {
                    Logger.i("address", aMapLocation.getAddress());
                    Logger.i("latitude", aMapLocation.getLatitude() + "");
                    Logger.i("longitude", aMapLocation.getLongitude() + "");
                    UserManager.putAddress(FriendActivity.this, aMapLocation.getAddress());
                    UserManager.putLatitude(FriendActivity.this, aMapLocation.getLatitude() + "");
                    UserManager.putLongitude(FriendActivity.this, aMapLocation.getLongitude() + "");
                    userDao2.saveLocation(getUsername(), getPassword(), aMapLocation.getLatitude(), aMapLocation.getLongitude());
                    location.setText(aMapLocation.getAddress());
                } else {
                    //定位失败
                    sb.append("定位失败" + "\n");
                    sb.append("错误码:" + aMapLocation.getErrorCode() + "\n");
                    sb.append("错误信息:" + aMapLocation.getErrorInfo() + "\n");
                    sb.append("错误描述:" + aMapLocation.getLocationDetail() + "\n");
                    Logger.i("error", sb.toString());
                    location.setText("网络错误，点击重试");
                }

            }
        });
        if (GlobalConfig.getInstance().getAdvertisements2() != null) {
            Logger.i("ad_image", GlobalConfig.getInstance().getAdvertisements2().get(0).getImage());
            SimpleHandler.getInstance().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Glide.with(FriendActivity.this).load(GlobalConfig.getInstance().getAdvertisements2().get(0).getImage()).into(imageView);
                }
            }, 500);
        } else
            userDao1.getAd(2, getUsername(), getPassword());

    }

    private void showDialog(final NearByUser entity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(entity.getNickname());
        builder.setItems(new String[]{getString(R.string.add_friend)},
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                if (EMClient.getInstance().getCurrentUser().equals(entity.getPhoneNumber() + "-callba")) {
                                    new EaseAlertDialog(FriendActivity.this, R.string.not_add_myself).show();
                                    return;
                                }

                                if (DemoHelper.getInstance().getContactList().containsKey(entity.getPhoneNumber() + "-callba")) {
                                    //提示已在好友列表中(在黑名单列表里)，无需添加
                                    if (EMClient.getInstance().contactManager().getBlackListUsernames().contains(entity.getPhoneNumber() + "-callba")) {
                                        new EaseAlertDialog(FriendActivity.this, R.string.user_already_in_contactlist).show();
                                        return;
                                    }
                                    new EaseAlertDialog(FriendActivity.this, R.string.This_user_is_already_your_friend).show();
                                    return;
                                }

                                final ProgressDialog progressDialog = new ProgressDialog(FriendActivity.this);
                                String stri = getResources().getString(R.string.Is_sending_a_request);
                                progressDialog.setMessage(stri);
                                progressDialog.setCanceledOnTouchOutside(false);
                                progressDialog.show();
                                OkHttpUtils
                                        .post()
                                        .url(Interfaces.ADD_FRIEND)
                                        .addParams("loginName", getUsername())
                                        .addParams("loginPwd", getPassword())
                                        .addParams("phoneNumber", entity.getPhoneNumber())
                                        .build()
                                        .execute(new StringCallback() {
                                            @Override
                                            public void onError(Call call, Exception e, int id) {
                                                e.printStackTrace();
                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressDialog.dismiss();
                                                        String s2 = getResources().getString(R.string.Request_add_buddy_failure);
                                                        Toast.makeText(getApplicationContext(), s2, 1).show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onResponse(String response, int id) {
                                                try {
                                                    Logger.i("add_result", response);
                                                    String[] result = response.split("\\|");
                                                    if (result[0].equals("0")) {
                                                        try {
                                                            //demo写死了个reason，实际应该让用户手动填入
                                                            String s = getResources().getString(R.string.Add_a_friend);
                                                            //EMClient.getInstance().contactManager().addContact(entity.getPhoneNumber()+"-callba", s);
                                                            sendBroadcast(new Intent(Constant.ACTION_CONTACT_CHANAGED));
                                                            EaseUser user = new EaseUser(entity.getPhoneNumber() + "-callba");
                                                            user.setAvatar(entity.getUrl_head());
                                                            user.setNick(entity.getNickname());
                                                            user.setSign(entity.getSign());
                                                            EaseCommonUtils.setUserInitialLetter(user);
                                                            DemoHelper.getInstance().saveContact(user);
                                                            //LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(Constant.ACTION_CONTACT_CHANAGED));
                                                            runOnUiThread(new Runnable() {
                                                                public void run() {
                                                                    progressDialog.dismiss();
                                                                    String s1 = "添加成功";
                                                                    Toast.makeText(getApplicationContext(), s1, 1).show();
                                                                }
                                                            });
                                                        } catch (final Exception e) {
                                                            runOnUiThread(new Runnable() {
                                                                public void run() {
                                                                    progressDialog.dismiss();
                                                                    String s2 = getResources().getString(R.string.Request_add_buddy_failure);
                                                                    Toast.makeText(getApplicationContext(), s2 + e.getMessage(), 1).show();
                                                                }
                                                            });
                                                        }
                                                    } else {
                                                        toast(result[1]);
                                                        progressDialog.dismiss();
                                                    }
                                                } catch (Exception e) {
                                                    toast(R.string.getserverdata_exception);
                                                }
                                            }
                                        });

                                break;
                            default:
                                break;
                        }
                    }
                });
        builder.create().show();
    }

    @Override
    public void failure(String msg) {
        userList.refreshComplete();
        toast(msg);
        if (!is_refresh)
            nearByUserAdapter.pauseMore();
    }

    @Override
    public void start() {

    }

    @Override
    public void success(String msg) {
        try {
            userList.refreshComplete();
            result = msg.split("\\|");
            Logger.i("friend_result", msg);
            if (result[0].equals("0")) {
                list = new ArrayList<>();
                try {
                    list = gson.fromJson(result[1], new TypeToken<ArrayList<NearByUser>>() {
                    }.getType());
                } catch (Exception e) {

                }
                Logger.i("size", list.size() + "");
                if (list.size() == 0) {
                } else {
                    if (is_refresh) {
                        nearByUserAdapter.clear();
                        nearByUserAdapter.addAll(list);
                        page = 1;
                        nearByUserAdapter.setOnItemLongClickListener(new RecyclerArrayAdapter.OnItemLongClickListener() {
                            @Override
                            public boolean onItemClick(int position) {
                                Logger.i("userlist", "longclick");
                                showDialog(nearByUserAdapter.getData().get(position - 2));
                                return true;
                            }
                        });
                    } else {
                        nearByUserAdapter.addAll(list);
                        page += 1;
                    }
                }
            } else {
                toast(result[1]);
                if (!is_refresh)
                    nearByUserAdapter.stopMore();
            }
        } catch (Exception e) {
            toast(R.string.getserverdata_exception);
            userList.refreshComplete();
            if (!is_refresh)
                nearByUserAdapter.pauseMore();
        }

    }


    @OnClick(R.id.location)
    public void onClick() {
        location.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        locationClient.startLocation();
    }

    public void getNearBy(final boolean is_next) {
        OkHttpUtils.post().url(Interfaces.GET_NEARBY)
                .addParams("loginName", getUsername())
                .addParams("loginPwd", getPassword())
                .addParams("latitude", UserManager.getLatitude(this))
                .addParams("longitude", UserManager.getLongitude(this))
                .addParams("radius", "1000")
                .addParams("page", is_next ? page + 1 + "" : page + "")
                .build().execute(new StringCallback() {
                                     @Override
                                     public void onError(Call call, Exception e, int id) {
                                         userList.refreshComplete();
                                         toast(R.string.network_error);
                                         if (!is_refresh)
                                             nearByUserAdapter.pauseMore();
                                     }

                                     @Override
                                     public void onResponse(String response, int id) {
                                         try {
                                             userList.refreshComplete();
                                             result = response.split("\\|");
                                             Logger.i("friend_result", response);
                                             if (result[0].equals("0")) {
                                                 list = new ArrayList<>();
                                                 try {
                                                     list = gson.fromJson(result[1], new TypeToken<ArrayList<NearByUser>>() {
                                                     }.getType());
                                                 } catch (Exception e) {

                                                 }
                                                 Logger.i("size", list.size() + "");
                                                 if (list.size() == 0) {
                                                 } else {
                                                     if (is_refresh) {
                                                         nearByUserAdapter.clear();
                                                         nearByUserAdapter.addAll(list);
                                                         page = 1;
                                                         nearByUserAdapter.setOnItemLongClickListener(new RecyclerArrayAdapter.OnItemLongClickListener() {
                                                             @Override
                                                             public boolean onItemClick(int position) {
                                                                 showDialog(nearByUserAdapter.getData().get(position - 2));
                                                                 return true;
                                                             }
                                                         });
                                                     } else {
                                                         nearByUserAdapter.addAll(list);
                                                         page += 1;
                                                     }
                                                 }
                                             } else {
                                                 toast(result[1]);
                                                 if (!is_refresh)
                                                     nearByUserAdapter.stopMore();
                                             }
                                         } catch (Exception e) {
                                             toast(R.string.getserverdata_exception);
                                             userList.refreshComplete();
                                             if (!is_refresh)
                                                 nearByUserAdapter.pauseMore();
                                         }

                                     }

                                 }
                );
     /*   subscription = apiService.getNearBy(getUsername(), getPassword(), UserManager.getLatitude(FriendActivity.this), UserManager.getLongitude(FriendActivity.this), "1000", is_next ? page + 1 + "" : page + "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.io())
                .subscribe(new Subscriber<String>() {

                    @Override
                    public void onCompleted() {
                        userList.refreshComplete();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        userList.refreshComplete();
                        toast(R.string.network_error);
                        if (!is_refresh)
                            nearByUserAdapter.pauseMore();
                    }

                    @Override
                    public void onNext(String s) {
                        try {
                            result = s.split("\\|");
                            Logger.i("friend_result", s);
                            if (result[0].equals("0")) {
                                list = new ArrayList<>();
                                try {
                                    list = gson.fromJson(result[1], new TypeToken<ArrayList<NearByUser>>() {
                                    }.getType());
                                } catch (Exception e) {

                                }
                                Logger.i("size", list.size() + "");
                                if (list.size() == 0) {
                                } else {
                                    if (is_refresh) {
                                        nearByUserAdapter.clear();
                                        nearByUserAdapter.addAll(list);
                                        page = 1;
                                        nearByUserAdapter.setOnItemLongClickListener(new RecyclerArrayAdapter.OnItemLongClickListener() {
                                            @Override
                                            public boolean onItemClick(int position) {
                                                showDialog(nearByUserAdapter.getData().get(position - 2));
                                                return true;
                                            }
                                        });
                                    } else {
                                        nearByUserAdapter.addAll(list);
                                        page += 1;
                                    }
                                }
                            } else {
                                toast(result[1]);
                                if (!is_refresh)
                                    nearByUserAdapter.stopMore();
                            }
                        } catch (Exception e) {
                            toast(R.string.getserverdata_exception);
                            userList.refreshComplete();
                            if (!is_refresh)
                                nearByUserAdapter.pauseMore();
                        }
                    }
                });*/
    }

}
