package com.callba.phone.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.DemoHelper;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Coupon;
import com.callba.phone.bean.EaseUser;
import com.callba.phone.ui.adapter.EaseContactAdapter;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.widget.EaseSidebar;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by PC-20160514 on 2016/7/31.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.em_activity_group_pick_contacts,
        toolbarTitle = R.string.Select_the_contact,
        navigationId = R.drawable.press_back
)
public class SelectFriendActivity extends BaseActivity {
    @BindView(R.id.list)
    ListView list;
    @BindView(R.id.sidebar)
    EaseSidebar sidebar;
    @BindView(R.id.floating_header)
    TextView floatingHeader;
    private Coupon coupon;
    private ProgressDialog progressDialog;
    /**
     * 是否为单选
     */
    private boolean isSignleChecked = true;
    private PickContactAdapter contactAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        coupon = (Coupon) getIntent().getSerializableExtra("coupon");
        final List<EaseUser> alluserList = new ArrayList<>();
        for (EaseUser user : DemoHelper.getInstance().getContactList().values()) {
            alluserList.add(user);
        }
        // 对list进行排序
        Collections.sort(alluserList, new Comparator<EaseUser>() {

            @Override
            public int compare(EaseUser lhs, EaseUser rhs) {
                if (lhs.getInitialLetter().equals(rhs.getInitialLetter())) {
                    return lhs.getNick().compareTo(rhs.getNick());
                } else {
                    if ("#".equals(lhs.getInitialLetter())) {
                        return 1;
                    } else if ("#".equals(rhs.getInitialLetter())) {
                        return -1;
                    }
                    return lhs.getInitialLetter().compareTo(rhs.getInitialLetter());
                }

            }
        });
        contactAdapter = new PickContactAdapter(this, R.layout.ease_row_contact, alluserList);
        list.setAdapter(contactAdapter);
        ((EaseSidebar) findViewById(R.id.sidebar)).setListView(list);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
           /*   Intent intent=new Intent(SelectFriendActivity.this, FlowActivity.class);
                intent.putExtra("index",getIntent().getIntExtra("index",0));
                intent.putExtra("coupon",getIntent().getSerializableExtra("coupon"));
                intent.putExtra("number",alluserList.get(position).getUsername().substring(0,11));
                startActivity(intent);
                finish();*/
                AlertDialog.Builder builder = new AlertDialog.Builder(SelectFriendActivity.this);
                builder.setMessage("确认赠送给" + alluserList.get(position).getNick() + "?");
                builder.setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                OkHttpUtils.post().url(Interfaces.GIVE_COUPON)
                                        .addParams("loginName", getUsername())
                                        .addParams("loginPwd", getPassword())
                                        .addParams("cid", coupon.getCid())
                                        .addParams("phoneNumber", alluserList.get(position).getUsername().substring(0, 11))
                                        .build().execute(new StringCallback() {
                                    @Override
                                    public void onAfter(int id) {
                                        progressDialog.dismiss();
                                    }

                                    @Override
                                    public void onBefore(Request request, int id) {
                                        progressDialog = ProgressDialog.show(SelectFriendActivity.this, "", "请稍后");
                                    }

                                    @Override
                                    public void onError(Call call, Exception e, int id) {
                                        toast(R.string.network_error);
                                    }

                                    @Override
                                    public void onResponse(String response, int id) {
                                        try {Logger.i("give_result", response);
                                            String[] result = response.split("\\|");
                                            if (result[0].equals("0")) {
                                                toast(result[1]);
                                                setResult(RESULT_OK);
                                                finish();
                                            } else toast(result[1]);
                                        } catch (Exception e) {
                                            toast(R.string.getserverdata_exception);
                                        }
                                    }
                                });
                                dialog.dismiss();
                            }

                        });
                builder.setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(true);
                alertDialog.setCancelable(true);
                alertDialog.show();
            }
        });
    }

    /**
     * adapter
     */
    private class PickContactAdapter extends EaseContactAdapter {
        public PickContactAdapter(Context context, int resource, List<EaseUser> users) {
            super(context, resource, users);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
//			if (position > 0) {
            // 选择框checkbox
//			}
            return view;
        }
    }
}
