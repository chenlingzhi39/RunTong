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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.callba.phone.widget.DividerItemDecoration;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
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
    @BindView(R.id.empty)
    TextView empty;
    @BindView(R.id.error)
    TextView error;
    private ArrayList<SeparatedEMGroup> separatedEMGroups;
    private GroupAdapter groupAdapter;
    private MyFilter filter;
    private BroadcastReceiver broadcastReceiver;
    private LocalBroadcastManager broadcastManager;
    private boolean first = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
        empty.setHint(getString(R.string.no_group));
        setOverflowShowingAlways();
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
                refresh();
            }
        });
        refresh();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getIntExtra("result",1)==1)
                refresh();
            }
        };
        broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(Constant.ACTION_GROUP_CHANAGED));
    }

    public void refresh() {
        subscription = Observable.create(new Observable.OnSubscribe<List<EMGroup>>() {
            @Override
            public void call(Subscriber<? super List<EMGroup>> subscriber) {
                try {
                    subscriber.onNext(EMClient.getInstance().groupManager().getJoinedGroupsFromServer());
                } catch (Exception e) {
                    subscriber.onNext(null);
                    e.printStackTrace();
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<List<EMGroup>>() {
            @Override
            public void call(final List<EMGroup> emGroups) {
                refreshLayout.setRefreshing(false);
                if (first) {
                    if (emGroups == null) {
                        error.setVisibility(View.VISIBLE);

                        return;
                    } else if (emGroups.size() == 0) {
                        empty.setVisibility(View.VISIBLE);
                        return;
                    } else {
                        error.setVisibility(View.GONE);
                        empty.setVisibility(View.GONE);
                    }
                }
                separatedEMGroups.clear();
                for (EMGroup emGroup : emGroups) {
                    if (emGroup.getOwner().equals(EMClient.getInstance().getCurrentUser()))
                        separatedEMGroups.add(new SeparatedEMGroup(emGroup, "0"));
                    else separatedEMGroups.add(new SeparatedEMGroup(emGroup, "1"));
                }
                Collections.sort(separatedEMGroups, new GroupComparator());
                filter = new MyFilter(separatedEMGroups);
                if (TextUtils.isEmpty(editTextSearch.getText().toString())) {
                    groupAdapter.clear();
                    groupAdapter.addAll(separatedEMGroups);
                } else
                    filter.filter(editTextSearch.getText().toString());
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

    @OnClick(R.id.error)
    public void onClick() {
        refresh();
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
                    if (string.contains(prefixString) || CharacterParser.getInstance().getSelling(string).contains(prefixString) || separatedEMGroup.getEmGroup().getGroupId().contains(prefixString)) {
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
        if(broadcastReceiver!=null)
        broadcastManager.unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(cardSearch.getWindowToken(), 0);
        if (cardSearch.getVisibility() == View.VISIBLE && TextUtils.isEmpty(editTextSearch.getText().toString()))
            InitiateSearch.handleToolBar(GroupsActivity.this, cardSearch, editTextSearch, 56);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod(
                            "setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    private void setOverflowShowingAlways() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class
                    .getDeclaredField("sHasPermanentMenuKey");
            menuKeyField.setAccessible(true);
            menuKeyField.setBoolean(config, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
