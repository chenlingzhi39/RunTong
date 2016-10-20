package com.callba.phone.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.callba.R;
import com.callba.phone.util.CallUtils;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by PC-20160514 on 2016/7/7.
 */
public class SelectDialPopupWindow extends Activity {
    CallUtils callUtils;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_dial);
        ButterKnife.bind(this);
        callUtils=new CallUtils();
    }

    @OnClick({R.id.dial, R.id.cancel,R.id.root})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dial:
                callUtils.judgeCallMode(this,getIntent().getStringExtra("number"),getIntent().getStringExtra("name"));
                finish();
                break;
            case R.id.cancel:
                finish();
                break;
            case R.id.root:
                finish();
                break;
        }
    }
}
