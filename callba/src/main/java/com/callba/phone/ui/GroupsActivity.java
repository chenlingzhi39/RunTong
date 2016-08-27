/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.callba.phone.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.callba.R;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.Constant;
import com.callba.phone.ui.adapter.GroupAdapter;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.util.EaseUserUtils;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.exceptions.HyphenateException;

import java.util.ArrayList;
import java.util.List;

@ActivityFragmentInject(
        contentViewId = R.layout.em_fragment_groups,
        navigationId = R.drawable.press_back,
        toolbarTitle = R.string.group_chat
)
public class GroupsActivity extends BaseActivity {
    public static final String TAG = "GroupsActivity";
    private ListView groupListView;
    protected List<EMGroup> grouplist,myGroupList,joinGroupList,allGroupList;
    private GroupAdapter groupAdapter;
    private InputMethodManager inputMethodManager;
    public static GroupsActivity instance;
    private View progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver broadcastReceiver;
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            swipeRefreshLayout.setRefreshing(false);
            switch (msg.what) {
                case 0:
                    refresh();
                    break;
                case 1:
                    Toast.makeText(GroupsActivity.this, R.string.Failed_to_get_group_chat_information, Toast.LENGTH_LONG).show();
                    break;

                default:
                    break;
            }
        }

        ;
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        grouplist =EMClient.getInstance().groupManager().getAllGroups();
        groupListView = (ListView) findViewById(R.id.list);
        //show group list
        sortGroupList();
        groupAdapter = new GroupAdapter(this, 1, allGroupList,myGroupList.size());
        groupListView.setAdapter(groupAdapter);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.holo_blue_bright, R.color.holo_green_light,
                R.color.holo_orange_light, R.color.holo_red_light);
        //下拉刷新
        swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            EMClient.getInstance().groupManager().getJoinedGroupsFromServer();
                            handler.sendEmptyMessage(0);
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                            handler.sendEmptyMessage(1);
                        }
                    }
                }.start();
            }
        });

        groupListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {
                    // 新建群聊
                    startActivityForResult(new Intent(GroupsActivity.this, NewGroupActivity.class), 0);
                } else if (position == 2) {
                    // 添加公开群
                    Intent intent=new Intent(GroupsActivity.this, PublicGroupsActivity.class);
                    ArrayList<String> groupIds=new ArrayList<String>();
                    for (EMGroup group:grouplist){
                        groupIds.add(group.getGroupId());
                    }
                    intent.putStringArrayListExtra("groupIds",groupIds);
                    startActivityForResult(intent, 0);
                } else {
                    // 进入群聊
                    Intent intent = new Intent(GroupsActivity.this, ChatActivity.class);
                    // it is group chat
                    intent.putExtra("chatType", Constant.CHATTYPE_GROUP);
                    intent.putExtra("userId", groupAdapter.getItem(position - 3).getGroupId());
                    startActivityForResult(intent, 0);
                }
            }

        });
        groupListView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
                    if (getCurrentFocus() != null)
                        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                }
                return false;
            }
        });
        IntentFilter intentFilter = new IntentFilter();
        //intentFilter.addAction(Constant.ACTION_CONTACT_CHANAGED);
        intentFilter.addAction(Constant.ACTION_GROUP_CHANAGED);
        broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refresh();
            }
        };
        broadcastManager.registerReceiver(broadcastReceiver, intentFilter);
    }

    /**
     * 进入公开群聊列表
     */
    public void onPublicGroups(View view) {
        startActivity(new Intent(this, PublicGroupsActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        refresh();
        super.onResume();
    }

    private void refresh() {
        grouplist = EMClient.getInstance().groupManager().getAllGroups();
        sortGroupList();
        groupAdapter = new GroupAdapter(this, 1, allGroupList,myGroupList.size());
        groupListView.setAdapter(groupAdapter);
        groupAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        broadcastManager.unregisterReceiver(broadcastReceiver);
        super.onDestroy();
        instance = null;
    }
  private void sortGroupList(){
      myGroupList=new ArrayList<>();
      joinGroupList=new ArrayList<>();
      allGroupList=new ArrayList<>();
      for(EMGroup emGroup:grouplist){
          if(emGroup.getOwner().equals(EMClient.getInstance().getCurrentUser()))
              myGroupList.add(emGroup);
              else joinGroupList.add(emGroup);
      }
      allGroupList.addAll(myGroupList);
      allGroupList.addAll(joinGroupList);
  }


}
