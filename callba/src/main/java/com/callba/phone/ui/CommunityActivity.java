package com.callba.phone.ui;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.callba.R;
import com.callba.phone.ui.adapter.MoodAdapter;
import com.callba.phone.ui.adapter.RecyclerArrayAdapter;
import com.callba.phone.bean.Mood;
import com.callba.phone.bean.UserDao;
import com.callba.phone.manager.UserManager;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.util.Utils;
import com.callba.phone.widget.AlphaView;
import com.callba.phone.widget.DividerItemDecoration;
import com.callba.phone.widget.refreshlayout.EasyRecyclerView;
import com.callba.phone.widget.refreshlayout.RefreshLayout;
import com.google.gson.Gson;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;

/**
 * Created by PC-20160514 on 2016/5/25.
 */
public class CommunityActivity extends AppCompatActivity implements UserDao.PostListener, RefreshLayout.OnRefreshListener, RecyclerArrayAdapter.OnLoadMoreListener {

    @InjectView(R.id.list)
    EasyRecyclerView moodList;
    int page, pageSize = 30;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.toolbar_background)
    AlphaView toolbarBackground;
    private ArrayList<Mood> moods;
    private Gson gson;
    private String[] result;
    private MoodAdapter moodAdapter;
    private View headerView;
    private int toolbar_height, statusbar_height, background_height, image_height, width, height;
    private DisplayMetrics displayMetrics;
    private int y = 0;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
          /*  case R.id.camera:
                Intent intent = new Intent(CommunityActivity.this, PostActivity.class);
                startActivityForResult(intent, 0);
                break;*/
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.community);
        // TODO: add setContentView(...) invocation
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.community));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar_height = Utils.getToolbarHeight(this);
        statusbar_height = Utils.getStatusBarHeight(this);
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            );
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            toolbar.setPadding(0, Utils.getStatusBarHeight(this), 0, 0);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, Utils.getStatusBarHeight(this) + Utils.getToolbarHeight(this));
            toolbarBackground.setLayoutParams(lp);

        } else {
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, Utils.getToolbarHeight(this));
            toolbarBackground.setLayoutParams(lp);
        }
        background_height = statusbar_height + toolbar_height;
        image_height = (int) (250 * displayMetrics.density);
        toolbarBackground.post(new Runnable() {
            @Override
            public void run() {
                toolbarBackground.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.center_background_pic), width, image_height);
            }
        });
        moodList.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                y += dy;
                if (y >= 0 && y <= (image_height / 2 - background_height)) {
                    toolbarBackground.setAlpha(0);
                    toolbar.setBackgroundColor(Color.argb(255 * y / image_height / 2, 0, 0, 0));
                }
                if (y > (image_height / 2 - background_height) && y <= (image_height)) {
                    toolbarBackground.setAlpha(255 * (y - (image_height / 2 - background_height)) / (image_height / 2));
                }
                if (y > (image_height - background_height)) {
                    toolbarBackground.setAlpha(255);
                }
                if (y > (image_height / 2 - background_height)) {
                    toolbar.setBackgroundColor(Color.argb(255 * (int) (image_height / 2 - background_height) / (image_height / 2), 0, 0, 0));
                }
            }
        });
        initRefreshLayout();
        moodList.setLayoutManager(new LinearLayoutManager(this));
        moodList.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL_LIST));
        moodList.setFooterEnabled(false);
        gson = new Gson();
        moodAdapter = new MoodAdapter(this);
        headerView = getLayoutInflater().inflate(R.layout.header, null);
        moodAdapter.addHeader(new RecyclerArrayAdapter.ItemView() {
            @Override
            public View onCreateView(ViewGroup parent) {
                return headerView;
            }

            @Override
            public void onBindView(View headerView) {
                CircleImageView head = (CircleImageView) headerView.findViewById(R.id.head);
                TextView number = (TextView) headerView.findViewById(R.id.number);
                if (!UserManager.getUserAvatar(CommunityActivity.this).equals(""))
                    Glide.with(CommunityActivity.this).load(UserManager.getUserAvatar(CommunityActivity.this)).into(head);
                number.setHint(UserManager.getUsername(CommunityActivity.this));
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
        getMoods( page + "", pageSize + "");
    }

    @Override
    public void onLoadMore() {
        getMoods(page + "", pageSize + "");

    }

    @Override
    public void onFooterRefresh() {

    }

    @Override
    public void onHeaderRefresh() {
       getMoods("0", pageSize + "");

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
        try {
            result = msg.split("\\|");
            if (result[0].equals("0")) {
                moods = new ArrayList<>();
                try {
                /*moods = gson.fromJson(result[1], new TypeToken<ArrayList<Mood>>() {
                }.getType());*/
                    for (int i = 0; i < 10; i++)
                        moods.add(new Mood());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Logger.i("size", moods.size() + "");
                if (moods.size() == 0)
                    moodAdapter.stopMore();
                if (moods.size() > 0 && moodAdapter.getData().size() == 0) {
                    moodAdapter.addAll(moods);
                    moodAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position) {

                        }
                    });
                }
                if (moods.size() > 0 && moodAdapter.getData().size() > 0) {
                    moodAdapter.addAll(moods);
                }
            } else {
                moodAdapter.stopMore();
            }
        } catch (Exception e) {
            moodAdapter.pauseMore();
            moodList.setHeaderRefreshing(false);
        }
    }

    public void initRefreshLayout() {
        moodList.setRefreshListener(this);
        moodList.setProgressViewOffset(false, background_height, background_height);
        moodList.setProgressViewEndTarget(false, background_height);
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
          getMoods("0", pageSize + "");
        }
    }

    @OnClick(R.id.submit)
    public void onClick() {
        Intent intent = new Intent(CommunityActivity.this, PostActivity.class);
        startActivityForResult(intent, 0);
    }
    public void getMoods(String page, String pageSize){
        OkHttpUtils.post().url(Interfaces.GET_MOODS)
                .addParams("loginName",getUsername())
                .addParams("loginPwd",getPassword())
                .addParams("page", page)
                .addParams("pagesize",pageSize)
                .build().execute(new StringCallback() {
            @Override
            public void onAfter(int id) {
                moodList.setHeaderRefreshing(false);
            }

            @Override
            public void onError(Call call, Exception e, int id) {
                moodAdapter.pauseMore();
            }

            @Override
            public void onResponse(String response, int id) {
                try {
                    result = response.split("\\|");
                    if (result[0].equals("0")) {
                        moods = new ArrayList<>();
                        try {
                /*moods = gson.fromJson(result[1], new TypeToken<ArrayList<Mood>>() {
                }.getType());*/
                            for (int i = 0; i < 10; i++)
                                moods.add(new Mood());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Logger.i("size", moods.size() + "");
                        if (moods.size() == 0)
                            moodAdapter.stopMore();
                        if (moods.size() > 0 && moodAdapter.getData().size() == 0) {
                            moodAdapter.addAll(moods);
                            moodAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(int position) {

                                }
                            });
                        }
                        if (moods.size() > 0 && moodAdapter.getData().size() > 0) {
                            moodAdapter.addAll(moods);
                        }
                    } else {
                        moodAdapter.stopMore();
                    }
                } catch (Exception e) {
                    moodAdapter.pauseMore();
                }
            }
        });
    }
    public String getUsername() {
        return UserManager.getUsername(this);
    }

    public String getPassword() {
        return UserManager.getPassword(this);
    }
}
