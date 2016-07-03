package com.callba.phone.activity.recharge;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseFragment;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.UserDao;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.cfg.Constant;
import com.callba.phone.util.NumberAddressService;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by PC-20160514 on 2016/5/18.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.fragment_number)
public  class NumberFragment extends BaseFragment implements UserDao.PostListener {
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
    private UserDao userDao;
    private String address;


    public static NumberFragment newInstance() {
        NumberFragment numberFragment = new NumberFragment();
        return numberFragment;
    }

    @Override
    protected void lazyLoad() {

    }

    @Override
    protected void initView(View fragmentRootView) {
        ButterKnife.inject(this, fragmentRootView);
        number.setText(CalldaGlobalConfig.getInstance().getUsername());
        String address = NumberAddressService.getAddress(
                CalldaGlobalConfig.getInstance().getUsername(), Constant.DB_PATH,
                getActivity());
        tv_address.setHint(address);
        userDao = new UserDao(getActivity(), this);

        card.setOnFocusChangeListener(new View.OnFocusChangeListener() {

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
        });

    }

    @Override
    public void start() {
        progressDialog = ProgressDialog.show(getActivity(), null, getString(R.string.on_recharge));
    }

    @Override
    public void success(String msg) {
        toast(msg);
        if (progressDialog != null) progressDialog.dismiss();
    }

    @Override
    public void failure(String msg) {
        toast(msg);
        if (progressDialog != null) progressDialog.dismiss();
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
            userDao.recharge(number.getText().toString(), card.getText().toString(), CalldaGlobalConfig.getInstance().getUsername());
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }



    public class DialogHelper implements DialogInterface.OnDismissListener {
        private Dialog mDialog;
        private View mView;
        private EditText change;

        public DialogHelper() {
            mView = getActivity().getLayoutInflater().inflate(R.layout.dialog_change_number, null);
            change = (EditText) mView.findViewById(R.id.et_change);
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
    public void onStop() {
        super.onStop();
        Log.i("number", "onStop");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("number", "onPause");
    }
}
