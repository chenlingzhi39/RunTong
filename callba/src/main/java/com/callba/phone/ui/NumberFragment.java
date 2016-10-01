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

import com.callba.R;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.cfg.Constant;
import com.callba.phone.ui.base.BaseSelectContactFragment;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.NumberAddressService;
import com.callba.phone.util.RxBus;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by PC-20160514 on 2016/5/18.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.fragment_number)
public class NumberFragment extends BaseSelectContactFragment {
    @InjectView(R.id.number)
    TextView number;
    @InjectView(R.id.address)
    TextView tv_address;
    @InjectView(R.id.card)
    EditText card;
    @InjectView(R.id.recharge)
    Button recharge;
    @InjectView(R.id.change_number)
    LinearLayout change;
    @InjectView(R.id.relative)
    RelativeLayout relative;
    private String address;

    public static NumberFragment newInstance() {
        NumberFragment numberFragment = new NumberFragment();
        return numberFragment;
    }


    @Override
    protected void initView(View fragmentRootView) {
        ButterKnife.inject(this, fragmentRootView);
        number.setText(getUsername());
        String address = NumberAddressService.getAddress(
                getUsername(), Constant.DB_PATH,
                getActivity());
        tv_address.setHint(address);

      /*  card.setOnFocusChangeListener(new View.OnFocusChangeListener() {

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
      /*  card.requestFocus();
        Timer timer = new Timer(); //设置定时器
        timer.schedule(new TimerTask() {
            @Override
            public void run() { //弹出软键盘的代码
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInputFromWindow(card.getWindowToken(), 0, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }, 300); //设置300毫秒的时长*/
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
        OkHttpUtils.post().url(Interfaces.CALLDA_PAY)
                .addParams("phoneNumber", number.getText().toString())
                .addParams("cardNumber", card.getText().toString())
                .addParams("fromtel",getUsername())
                .addParams("softType", "android")
                .build().execute(new StringCallback() {
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


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @OnClick(R.id.contacts)
    public void onClick() {
        //Uri uri = Uri.parse("content://contacts/people");
        Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        //i.setType("vnd.android.cursor.dir/phone");
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
                            address = NumberAddressService.getAddress(
                                    helper.getNumber(), Constant.DB_PATH,
                                    getActivity());
                            //if (!address.equals("")) {
                            number.setText(helper.getNumber());
                            tv_address.setHint(address);
                           /* } else
                                toast("请输入正确的手机号!");*/
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
                    address = NumberAddressService.getAddress(
                            phone_number, Constant.DB_PATH,
                            getActivity());
                    //if (!address.equals("")) {
                    number.setText(phone_number);
                    tv_address.setHint(address);
                           /* } else
                                toast("请输入正确的手机号!");*/
                } else
                    toast("请选择手机号!");
                break;

            default:
                break;
        }
    }
}
