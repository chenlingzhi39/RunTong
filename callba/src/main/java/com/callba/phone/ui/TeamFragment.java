package com.callba.phone.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.DemoHelper;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Team;
import com.callba.phone.ui.adapter.RecyclerArrayAdapter;
import com.callba.phone.ui.adapter.TeamAdapter;
import com.callba.phone.ui.base.BaseFragment;
import com.callba.phone.util.Logger;
import com.callba.phone.widget.DividerItemDecoration;
import com.callba.phone.widget.EaseAlertDialog;
import com.hyphenate.chat.EMClient;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by PC-20160514 on 2016/7/15.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.fragment_order
)
public class TeamFragment extends BaseFragment {
    @BindView(R.id.list)
    RecyclerView list;
    @BindView(R.id.hint)
    TextView hint;
    ArrayList<Team> teams;
    TeamAdapter teamAdapter;

    @Override
    protected void initView(View fragmentRootView) {
        ButterKnife.bind(this,fragmentRootView);
        teamAdapter = new TeamAdapter(getActivity());
        teams = (ArrayList<Team>) getArguments().getParcelableArrayList("list").get(0);
        Logger.i("team_size",teams.size()+"");
        list.addItemDecoration(new DividerItemDecoration(
                getActivity(), DividerItemDecoration.VERTICAL_LIST));
            teamAdapter.addAll(teams);
            list.setAdapter(teamAdapter);
        if(teams.size()==0)
         hint.setVisibility(View.VISIBLE);
        teamAdapter.setOnItemLongClickListener(new RecyclerArrayAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemClick(int position) {
                showDialog(teamAdapter.getItem(position));
                return false;
            }
        });
        teamAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                startActivity(new Intent(getActivity(),UserInfoActivity.class).putExtra("username",teams.get(position).getPhoneNumber()+"-callba"));
            }
        });
    }
    private void showDialog(final Team entity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(entity.getNickname());
        builder.setItems(new String[]{getString(R.string.add_friend)},
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                if(EMClient.getInstance().getCurrentUser().equals(entity.getPhoneNumber()+"-callba")){
                                    new EaseAlertDialog(getActivity(), R.string.not_add_myself).show();
                                    return;
                                }

                                if(DemoHelper.getInstance().getContactList().containsKey(entity.getPhoneNumber()+"-callba")){
                                    //提示已在好友列表中(在黑名单列表里)，无需添加
                                    if(EMClient.getInstance().contactManager().getBlackListUsernames().contains(entity.getPhoneNumber()+"-callba")){
                                        new EaseAlertDialog(getActivity(), R.string.user_already_in_contactlist).show();
                                        return;
                                    }
                                    new EaseAlertDialog(getActivity(), R.string.This_user_is_already_your_friend).show();
                                    return;
                                }
                                showDialog(entity.getPhoneNumber()+"-callba");
                                break;
                            default:
                                break;
                        }
                    }
                });
        builder.create().show();
    }
    public class DialogHelper implements DialogInterface.OnDismissListener {
        private Dialog mDialog;
        private View mView;
        private EditText change;

        public DialogHelper() {
            mView = getActivity().getLayoutInflater().inflate(R.layout.dialog_change_number, null);
            change = (EditText) mView.findViewById(R.id.et_change);
            change.setInputType(InputType.TYPE_CLASS_TEXT);
            change.requestFocus();
            Timer timer = new Timer(); //设置定时器
            timer.schedule(new TimerTask() {
                @Override
                public void run() { //弹出软键盘的代码
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
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

    public void showDialog(final String username) {
        final DialogHelper helper = new DialogHelper();
        Dialog dialog = new android.support.v7.app.AlertDialog.Builder(getActivity())
                .setView(helper.getView())
                .setTitle("请输入验证信息")
                .setOnDismissListener(helper)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        progressDialog = new ProgressDialog(getActivity());
                        String stri = getResources().getString(R.string.Is_sending_a_request);
                        progressDialog.setMessage(stri);
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();

                        new Thread(new Runnable() {
                            public void run() {

                                try {
                                    //demo写死了个reason，实际应该让用户手动填入
                                    String s = helper.getNumber();
                                    EMClient.getInstance().contactManager().addContact(username, s);
                                    getActivity().runOnUiThread(new Runnable() {
                                        public void run() {
                                            progressDialog.dismiss();
                                            String s1 = getResources().getString(R.string.send_successful);
                                           toast(s1);
                                        }
                                    });
                                } catch (final Exception e) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        public void run() {
                                            progressDialog.dismiss();
                                            String s2 = getResources().getString(R.string.Request_add_buddy_failure);
                                            toast(s2 + e.getMessage());
                                        }
                                    });
                                }
                            }
                        }).start();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        helper.setDialog(dialog);
        dialog.show();
    }
}
