package com.callba.phone.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts;
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

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by PC-20160514 on 2016/7/25.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.fragment_number
)
public class NumberFragment2 extends BaseSelectContactFragment {
    @InjectView(R.id.number)
    TextView number;
    @InjectView(R.id.address)
    TextView tv_address;
    @InjectView(R.id.change_number)
    LinearLayout changeNumber;
    @InjectView(R.id.relative)
    RelativeLayout relative;
    @InjectView(R.id.card)
    EditText card;
    @InjectView(R.id.recharge)
    Button recharge;
    private ProgressDialog progressDialog;

    @Override
    protected void initView(View fragmentRootView) {
        ButterKnife.inject(this, fragmentRootView);
        number.setText(getUsername());
        query(getUsername());
    /*    card.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }

            private void hideKeyboard(View v) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                }
            }
        });*/
       /* card.requestFocus();
        Timer timer = new Timer(); //设置定时器
        timer.schedule(new TimerTask() {
            @Override
            public void run() { //弹出软键盘的代码
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInputFromWindow(card.getWindowToken(), 0, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }, 300); //设置300毫秒的时长*/
    }

    public static NumberFragment2 newInstance() {
        NumberFragment2 numberFragment = new NumberFragment2();
        return numberFragment;
    }

    public void query(final String number) {
        OkHttpUtils.get().url("http://apis.juhe.cn/mobile/get")
                .addParams("phone", number)
                .addParams("key", "1dfd68c50bbf3f58755f1d537fe817a4")
                .build().execute(new StringCallback() {
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
                Toast.makeText(getActivity(), "查询归属地失败", 1).show();
                tv_address.setHint("");
            }

            @Override
            public void onResponse(String response, int id) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getString("resultcode").equals("200")) {
                        JSONObject result = new JSONObject(jsonObject.getString("result"));
                        String address = result.getString("company");
                        tv_address.setHint(address);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "查询归属地失败", 1).show();
                    tv_address.setHint("");

                }

            }
        });
    /*  progressDialog=ProgressDialog.show(getActivity(),"","正在查询归属地");
      new Thread(new Runnable() {
          @Override
          public void run() {
              try {
                  final String address = AddressService.getAddress(getActivity(),number);
                  //把查询结果返回的归属地显示在textView上
                  getActivity().runOnUiThread(new Runnable() {
                      @Override
                      public void run() {
                          if(address.equals("没有此号码记录"))
                              tv_address.setHint(address);
                         else {String[] result=address.split(" ");
                              tv_address.setHint(result[2]);}
                          progressDialog.dismiss();
                      }
                  });
              } catch (Exception e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                  getActivity().runOnUiThread(new Runnable() {
                      @Override
                      public void run() {
                          Toast.makeText(getActivity(),"查询归属地失败", 1).show();
                          tv_address.setHint("");
                          progressDialog.dismiss();
                      }
                  });

              }
          }
      }).start();
*/
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @OnClick({R.id.contacts, R.id.relative, R.id.recharge})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.contacts:
                //Uri uri = Uri.parse("content://contacts/people");
                Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                //i.setType("vnd.android.cursor.dir/phone");
                startActivityForResult(i, 0);
                break;
            case R.id.relative:
                showDialog();
                break;
            case R.id.recharge:
                OkHttpUtils.post()
                        .url(Interfaces.FLOW_CARD)
                        .addParams("loginName", getUsername())
                        .addParams("loginPwd", getPassword())
                        .addParams("cardNumber", card.getText().toString())
                        .addParams("phoneNumber",number.getText().toString())
                        .build().execute(new StringCallback() {
                    @Override
                    public void onAfter(int id) {
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onBefore(Request request, int id) {
                        progressDialog = ProgressDialog.show(getActivity(), null, getString(R.string.on_recharge));
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Toast.makeText(getActivity(), R.string.network_error, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        try {
                            String[] results = response.split("\\|");
                            toast(results[1]);
                            if(results[1].equals("0"))
                            RxBus.get().post("refresh_ad",true);
                        } catch (Exception e) {
                            toast(R.string.getserverdata_exception);
                        }
                    }
                });
                break;
        }
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
