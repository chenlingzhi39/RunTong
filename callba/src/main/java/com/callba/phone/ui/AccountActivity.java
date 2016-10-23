package com.callba.phone.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.callba.R;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Meal;
import com.callba.phone.manager.UserManager;
import com.callba.phone.ui.adapter.MealAdapter;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.net.UnknownHostException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by PC-20160514 on 2016/6/1.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.account,
        toolbarTitle = R.string.my_number,
        navigationId = R.drawable.press_back
)
public class AccountActivity extends BaseActivity {
    @BindView(R.id.head)
    CircleImageView head;
    @BindView(R.id.account)
    TextView account;
    @BindView(R.id.calllog_search)
    RelativeLayout calllogSearch;
    @BindView(R.id.meal_search)
    RelativeLayout mealSearch;
    @BindView(R.id.commission)
    Button commission;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
        account.setHint(getUsername());
        if (!UserManager.getUserAvatar(this).equals(""))
            Glide.with(this).load(UserManager.getUserAvatar(this)).into(head);
        commission.setText(UserManager.getCommission(this));
    }

    @OnClick({R.id.calllog_search, R.id.meal_search, R.id.balance, R.id.gold})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.calllog_search:
                Intent intent = new Intent(AccountActivity.this, QueryCalllogActivity.class);
                startActivity(intent);
                break;
            case R.id.meal_search:
                OkHttpUtils.post().url(Interfaces.QUERY_MEAL)
                        .addParams("loginName",getUsername())
                        .addParams("loginPwd",getPassword())
                        .addParams("softType","android")
                        .build().execute(new StringCallback() {
                    @Override
                    public void onAfter(int id) {
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onBefore(Request request, int id) {
                        progressDialog=ProgressDialog.show(AccountActivity.this,"","正在查询套餐");
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        if(e instanceof UnknownHostException)toast(R.string.conn_failed);
                        else toast(R.string.network_error);
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        try {
                           String[] result =response.split("\\|");
                            Log.i("get_suits", response);
                            if (result[0].equals("0")) {
                                if (result.length > 1)
                                {
                                    if (result[1]!= null) {
                                        String[] result1 = result[1].split("&");
                                        ArrayList<Meal> meals = new ArrayList<>();
                                        Logger.i("mealaccount", result1.length + "");
                                        for (String str : result1) {
                                            String[] element = str.split(",");
                                            Meal meal = new Meal();
                                            meal.setName(element[2]);
                                            meal.setTime(element[3]);
                                            meal.setMax(element[4]);
                                            meal.setRest(element[5]);
                                            meals.add(meal);
                                        }
                                        showDialog(meals);
                                    } else toast("无套餐");
                                }
                                else toast("无套餐");
                            } else toast(result[1]);

                        } catch (Exception e) {
                            toast(R.string.getserverdata_exception);
                        }
                    }
                });
                break;
            case R.id.balance:
                startActivity(new Intent(AccountActivity.this, BalanceActivity.class));
                break;
            case R.id.gold:
                startActivity(new Intent(AccountActivity.this, GoldActivity.class));
                break;
        }

    }


    public class DialogHelper implements DialogInterface.OnDismissListener {
        private Dialog mDialog;
        private View mView;
        private RecyclerView mealList;
        private MealAdapter mealAdapter;

        public DialogHelper(ArrayList<Meal> meals) {
            mView = getLayoutInflater().inflate(R.layout.dialog_list, null);
            mealList = (RecyclerView) mView.findViewById(R.id.list);
            mealAdapter = new MealAdapter(AccountActivity.this);
            mealAdapter.addAll(meals);
            mealList.setAdapter(mealAdapter);
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

    public void showDialog(ArrayList<Meal> meals) {
        final DialogHelper helper = new DialogHelper(meals);
        Dialog dialog = new AlertDialog.Builder(this)
                .setView(helper.getView()).setTitle("套餐")
                .setOnDismissListener(helper).create();
        helper.setDialog(dialog);
        dialog.show();
    }
}
