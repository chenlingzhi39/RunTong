package com.callba.phone.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.activity.more.QueryCalllogActivity;
import com.callba.phone.adapter.MealAdapter;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Meal;
import com.callba.phone.bean.UserDao;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.umeng.socialize.utils.Log;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by PC-20160514 on 2016/6/1.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.account,
        toolbarTitle = R.string.my_number,
        navigationId = R.drawable.press_back
)
public class AccountActivity extends BaseActivity {
    @InjectView(R.id.head)
    CircleImageView head;
    @InjectView(R.id.account)
    TextView account;
    @InjectView(R.id.balance)
    Button balance;
    UserDao userDao, userDao1;
    @InjectView(R.id.calllog_search)
    RelativeLayout calllogSearch;
    @InjectView(R.id.meal_search)
    RelativeLayout mealSearch;
    @InjectView(R.id.gold)
    Button gold;

    @Override
    public void init() {

    }

    @Override
    public void refresh(Object... params) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.inject(this);
        account.setHint(CalldaGlobalConfig.getInstance().getUsername());
        if (!CalldaGlobalConfig.getInstance().getUserhead().equals(""))
            Glide.with(this).load(CalldaGlobalConfig.getInstance().getUserhead()).into(head);
        userDao = new UserDao(this, new UserDao.PostListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(String msg) {
                if (msg != null) {
                    String[] result = msg.split("&");
                    ArrayList<Meal> meals = new ArrayList<>();
                    Log.i("mealaccount", result.length + "");
                    for (String str : result) {
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

            @Override
            public void failure(String msg) {
                toast(msg);
            }
        });
        userDao1 = new UserDao(this, new UserDao.PostListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(String msg) {
                balance.setText(msg + "元");
            }

            @Override
            public void failure(String msg) {

            }
        });
        userDao1.getBalance(CalldaGlobalConfig.getInstance().getUsername(), CalldaGlobalConfig.getInstance().getPassword());
        gold.setText(CalldaGlobalConfig.getInstance().getGold()+"");
    }

    @OnClick({R.id.calllog_search, R.id.meal_search, R.id.balance})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.calllog_search:
                Intent intent = new Intent(AccountActivity.this, QueryCalllogActivity.class);
                startActivity(intent);
                break;
            case R.id.meal_search:
                userDao.getSuits(CalldaGlobalConfig.getInstance().getUsername(), CalldaGlobalConfig.getInstance().getPassword());
                break;
            case R.id.balance:
                userDao1.getBalance(CalldaGlobalConfig.getInstance().getUsername(), CalldaGlobalConfig.getInstance().getPassword());
                break;
        }

    }


    public class DialogHelper implements DialogInterface.OnDismissListener {
        private Dialog mDialog;
        private View mView;
        private RecyclerView mealList;
        private MealAdapter mealAdapter;

        public DialogHelper(ArrayList<Meal> meals) {
            mView = getLayoutInflater().inflate(R.layout.dialog_meal, null);
            mealList = (RecyclerView) mView.findViewById(R.id.meal_list);
            mealList.setLayoutManager(new LinearLayoutManager(AccountActivity.this));
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
