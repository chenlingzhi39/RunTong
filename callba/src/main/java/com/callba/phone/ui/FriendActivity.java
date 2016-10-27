package com.callba.phone.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.callba.phone.Constant;
import com.callba.phone.DemoHelper;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Advertisement;
import com.callba.phone.bean.EaseUser;
import com.callba.phone.bean.NearByUser;
import com.callba.phone.manager.UserManager;
import com.callba.phone.ui.adapter.NearByUserAdapter;
import com.callba.phone.ui.adapter.RecyclerArrayAdapter;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.util.EaseCommonUtils;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.util.SimpleHandler;
import com.callba.phone.view.AlwaysMarqueeTextView;
import com.callba.phone.widget.EaseAlertDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hyphenate.chat.EMClient;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;

/**
 * Created by PC-20160514 on 2016/5/21.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.friend,
        toolbarTitle = R.string.friend,
        navigationId = R.drawable.press_back
)
public class FriendActivity extends BaseActivity {
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.mToolbarContainer)
    AppBarLayout mToolbarContainer;
    @BindView(R.id.location)
    AlwaysMarqueeTextView location;
    @BindView(R.id.list)
    RecyclerView userList;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    private NearByUserAdapter nearByUserAdapter;
    private Gson gson;
    List<NearByUser> list;
    private String[] result;
    private int page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        gson = new Gson();
        list = new ArrayList<>();
        location.setTextColor(getResources().getColor(R.color.black_2f));
        location.setSelected(true);
        Logger.i("address", UserManager.getAddress(this));
        location.setText(UserManager.getAddress(this));
        nearByUserAdapter = new NearByUserAdapter(this);
        nearByUserAdapter.setError(R.layout.view_more_error).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nearByUserAdapter.resumeMore();
            }
        });
        nearByUserAdapter.setMore(R.layout.view_more, new RecyclerArrayAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                getNearBy();
            }
        });
        nearByUserAdapter.setNoMore(R.layout.view_nomore);
        refreshLayout.setColorSchemeResources(R.color.holo_blue_bright, R.color.holo_green_light,
                R.color.holo_orange_light, R.color.holo_red_light);
        //下拉刷新
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                nearByUserAdapter.clear();
                page=1;
                getNearBy();
            }
        });
        getNearBy();
        refreshLayout.setRefreshing(true);
        userList.setAdapter(nearByUserAdapter);
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
                    //userDao2.saveLocation(getUsername(), getPassword(), aMapLocation.getLatitude(), aMapLocation.getLongitude());
                    OkHttpUtils.post().url(Interfaces.SAVE_LOCATION)
                            .addParams("loginName", getUsername())
                            .addParams("loginPwd", getPassword())
                            .addParams("latitude", aMapLocation.getLatitude() + "")
                            .addParams("longitude", aMapLocation.getLongitude() + "")
                            .build().execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {

                        }

                        @Override
                        public void onResponse(String response, int id) {
                            Log.i("save_success", response);
                        }
                    });
                    location.setText(aMapLocation.getAddress());
                } else {
                    //定位失败
                    sb.append("定位失败" + "\n");
                    sb.append("错误码:" + aMapLocation.getErrorCode() + "\n");
                    sb.append("错误信息:" + aMapLocation.getErrorInfo() + "\n");
                    sb.append("错误描述:" + aMapLocation.getLocationDetail() + "\n");
                    Logger.i("error", sb.toString());
                    progressBar.setVisibility(View.GONE);
                    location.setVisibility(View.VISIBLE);
                    location.setText("网络错误，点击重试");
                }

            }
        });
        //userDao1.getAd(2, getUsername(), getPassword());

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
                                                        Toast.makeText(getApplicationContext(), s2, Toast.LENGTH_SHORT).show();
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
                                                                    Toast.makeText(getApplicationContext(), s1, Toast.LENGTH_SHORT).show();
                                                                    LocalBroadcastManager.getInstance(FriendActivity.this).sendBroadcast(new Intent(Constant.ACTION_CONTACT_CHANAGED));
                                                                }
                                                            });
                                                        } catch (final Exception e) {
                                                            runOnUiThread(new Runnable() {
                                                                public void run() {
                                                                    progressDialog.dismiss();
                                                                    String s2 = getResources().getString(R.string.Request_add_buddy_failure);
                                                                    Toast.makeText(getApplicationContext(), s2 + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    @OnClick(R.id.location)
    public void onClick() {
        location.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        locationClient.startLocation();
    }

    public void getNearBy() {
        OkHttpUtils.post().url(Interfaces.GET_NEARBY)
                .addParams("loginName", getUsername())
                .addParams("loginPwd", getPassword())
                .addParams("latitude", UserManager.getLatitude(this))
                .addParams("longitude", UserManager.getLongitude(this))
                .addParams("radius", "1000")
                .addParams("page", page+"")
                .build().execute(new StringCallback() {
                                     @Override
                                     public void onError(Call call, Exception e, int id) {
                                         toast(R.string.network_error);
                                         if (nearByUserAdapter.getCount()>0)
                                             nearByUserAdapter.pauseMore();
                                     }

                                     @Override
                                     public void onAfter(int id) {
                                         refreshLayout.setRefreshing(false);
                                     }

                                     @Override
                                     public void onResponse(String response, int id) {
                                         try {
                                             result = response.split("\\|");
                                             Logger.i("friend_result", response);
                                             if (result[0].equals("0")) {
                                                 list.clear();
                                                 try {
                                                     list = gson.fromJson(result[1], new TypeToken<ArrayList<NearByUser>>() {
                                                     }.getType());
                                                 } catch (Exception e) {

                                                 }
                                                 Logger.i("size", list.size() + "");
                                                 if (nearByUserAdapter.getCount()==0) {
                                                     page+=1;
                                                     nearByUserAdapter.addAll(list);
                                                     nearByUserAdapter.setOnItemLongClickListener(new RecyclerArrayAdapter.OnItemLongClickListener() {
                                                         @Override
                                                         public boolean onItemClick(int position) {
                                                             showDialog(nearByUserAdapter.getData().get(position - 2));
                                                             return true;
                                                         }
                                                     });
                                                     if (nearByUserAdapter.getHeaders().size() == 0)
                                                         nearByUserAdapter.addHeader(new RecyclerArrayAdapter.ItemView() {
                                                             @Override
                                                             public View onCreateView(ViewGroup parent) {
                                                                 return getLayoutInflater().inflate(R.layout.ad, null);
                                                             }

                                                             @Override
                                                             public void onBindView(View headerView) {
                                                                 final ImageView imageView = (ImageView) headerView.findViewById(R.id.image);
                                                                 OkHttpUtils.post().url(Interfaces.GET_ADVERTICEMENT2)
                                                                         .addParams("loginName", getUsername())
                                                                         .addParams("loginPwd", getPassword())
                                                                         .addParams("softType", "android")
                                                                         .build().execute(new StringCallback() {
                                                                     @Override
                                                                     public void onError(Call call, Exception e, int id) {
                                                                         e.printStackTrace();
                                                                     }

                                                                     @Override
                                                                     public void onResponse(String response, int id) {
                                                                         try {
                                                                             Logger.i("ad_result", response);

                                                                             String[] result = response.split("\\|");
                                                                             if (result[0].equals("0")) {
                                                                                 final ArrayList<Advertisement> list;
                                                                                 list = gson.fromJson(result[1], new TypeToken<ArrayList<Advertisement>>() {
                                                                                 }.getType());
                                                                                 SimpleHandler.getInstance().postDelayed(new Runnable() {
                                                                                     @Override
                                                                                     public void run() {
                                                                                         Glide.with(getApplicationContext()).load(list.get(0).getImage()).into(imageView);
                                                                                     }
                                                                                 }, 500);
                                                                                 imageView.setOnClickListener(new View.OnClickListener() {
                                                                                     @Override
                                                                                     public void onClick(View v) {
                                                                                         Intent intent1 = new Intent(Intent.ACTION_VIEW);
                                                                                         intent1.setData(Uri.parse(list.get(0).getAdurl()));
                                                                                         startActivity(intent1);
                                                                                     }
                                                                                 });
                                                                             }
                                                                         } catch (Exception e) {
                                                                             e.printStackTrace();
                                                                         }
                                                                     }
                                                                 });
                                                             }
                                                         });
                                                 }else if(list.size()==0){
                                                     nearByUserAdapter.stopMore();
                                                 }else {nearByUserAdapter.addAll(list);
                                                   page+=1;
                                                 }
                                             } else {
                                                 toast(result[1]);
                                                 if(nearByUserAdapter.getCount()>0)
                                                     nearByUserAdapter.stopMore();
                                             }
                                         } catch (Exception e) {
                                             toast(R.string.getserverdata_exception);
                                             if (nearByUserAdapter.getCount()>0)
                                                 nearByUserAdapter.pauseMore();
                                         }

                                     }

                                 }
        );

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationClient.stopLocation();
        locationClient.onDestroy();
        locationClient = null;
        locationOption = null;
    }
}
