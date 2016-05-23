package com.callba.phone.activity.recharge;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseFragment;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Task;
import com.callba.phone.bean.UserDao;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.cfg.Constant;
import com.callba.phone.service.MainService;
import com.callba.phone.util.NumberAddressService;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by PC-20160514 on 2016/5/18.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.fragment_straight)
public class StraightFragment extends BaseFragment implements UserDao.PostListener{
    @InjectView(R.id.price1)
    RadioButton price1;
    @InjectView(R.id.price2)
    RadioButton price2;
    @InjectView(R.id.number)
    TextView number;
    @InjectView(R.id.address)
    TextView tv_address;
    @InjectView(R.id.recharge)
    Button recharge;
    @InjectView(R.id.change_number)
    LinearLayout change;
    private UserDao userDao;
    private String address;

    public static StraightFragment newInstance() {
        StraightFragment straightFragment = new StraightFragment();
        return straightFragment;
    }
    @Override
    protected void initView(View fragmentRootView) {
        ButterKnife.inject(this, fragmentRootView);
        Spannable spannable = new SpannableString("38元\n\n原价50元");
        spannable.setSpan(new AbsoluteSizeSpan(60), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new AbsoluteSizeSpan(30), spannable.toString().lastIndexOf("\n"), spannable.toString().length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        price1.setText(spannable);
        Spannable spannable1 = new SpannableString("398元\n\n原价500元");
        spannable1.setSpan(new AbsoluteSizeSpan(60), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable1.setSpan(new AbsoluteSizeSpan(30), spannable.toString().lastIndexOf("\n"), spannable1.toString().length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        price2.setText(spannable1);
        number.setText(CalldaGlobalConfig.getInstance().getUsername());
        String address = NumberAddressService.getAddress(
                CalldaGlobalConfig.getInstance().getUsername(), Constant.DB_PATH,
                getActivity());
        tv_address.setText(address);
        userDao=new UserDao(getActivity(),this);
        userDao.getRechargeMeal(CalldaGlobalConfig.getInstance().getUsername(),CalldaGlobalConfig.getInstance().getPassword());
    }

    @Override
    public void start() {

    }

    @Override
    public void success(String msg) {

    }

    @Override
    public void failure(String msg) {

    }

    @OnClick(R.id.change_number)
    public void change() {
        showDialog();
    }

    @OnClick(R.id.recharge)
    public void recharge() {

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
                .setView(helper.getView())
                .setOnDismissListener(helper)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (helper.getNumber().length() > 10) {
                            address = NumberAddressService.getAddress(
                                    helper.getNumber(), Constant.DB_PATH,
                                    getActivity());
                            if (!address.equals("")) {
                                number.setText(helper.getNumber());
                                tv_address.setText(address);
                            } else
                                toast("请输入正确的手机号!");
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

}
