package com.callba.phone.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.callba.R;
import com.callba.phone.ui.base.BaseFragment;
import com.callba.phone.Constant;
import com.callba.phone.DemoHelper;
import com.callba.phone.ui.adapter.RecyclerArrayAdapter;
import com.callba.phone.ui.adapter.TeamAdapter;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.EaseUser;
import com.callba.phone.bean.Team;
import com.callba.phone.util.EaseCommonUtils;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.widget.DividerItemDecoration;
import com.callba.phone.widget.EaseAlertDialog;
import com.hyphenate.chat.EMClient;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import okhttp3.Call;

/**
 * Created by PC-20160514 on 2016/7/15.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.fragment_order
)
public class TeamFragment extends BaseFragment {
    @InjectView(R.id.list)
    RecyclerView list;
    @InjectView(R.id.hint)
    TextView hint;
    ArrayList<Team> teams;
    TeamAdapter teamAdapter;

    @Override
    protected void initView(View fragmentRootView) {
        ButterKnife.inject(this,fragmentRootView);
        teamAdapter = new TeamAdapter(getActivity());
        teams = (ArrayList<Team>) getArguments().getParcelableArrayList("list").get(0);
        list.addItemDecoration(new DividerItemDecoration(
                getActivity(), DividerItemDecoration.VERTICAL_LIST));
            teamAdapter.addAll(teams);
            list.setAdapter(teamAdapter);
        if(teams.size()==0)
         hint.setVisibility(View.VISIBLE);
        teamAdapter.setOnItemLongClickListener(new RecyclerArrayAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemClick(int position) {
                showDialog(teamAdapter.getData().get(position));
                return false;
            }
        });
        teamAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

            }
        });
    }
    private void showDialog(final Team entity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(entity.getNickname());
        builder.setItems(new String[] { getString(R.string.add_friend) },
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

                                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                                String stri = getResources().getString(R.string.Is_sending_a_request);
                                progressDialog.setMessage(stri);
                                progressDialog.setCanceledOnTouchOutside(false);
                                progressDialog.show();
                                OkHttpUtils
                                        .post()
                                        .url(Interfaces.ADD_FRIEND)
                                        .addParams("loginName", getUsername())
                                        .addParams("loginPwd",  getPassword())
                                        .addParams("phoneNumber",entity.getPhoneNumber())
                                        .build()
                                        .execute(new StringCallback() {
                                            @Override
                                            public void onError(Call call, Exception e, int id) {
                                                e.printStackTrace();
                                               getActivity(). runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressDialog.dismiss();
                                                        String s2 = getResources().getString(R.string.Request_add_buddy_failure);
                                                        Toast.makeText(getActivity(), s2 , 1).show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onResponse(String response, int id) {
                                                Logger.i("add_result",response);
                                                String[] result=response.split("\\|");
                                                if(result[0].equals("0"))
                                                {
                                                    try {
                                                        //demo写死了个reason，实际应该让用户手动填入
                                                        String s = getResources().getString(R.string.Add_a_friend);
                                                        //EMClient.getInstance().contactManager().addContact(entity.getPhoneNumber()+"-callba", s);
                                                        getActivity().sendBroadcast(new Intent(Constant.ACTION_CONTACT_CHANAGED));
                                                        List<EaseUser> mList = new ArrayList<EaseUser>();
                                                        EaseUser user = new EaseUser(entity.getPhoneNumber()+"-callba");
                                                        user.setAvatar(entity.getUrl_head());
                                                        user.setNick(entity.getNickname());
                                                        EaseCommonUtils.setUserInitialLetter(user);
                                                        mList.add(user);
                                                        DemoHelper.getInstance().updateContactList(mList);
                                                        //LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(Constant.ACTION_CONTACT_CHANAGED));
                                                        getActivity().runOnUiThread(new Runnable() {
                                                            public void run() {
                                                                progressDialog.dismiss();
                                                                String s1 = "添加成功";
                                                                Toast.makeText(getActivity(), s1, 1).show();
                                                            }
                                                        });
                                                    } catch (final Exception e) {
                                                        getActivity().runOnUiThread(new Runnable() {
                                                            public void run() {
                                                                progressDialog.dismiss();
                                                                String s2 = getResources().getString(R.string.Request_add_buddy_failure);
                                                                Toast.makeText(getActivity(), s2 + e.getMessage(), 1).show();
                                                            }
                                                        });
                                                    }
                                                }else { toast(result[1]);
                                                    progressDialog.dismiss();
                                                }
                                            }
                                        });

                                break;
                            default:
                                break;
                        }
                    }
                });
        builder.create().show();
    }
    @Override
    protected void lazyLoad() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
