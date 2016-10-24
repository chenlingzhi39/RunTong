package com.callba.phone.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.Constant;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.pinyin.CharacterParser;
import com.callba.phone.ui.adapter.GroupInfoAdapter;
import com.callba.phone.ui.adapter.RecyclerArrayAdapter;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.util.InitiateSearch;
import com.callba.phone.util.Logger;
import com.callba.phone.widget.DividerItemDecoration;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCursorResult;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMGroupInfo;
import com.hyphenate.exceptions.HyphenateException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by PC-20160514 on 2016/10/22.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.public_groups,
        navigationId = R.drawable.press_back,
        toolbarTitle = R.string.Open_group_chat,
        menuId = R.menu.message
)
public class PublicGroupsActivity extends BaseActivity {

    @BindView(R.id.group_list)
    RecyclerView groupList;
    @BindView(R.id.image_search_back)
    ImageView imageSearchBack;
    @BindView(R.id.edit_text_search)
    EditText editTextSearch;
    @BindView(R.id.clearSearch)
    ImageView clearSearch;
    @BindView(R.id.card_search)
    CardView cardSearch;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.empty)
    TextView empty;
    @BindView(R.id.error)
    TextView error;
    private GroupInfoAdapter groupInfoAdapter;
    private int pageSize = 20;
    private int currentSize = 0;
    private String cursor, preCursor = "";
    private boolean first = true;
    private Filter filter;
    private ArrayList<EMGroupInfo> groupInfos, allGroupInfos;
    private ArrayList<String> groupIds;
    private BroadcastReceiver broadcastReceiver;
    private LocalBroadcastManager broadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        broadcastManager = LocalBroadcastManager.getInstance(this);
        groupInfoAdapter = new GroupInfoAdapter(this);
        groupInfoAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                startActivity(new Intent(PublicGroupsActivity.this, GroupSimpleDetailActivity.class).
                        putExtra("groupinfo", groupInfoAdapter.getItem(position)));
            }
        });
        groupList.setAdapter(groupInfoAdapter);
        groupList.setLayoutManager(new LinearLayoutManager(this));
        groupList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        groupIds = new ArrayList<>();
        groupInfos = new ArrayList<>();
        allGroupInfos = new ArrayList<>();
        InitiateSearch();
        HandleSearch();
        refresh();
        refreshLayout.setColorSchemeResources(R.color.holo_blue_bright, R.color.holo_green_light,
                R.color.holo_orange_light, R.color.holo_red_light);
        //下拉刷新
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                first=true;
                groupInfoAdapter.clear();
                allGroupInfos.clear();
                cursor = "";
                currentSize = 0;
                refresh();
            }
        });
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                first=true;
                groupInfoAdapter.clear();
                allGroupInfos.clear();
                cursor = "";
                currentSize = 0;
                refresh();
            }
        };
        broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(Constant.ACTION_GROUP_CHANAGED));
    }

    private void refresh() {
        subscription = Observable.create(new Observable.OnSubscribe<List<EMGroupInfo>>() {
            @Override
            public void call(Subscriber<? super List<EMGroupInfo>> subscriber) {
                try {
                    EMCursorResult<EMGroupInfo> result = EMClient.getInstance().groupManager().getPublicGroupsFromServer(pageSize, cursor);
                    groupIds.clear();
                    groupInfos.clear();
                    preCursor = cursor;
                    cursor = result.getCursor();
                    Logger.i("cursor", cursor);
                    for (EMGroup emGroup : EMClient.getInstance().groupManager().getJoinedGroupsFromServer()) {
                        groupIds.add(emGroup.getGroupId());
                    }
                    currentSize = result.getData().size();
                    Logger.i("current_size", currentSize + "");
                    for (EMGroupInfo emGroupInfo : result.getData()) {
                        if (!groupIds.contains(emGroupInfo.getGroupId()))
                            groupInfos.add(emGroupInfo);
                    }
                    Logger.i("group_info", groupInfos.size() + "");

                    subscriber.onNext(groupInfos);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    subscriber.onNext(null);

                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<List<EMGroupInfo>>() {
            @Override
            public void call(final List<EMGroupInfo> emGroupInfos) {
                refreshLayout.setRefreshing(false);
                if (emGroupInfos == null) {
                    if(allGroupInfos.size()==0)
                    {error.setVisibility(View.VISIBLE);
                    return;
                    }else
                     groupInfoAdapter.pauseMore();
                } else if (emGroupInfos.size() == 0&&allGroupInfos.size()==0) {
                    empty.setVisibility(View.VISIBLE);
                    return;
                } else {
                    error.setVisibility(View.GONE);
                    empty.setVisibility(View.GONE);
                }
                if (first) {
                    if (currentSize == pageSize) {
                        groupInfoAdapter.setError(R.layout.view_more_error).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                groupInfoAdapter.resumeMore();
                            }
                        });
                        groupInfoAdapter.setMore(R.layout.view_more, new RecyclerArrayAdapter.OnLoadMoreListener() {
                            @Override
                            public void onLoadMore() {
                                try{
                                if (!TextUtils.isEmpty(preCursor)) {
                                    if (!TextUtils.isEmpty(cursor))
                                        refresh();
                                    else groupInfoAdapter.stopMore();
                                } else {
                                    refresh();
                                }}catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
                        groupInfoAdapter.setNoMore(R.layout.view_nomore);
                    }
                    first = false;
                } else {
                    if (currentSize < pageSize) {
                        groupInfoAdapter.stopMore();
                    }
                }
                allGroupInfos.addAll(emGroupInfos);
                filter = new MyFilter(allGroupInfos);
                if (!TextUtils.isEmpty(editTextSearch.getText().toString()))
                    filter.filter(editTextSearch.getText().toString());
                else groupInfoAdapter.addAll(emGroupInfos);
            }
        });
    }

    private void InitiateSearch() {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (filter != null)
                    filter.filter(s);
                if (editTextSearch.getText().toString().length() == 0) {
                    clearSearch.setVisibility(View.GONE);
                } else {
                    clearSearch.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextSearch.setText("");
                ((InputMethodManager) PublicGroupsActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });

    }

    private void HandleSearch() {
        imageSearchBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("search", "back");
                InitiateSearch.handleToolBar(PublicGroupsActivity.this, cardSearch, editTextSearch, 20);
            }
        });
        editTextSearch.requestFocus();
    }

    @OnClick(R.id.error)
    public void onClick() {
        refresh();
    }

    public class MyFilter extends Filter {
        List<EMGroupInfo> mOriginalList;

        public MyFilter(List<EMGroupInfo> separatedEMGroups) {
            this.mOriginalList = separatedEMGroups;
        }

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();
            if (mOriginalList == null) {
                mOriginalList = new ArrayList<>();
            }


            if (prefix == null || prefix.length() == 0) {
                results.values = mOriginalList;
                results.count = mOriginalList.size();
            } else {
                String prefixString = prefix.toString();
                final ArrayList<EMGroupInfo> newValues = new ArrayList<>();
                for (EMGroupInfo emGroupInfo : mOriginalList) {
                    final String string = emGroupInfo.getGroupName();
                    if (string.contains(prefixString) || CharacterParser.getInstance().getSelling(string).contains(prefixString) || emGroupInfo.getGroupId().contains(prefixString)) {
                        newValues.add(emGroupInfo);
                    }
                }
                results.values = newValues;
                results.count = newValues.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            groupInfoAdapter.clear();
            groupInfoAdapter.addAll((ArrayList<EMGroupInfo>) results.values);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(cardSearch.getWindowToken(), 0);
        if (cardSearch.getVisibility() == View.VISIBLE && TextUtils.isEmpty(editTextSearch.getText().toString()))
            InitiateSearch.handleToolBar(PublicGroupsActivity.this, cardSearch, editTextSearch, 20);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                InitiateSearch.handleToolBar(PublicGroupsActivity.this, cardSearch, editTextSearch, 20);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        broadcastManager.unregisterReceiver(broadcastReceiver);
    }
}
