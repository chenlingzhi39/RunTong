package com.callba.phone.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.callba.R;
import com.callba.phone.DemoHelper;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.BaseUser;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.widget.EaseAlertDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hyphenate.chat.EMClient;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by PC-20160514 on 2016/6/22.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.add_contact,
        toolbarTitle = R.string.add_friend,
        navigationId = R.drawable.press_back,
        menuId = R.menu.menu_add_contact
)
public class AddContactActivity extends BaseActivity {
    @BindView(R.id.edit_note)
    EditText editText;
    @BindView(R.id.avatar)
    ImageView avatar;
    @BindView(R.id.name)
    TextView nameText;
    @BindView(R.id.indicator)
    Button indicator;
    @BindView(R.id.ll_user)
    LinearLayout searchedUserLayout;
    String toAddUsername;
    ProgressDialog progressDialog;
    Gson gson=new Gson();
    BaseUser baseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }
    /**
     * 查找contact
     * @param v
     */
    public void searchContact() {
        final String name = editText.getText().toString();


            toAddUsername = name;
            if(TextUtils.isEmpty(name)) {
                new EaseAlertDialog(this, R.string.Please_enter_a_username).show();
                return;
            }

            // TODO 从服务器获取此contact,如果不存在提示不存在此用户
        OkHttpUtils.post().url(Interfaces.USER_INFO)
                .addParams("loginName",getUsername())
                .addParams("loginPwd",getPassword())
                .addParams("phoneNumber",name)
                .build().execute(new StringCallback() {
            @Override
            public void onAfter(int id) {
             progressDialog.dismiss();
            }

            @Override
            public void onBefore(Request request, int id) {
             progressDialog=ProgressDialog.show(AddContactActivity.this,"","正在查找用户");
            }

            @Override
            public void onError(Call call, Exception e, int id) {
              showException(e);
            }

            @Override
            public void onResponse(String response, int id) {
                {
                    try {
                        String[] result = response.split("\\|");
                        if (result[0].equals("0")) {
                            baseUser = gson.fromJson(result[1], new TypeToken<BaseUser>() {
                            }.getType());
                           if(!TextUtils.isEmpty(baseUser.getUrl_head()))
                               Glide.with(AddContactActivity.this).load(baseUser.getUrl_head()).into(avatar);
                            nameText.setText(baseUser.getNickname());
                            //服务器存在此用户，显示此用户和添加按钮
                            searchedUserLayout.setVisibility(View.VISIBLE);
                        } else toast(result[1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                        toast(R.string.getserverdata_exception);
                    }
                }
            }
        });



        }


    /**
     *  添加contact
     * @param view
     */
    public void addContact(View view){
      showDialog();
       /* OkHttpUtils
                .post()
                .url(Interfaces.ADD_FRIEND)
                .addParams("loginName", getUsername())
                .addParams("loginPwd",  getPassword())
                .addParams("phoneNumber",toAddUsername)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        e.printStackTrace();
                        showException(e);
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        try { Logger.i("add_result",response);
                        String[] result=response.split("\\|");
                        if(result[0].equals("0")){
                        try {
                            //demo写死了个reason，实际应该让用户手动填入
                            String s = getResources().getString(R.string.Add_a_friend);
                            //EMClient.getInstance().contactManager().addContact(toAddUsername+"-callba", s);
                            sendBroadcast(new Intent(Constant.ACTION_CONTACT_CHANAGED));
                            OkHttpUtils
                                    .post()
                                    .url(Interfaces.GET_FRIENDS)
                                    .addParams("loginName", getUsername())
                                    .addParams("loginPwd",  getPassword())
                                    .build().execute(new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e, int id) {
                                    e.printStackTrace();
                                }

                                @Override
                                public void onResponse(String response, int id) {
                                    try{ Logger.i("get_result",response);
                                    String[] result = response.split("\\|");
                                    if (result[0].equals("0")) {
                                        ArrayList<BaseUser> list;
                                        list = gson.fromJson(result[1], new TypeToken<ArrayList<BaseUser>>() {
                                        }.getType());
                                        List<EaseUser> mList = new ArrayList<EaseUser>();
                                        for (BaseUser baseUser : list) {
                                            EaseUser user = new EaseUser(baseUser.getPhoneNumber()+"-callba");
                                            user.setAvatar(baseUser.getUrl_head());
                                            user.setNick(baseUser.getNickname());
                                            user.setSign(baseUser.getSign());
                                            user.setRemark(baseUser.getRemark());
                                            EaseCommonUtils.setUserInitialLetter(user);
                                            mList.add(user);
                                        }
                                        DemoHelper.getInstance().updateContactList(mList);
                                        LocalBroadcastManager.getInstance(AddContactActivity.this).sendBroadcast(new Intent(Constant.ACTION_CONTACT_CHANAGED));

                                    }
                                    }catch (Exception e){
                                        toast(R.string.getserverdata_exception);
                                    }
                                }
                            });
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    progressDialog.dismiss();
                                    String s1 = "添加成功";
                                    Toast.makeText(getApplicationContext(), s1, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (final Exception e) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    progressDialog.dismiss();
                                    String s2 = getResources().getString(R.string.Request_add_buddy_failure);
                                    Toast.makeText(getApplicationContext(), s2 + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }else { toast(result[1]);
                            progressDialog.dismiss();
                        }
                        }catch (Exception e){
                            toast(R.string.getserverdata_exception);
                        }
                    }
                });*/
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
                        Logger.i("phoneNumber",baseUser.getPhoneNumber()+"");
                        if(EMClient.getInstance().getCurrentUser().equals(toAddUsername+"-callba")){
                            new EaseAlertDialog(AddContactActivity.this, R.string.not_add_myself).show();
                            return;
                        }

                        if(DemoHelper.getInstance().getContactList().containsKey(toAddUsername+"-callba")){
                            //提示已在好友列表中(在黑名单列表里)，无需添加
                            if(EMClient.getInstance().contactManager().getBlackListUsernames().contains(toAddUsername+"-callba")){
                                new EaseAlertDialog(AddContactActivity.this, R.string.user_already_in_contactlist).show();
                                return;
                            }
                            new EaseAlertDialog(AddContactActivity.this, R.string.This_user_is_already_your_friend).show();
                            return;
                        }

                        progressDialog = new ProgressDialog(AddContactActivity.this);
                        String stri = getResources().getString(R.string.Is_sending_a_request);
                        progressDialog.setMessage(stri);
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();

                        new Thread(new Runnable() {
                            public void run() {

                                try {
                                    //demo写死了个reason，实际应该让用户手动填入
                                    String s = helper.getNumber();
                                    EMClient.getInstance().contactManager().addContact(toAddUsername+"-callba", s);
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            progressDialog.dismiss();
                                            String s1 = getResources().getString(R.string.send_successful);
                                            Toast.makeText(getApplicationContext(), s1, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } catch (final Exception e) {
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            progressDialog.dismiss();
                                            String s2 = getResources().getString(R.string.Request_add_buddy_failure);
                                            Toast.makeText(getApplicationContext(), s2 + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.find:
                searchContact();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
