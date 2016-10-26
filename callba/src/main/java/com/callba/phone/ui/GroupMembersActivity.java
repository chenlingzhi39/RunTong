package com.callba.phone.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.widget.TextView;
import android.widget.Toast;

import com.callba.R;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.ContactMultiNumBean;
import com.callba.phone.bean.EaseUser;
import com.callba.phone.bean.Member;
import com.callba.phone.pinyin.CharacterParser;
import com.callba.phone.ui.adapter.GroupMemberAdapter;
import com.callba.phone.ui.adapter.RecyclerArrayAdapter;
import com.callba.phone.ui.adapter.expandRecyclerviewadapter.StickyRecyclerHeadersDecoration;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.util.ContactsAccessPublic;
import com.callba.phone.util.EaseUserUtils;
import com.callba.phone.util.Logger;
import com.callba.phone.widget.DividerDecoration;
import com.callba.phone.widget.SideBar;
import com.hyphenate.EMGroupChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by PC-20160514 on 2016/10/25.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.activity_group_member,
        toolbarTitle = R.string.group_member,
        navigationId = R.drawable.press_back
)
public class GroupMembersActivity extends BaseActivity {

    @BindView(R.id.member_list)
    RecyclerView memberList;
    @BindView(R.id.sidebar)
    SideBar sidebar;
    @BindView(R.id.dialog)
    TextView dialog;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;
    private GroupMemberAdapter groupMemberAdapter;
    private ArrayList<Member> members;
    private EMGroupChangeListener emGroupChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
        emGroupChangeListener = new EMGroupChangeListener() {
            @Override
            public void onInvitationReceived(String s, String s1, String s2, String s3) {

            }

            @Override
            public void onApplicationReceived(String s, String s1, String s2, String s3) {

            }

            @Override
            public void onApplicationAccept(String s, String s1, String s2) {

            }

            @Override
            public void onApplicationDeclined(String s, String s1, String s2, String s3) {

            }

            @Override
            public void onInvitationAccepted(String s, String s1, String s2) {

            }

            @Override
            public void onInvitationDeclined(String s, String s1, String s2) {

            }

            @Override
            public void onUserRemoved(String s, String s1) {
                if (s.equals(getIntent().getStringExtra("group_id")))
                    finish();
            }

            @Override
            public void onGroupDestroyed(String s, String s1) {
                if (s.equals(getIntent().getStringExtra("group_id")))
                    finish();
            }

            @Override
            public void onAutoAcceptInvitationFromGroup(String s, String s1, String s2) {
                if (s.equals(getIntent().getStringExtra("group_id")))
                    refresh();
            }
        };
        EMClient.getInstance().groupManager().addGroupChangeListener(emGroupChangeListener);
        refreshLayout.setEnabled(false);
        sidebar.setTextView(dialog);
        sidebar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                int position = groupMemberAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    ((LinearLayoutManager) memberList.getLayoutManager()).scrollToPositionWithOffset(position, memberList.getLayoutManager().getChildCount());
                }
            }
        });
        groupMemberAdapter = new GroupMemberAdapter(this);
        groupMemberAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                startActivityForResult(new Intent(GroupMembersActivity.this, GroupUserInfoActivity.class).putExtra("username", groupMemberAdapter.getData().get(position).getName()).putExtra("group_id",getIntent().getStringExtra("group_id")),0);
            }
        });
        groupMemberAdapter.setOnItemLongClickListener(new RecyclerArrayAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemClick(int position) {
              if(getIntent().getStringExtra("group_owner").equals(EMClient.getInstance().getCurrentUser())){
                  if(!groupMemberAdapter.getItem(position).getName().equals(EMClient.getInstance().getCurrentUser()))
                      showDeleteDialog(groupMemberAdapter.getItem(position));
              }
                return false;
            }
        });
        members = new ArrayList<>();
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        memberList.setLayoutManager(layoutManager);
        memberList.setAdapter(groupMemberAdapter);
        final StickyRecyclerHeadersDecoration headersDecor = new StickyRecyclerHeadersDecoration(groupMemberAdapter);
        memberList.addItemDecoration(headersDecor);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int mDensity = metrics.densityDpi;
        Logger.i("density", mDensity + "");
        memberList.addItemDecoration(new DividerDecoration(this, mDensity * 50 / 160));
        //   setTouchHelper();
        groupMemberAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                headersDecor.invalidateHeaders();
            }
        });
        refresh();
    }

    public void refresh() {
        members.clear();
        for (String s : getIntent().getStringArrayListExtra("members")) {
            Member member = new Member();
            member.setName(s);
            String pinyin = CharacterParser.getInstance().getSelling(EaseUserUtils.getUserNick(s));
            String sortString = pinyin.substring(0, 1).toUpperCase();
            Logger.i("sortString", sortString.toUpperCase());
            if (s.equals(getIntent().getStringExtra("group_owner")))
                member.setSortLetters("$");
            else {
                if (sortString.matches("[A-Z]")) {
                    member.setSortLetters(sortString.toUpperCase());
                } else {
                    member.setSortLetters("#");
                }
            }
            members.add(member);
        }
        Collections.sort(members, new Comparator<Member>() {
            @Override
            public int compare(Member member, Member t1) {
                if (member.getSortLetters().equals("@")
                        || t1.getSortLetters().equals("#")) {
                    return -1;
                } else if (member.getSortLetters().equals("#")
                        || t1.getSortLetters().equals("@")) {
                    return 1;
                } else {
                    return member.getSortLetters().compareTo(t1.getSortLetters());
                }
            }
        });
        groupMemberAdapter.clear();
        groupMemberAdapter.addAll(members);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EMClient.getInstance().groupManager().removeGroupChangeListener(emGroupChangeListener);
    }
    private void showDeleteDialog(final Member member) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(new String[]{getString(R.string.remove)+"\""+ EaseUserUtils.getUserNick(member.getName())+"\"",getString(R.string.move_to_black_list)},
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                deleteMembersFromGroup(member);
                                break;
                            case 1:
                                addUserToBlackList(member);
                                break;
                            default:
                                break;
                        }
                    }
                });
        builder.create().show();
    }
    /**
     * 删除群成员
     *
     * @param member
     */
    protected void deleteMembersFromGroup(final Member member) {
        final ProgressDialog deleteDialog = new ProgressDialog(GroupMembersActivity.this);
        deleteDialog.setMessage(getString(R.string.Are_removed));
        deleteDialog.setCanceledOnTouchOutside(false);
        deleteDialog.show();
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    // 删除被选中的成员
                    EMClient.getInstance().groupManager().removeUserFromGroup(getIntent().getStringExtra("group_id"), member.getName());
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            groupMemberAdapter.remove(member);
                            setResult(RESULT_OK);
                            deleteDialog.dismiss();
                        }
                    });
                } catch (final Exception e) {
                    deleteDialog.dismiss();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), getString(R.string.Delete_failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        }).start();
    }

    protected void addUserToBlackList(final Member member) {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage(getString(R.string.Are_moving_to_blacklist));
        pd.show();
        new Thread(new Runnable() {
            public void run() {
                try {
                    EMClient.getInstance().groupManager().blockUser(getIntent().getStringExtra("group_id"), member.getName());
                    runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                            groupMemberAdapter.remove(member);
                            setResult(RESULT_OK);
                            Toast.makeText(getApplicationContext(), R.string.Move_into_blacklist_success, Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (HyphenateException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                            Toast.makeText(getApplicationContext(), R.string.failed_to_move_into, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
       if(resultCode==RESULT_OK)
           groupMemberAdapter.notifyDataSetChanged();
    }
}
