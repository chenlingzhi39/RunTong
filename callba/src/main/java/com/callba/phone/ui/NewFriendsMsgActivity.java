/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.callba.phone.ui;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.callba.R;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.Constant;
import com.callba.phone.ui.adapter.NewFriendsMsgAdapter;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.db.InviteMessage;
import com.callba.phone.db.InviteMessgeDao;
import com.callba.phone.util.SimpleHandler;

import java.util.List;

/**
 * 申请与通知
 *
 */
@ActivityFragmentInject(
		contentViewId = R.layout.em_activity_new_friends_msg,
		toolbarTitle = R.string.Application_and_notify,
		navigationId = R.drawable.press_back,
		menuId = R.menu.menu_notice
)
public class NewFriendsMsgActivity extends BaseActivity {
	private ListView listView;
	InviteMessgeDao dao;
	List<InviteMessage> msgs;
	NewFriendsMsgAdapter adapter;
	LocalBroadcastManager localBroadcastManager;
	BroadcastReceiver broadcastReceiver;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		listView = (ListView) findViewById(R.id.list);
		 dao = new InviteMessgeDao(this);
		msgs = dao.getMessagesList();
		//设置adapter
		adapter = new NewFriendsMsgAdapter(this, 1, msgs);
		listView.setAdapter(adapter);
		dao.saveUnreadMessageCount(0);
		localBroadcastManager=LocalBroadcastManager.getInstance(this);
		broadcastReceiver=new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				SimpleHandler.getInstance().postDelayed(new Runnable() {
					@Override
					public void run() {
						dao.saveUnreadMessageCount(0);
						 msgs = dao.getMessagesList();
						adapter = new NewFriendsMsgAdapter(NewFriendsMsgActivity.this, 1, msgs);
						listView.setAdapter(adapter);
					}
				},500);

			}
		};
		localBroadcastManager.registerReceiver(broadcastReceiver,new IntentFilter(Constant.ACTION_GROUP_CHANAGED));
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(0526);
	}

	public void back(View view) {
		finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.clear:
				dao.deleteMessage();
				msgs.clear();
				adapter.notifyDataSetChanged();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		localBroadcastManager.unregisterReceiver(broadcastReceiver);
		super.onDestroy();
	}
}
