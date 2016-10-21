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

import com.callba.R;
import com.callba.phone.Constant;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.SeparatedEMGroup;
import com.callba.phone.pinyin.CharacterParser;
import com.callba.phone.pinyin.GroupComparator;
import com.callba.phone.ui.adapter.GroupAdapter;
import com.callba.phone.ui.adapter.RecyclerArrayAdapter;
import com.callba.phone.ui.adapter.expandRecyclerviewadapter.StickyRecyclerHeadersDecoration;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.util.InitiateSearch;
import com.callba.phone.util.SimpleHandler;
import com.callba.phone.widget.DividerItemDecoration;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.exceptions.HyphenateException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by PC-20160514 on 2016/10/20.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.activity_groups,
        toolbarTitle = R.string.group_chat,
        navigationId = R.drawable.press_back,
        menuId = R.menu.groups
)
public class GroupsActivity extends BaseActivity {

    @BindView(R.id.group_list)
    RecyclerView groupList;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.image_search_back)
    ImageView imageSearchBack;
    @BindView(R.id.edit_text_search)
    EditText editTextSearch;
    @BindView(R.id.clearSearch)
    ImageView clearSearch;
    @BindView(R.id.card_search)
    CardView cardSearch;
    private ArrayList<SeparatedEMGroup> separatedEMGroups;
    private GroupAdapter groupAdapter;
    private MyFilter filter;
    private BroadcastReceiver broadcastReceiver;
    private LocalBroadcastManager broadcastManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
        InitiateSearch();
        HandleSearch();
        separatedEMGroups = new ArrayList<>();
        groupAdapter = new GroupAdapter(this);
        groupAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // 进入群聊
                Intent intent = new Intent(GroupsActivity.this, ChatActivity.class);
                // it is group chat
                intent.putExtra("chatType", Constant.CHATTYPE_GROUP);
                intent.putExtra("userId", groupAdapter.getItem(position).getEmGroup().getGroupId()
                );
                startActivityForResult(intent, 0);
            }
        });
        groupAdapter.addAll(separatedEMGroups);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        groupList.setLayoutManager(layoutManager);
        groupList.setAdapter(groupAdapter);
        final StickyRecyclerHeadersDecoration headersDecor = new StickyRecyclerHeadersDecoration(groupAdapter);
        groupList.addItemDecoration(headersDecor);
        groupList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        groupAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                headersDecor.invalidateHeaders();
            }
        });
        refreshLayout.setColorSchemeResources(R.color.holo_blue_bright, R.color.holo_green_light,
                R.color.holo_orange_light, R.color.holo_red_light);
        //下拉刷新
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            EMClient.getInstance().groupManager().getJoinedGroupsFromServer();
                            SimpleHandler.getInstance().post(new Runnable() {
                                @Override
                                public void run() {
                                    refresh();
                                    refreshLayout.setRefreshing(false);
                                }
                            });
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                            SimpleHandler.getInstance().post(new Runnable() {
                                @Override
                                public void run() {
                                    toast(R.string.Failed_to_get_group_chat_information);
                                    refreshLayout.setRefreshing(false);
                                }
                            });
                        }
                    }
                }.start();
            }
        });
        refresh();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refresh();
            }
        };
        broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(Constant.ACTION_GROUP_CHANAGED));
    }

    public void refresh() {
        separatedEMGroups.clear();
        for (EMGroup emGroup : EMClient.getInstance().groupManager().getAllGroups()) {
            if (emGroup.getOwner().equals(EMClient.getInstance().getCurrentUser()))
                separatedEMGroups.add(new SeparatedEMGroup(emGroup, "0"));
            else separatedEMGroups.add(new SeparatedEMGroup(emGroup, "1"));
        }
        Collections.sort(separatedEMGroups, new GroupComparator());
        filter = new MyFilter(separatedEMGroups);
        if(TextUtils.isEmpty(editTextSearch.getText().toString()))
        {groupAdapter.clear();
            groupAdapter.addAll(separatedEMGroups);}else
            filter.filter(editTextSearch.getText().toString());
    }

    private void InitiateSearch() {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
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
                ((InputMethodManager) GroupsActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });

    }

    private void HandleSearch() {
        imageSearchBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("search", "back");
                InitiateSearch.handleToolBar(GroupsActivity.this, cardSearch, editTextSearch, 56);
            }
        });
        editTextSearch.requestFocus();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                InitiateSearch.handleToolBar(GroupsActivity.this, cardSearch, editTextSearch, 56);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public class MyFilter extends Filter {
        List<SeparatedEMGroup> mOriginalList;

        public MyFilter(List<SeparatedEMGroup> separatedEMGroups) {
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
                final ArrayList<SeparatedEMGroup> newValues = new ArrayList<>();
                for (SeparatedEMGroup separatedEMGroup : mOriginalList) {
                    final String string = separatedEMGroup.getEmGroup().getGroupName();
                    if (string.contains(prefixString) || CharacterParser.getInstance().getSelling(string).contains(prefixString)) {
                        newValues.add(separatedEMGroup);
                    }
                }
                results.values = newValues;
                results.count = newValues.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            groupAdapter.clear();
            groupAdapter.addAll((ArrayList<SeparatedEMGroup>) results.values);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        broadcastManager.unregisterReceiver(broadcastReceiver);
    }
}
