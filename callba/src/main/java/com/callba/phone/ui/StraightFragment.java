package com.callba.phone.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Contacts;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.callba.R;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Commodity;
import com.callba.phone.bean.Coupon;
import com.callba.phone.bean.UserDao;
import com.callba.phone.cfg.Constant;
import com.callba.phone.ui.adapter.BillAdapter;
import com.callba.phone.ui.adapter.CouponSelectAdapter;
import com.callba.phone.ui.adapter.RadioAdapter;
import com.callba.phone.ui.base.BaseFragment;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.util.NumberAddressService;
import com.callba.phone.util.PayResult;
import com.callba.phone.util.SignUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
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
        contentViewId = R.layout.fragment_straight)
public class StraightFragment extends BaseFragment implements UserDao.PostListener {
    /*  @InjectView(R.id.price1)
      RadioButton price1;
      @InjectView(R.id.price2)
      RadioButton price2;*/
    @InjectView(R.id.number)
    TextView number;
    @InjectView(R.id.address)
    TextView tv_address;
    @InjectView(R.id.recharge)
    Button recharge;
    @InjectView(R.id.change_number)
    LinearLayout change;
    /*  @InjectView(R.id.group)
      RadioGroup group;*/
    @InjectView(R.id.relative)
    RelativeLayout relative;
    @InjectView(R.id.list)
    RecyclerView list;
    @InjectView(R.id.campaign)
    TextView campaign;
    @InjectView(R.id.bt_coupon)
    Button btCoupon;
    @InjectView(R.id.linear)
    LinearLayout linear;
    private UserDao userDao, userDao1;
    private String address;
    private int size;
    private String subject, body, price;
    private String outTradeNo;
    private ArrayList<Commodity> bills;
    private BillAdapter billAdapter;
    private CouponSelectAdapter couponSelectAdapter;
    private ArrayList<Coupon> coupons;
    private Coupon coupon;
    private Gson gson;
    private Dialog dialog;
    private Commodity bill;
    private HashMap<Integer, Integer> map = new HashMap<>();
    int bill_pos = 0, coupon_pos = 0;
    // 商户PID
    public static final String PARTNER = "2088221931971814";
    // 商户收款账号
    public static final String SELLER = "callba@aihuzhongchou.com";
    // 商户私钥，pkcs8格式
    public static final String RSA_PRIVATE =
            "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAOtUqNpqQuxOq174" +
                    "AoOGhEbRauzhp/fXaxp4CFBjZNntfSpHJ31kJ1Gi2SyQupA3VHUNXZfelMafdDjV" +
                    "3NVdNo1M4WOu+8PY4ugVQdKnvbbvV2ui40y8l1suI/WI6DORowOZjAg+XNTys5bk" +
                    "0ZtyHfluM/Eb9vmkRu8dyhUH0WQPAgMBAAECgYEA0qPro9z7XBMql438igfMvKrU" +
                    "P4XYWCIszvtjzbi528sUftRDx5vvCcZvB9Hf/Bhac49sF/T2TDcLy5e53A1cNjSP" +
                    "VjPPg/iUq74sC47YivCfyfalOYZVpwnTrsHB6gmCEPz0zmaPSyOGoJvimxQ2AObc" +
                    "blbHS51WGoi5IkpnfXECQQD4BQfqrMrg4nSRpLmD4gh3JchQNhnLxDdD9HZSD9kM" +
                    "L3xjq/Mp1wkVMCa1f6Iemp8tzJ9GvaXUiYlvzv/ebXxXAkEA8ucbsW5JHUAkq1uq" +
                    "KZ0ne+5DWd8/52XK9udTcfdp2tDwS/9klT3q0gGlbJK8mMdwqV7uWnFY2oQTavn7" +
                    "nLUDCQJAVgnwwCFnU/JbO/coAC9WfnbV8bWC52RPQ7y3myoyQn7qqO0KsvYNCZOl" +
                    "qgr346QCGnJEwtahg4Se7/GgY7oZiwJBAIGshWk8skWuV6Uvg3FB17FeqpAREgGL" +
                    "o0Yair690cIiZxZ7WoweCP1iKZkD4TFCz89rwZ2BA2lstx0WJZAsRlkCQEXelOoa" +
                    "VOQHaRxGyHVeBXlniTD4R+PWuLh/T3sBxIJzUb1VgasvKlvSQomv+v/cKwd9rJCj" +
                    "4WwHfl+tSIazTbM=";
    // 支付宝公钥
    public static final String RSA_PUBLIC = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";
    private static final int SDK_PAY_FLAG = 1;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    PayResult payResult = new PayResult((String) msg.obj);
                    /**
                     * 同步返回的结果必须放置到服务端进行验证（验证的规则请看https://doc.open.alipay.com/doc2/
                     * detail.htm?spm=0.0.0.0.xdvAU6&treeId=59&articleId=103665&
                     * docType=1) 建议商户依赖异步通知
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                    if (TextUtils.equals(resultStatus, "9000")) {
                        Toast.makeText(getActivity(), "支付成功", Toast.LENGTH_SHORT).show();
                        if (map.get(billAdapter.getmSelectedItem()) != 0 && coupons.size() > 1) {
                            coupons.remove((int) map.get(billAdapter.getmSelectedItem()));
                            map.put(billAdapter.getmSelectedItem(), 0);
                            coupon = coupons.get(0);
                            btCoupon.setText(coupon.getTitle());
                            if (coupons.size() == 1) {
                                btCoupon.setVisibility(View.GONE);
                                coupons.clear();
                            }
                        }
                        //userDao1.pay(getUsername(), getPassword(), outTradeNo, "success");
                        Map<String, String> paramsMap = new HashMap<String, String>();
                        paramsMap.put("tradeNo", outTradeNo);
                        paramsMap.put("price", price);
                        MobclickAgent.onEvent(getActivity(), "pay_success", paramsMap);
                        OkHttpUtils.post().addParams("loginName", getUsername())
                                .addParams("loginPwd", getPassword())
                                .addParams("orderNumber", outTradeNo)
                                .addParams("payResult", "success")
                                .build()
                                .execute(new StringCallback() {
                                    @Override
                                    public void onAfter(int id) {

                                    }

                                    @Override
                                    public void onBefore(Request request, int id) {

                                    }

                                    @Override
                                    public void onError(Call call, Exception e, int id) {

                                    }

                                    @Override
                                    public void onResponse(String response, int id) {
                                        try {
                                            String[] result = response.split("\\|");
                                            toast(result[1]);
                                        } catch (Exception e) {
                                        }
                                    }
                                });
                    } else {
                        // 判断resultStatus 为非"9000"则代表可能支付失败
                        // "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                        if (TextUtils.equals(resultStatus, "8000")) {
                            Toast.makeText(getActivity(), "支付结果确认中", Toast.LENGTH_SHORT).show();

                        } else {
                            // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                            Toast.makeText(getActivity(), "支付失败", Toast.LENGTH_SHORT).show();
                            //  userDao1.pay(getUsername(), getPassword(), outTradeNo, "failure");
                        }
                    }
                    break;
                }
                default:
                    break;
            }
        }

        ;
    };

    public static StraightFragment newInstance() {
        StraightFragment straightFragment = new StraightFragment();
        return straightFragment;
    }

    @Override
    protected void initView(View fragmentRootView) {
        ButterKnife.inject(this, fragmentRootView);
        gson = new Gson();
        number.setText(getUsername());
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int mDensity = metrics.densityDpi;
        switch (mDensity) {
            case 120:
                size = 15;
                break;
            case 160:
                size = 20;
                break;
            case 240:
                size = 30;
                break;
            case 320:
                size = 40;
                break;
            case 480:
                size = 60;
                break;
            case 640:
                size = 80;
                break;
        }
     /*   Spannable spannable = new SpannableString("39元\n\n（包月畅聊）\n原价150元");
        spannable.setSpan(new AbsoluteSizeSpan(size), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new AbsoluteSizeSpan(size / 2), 4, spannable.toString().length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        price1.setText(spannable);
        Spannable spannable1 = new SpannableString("399元\n\n（包年畅聊）\n原价1500元");
        spannable1.setSpan(new AbsoluteSizeSpan(size), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable1.setSpan(new AbsoluteSizeSpan(size / 2), 5, spannable1.toString().length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        price2.setText(spannable1);
        number.setText(getUsername());*/
        String address = NumberAddressService.getAddress(
                getUsername(), Constant.DB_PATH,
                getActivity());
        tv_address.setHint(address);
        userDao = new UserDao(getActivity(), this);
        subject = "39元套餐";
        body = "包月畅聊";
        price = "39";
      /*  group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.price1:
                        subject = "39元套餐";
                        body = "包月畅聊";
                        price = "39";
                        break;
                    case R.id.price2:
                        subject = "399元套餐";
                        body = "包年畅聊";
                        price = "399";
                        break;
                }
            }
        });*/

        getBills();
    }


    @Override
    public void start() {

    }

    @Override
    public void success(String msg) {
        outTradeNo = msg;
        pay();
    }

    @Override
    public void failure(String msg) {
        toast(msg);

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.inject(this, rootView);
        return rootView;
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

                String phone_number = cursor.getString(cursor.getColumnIndexOrThrow(Contacts.Phones.NUMBER));

                if (phone_number.length() > 10) {
                    number.setText(phone_number);
                    String address = NumberAddressService.getAddress(
                            phone_number, Constant.DB_PATH,
                            getActivity());
                    tv_address.setHint(address);
                } else
                    toast("请选择手机号!");
                break;

            default:
                break;
        }
    }


    @OnClick({R.id.relative, R.id.contacts, R.id.bt_coupon, R.id.recharge})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.relative:
                showDialog();
                break;
            case R.id.contacts:
                Uri uri = Uri.parse("content://contacts/people");
                Intent i = new Intent(Intent.ACTION_PICK, uri);
                i.setType("vnd.android.cursor.dir/phone");
                startActivityForResult(i, 0);
                break;
            case R.id.bt_coupon:
                showCouponsDialog(coupons);
                break;
            case R.id.recharge:
              /*  if(map.get(billAdapter.getmSelectedItem())!=0&&coupons.size()>1)
                {Logger.i("coupon_position",map.get(billAdapter.getmSelectedItem())+"");
                    coupons.remove((int)map.get(billAdapter.getmSelectedItem()));
                    map.put(billAdapter.getmSelectedItem(),0);
                    coupon=coupons.get(0);
                    btCoupon.setText(coupon.getTitle());
                    if(coupons.size()==1){
                        btCoupon.setVisibility(View.GONE);
                        coupons.clear();
                    }
                }*/
                Map<String, String> params = new HashMap<>();
                params.put("loginName", getUsername());
                params.put("phoneNumber", number.getText().toString());
                params.put("loginPwd", getPassword());
                params.put("softType", "android");
                params.put("payMethod", "0");
                params.put("iid", bill.getIid());
                if (coupon != null)
                    if (coupon.getCid() != null) {
                        params.put("cid", coupon.getCid());
                    }
                OkHttpUtils.post().url(Interfaces.PAY_ORDER)
                        .params(params)
                        .build()
                        .execute(new StringCallback() {
                            @Override
                            public void onAfter(int id) {
                                progressDialog.dismiss();
                            }

                            @Override
                            public void onBefore(Request request, int id) {
                                progressDialog = ProgressDialog.show(getActivity(), "", "正在获取价格");
                            }

                            @Override
                            public void onError(Call call, Exception e, int id) {
                                e.printStackTrace();
                                toast(getString(R.string.network_error));
                            }

                            @Override
                            public void onResponse(String response, int id) {
                                try {
                                    Logger.i("trade_result", response);
                                    String[] results = response.split("\\|");
                                    if (results[0].equals("0")) {
                                        Logger.i("trade", results[1]);
                                        String[] results1 = results[1].split(",");
                                        outTradeNo = results1[0];
                                        price = results1[1];
                                        pay();
                                    } else {
                                        toast(results[1]);
                                    }
                                } catch (Exception e) {
                                    toast(R.string.getserverdata_exception);
                                }
                            }
                        });
                //userDao.setOrder(getUsername(), getPassword(), price, "0", "callback-unlimit-" + price, number.getText().toString());
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
                            address = NumberAddressService.getAddress(
                                    helper.getNumber(), Constant.DB_PATH,
                                    getActivity());
                            //if (!address.equals("")) {
                            number.setText(helper.getNumber());
                            tv_address.setHint(address);
                          /*  } else
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

    /**
     * call alipay sdk pay. 调用SDK支付
     */
    public void pay() {

        String orderInfo = getOrderInfo(bill.getPrice() + "元套餐", bill.getTitle(), price);

        /**
         * 特别注意，这里的签名逻辑需要放在服务端，切勿将私钥泄露在代码中！
         */
        String sign = sign(orderInfo);
        Logger.i("sign", sign);
        try {
            /**
             * 仅需对sign 做URL编码
             */
            sign = URLEncoder.encode(sign, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        /**
         * 完整的符合支付宝参数规范的订单信息
         */
        final String payInfo = orderInfo + "&sign=\"" + sign + "\"&" + getSignType();

        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                // 构造PayTask 对象
                PayTask alipay = new PayTask(getActivity());
                // 调用支付接口，获取支付结果
                String result = alipay.pay(payInfo, true);

                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };

        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    /**
     * create the order info. 创建订单信息
     */
    private String getOrderInfo(String subject, String body, String price) {

        // 签约合作者身份ID
        String orderInfo = "partner=" + "\"" + PARTNER + "\"";

        // 签约卖家支付宝账号
        orderInfo += "&seller_id=" + "\"" + SELLER + "\"";

        // 商户网站唯一订单号
        orderInfo += "&out_trade_no=" + "\"" + outTradeNo + "\"";

        // 商品名称
        orderInfo += "&subject=" + "\"" + subject + "\"";

        // 商品详情
        orderInfo += "&body=" + "\"" + body + "\"";

        // 商品金额
        orderInfo += "&total_fee=" + "\"" + price + "\"";

        // 服务器异步通知页面路径
        orderInfo += "&notify_url=" + "\"" + "http://inter.boboit.cn/inter/pay/alipay_notify_url.jsp?loginName=" + getUsername() + "&loginPwd=" + getPassword() + "\"";

        // 服务接口名称， 固定值
        orderInfo += "&service=\"mobile.securitypay.pay\"";

        // 支付类型， 固定值
        orderInfo += "&payment_type=\"1\"";

        // 参数编码， 固定值
        orderInfo += "&_input_charset=\"utf-8\"";

        // 设置未付款交易的超时时间
        // 默认30分钟，一旦超时，该笔交易就会自动被关闭。
        // 取值范围：1m～15d。
        // m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
        // 该参数数值不接受小数点，如1.5h，可转换为90m。
        orderInfo += "&it_b_pay=\"3d\"";

        // extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
        // orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

        // 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
        orderInfo += "&return_url=\"m.alipay.com\"";

        // 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
        // orderInfo += "&paymethod=\"expressGateway\"";

        return orderInfo;
    }

    /**
     * get the out_trade_no for an order. 生成商户订单号，该值在商户端应保持唯一（可自定义格式规范）
     */
    private String getOutTradeNo() {
        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss", Locale.getDefault());
        Date date = new Date();
        String key = format.format(date);

        Random r = new Random();
        key = key + r.nextInt();
        key = key.substring(0, 15);
        return key;
    }

    /**
     * sign the order info. 对订单信息进行签名
     *
     * @param content 待签名订单信息
     */
    private String sign(String content) {
        return SignUtils.sign(content, RSA_PRIVATE);
    }

    /**
     * get the sign type we use. 获取签名方式
     */
    private String getSignType() {
        return "sign_type=\"RSA\"";
    }

    public void getBills() {
        OkHttpUtils.post().url(Interfaces.COMMODITY_INFO)
                .addParams("loginName", getUsername())
                .addParams("loginPwd", getPassword())
                .addParams("itemType", "0")
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                linear.setVisibility(View.GONE);
                if (e instanceof UnknownHostException) {
                    toast(R.string.conn_failed);
                } else toast(R.string.network_error);
            }

            @Override
            public void onResponse(String response, int id) {
                try {
                    Logger.i("bill_result", response);
                    String[] result = response.split("\\|");
                    if (result[0].equals("0")) {
                        bills = gson.fromJson(result[1], new TypeToken<ArrayList<Commodity>>() {
                        }.getType());
                        if (bills.get(0).getActivity().size() > 0)
                            campaign.setText("活动:" + bills.get(0).getActivity().get(0).getContent());
                        for (int i = 0; i < bills.size(); i++)
                            if (bills.get(i).getCoupon().size() > 0) {
                                bills.get(i).getCoupon().add(0, new Coupon("不使用优惠券"));
                                if (getArguments().getString("cid") != null)
                                    for (int j = 0; j < bills.get(i).getCoupon().size(); j++) {
                                        if (bills.get(i).getCoupon().get(j).getCid() != null)
                                            if (bills.get(i).getCoupon().get(j).getCid().equals(getArguments().getString("cid"))) {
                                                bill_pos = i;
                                                coupon_pos = j;
                                            }
                                    }
                            }
                        bill = bills.get(bill_pos);
                        coupons = bill.getCoupon();
                        if (coupons.size() > 0) {
                            btCoupon.setVisibility(View.VISIBLE);
                            btCoupon.setText(coupons.get(coupon_pos).getTitle());
                            coupon = coupons.get(coupon_pos);
                        } else btCoupon.setVisibility(View.GONE);
                        billAdapter = new BillAdapter(getActivity(), bills, size);
                        billAdapter.setmSelectedItem(bill_pos);
                        list.setLayoutManager(new GridLayoutManager(getActivity(), 2));
                        list.setAdapter(billAdapter);
                        map.clear();
                        map.put(billAdapter.getmSelectedItem(), coupon_pos);
                        billAdapter.setOnItemClickListener(new RadioAdapter.ItemClickListener() {
                            @Override
                            public void onClick(int position) {
                                bill = bills.get(position);
                                if (bills.get(position).getActivity().size() > 0)
                                    campaign.setText("活动:" + bills.get(position).getActivity().get(0).getContent());
                                else campaign.setText("");
                                coupons = bills.get(position).getCoupon();
                                if (coupons.size() > 0) {
                                    btCoupon.setVisibility(View.VISIBLE);
                                    if (map.get(position) != null) {
                                        coupon = coupons.get(map.get(position));
                                        btCoupon.setText(coupons.get(map.get(position)).getTitle());
                                    } else {
                                        coupon = coupons.get(0);
                                        btCoupon.setText(coupons.get(0).getTitle());
                                    }
                                } else btCoupon.setVisibility(View.GONE);

                            }
                        });
                        linear.setVisibility(View.VISIBLE);
                    } else {toast(result[1]);
                        linear.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    linear.setVisibility(View.GONE);
                    e.printStackTrace();
                    toast(R.string.getserverdata_exception);
                }
            }
        });
    }

    public void showCouponsDialog(ArrayList<Coupon> coupons) {
        final CouponDialogHelper helper = new CouponDialogHelper(coupons);
        dialog = new AlertDialog.Builder(getActivity())
                .setView(helper.getView()).setTitle("选择优惠券")
                .setOnDismissListener(helper)
                .create();
        helper.setDialog(dialog);
        dialog.show();
    }

    public class CouponDialogHelper implements DialogInterface.OnDismissListener {
        private Dialog mDialog;
        private View mView;
        private RecyclerView list;

        public CouponDialogHelper(final ArrayList<Coupon> coupons) {
            mView = getActivity().getLayoutInflater().inflate(R.layout.dialog_list, null);
            list = (RecyclerView) mView.findViewById(R.id.list);
            couponSelectAdapter = new CouponSelectAdapter(getActivity(), coupons);
            if (map.get(billAdapter.getmSelectedItem()) != null)
                couponSelectAdapter.setmSelectedItem(map.get(billAdapter.getmSelectedItem()));
            list.setAdapter(couponSelectAdapter);
            couponSelectAdapter.setOnItemClickListener(new RadioAdapter.ItemClickListener() {
                @Override
                public void onClick(int position) {
                    btCoupon.setText(coupons.get(position).getTitle());
                    coupon = coupons.get(position);
                    map.put(billAdapter.getmSelectedItem(), position);
                    mDialog.dismiss();
                }
            });
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
}
