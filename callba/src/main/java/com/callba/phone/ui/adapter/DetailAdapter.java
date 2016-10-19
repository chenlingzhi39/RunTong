package com.callba.phone.ui.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.callba.R;
import com.callba.phone.Constant;
import com.callba.phone.DemoHelper;
import com.callba.phone.bean.ContactMultiNumBean;
import com.callba.phone.bean.BaseUser;
import com.callba.phone.bean.EaseUser;
import com.callba.phone.manager.FullyLinearLayoutManager;
import com.callba.phone.manager.UserManager;
import com.callba.phone.ui.SelectDialPopupWindow;
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
 * Created by PC-20160514 on 2016/7/30.
 */
public class DetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ContactMultiNumBean bean;
    private ProgressDialog progressDialog;
    private String userrName, password;
    private Gson gson;
    private ContactNumberAdapter contactNumberAdapter;

    public DetailAdapter(ProgressDialog progressDialog, ContactMultiNumBean bean, Context context, String password, String userrName) {
        this.progressDialog = progressDialog;
        this.bean = bean;
        this.context = context;
        this.password = password;
        this.userrName = userrName;
        gson = new Gson();
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0)
            return new ViewHolder0(LayoutInflater.from(parent.getContext()).inflate(R.layout.header_detail, parent, false));
        else
            return new ViewHolder1(LayoutInflater.from(parent.getContext()).inflate(R.layout.body_detail, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position == 0) {
            ((ViewHolder0) holder).addFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressDialog = new ProgressDialog(context);
                    String stri = context.getResources().getString(R.string.Is_sending_a_request);
                    progressDialog.setMessage(stri);
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    OkHttpUtils
                            .post()
                            .url(Interfaces.ADD_FRIEND)
                            .addParams("loginName", userrName)
                            .addParams("loginPwd", password)
                            .addParams("phoneNumber", bean.getContactPhones().get(0))
                            .build()
                            .execute(new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e, int id) {
                                    e.printStackTrace();
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            progressDialog.dismiss();
                                            String s2 = context.getResources().getString(R.string.Request_add_buddy_failure);
                                            Toast.makeText(context, s2, 1).show();
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
                                                String s = context.getResources().getString(R.string.Add_a_friend);
                                                //EMClient.getInstance().contactManager().addContact(toAddUsername+"-callba", s);
                                                ((Activity) context).sendBroadcast(new Intent(Constant.ACTION_CONTACT_CHANAGED));
                                                OkHttpUtils
                                                        .post()
                                                        .url(Interfaces.GET_FRIENDS)
                                                        .addParams("loginName", userrName)
                                                        .addParams("loginPwd", password)
                                                        .build().execute(new StringCallback() {
                                                    @Override
                                                    public void onError(Call call, Exception e, int id) {
                                                        e.printStackTrace();
                                                    }

                                                    @Override
                                                    public void onResponse(String response, int id) {
                                                        try { Logger.i("get_result", response);
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
                                                                    user.setRemark(baseUser.getRemark());
                                                                    EaseCommonUtils.setUserInitialLetter(user);
                                                                    mList.add(user);
                                                                }
                                                                DemoHelper.getInstance().updateContactList(mList);
                                                                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constant.ACTION_CONTACT_CHANAGED));

                                                            } else {
                                                                Toast.makeText(context, result[1], Toast.LENGTH_SHORT).show();
                                                            }
                                                        } catch (Exception e) {
                                                            Toast.makeText(context, context.getString(R.string.getserverdata_exception), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressDialog.dismiss();
                                                        String s1 = "添加成功";
                                                        Toast.makeText(context, s1, 1).show();
                                                    }
                                                });
                                            } catch (final Exception e) {
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        progressDialog.dismiss();
                                                        String s2 = context.getResources().getString(R.string.Request_add_buddy_failure);
                                                        Toast.makeText(context, s2 + e.getMessage(), 1).show();
                                                    }
                                                });
                                            }
                                        } else {
                                            Toast.makeText(context, result[1], Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }
                                    } catch (Exception e) {
                                        Toast.makeText(context, context.getString(R.string.getserverdata_exception), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            });
            ((ViewHolder0) holder).inviteFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri smsToUri = Uri.parse("smsto://" + bean.getContactPhones().get(0));
                    Intent mIntent = new Intent(Intent.ACTION_SENDTO, smsToUri);
                    mIntent.putExtra("sms_body", "我是" + UserManager.getNickname(context) + "，"+context.getString(R.string.share_content));
                    context.startActivity(mIntent);
                }
            });
        } else {
            contactNumberAdapter = new ContactNumberAdapter(context);
            contactNumberAdapter.addAll(bean.getContactPhones());
            ((ViewHolder1) holder).numberList.setLayoutManager(new FullyLinearLayoutManager(context));
            ((ViewHolder1) holder).numberList.setAdapter(contactNumberAdapter);
            ((ViewHolder1) holder).numberList.addItemDecoration(new DividerItemDecoration(
                    context, DividerItemDecoration.VERTICAL_LIST));
            contactNumberAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    String phoneNum = contactNumberAdapter.getItem(position);
                    Intent intent = new Intent(context, SelectDialPopupWindow.class);
                    intent.putExtra("name", bean.getDisplayName());
                    intent.putExtra("number", phoneNum);
                    context.startActivity(intent);
                    //callUtils.judgeCallMode(getActivity(), phoneNum,bean.getDisplayName());
                }
            });
        }
    }

    class ViewHolder0 extends RecyclerView.ViewHolder {
        @InjectView(R.id.add_friend)
        Button addFriend;
        @InjectView(R.id.invite_friend)
        LinearLayout inviteFriend;

        public ViewHolder0(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }

    class ViewHolder1 extends RecyclerView.ViewHolder {
        @InjectView(R.id.number_list)
        RecyclerView numberList;

        public ViewHolder1(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }
}
