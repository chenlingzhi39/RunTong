package com.callba.phone.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.callba.R;
import com.callba.phone.bean.ContactMutliNumBean;
import com.callba.phone.ui.base.BaseFragment;
import com.callba.phone.Constant;
import com.callba.phone.DemoHelper;
import com.callba.phone.ui.adapter.ContactNumberAdapter;
import com.callba.phone.ui.adapter.RecyclerArrayAdapter;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.BaseUser;
import com.callba.phone.bean.EaseUser;
import com.callba.phone.manager.UserManager;
import com.callba.phone.util.CallUtils;
import com.callba.phone.util.EaseCommonUtils;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.widget.DividerItemDecoration;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import okhttp3.Call;

/**
 * Created by PC-20160514 on 2016/7/6.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.fragment_contact_detail
)
public class ContactDetailFragment extends BaseFragment {
    @InjectView(R.id.lv_phone_nums)
    RecyclerView lvPhoneNums;
    private ContactMutliNumBean bean;
    private ContactNumberAdapter contactNumberAdapter;
    //private DetailAdapter detailAdapter;
    CallUtils callUtils;
    Gson gson;
    View headerView;
    @Override
    protected void initView(View fragmentRootView) {
        ButterKnife.inject(this, fragmentRootView);
        bean = (ContactMutliNumBean) getArguments().get("contact");
        setDatatoAdapter();
        gson = new Gson();
       /* detailAdapter=new DetailAdapter(progressDialog,bean,getActivity(),getPassword(),getUsername());
        lvPhoneNums.setAdapter(detailAdapter);*/
    }


    private void setDatatoAdapter() {
        List<String> phoneNums = bean.getContactPhones();
        Logger.i("number", phoneNums.size() + "");
        contactNumberAdapter = new ContactNumberAdapter(getActivity());
        headerView = getActivity().getLayoutInflater().inflate(R.layout.header_contact_detail, null);
        contactNumberAdapter.addHeader(new RecyclerArrayAdapter.ItemView() {
            @Override
            public View onCreateView(ViewGroup parent) {
                return headerView;
            }

            @Override
            public void onBindView(View headerView) {
                getActivity().getLayoutInflater().inflate(R.layout.header_contact_detail, null);
                LinearLayout inviteFriend = (LinearLayout) headerView.findViewById(R.id.invite_friend);
                Button addFriend = (Button) headerView.findViewById(R.id.add_friend);
                addFriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progressDialog = new ProgressDialog(getActivity());
                        String stri = getResources().getString(R.string.Is_sending_a_request);
                        progressDialog.setMessage(stri);
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();
                        OkHttpUtils
                                .post()
                                .url(Interfaces.ADD_FRIEND)
                                .addParams("loginName", getUsername())
                                .addParams("loginPwd", getPassword())
                                .addParams("phoneNumber", bean.getContactPhones().get(0))
                                .build()
                                .execute(new StringCallback() {
                                    @Override
                                    public void onError(Call call, Exception e, int id) {
                                        e.printStackTrace();
                                        getActivity().runOnUiThread(new Runnable() {
                                            public void run() {
                                                progressDialog.dismiss();
                                                String s2 = getResources().getString(R.string.Request_add_buddy_failure);
                                                Toast.makeText(getActivity(), s2, 1).show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onResponse(String response, int id) {
                                        try { Logger.i("add_result", response);
                                        String[] result = response.split("\\|");
                                        if (result[0].equals("0")) {
                                            try {
                                                //demo写死了个reason，实际应该让用户手动填入
                                                String s = getResources().getString(R.string.Add_a_friend);
                                                //EMClient.getInstance().contactManager().addContact(toAddUsername+"-callba", s);
                                                getActivity().sendBroadcast(new Intent(Constant.ACTION_CONTACT_CHANAGED));
                                                OkHttpUtils
                                                        .post()
                                                        .url(Interfaces.GET_FRIENDS)
                                                        .addParams("loginName", getUsername())
                                                        .addParams("loginPwd", getPassword())
                                                        .build().execute(new StringCallback() {
                                                    @Override
                                                    public void onError(Call call, Exception e, int id) {
                                                        e.printStackTrace();
                                                    }

                                                    @Override
                                                    public void onResponse(String response, int id) {
                                                        try{
                                                        Logger.i("get_result", response);
                                                        String[] result = response.split("\\|");
                                                        if (result[0].equals("0")) {
                                                            ArrayList<BaseUser> list;
                                                            list = gson.fromJson(result[1], new TypeToken<ArrayList<BaseUser>>() {
                                                            }.getType());
                                                            List<EaseUser> mList = new ArrayList<EaseUser>();
                                                            for (BaseUser baseUser : list) {
                                                                EaseUser user = new EaseUser(baseUser.getPhoneNumber() + "-callba");
                                                                user.setAvatar(baseUser.getUrl_head());
                                                                user.setNick(baseUser.getNickname());
                                                                user.setSign(baseUser.getSign());
                                                                EaseCommonUtils.setUserInitialLetter(user);
                                                                mList.add(user);
                                                            }
                                                            DemoHelper.getInstance().updateContactList(mList);
                                                            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(Constant.ACTION_CONTACT_CHANAGED));

                                                        } else {
                                                            Toast.makeText(getActivity(), result[1], Toast.LENGTH_SHORT).show();
                                                        }}catch (Exception e){
                                                            toast(R.string.getserverdata_exception);
                                                        }
                                                    }
                                                });
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
                                        } else {
                                            toast(result[1]);
                                            progressDialog.dismiss();
                                        }
                                        }catch(Exception e){
                                            toast(R.string.getserverdata_exception);
                                        }
                                    }
                                });
                    }
                });
               inviteFriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri smsToUri = Uri.parse("smsto://" + bean.getContactPhones().get(0));
                        Intent mIntent = new Intent(Intent.ACTION_SENDTO, smsToUri);
                        mIntent.putExtra("sms_body", "我是" + UserManager.getNickname(getActivity()) + "，我正在使用CALL吧！ CALL吧“0月租”“0漫游”“通话不计分钟”，赶快加入我们吧！");
                        startActivity(mIntent);
                    }
                });
            }
        });
        contactNumberAdapter.addAll(phoneNums);
        lvPhoneNums.setAdapter(contactNumberAdapter);
        lvPhoneNums.addItemDecoration(new DividerItemDecoration(
                getActivity(), DividerItemDecoration.VERTICAL_LIST));
        callUtils = new CallUtils();

        contactNumberAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String phoneNum = contactNumberAdapter.getData().get(position);
                Intent intent = new Intent(getActivity(), SelectDialPopupWindow.class);
                intent.putExtra("name", bean.getDisplayName());
                intent.putExtra("number", phoneNum);
                startActivity(intent);
                //callUtils.judgeCallMode(getActivity(), phoneNum,bean.getDisplayName());
            }
        });
        // bean为空时，设置联系人编辑不可用

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }


}
