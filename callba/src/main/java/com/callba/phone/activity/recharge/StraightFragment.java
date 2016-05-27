package com.callba.phone.activity.recharge;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.callba.R;
import com.callba.phone.BaseFragment;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Task;
import com.callba.phone.bean.UserDao;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.cfg.Constant;
import com.callba.phone.pay.alipay.AlipayClient;
import com.callba.phone.service.MainService;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.HttpUtils;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.util.NumberAddressService;
import com.callba.phone.view.CalldaToast;
import com.callba.phone.view.MyProgressDialog;

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
    private int size;
    public static StraightFragment newInstance() {
        StraightFragment straightFragment = new StraightFragment();
        return straightFragment;
    }
    @Override
    protected void initView(View fragmentRootView) {
        ButterKnife.inject(this, fragmentRootView);
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int mDensity = metrics.densityDpi;
        switch (mDensity){
            case 120:
                size=15;
                break;
            case 160:
                size=20;
                break;
            case 240:
                size=30;
                break;
            case 320:
                size=40;
                break;
            case 480:
                size=60;
                break;
            case 640:
                size=80;
                break;
        }
        Spannable spannable = new SpannableString("38元\n\n原价50元");
        spannable.setSpan(new AbsoluteSizeSpan(size), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new AbsoluteSizeSpan(size/2), spannable.toString().lastIndexOf("\n"), spannable.toString().length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        price1.setText(spannable);
        Spannable spannable1 = new SpannableString("398元\n\n原价500元");
        spannable1.setSpan(new AbsoluteSizeSpan(size), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable1.setSpan(new AbsoluteSizeSpan(size/2), spannable.toString().lastIndexOf("\n"), spannable1.toString().length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
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
    /**
     * 调用支付宝客户端支付
     */
    private void rechargeAlipayClient(final String payMoney) {
        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(),null, getString(R.string.rech_hqddh));
        final Handler mHanlder = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (progressDialog!=null&&progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                if (msg.what == 0) {
                    String orderNo = (String) msg.obj;

                    new AlipayClient(getActivity(), orderNo,
                            payMoney).prepared2Pay();
                } else if (msg.what == -1) {
                    String errorMsg = (String) msg.obj;
                    Toast.makeText(getActivity(),errorMsg,Toast.LENGTH_SHORT).show();
               /*     CalldaToast calldaToast = new CalldaToast();
                    calldaToast.showToast(context, errorMsg);*/
                }
            }
        };

        new Thread() {
            public void run() {
                ActivityUtil activityUtil=new ActivityUtil();
                Map<String, String> params = new HashMap<String, String>();
                params.put("loginName", CalldaGlobalConfig.getInstance().getUsername());
                params.put("loginPwd", CalldaGlobalConfig.getInstance().getPassword());
                params.put("softType", "android");
                params.put("payMoney", payMoney);
                params.put("payMethod", "0");
              /*  params.put("suiteName", suiteName);
                params.put("lan", lan);*/

                String result = null;
                Message msg = mHanlder.obtainMessage();
                try {
                    Logger.i("套餐客户端充值", Interfaces.GET_RECHARGE_TRADENO
                            + params);
                    result = HttpUtils.getDataFromHttpPost(
                            Interfaces.GET_RECHARGE_TRADENO, params);
                } catch (Exception e1) {
                    e1.printStackTrace();
                    msg.what = -1;
                    msg.obj = getString(R.string.getserverdata_exception);
                    mHanlder.sendMessage(msg);

                    return;
                }

                try {
                    String content[] = result.trim().split("\\|");
                    if ("0".equals(content[0])) {
                        String orderNo = content[1];

                        msg.what = 0;
                        msg.obj = orderNo;
                    } else if ("1".equals(content[0])) {
                        msg.what = -1;
                        msg.obj = content[1];
                    } else {
                        msg.what = -1;
                        msg.obj = getString(R.string.rech_hqddsb);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    msg.what = -1;
                    msg.obj = getString(R.string.getserverdata_exception);
                } finally {
                    mHanlder.sendMessage(msg);
                }
            }
        }.start();
    }
}
