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

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.callba.R;
import com.callba.phone.Constant;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.util.EaseUserUtils;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMGroupInfo;
import com.hyphenate.exceptions.HyphenateException;

import java.util.Timer;
import java.util.TimerTask;

@ActivityFragmentInject(
		contentViewId = R.layout.em_activity_group_simle_details,
		toolbarTitle = R.string.Group_chat_information,
		navigationId = R.drawable.press_back
)
public class GroupSimpleDetailActivity extends BaseActivity {
	private Button btn_add_group;
	private TextView tv_admin;
	private TextView tv_name;
	private TextView tv_introduction;
	private TextView tv_id;
	private TextView tv_need_apply;
	private EMGroup group;
	private String groupid;
    private String apply;
	private LinearLayout introduction;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tv_name = (TextView) findViewById(R.id.name);
		tv_admin = (TextView) findViewById(R.id.tv_admin);
		btn_add_group = (Button) findViewById(R.id.btn_add_to_group);
		tv_introduction = (TextView) findViewById(R.id.tv_introduction);
        tv_id=(TextView) findViewById(R.id.tv_id);
        tv_need_apply=(TextView)findViewById(R.id.tv_need_apply);
		introduction=(LinearLayout) findViewById(R.id.introduction);
		introduction.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!group.getDescription().equals(""))
				{android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(GroupSimpleDetailActivity.this);
				builder.setMessage(group.getDescription());
				android.app.AlertDialog alertDialog = builder.create();
				alertDialog.setCanceledOnTouchOutside(true);
				alertDialog.setCancelable(true);
				alertDialog.show();}
			}
		});
		EMGroupInfo groupInfo = (EMGroupInfo) getIntent().getSerializableExtra("groupinfo");
		String groupname = null;
		    groupname = groupInfo.getGroupName();
		    groupid = groupInfo.getGroupId();
		tv_name.setText(groupname);
		tv_id.setText(groupid);
		if(group != null){
		    showGroupDetail();
		    return;
		}
		new Thread(new Runnable() {

			public void run() {
				//从服务器获取详情
				try {
					group = EMClient.getInstance().groupManager().getGroupFromServer(groupid);

					runOnUiThread(new Runnable() {
						public void run() {
							showGroupDetail();
							tv_need_apply.setText(group.isMembersOnly()?"是":"否");
						}
					});
				} catch (final HyphenateException e) {
					e.printStackTrace();
					final String st1 = getResources().getString(R.string.Failed_to_get_group_chat_information);
					runOnUiThread(new Runnable() {
						public void run() {
							//progressBar.setVisibility(View.INVISIBLE);
							Toast.makeText(GroupSimpleDetailActivity.this, st1+e.getMessage(), 1).show();

						}
					});
				}
				
			}
		}).start();
		
	}
	
	//加入群聊
	public void addToGroup(View view){
		String st1 = getResources().getString(R.string.Is_sending_a_request);
		final String st2 = getResources().getString(R.string.Request_to_join);
		final String st3 = getResources().getString(R.string.send_the_request_is);
		final String st4 = getResources().getString(R.string.Join_the_group_chat);
		final String st5 = getResources().getString(R.string.Failed_to_join_the_group_chat);
		final ProgressDialog pd = new ProgressDialog(this);
//		getResources().getString(R.string)
		pd.setMessage(st1);
		pd.setCanceledOnTouchOutside(false);
		pd.show();
		new Thread(new Runnable() {
			public void run() {
				try {
					//如果是membersOnly的群，需要申请加入，不能直接join
					if(group.isMembersOnly()){
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								showDialog();
							}
						});
					}else{
					    EMClient.getInstance().groupManager().joinGroup(groupid);
						LocalBroadcastManager.getInstance(GroupSimpleDetailActivity.this).sendBroadcast(new Intent(Constant.ACTION_GROUP_CHANAGED));
					}
					runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
							if(!group.isMembersOnly())
								Toast.makeText(GroupSimpleDetailActivity.this, st4, 0).show();
							btn_add_group.setEnabled(false);
						}
					});
				} catch (final HyphenateException e) {
					e.printStackTrace();
					runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
							Toast.makeText(GroupSimpleDetailActivity.this, st5+e.getMessage(), 0).show();
							btn_add_group.setEnabled(true);
						}
					});
				}
			}
		}).start();
	}
	
     private void showGroupDetail() {
         //progressBar.setVisibility(View.INVISIBLE);
         //获取详情成功，并且自己不在群中，才让加入群聊按钮可点击
         if(!group.getMembers().contains(EMClient.getInstance().getCurrentUser()))
             btn_add_group.setEnabled(true);
         tv_name.setText(group.getGroupName());
		 EaseUserUtils.setUserNick(group.getOwner(),tv_admin);
         tv_introduction.setText(group.getDescription());
     }
	public class DialogHelper implements DialogInterface.OnDismissListener {
		private Dialog mDialog;
		private View mView;
		private EditText change;

		public DialogHelper() {
			mView = getLayoutInflater().inflate(R.layout.dialog_change_number, null);
			change = (EditText) mView.findViewById(R.id.et_change);
			change.setInputType(InputType.TYPE_CLASS_TEXT);
			change.requestFocus();
			Timer timer = new Timer(); //设置定时器
			timer.schedule(new TimerTask() {
				@Override
				public void run() { //弹出软键盘的代码
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.toggleSoftInputFromWindow(change.getWindowToken(), 0, InputMethodManager.HIDE_NOT_ALWAYS);
				}
			}, 300); //设置300毫秒的时长
		}

		private String getNumber() {
			return change.getText().toString();
		}

		@Override
		public void onDismiss(DialogInterface dialogInterface) {
			mDialog = null;
		}

		public void setDialog(Dialog mDialog) {
			this.mDialog = mDialog;
		}

		public View getView() {
			return mView;
		}
	}

	public void showDialog() {
		final DialogHelper helper = new DialogHelper();
		Dialog dialog = new AlertDialog.Builder(this)
				.setView(helper.getView())
				.setTitle("请输入验证信息")
				.setOnDismissListener(helper)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						apply=helper.getNumber();
							new Thread(new Runnable() {
								@Override
								public void run() {
									try {
									EMClient.getInstance().groupManager().applyJoinToGroup(groupid, apply);

								}catch (HyphenateException e){

									}}
							}).start();
						Toast.makeText(GroupSimpleDetailActivity.this, getResources().getString(R.string.send_the_request_is), 0).show();
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						btn_add_group.setEnabled(true);
					}
				})
				.create();

		helper.setDialog(dialog);
		dialog.show();
	}

	public void back(View view){
		finish();
	}
}
