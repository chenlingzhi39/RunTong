package com.callba.phone.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Contacts;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.callba.R;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Coupon;
import com.callba.phone.bean.Flow;
import com.callba.phone.bean.UserDao;
import com.callba.phone.ui.adapter.FlowAdapter;
import com.callba.phone.ui.adapter.RadioAdapter;
import com.callba.phone.ui.base.BaseFragment;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.util.PayResult;
import com.callba.phone.util.SignUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
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
        contentViewId = R.layout.flow
)
public class StraightFragment2 extends BaseFragment {

    @InjectView(R.id.number)
    TextView number;
    @InjectView(R.id.address)
    TextView tv_address;
    @InjectView(R.id.flow_list)
    RecyclerView flowList;
    @InjectView(R.id.flow_name)
    TextView flowName;
    @InjectView(R.id.now_price_local)
    TextView nowPriceLocal;
    @InjectView(R.id.past_price_local)
    TextView pastPriceLocal;
    @InjectView(R.id.now_price_nation)
    TextView nowPriceNation;
    @InjectView(R.id.past_price_nation)
    TextView pastPriceNation;
    @InjectView(R.id.content)
    LinearLayout content;
    @InjectView(R.id.use_coupon)
    CheckBox useCoupon;
    @InjectView(R.id.coupon)
    LinearLayout ll_coupon;
    @InjectView(R.id.coupon_title)
    TextView couponTitle;
    private String subject, body, price;
    private String outTradeNo;
    private String flowValue;
    ArrayList<Flow> flows, seperateFlows;
    private Gson gson;
    FlowAdapter flowAdapter;
    private String iid;
    private boolean is_coupon;
    private boolean has_iid;
    private boolean has_coupon;
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
    private UserDao userDao;
    private Coupon coupon;

    public static StraightFragment2 newInstance() {
        StraightFragment2 straightFragment = new StraightFragment2();
        return straightFragment;
    }

    @Override
    protected void initView(View fragmentRootView) {
        ButterKnife.inject(this, fragmentRootView);
      /*  final List<Flow> flows =new ArrayList<>();
        flows.add(new Flow("100M","7.50","9.50","10.0","10.0"));
        flows.add(new Flow("200M","16.00","19.0","20.0","20.0"));
        flows.add(new Flow("300M","16.00","19.0","20.0","20.0"));
        flows.add(new Flow("500M","22.50","28.50","30.0","30.0"));
        flows.add(new Flow("1000M","37.50","47.50","50.0","50.0"));
        flows.add(new Flow("2000M","55.00","65.50","70.0","70.0"));
        flowName.setText(flows.get(0).getName() + "流量包");
        nowPriceLocal.setText(flows.get(0).getNow_price_local()+"元");
        pastPriceLocal.setHint(flows.get(0).getPast_price_local()+"元");
        nowPriceNation.setText(flows.get(0).getNow_price_nation()+"元");
        pastPriceNation.setHint(flows.get(0).getPast_price_nation()+"元");
        flowAdapter = new FlowAdapter(getActivity(), flows);*/
        flowList.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        // flowList.setAdapter(flowAdapter);

    /*    if (getArguments().getString("number") != null) {
            query(getArguments().getString("number"));
            number.setText(getArguments().getString("number"));
        } else {*/
        query(getUsername());
        number.setText(getUsername());
        // }
        /*flowAdapter.setOnItemClickListener(new RadioAdapter.ItemClickListener() {
            @Override
            public void onClick(int position) {
                flowName.setText(flows.get(position).getName() + "流量包");
                nowPriceLocal.setText(flows.get(position).getNow_price_local()+"元");
                pastPriceLocal.setHint(flows.get(position).getPast_price_local()+"元");
                nowPriceNation.setText(flows.get(position).getNow_price_nation()+"元");
                pastPriceNation.setHint(flows.get(position).getPast_price_nation()+"元");
            }
        });*/
        pastPriceLocal.getPaint().setAntiAlias(true);//抗锯齿
        pastPriceLocal.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG); //中划线
        pastPriceLocal.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        pastPriceNation.getPaint().setAntiAlias(true);//抗锯齿
        pastPriceNation.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG); //中划线
        pastPriceNation.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        userDao = new UserDao(getActivity(), new UserDao.PostListener() {
            @Override
            public void start() {
                progressDialog = ProgressDialog.show(getActivity(), null,
                        "正在验证支付结果");
            }

            @Override
            public void success(String msg) {
                progressDialog.dismiss();
                try {
                    String[] result = msg.split("\\|");
                    if (result[0].equals("0")) {
                        toast(result[1]);
               /* getActivity().sendBroadcast(new Intent("com.callba.pay"));
                getActivity().finish();*/
                    } else if (result.length > 1) toast(result[1]);
                } catch (Exception e) {
                    progressDialog.dismiss();
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("充值失败，请重试");
                    builder.setPositiveButton("重试",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    userDao.pay(getUsername(), getPassword(), outTradeNo, "success");
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.setCancelable(false);
                    alertDialog.show();
                }
            }

            @Override
            public void failure(String msg) {
                progressDialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("充值失败，请重试");
                builder.setPositiveButton("重试",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                userDao.pay(getUsername(), getPassword(), outTradeNo, "success");
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.setCancelable(false);
                alertDialog.show();
                //toast("充值失败，请联系客服");
            }
        });
        body = "包月流量";
        gson = new Gson();
        seperateFlows = new ArrayList<>();
        useCoupon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                is_coupon = isChecked;
            }
        });
        has_iid = getArguments().getSerializable("coupon") != null;
        if (has_iid) {
            coupon = (Coupon) getArguments().getSerializable("coupon");
            couponTitle.setText(coupon.getTitle());
        }
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
                Uri uri = Uri.parse("content://contacts/people");
                Intent i = new Intent(Intent.ACTION_PICK, uri);
                i.setType("vnd.android.cursor.dir/phone");
                startActivityForResult(i, 0);
                break;
            case R.id.relative:
                showDialog();
                break;
            case R.id.recharge:
                if (!is_coupon) {
                    Logger.i("flow_url", Interfaces.FLOW_ORDER + "?loginName=" + getUsername() + "&phoneNumber=" + number.getText().toString() + "&loginPwd=" + getPassword() + "&flowValue=" + flowValue + "&softType=android&payMethod=0");
                    OkHttpUtils.post().url(Interfaces.FLOW_ORDER)
                            .addParams("loginName", getUsername())
                            .addParams("phoneNumber", number.getText().toString())
                            .addParams("loginPwd", getPassword())
                            .addParams("flowValue", flowValue)
                            .addParams("softType", "android")
                            .addParams("payMethod", "0")
                            .addParams("iid", iid).build()
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
                } else {
                    OkHttpUtils.post().url(Interfaces.FLOW_ORDER)
                            .addParams("loginName", getUsername())
                            .addParams("phoneNumber", number.getText().toString())
                            .addParams("loginPwd", getPassword())
                            .addParams("flowValue", flowValue)
                            .addParams("softType", "android")
                            .addParams("cid", coupon.getCid())
                            .addParams("payMethod", "0")
                            .addParams("iid", coupon.getIid())
                            .addParams("iid2", coupon.getIid2())
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
                }
                break;
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.inject(this, rootView);
        return rootView;
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

                content.setVisibility(View.GONE);
            }

            @Override
            public void onResponse(String response, int id) {
                progressDialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getString("resultcode").equals("200")) {
                        final JSONObject result = new JSONObject(jsonObject.getString("result"));
                        final String address = result.getString("company");
                        tv_address.setHint(address);
                        OkHttpUtils.post().url(Interfaces.FLOW_ITEM).addParams("loginPwd", getPassword()).addParams("loginName", getUsername())
                                .build().execute(new StringCallback() {
                            @Override
                            public void onBefore(Request request, int id) {
                                progressDialog = ProgressDialog.show(getActivity(), "", "正在获取流量包信息");
                            }

                            @Override
                            public void onAfter(int id) {
                                progressDialog.dismiss();
                            }

                            @Override
                            public void onError(Call call, Exception e, int id) {
                                toast(getContext().getString(R.string.network_error));
                                content.setVisibility(View.GONE);
                            }

                            @Override
                            public void onResponse(String response, int id) {
                                try {
                                    Logger.i("flow_result", response);
                                    String[] results = response.split("\\|");
                                    if (results[0].equals("0")) {
                                        flows = gson.fromJson(results[1], new TypeToken<ArrayList<Flow>>() {
                                        }.getType());
                                        seperateFlows.clear();
                                        for (Flow flow : flows) {
                                            if (address.contains(flow.getOperators())) {
                                                seperateFlows.add(flow);
                                            }
                                        }
                                        has_coupon = false;
                                        if (coupon != null) {
                                            for (int i = 0; i < seperateFlows.size(); i++) {
                                                if (seperateFlows.get(i).getIid().equals(coupon.getIid2())) {
                                                    flowAdapter = new FlowAdapter(getActivity(), seperateFlows);
                                                    flowAdapter.setmSelectedItem(i);
                                                    flowList.setAdapter(flowAdapter);
                                                    iid = seperateFlows.get(i).getIid();
                                                    flowName.setText(seperateFlows.get(i).getTitle());
                                                    flowValue = seperateFlows.get(i).getFlowValue();
                                                    price = seperateFlows.get(i).getPrice();
                                                    subject = seperateFlows.get(i).getTitle();
                                                    nowPriceNation.setText(seperateFlows.get(i).getPrice() + "元");
                                                    pastPriceNation.setHint(seperateFlows.get(i).getOldPrice() + "元");
                                                    ll_coupon.setVisibility(View.VISIBLE);
                                                    is_coupon = true;
                                                    has_coupon = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (!has_coupon) {
                                            iid = seperateFlows.get(0).getIid();
                                            flowName.setText(seperateFlows.get(0).getTitle());
                                            flowValue = seperateFlows.get(0).getFlowValue();
                                            price = seperateFlows.get(0).getPrice();
                                            subject = seperateFlows.get(0).getTitle();
                                            nowPriceNation.setText(seperateFlows.get(0).getPrice() + "元");
                                            pastPriceNation.setHint(seperateFlows.get(0).getOldPrice() + "元");
                                            flowAdapter = new FlowAdapter(getActivity(), seperateFlows);
                                            flowAdapter.setmSelectedItem(0);
                                            flowList.setAdapter(flowAdapter);
                                            if (has_iid)
                                                if (iid.equals(coupon.getIid2())) {
                                                    ll_coupon.setVisibility(View.VISIBLE);
                                                    is_coupon = true;
                                                } else {
                                                    ll_coupon.setVisibility(View.GONE);
                                                    is_coupon = false;
                                                }
                                        }
                                        flowAdapter.setOnItemClickListener(new RadioAdapter.ItemClickListener() {
                                            @Override
                                            public void onClick(int position) {
                                                iid = seperateFlows.get(position).getIid();
                                                flowName.setText(seperateFlows.get(position).getTitle());
                                                flowValue = seperateFlows.get(position).getFlowValue();
                                                price = seperateFlows.get(position).getPrice();
                                                subject = seperateFlows.get(position).getTitle();
                                                nowPriceNation.setText(seperateFlows.get(position).getPrice() + "元");
                                                pastPriceNation.setHint(seperateFlows.get(position).getOldPrice() + "元");
                                                if (has_iid)
                                                    if (iid.equals(coupon.getIid2())) {
                                                        ll_coupon.setVisibility(View.VISIBLE);
                                                        is_coupon = true;
                                                    } else {
                                                        ll_coupon.setVisibility(View.GONE);
                                                        is_coupon = false;
                                                    }
                                            }
                                        });
                                        content.setVisibility(View.VISIBLE);
                                    } else {
                                        toast(results[1]);
                                        content.setVisibility(View.GONE);
                                    }
                                } catch (Exception e) {
                                    toast(R.string.getserverdata_exception);
                                }
                            }
                        });
                          /*  if (address.contains("移动")) {
                                flows.add(new Flow("500M", "22.50", "28.0", "30.0", "30.0"));
                                flows.add(new Flow("1000M", "37.50", "47.0", "50.0", "50.0"));
                                flows.add(new Flow("2000M", "55.00", "66.0", "70.0", "70.0"));

                            }
                            if (address.contains("联通")) {
                                flows.add(new Flow("500M", "22.50", "30.0", "30.0", "30.0"));
                            }
                            if (address.contains("电信")) {
                                flows.add(new Flow("500M", "22.50", "28.50", "30.0", "30.0"));
                                flows.add(new Flow("1000M", "37.50", "47.50", "50.0", "50.0"));
                            }
                            flowName.setText(flows.get(0).getName() + "流量包");
                            nowPriceNation.setText(flows.get(0).getNow_price_nation() + "元");
                            pastPriceNation.setHint(flows.get(0).getPast_price_nation() + "元");
                            if (flows.get(0).getNow_price_nation().equals(flows.get(0).getPast_price_nation()))
                                pastPriceNation.setVisibility(View.GONE);
                            price=flows.get(0).getNow_price_nation();
                            subject=flows.get(0).getName()+"套餐";
                            flowValue=flows.get(0).getName();
                            flowAdapter = new FlowAdapter(getActivity(), flows);
                            flowAdapter.setOnItemClickListener(new RadioAdapter.ItemClickListener() {
                                @Override
                                public void onClick(int position) {
                                    flowName.setText(flows.get(position).getName() + "流量包");
                                    nowPriceLocal.setText(flows.get(position).getNow_price_local() + "元");
                                    pastPriceLocal.setHint(flows.get(position).getPast_price_local() + "元");
                                    nowPriceNation.setText(flows.get(position).getNow_price_nation() + "元");
                                    pastPriceNation.setHint(flows.get(position).getPast_price_nation() + "元");
                                    if (flows.get(position).getNow_price_nation().equals(flows.get(position).getPast_price_nation()))
                                        pastPriceNation.setVisibility(View.GONE);
                                    else pastPriceNation.setVisibility(View.VISIBLE);
                                    price=flows.get(position).getNow_price_nation();
                                    flowValue=flows.get(position).getName();
                                    subject=flows.get(position).getName()+"套餐";
                                }
                            });
                            flowList.setAdapter(flowAdapter);
                            content.setVisibility(View.VISIBLE);*/

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "查询归属地失败", 1).show();
                    tv_address.setHint("");
                    content.setVisibility(View.GONE);
                }

            }
        });
      /*  new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String address = AddressService.getAddress(getActivity(), number);
                    //把查询结果返回的归属地显示在textView上
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Logger.i("address", address);
                            String[] result = address.split(" ");
                            if (result.length < 3) {
                                tv_address.setHint(address);
                                content.setVisibility(View.GONE);
                            } else {
                                tv_address.setHint(result[2]);
                                final List<Flow> flows = new ArrayList<>();
                                if (result[2].contains("移动")) {
                                    flows.add(new Flow("500M", "22.50", "28.50", "30.0", "30.0"));
                                    flows.add(new Flow("1000M", "37.50", "48.0", "50.0", "50.0"));
                                    flows.add(new Flow("2000M", "55.00", "66.0", "70.0", "70.0"));

                                }
                                if (result[2].contains("联通")) {
                                    flows.add(new Flow("500M", "22.50", "30.0", "30.0", "30.0"));
                                }
                                if (result[2].contains("电信")) {
                                    flows.add(new Flow("500M", "22.50", "28.50", "30.0", "30.0"));
                                    flows.add(new Flow("1000M", "37.50", "47.50", "50.0", "50.0"));
                                }
                                flowName.setText(flows.get(0).getName() + "流量包");
                                nowPriceNation.setText(flows.get(0).getNow_price_nation() + "元");
                                pastPriceNation.setHint(flows.get(0).getPast_price_nation() + "元");
                                if (flows.get(0).getNow_price_nation().equals(flows.get(0).getPast_price_nation()))
                                    pastPriceNation.setVisibility(View.GONE);
                                price=flows.get(0).getNow_price_nation();
                                subject=flows.get(0).getName()+"套餐";
                                flowValue=flows.get(0).getName();
                                flowAdapter = new FlowAdapter(getActivity(), flows);
                                flowAdapter.setOnItemClickListener(new RadioAdapter.ItemClickListener() {
                                    @Override
                                    public void onClick(int position) {
                                        flowName.setText(flows.get(position).getName() + "流量包");
                                        nowPriceLocal.setText(flows.get(position).getNow_price_local() + "元");
                                        pastPriceLocal.setHint(flows.get(position).getPast_price_local() + "元");
                                        nowPriceNation.setText(flows.get(position).getNow_price_nation() + "元");
                                        pastPriceNation.setHint(flows.get(position).getPast_price_nation() + "元");
                                        if (flows.get(position).getNow_price_nation().equals(flows.get(position).getPast_price_nation()))
                                            pastPriceNation.setVisibility(View.GONE);
                                        else pastPriceNation.setVisibility(View.VISIBLE);
                                        price=flows.get(position).getNow_price_nation();
                                        flowValue=flows.get(position).getName();
                                        subject=flows.get(position).getName()+"套餐";
                                    }
                                });
                                flowList.setAdapter(flowAdapter);
                                content.setVisibility(View.VISIBLE);
                            }
                            progressDialog.dismiss();
                        }
                    });
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "查询归属地失败", 1).show();
                            tv_address.setHint("");
                            progressDialog.dismiss();
                            content.setVisibility(View.GONE);
                        }
                    });

                }
            }
        }).start();*/
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
                    query(phone_number);
                } else
                    toast("请选择手机号!");
                break;

            default:
                break;
        }
    }

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
                        userDao.pay(getUsername(), getPassword(), outTradeNo, "success");
                        if (has_iid) has_iid = false;
                        is_coupon = false;
                        ll_coupon.setVisibility(View.GONE);
                        coupon = null;
                        getActivity().setResult(Activity.RESULT_OK);
                    } else {
                        // 判断resultStatus 为非"9000"则代表可能支付失败
                        // "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                        if (TextUtils.equals(resultStatus, "8000")) {
                            Toast.makeText(getActivity(), "支付结果确认中", Toast.LENGTH_SHORT).show();

                        } else {
                            // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                            Toast.makeText(getActivity(), "支付失败", Toast.LENGTH_SHORT).show();
                            userDao.pay(getUsername(), getPassword(), outTradeNo, "failure");
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

    /**
     * call alipay sdk pay. 调用SDK支付
     */
    public void pay() {

        String orderInfo = getOrderInfo(subject, body, price);

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
        orderInfo += "&notify_url=" + "\"" + "http://inter.boboit.cn/inter/pay/alipay_notify_url.jsp?loginName="+getUsername()+"&loginPwd="+getPassword() + "\"";

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
}
