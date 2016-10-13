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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.callba.R;
import com.callba.phone.Constant;
import com.callba.phone.DemoHelper;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.EaseUser;
import com.callba.phone.ui.adapter.EaseContactAdapter;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.util.Logger;
import com.callba.phone.widget.EaseAlertDialog;
import com.callba.phone.widget.EaseSidebar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SuppressLint("Registered")
@ActivityFragmentInject(
		contentViewId = R.layout.em_activity_pick_contact_no_checkbox,
		toolbarTitle=R.string.select_contact,
		navigationId = R.drawable.press_back
)
public class PickContactNoCheckboxActivity extends BaseActivity {
	private EaseUser selectUser;
	private String forward_msg_id;
	protected EaseContactAdapter contactAdapter;
	private List<EaseUser> contactList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		forward_msg_id = getIntent().getStringExtra("forward_msg_id");
		ListView listView = (ListView) findViewById(R.id.list);
		EaseSidebar sidebar = (EaseSidebar) findViewById(R.id.sidebar);
		sidebar.setListView(listView);
		contactList = new ArrayList<EaseUser>();
		// get contactlist
		getContactList();
		// set adapter
		contactAdapter = new EaseContactAdapter(this, R.layout.ease_row_contact, contactList);
		listView.setAdapter(contactAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onListItemClick(position);
			}
		});

	}

	protected void onListItemClick(int position) {
		selectUser = contactAdapter.getItem(position);
		new EaseAlertDialog(this, null, getString(R.string.confirm_forward_to, selectUser.getNick()), null, new EaseAlertDialog.AlertDialogUser() {
			@Override
			public void onResult(boolean confirmed, Bundle bundle) {
				if (confirmed) {
					if (selectUser == null)
						return;
					try {
						ChatActivity.activityInstance.finish();
					} catch (Exception e) {
					}
					Intent intent = new Intent(PickContactNoCheckboxActivity.this, ChatActivity.class);
					// it is single chat
					intent.putExtra("userId", selectUser.getUsername());
					intent.putExtra("forward_msg_id", forward_msg_id);
					startActivity(intent);
					finish();
				}
			}
		}, true).show();
	}

	public void back(View view) {
		finish();
	}

	private void getContactList() {
		contactList.clear();
		Map<String, EaseUser> users = DemoHelper.getInstance().getContactList();
		for (Entry<String, EaseUser> entry : users.entrySet()) {
			if (!entry.getKey().equals(Constant.NEW_FRIENDS_USERNAME) && !entry.getKey().equals(Constant.GROUP_USERNAME) && !entry.getKey().equals(Constant.CHAT_ROOM) && !entry.getKey().equals(Constant.CHAT_ROBOT))
				contactList.add(entry.getValue());
		}
		// sort
        Collections.sort(contactList, new Comparator<EaseUser>() {

            @Override
            public int compare(EaseUser lhs, EaseUser rhs) {
                if(lhs.getInitialLetter().equals(rhs.getInitialLetter())){
                    return lhs.getNick().compareTo(rhs.getNick());
                }else{
                    if("#".equals(lhs.getInitialLetter())){
                        return 1;
                    }else if("#".equals(rhs.getInitialLetter())){
                        return -1;
                    }
                    return lhs.getInitialLetter().compareTo(rhs.getInitialLetter());
                }
                
            }
        });
	}

}
