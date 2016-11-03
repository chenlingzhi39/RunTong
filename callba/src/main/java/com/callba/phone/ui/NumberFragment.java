package com.callba.phone.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.callba.R;
import com.callba.phone.MyApplication;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.ui.base.BaseSelectContactFragment;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.RxBus;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by PC-20160514 on 2016/5/18.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.fragment_number)
public class NumberFragment extends BaseSelectContactFragment {
    @BindView(R.id.number)
    TextView number;
    @BindView(R.id.address)
    TextView tv_address;
    @BindView(R.id.card)
    EditText card;
    @BindView(R.id.recharge)
    Button recharge;
    @BindView(R.id.change_number)
    LinearLayout change;
    @BindView(R.id.relative)
    RelativeLayout relative;

    public static NumberFragment newInstance() {
        NumberFragment numberFragment = new NumberFragment();
        return numberFragment;
    }


    @Override
    protected void initView(View fragmentRootView) {
        ButterKnife.bind(this, fragmentRootView);
        number.setText(getUsername());
        if(MyApplication.getInstance().detect())
        query(getUsername());
        else toast(R.string.conn_failed);
    }

    @OnClick(R.id.relative)
    public void change() {
        showDialog();
    }

    @OnClick(R.id.recharge)
    public void recharge() {
        if (card.getText().toString().equals(("")))
            toast(getString(R.string.input_card));
        else
        addRequestCall(OkHttpUtils.post().url(Interfaces.CALLDA_PAY)
                .addParams("phoneNumber", number.getText().toString())
                .addParams("cardNumber", card.getText().toString())
                .addParams("fromtel",getUsername())
                .addParams("softType", "android")
                .build()).execute(new StringCallback() {
            @Override
            public void onBefore(Request request, int id) {
                progressDialog = ProgressDialog.show(getActivity(), null, getString(R.string.on_recharge));
            }

            @Override
            public void onAfter(int id) {
               progressDialog.dismiss();
            }

            @Override
            public void onError(Call call, Exception e, int id) {
                showException(e);
            }

            @Override
            public void onResponse(String response, int id) {
               try{String[] result=response.split("\\|");
                   if(result[0].equals("0")){
                       RxBus.get().post("refresh_ad",true);
                   }
                 toast(result[1]);
               }catch (Exception e){
                   showException(e);
               }
            }
        });
    }

    @OnClick(R.id.contacts)
    public void onClick() {
        Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(i, 0);

    }


    public class DialogHelper implements DialogInterface.OnDismissListener {
        private Dialog mDialog;
        private View mView;
        private EditText change;

        public DialogHelper() {
            mView = getActivity().getLayoutInflater().inflate(R.layout.dialog_change_number, null);
            change = (EditText) mView.findViewById(R.id.et_change);
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

    public void showDialog() {
        final DialogHelper helper = new DialogHelper();
        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setView(helper.getView()).setTitle(getString(R.string.input_number))
                .setOnDismissListener(helper)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (helper.getNumber().length() > 10) {
                            number.setText(helper.getNumber());
                            query(helper.getNumber());
                        } else
                            toast("请输入正确的手机号!");
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
    public void query(final String number) {

        addRequestCall(OkHttpUtils.get().url("http://apis.juhe.cn/mobile/get")
                .addParams("phone", number)
                .addParams("key", "1dfd68c50bbf3f58755f1d537fe817a4")
                .build()).execute(new StringCallback() {
            @Override
            public void onBefore(Request request, int id) {
                progressDialog = ProgressDialog.show(getActivity(), "", "正在查询归属地");
            }

            @Override
            public void onAfter(int id) {
                progressDialog.dismiss();
            }

            @Override
            public void onError(Call call, Exception e, int id) {
                Toast.makeText(getActivity(), "查询归属地失败", Toast.LENGTH_SHORT).show();
                tv_address.setHint("");
            }

            @Override
            public void onResponse(String response, int id) {
                progressDialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getString("resultcode").equals("200")) {
                        final JSONObject result = new JSONObject(jsonObject.getString("result"));
                        String address = result.getString("company");
                        tv_address.setHint(address);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "查询归属地失败", Toast.LENGTH_SHORT).show();
                    tv_address.setHint("");
                }

            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 0:
                if (data == null) {
                    return;
                }
                Uri uri = data.getData();
                Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
                cursor.moveToFirst();

                String phone_number = getContactPhone(cursor);


                if (phone_number.length() > 10) {
                    number.setText(phone_number);
                    query(phone_number);
                } else
                    toast("请选择手机号!");
                break;

            default:
                break;
        }
    }
}
