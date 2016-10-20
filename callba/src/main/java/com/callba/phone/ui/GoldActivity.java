package com.callba.phone.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.manager.UserManager;
import com.callba.phone.util.Interfaces;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;

/**
 * Created by PC-20160514 on 2016/6/27.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.gold,
        toolbarTitle = R.string.my_gold,
        navigationId = R.drawable.press_back
)
public class GoldActivity extends BaseActivity {
    @BindView(R.id.gold)
    TextView gold;
    @BindView(R.id.exchange)
    Button exchange;
    int ex_gold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        gold.setText(UserManager.getGold(this) + "");
    }

    @OnClick(R.id.exchange)
    public void onClick() {
        // toast("暂未开放");
        showDialog();
    }

    public class DialogHelper implements DialogInterface.OnDismissListener {
        private Dialog mDialog;
        private View mView;
        private EditText change;

        public DialogHelper() {
            mView = getLayoutInflater().inflate(R.layout.dialog_change_gold, null);
            change = (EditText) mView.findViewById(R.id.et_change);
            change.setHint("当前兑换比率为"+UserManager.getProportion(GoldActivity.this)+"比1");
            change.requestFocus();
            Timer timer = new Timer(); //设置定时器
            timer.schedule(new TimerTask() {
                @Override
                public void run() { //弹出软键盘的代码
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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
        Dialog dialog = new AlertDialog.Builder(this)
                .setView(helper.getView()).setTitle(getString(R.string.input_money))
                .setOnDismissListener(helper)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!helper.getNumber().equals(""))
                        {
                            ex_gold = Integer.parseInt(helper.getNumber()) * UserManager.getProportion(GoldActivity.this);
                        OkHttpUtils.post().url(Interfaces.EXCHANGE_BALANCE)
                                .addParams("loginName", getUsername())
                                .addParams("loginPwd", getPassword())
                                .addParams("gold", ex_gold + "")
                                .build().execute(new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e, int id) {
                                toast(R.string.network_error);
                            }

                            @Override
                            public void onResponse(String response, int id) {
                                try {
                                    String[] result = response.split("\\|");
                                    if (result[0].equals("0")) {
                                        toast(result[1]);
                                        UserManager.putGold(GoldActivity.this, UserManager.getGold(GoldActivity.this) - ex_gold);
                                        gold.setText(UserManager.getGold(GoldActivity.this) + "");
                                    } else toast(result[1]);
                                } catch (Exception e) {
                                    toast(R.string.getserverdata_exception);
                                }
                            }
                        });
                        dialog.dismiss();}
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
