package com.callba.phone.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.callba.R;
import com.callba.phone.Constant;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.manager.ContactsManager;
import com.callba.phone.ui.adapter.MemberAdapter;
import com.callba.phone.ui.adapter.RecyclerArrayAdapter;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.ui.ease.ExitGroupDialog;
import com.callba.phone.widget.EaseAlertDialog;
import com.callba.phone.widget.EaseSwitchButton;
import com.hyphenate.EMGroupChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.exceptions.HyphenateException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by PC-20160514 on 2016/10/25.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.activity_group_detail,
        navigationId = R.drawable.press_back
)
public class GroupDetailActivity extends BaseActivity {
    @BindView(R.id.tv_group_id_value)
    TextView idText;
    @BindView(R.id.rl_group_id)
    RelativeLayout rlGroupId;
    @BindView(R.id.tv_group_nick)
    TextView tvGroupNick;
    @BindView(R.id.tv_group_nick_value)
    TextView tvGroupNickValue;
    @BindView(R.id.rl_group_nick)
    RelativeLayout rlGroupNick;
    @BindView(R.id.tv_group_owner)
    TextView tvGroupOwner;
    @BindView(R.id.tv_group_owner_value)
    TextView tvGroupOwnerValue;
    @BindView(R.id.rl_group_owner)
    RelativeLayout rlGroupOwner;
    @BindView(R.id.clear_all_history)
    RelativeLayout clearAllHistory;
    @BindView(R.id.rl_change_group_name)
    RelativeLayout changeGroupNameLayout;
    @BindView(R.id.rl_blacklist)
    RelativeLayout blacklistLayout;
    @BindView(R.id.switch_btn)
    EaseSwitchButton switchButton;
    @BindView(R.id.rl_switch_block_groupmsg)
    RelativeLayout rlSwitchBlockGroupmsg;
    @BindView(R.id.rl_search)
    RelativeLayout rlSearch;
    @BindView(R.id.introduction)
    TextView introduction;
    @BindView(R.id.rl_introduction)
    RelativeLayout rlIntroduction;
    @BindView(R.id.btn_exit_grp)
    Button exitBtn;
    @BindView(R.id.btn_exitdel_grp)
    Button deleteBtn;
    private static final int REQUEST_CODE_ADD_USER = 0;
    private static final int REQUEST_CODE_EXIT = 1;
    private static final int REQUEST_CODE_EXIT_DELETE = 2;
    private static final int REQUEST_CODE_EDIT_GROUPNAME = 5;
    private static final int MOVE_TO_BLACKLIST = 6;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.member_list)
    RecyclerView memberList;
    @BindView(R.id.group_number)
    TextView groupNumber;
    private String groupId;
    private EMGroup group;
    private ProgressDialog progressDialog;
    private MemberAdapter memberAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
        groupId = getIntent().getStringExtra("groupId");
        EMClient.getInstance().groupManager().addGroupChangeListener(new EMGroupChangeListener() {
            @Override
            public void onInvitationReceived(String s, String s1, String s2, String s3) {
                if(s.equals(groupId))
                    refresh();
            }

            @Override
            public void onApplicationReceived(String s, String s1, String s2, String s3) {
                if(s.equals(groupId))
                    refresh();
            }

            @Override
            public void onApplicationAccept(String s, String s1, String s2) {
                if(s.equals(groupId))
                    refresh();
            }

            @Override
            public void onApplicationDeclined(String s, String s1, String s2, String s3) {
                if(s.equals(groupId))
                    refresh();
            }

            @Override
            public void onInvitationAccepted(String s, String s1, String s2) {
                if(s.equals(groupId))
                    refresh();
            }

            @Override
            public void onInvitationDeclined(String s, String s1, String s2) {
                if(s.equals(groupId))
                    refresh();
            }

            @Override
            public void onUserRemoved(String s, String s1) {
                 finish();
            }

            @Override
            public void onGroupDestroyed(String s, String s1) {
                 finish();
            }

            @Override
            public void onAutoAcceptInvitationFromGroup(String s, String s1, String s2) {
                if(s.equals(groupId))
                    refresh();
            }
        });
        refresh();
    }
    private void refresh(){
        subscription = Observable.create(new rx.Observable.OnSubscribe<EMGroup>() {
            @Override
            public void call(Subscriber<? super EMGroup> subscriber) {
                try {
                    subscriber.onNext(EMClient.getInstance().groupManager().getGroupFromServer(groupId));
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    subscriber.onNext(EMClient.getInstance().groupManager().getGroup(groupId));
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<EMGroup>() {
            @Override
            public void call(EMGroup emGroup) {
                group=emGroup;
                // we are not supposed to show the group if we don't find the group
                if (group == null) {
                    finish();
                    return;
                }
                introduction.setText(getString(R.string.Introduction) + ":" + group.getDescription());
                idText.setText(groupId);
                title.setText(group.getGroupName());
                groupNumber.setHint(group.getAffiliationsCount() + "人");
                if (group.getOwner() == null || "".equals(group.getOwner())
                        || !group.getOwner().equals(EMClient.getInstance().getCurrentUser())) {
                    exitBtn.setVisibility(View.GONE);
                    deleteBtn.setVisibility(View.GONE);
                    blacklistLayout.setVisibility(View.GONE);
                    changeGroupNameLayout.setVisibility(View.GONE);
                }
                // 如果自己是群主，显示解散按钮
                if (EMClient.getInstance().getCurrentUser().equals(group.getOwner())) {
                    exitBtn.setVisibility(View.GONE);
                    deleteBtn.setVisibility(View.VISIBLE);
                }
                memberAdapter = new MemberAdapter(GroupDetailActivity.this);
                List<String> groups=new ArrayList<>();
                int j;
                if(EMClient.getInstance().getCurrentUser().equals(group.getOwner())||group.isAllowInvites())
                    j=4;
                else
                    j=5;
                for(int i=0;i<(group.getAffiliationsCount()>j?j:group.getAffiliationsCount());i++)
                {
                    groups.add(group.getMembers().get(i));
                }
                memberAdapter.addAll(groups);
                memberAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {

                    }
                });
                if(EMClient.getInstance().getCurrentUser().equals(group.getOwner())||group.isAllowInvites())
                    memberAdapter.addFooter(new RecyclerArrayAdapter.ItemView() {
                        @Override
                        public View onCreateView(ViewGroup parent) {
                            return getLayoutInflater().inflate(R.layout.item_member, null);
                        }

                        @Override
                        public void onBindView(View headerView) {
                            ((CircleImageView) headerView.findViewById(R.id.avatar)).setImageResource(R.drawable.member_add);
                            headerView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    startActivityForResult(
                                            (new Intent(GroupDetailActivity.this, GroupPickContactsActivity.class).putExtra("groupId", groupId)),
                                            REQUEST_CODE_ADD_USER);
                                }
                            });
                        }
                    });
                memberList.setAdapter(memberAdapter);
                memberList.setLayoutManager(new GridLayoutManager(GroupDetailActivity.this, 5));
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String st1 = getResources().getString(R.string.being_added);
        String st2 = getResources().getString(R.string.is_quit_the_group_chat);
        String st3 = getResources().getString(R.string.chatting_is_dissolution);
        String st4 = getResources().getString(R.string.are_empty_group_of_news);
        String st5 = getResources().getString(R.string.is_modify_the_group_name);
        final String st6 = getResources().getString(R.string.Modify_the_group_name_successful);
        final String st7 = getResources().getString(R.string.change_the_group_name_failed_please);

        if (resultCode == RESULT_OK) {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(GroupDetailActivity.this);
                progressDialog.setMessage(st1);
                progressDialog.setCanceledOnTouchOutside(false);
            }
            switch (requestCode) {
                case REQUEST_CODE_ADD_USER:// 添加群成员
                    final String[] newmembers = data.getStringArrayExtra("newmembers");
                    progressDialog.setMessage(st1);
                    progressDialog.show();
                    addMembersToGroup(newmembers);
                    break;
                case REQUEST_CODE_EXIT: // 退出群
                    progressDialog.setMessage(st2);
                    progressDialog.show();
                    exitGroup();
                    break;
                case REQUEST_CODE_EXIT_DELETE: // 解散群
                    progressDialog.setMessage(st3);
                    progressDialog.show();
                    deleteGroup();
                    break;

                case REQUEST_CODE_EDIT_GROUPNAME: //修改群名称
                    final String returnData = data.getStringExtra("data");
                    if (!TextUtils.isEmpty(returnData)) {
                        progressDialog.setMessage(st5);
                        progressDialog.show();

                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    EMClient.getInstance().groupManager().changeGroupName(groupId, returnData);
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            title.setText(returnData);
                                            progressDialog.dismiss();
                                            Toast.makeText(getApplicationContext(), st6, Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                } catch (HyphenateException e) {
                                    e.printStackTrace();
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            progressDialog.dismiss();
                                            Toast.makeText(getApplicationContext(), st7, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        }).start();
                        setResult(REQUEST_CODE_EDIT_GROUPNAME, data);
                    }
                    break;
                case MOVE_TO_BLACKLIST:
                    if (resultCode == RESULT_OK) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    EMClient.getInstance().groupManager().getGroupFromServer(groupId);
                                } catch (HyphenateException e) {
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        group = EMClient.getInstance().groupManager().getGroup(groupId);

                                    }
                                });
                            }
                        }).start();

                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 解散群组
     *
     * @param groupId
     */
    private void deleteGroup() {
        final String st5 = getResources().getString(R.string.Dissolve_group_chat_tofail);
        new Thread(new Runnable() {
            public void run() {
                try {
                    EMClient.getInstance().groupManager().destroyGroup(groupId);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            LocalBroadcastManager.getInstance(GroupDetailActivity.this).sendBroadcast(new Intent(Constant.ACTION_GROUP_CHANAGED));
                            progressDialog.dismiss();
                            finish();
                            if (ChatActivity.activityInstance != null)
                                ChatActivity.activityInstance.finish();
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), st5 + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * 增加群成员
     *
     * @param newmembers
     */
    private void addMembersToGroup(final String[] newmembers) {
        final String st6 = getResources().getString(R.string.Add_group_members_fail);
        new Thread(new Runnable() {

            public void run() {
                try {
                    // 创建者调用add方法
                    if (EMClient.getInstance().getCurrentUser().equals(group.getOwner())) {
                        EMClient.getInstance().groupManager().addUsersToGroup(groupId, newmembers);
                    } else {
                        // 一般成员调用invite方法
                        EMClient.getInstance().groupManager().inviteUser(groupId, newmembers, null);
                    }
                    runOnUiThread(new Runnable() {
                        public void run() {
                            groupNumber.setHint(group.getAffiliationsCount() + "人");
                            progressDialog.dismiss();
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), st6 + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * 退出群组
     *
     * @param groupId
     */
    private void exitGroup() {
        String st1 = getResources().getString(R.string.Exit_the_group_chat_failure);
        new Thread(new Runnable() {
            public void run() {
                try {
                    EMClient.getInstance().groupManager().leaveGroup(groupId);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            LocalBroadcastManager.getInstance(GroupDetailActivity.this).sendBroadcast(new Intent(Constant.ACTION_GROUP_CHANAGED));
                            progressDialog.dismiss();
                            finish();
                            if (ChatActivity.activityInstance != null)
                                ChatActivity.activityInstance.finish();
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.Exit_the_group_chat_failure) + " " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void toggleBlockGroup() {
        if (switchButton.isSwitchOpen()) {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(GroupDetailActivity.this);
                progressDialog.setCanceledOnTouchOutside(false);
            }
            progressDialog.setMessage(getString(R.string.Is_unblock));
            progressDialog.show();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        EMClient.getInstance().groupManager().unblockGroupMessage(groupId);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                switchButton.closeSwitch();
                                progressDialog.dismiss();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), R.string.remove_group_of, Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }
            }).start();

        } else {
            String st8 = getResources().getString(R.string.group_is_blocked);
            final String st9 = getResources().getString(R.string.group_of_shielding);
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(GroupDetailActivity.this);
                progressDialog.setCanceledOnTouchOutside(false);
            }
            progressDialog.setMessage(st8);
            progressDialog.show();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        EMClient.getInstance().groupManager().blockGroupMessage(groupId);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                switchButton.openSwitch();
                                progressDialog.dismiss();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), st9, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
            }).start();
        }
    }

    /**
     * 清空群聊天记录
     */
    private void clearGroupHistory() {

        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(group.getGroupId(), EMConversation.EMConversationType.GroupChat);
        if (conversation != null) {
            conversation.clearAllMessages();
        }
        Toast.makeText(this, R.string.messages_are_empty, Toast.LENGTH_SHORT).show();
    }

    /**
     * 点击退出群组按钮
     *
     * @param view
     */
    public void exitGroup(View view) {
        startActivityForResult(new Intent(this, ExitGroupDialog.class), REQUEST_CODE_EXIT);

    }

    /**
     * 点击解散群组按钮
     *
     * @param view
     */
    public void exitDeleteGroup(View view) {
        startActivityForResult(new Intent(this, ExitGroupDialog.class).putExtra("deleteToast", getString(R.string.dissolution_group_hint)),
                REQUEST_CODE_EXIT_DELETE);

    }

    @OnClick({R.id.clear_all_history, R.id.rl_change_group_name, R.id.rl_blacklist, R.id.rl_switch_block_groupmsg, R.id.rl_search, R.id.rl_introduction, R.id.group_layout,R.id.member_list})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.clear_all_history:
                String st9 = getResources().getString(R.string.sure_to_empty_this);
                new EaseAlertDialog(GroupDetailActivity.this, null, st9, null, new EaseAlertDialog.AlertDialogUser() {

                    @Override
                    public void onResult(boolean confirmed, Bundle bundle) {
                        if (confirmed) {
                            clearGroupHistory();
                        }
                    }
                }, true).show();
                break;
            case R.id.rl_change_group_name:
                startActivityForResult(new Intent(this, EditActivity.class).putExtra("data", group.getGroupName()), REQUEST_CODE_EDIT_GROUPNAME);
                break;
            case R.id.rl_blacklist:
                startActivityForResult(new Intent(GroupDetailActivity.this, GroupBlacklistActivity.class).putExtra("groupId", groupId), MOVE_TO_BLACKLIST);
                break;
            case R.id.rl_switch_block_groupmsg:
                toggleBlockGroup();
                break;
            case R.id.rl_introduction:
                if (!group.getDescription().equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(group.getDescription());
                    AlertDialog alertDialog = builder.create();
                    alertDialog.setCanceledOnTouchOutside(true);
                    alertDialog.setCancelable(true);
                    alertDialog.show();
                }
                break;
            case R.id.group_layout:
                startActivity(new Intent(GroupDetailActivity.this,GroupMembersActivity.class).putStringArrayListExtra("members",(ArrayList<String>) group.getMembers()));
                break;
            case R.id.member_list:
                startActivity(new Intent(GroupDetailActivity.this,GroupMembersActivity.class).putStringArrayListExtra("members",(ArrayList<String>) group.getMembers()));
                break;
        }
    }
}
