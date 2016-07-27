package com.callba.phone.activity.recharge;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.callba.R;
import com.callba.phone.BaseFragment;
import com.callba.phone.adapter.FlowAdapter;
import com.callba.phone.adapter.RadioAdapter;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Flow;
import com.callba.phone.service.AddressService;
import com.callba.phone.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

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
    FlowAdapter flowAdapter;
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
        number.setText(getUsername());
        query(getUsername());
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

    }

    @Override
    protected void lazyLoad() {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @OnClick({R.id.contacts, R.id.relative})
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
        progressDialog = ProgressDialog.show(getActivity(), "", "正在查询归属地");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String address = AddressService.getAddress(getActivity(), number);
                    //把查询结果返回的归属地显示在textView上
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Logger.i("address",address);
                            if (address.equals("没有此号码记录"))
                            { tv_address.setHint(address);
                            content.setVisibility(View.GONE);}
                            else {
                                String[] result = address.split(" ");
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
                                if(flows.get(0).getNow_price_nation().equals(flows.get(0).getPast_price_nation()))
                                    pastPriceNation.setVisibility(View.GONE);
                                flowAdapter = new FlowAdapter(getActivity(), flows);
                                flowAdapter.setOnItemClickListener(new RadioAdapter.ItemClickListener() {
                                    @Override
                                    public void onClick(int position) {
                                        flowName.setText(flows.get(position).getName() + "流量包");
                                        nowPriceLocal.setText(flows.get(position).getNow_price_local() + "元");
                                        pastPriceLocal.setHint(flows.get(position).getPast_price_local() + "元");
                                        nowPriceNation.setText(flows.get(position).getNow_price_nation() + "元");
                                        pastPriceNation.setHint(flows.get(position).getPast_price_nation() + "元");
                                        if(flows.get(position).getNow_price_nation().equals(flows.get(position).getPast_price_nation()))
                                            pastPriceNation.setVisibility(View.GONE);
                                        else    pastPriceNation.setVisibility(View.VISIBLE);
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
        }).start();
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
}
