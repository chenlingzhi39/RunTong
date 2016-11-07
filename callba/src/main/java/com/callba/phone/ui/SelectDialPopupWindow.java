package com.callba.phone.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.callba.R;
import com.callba.phone.util.CallUtils;
import com.callba.phone.util.SPUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by PC-20160514 on 2016/7/7.
 */
public class SelectDialPopupWindow extends Activity {
    CallUtils callUtils;
    @BindView(R.id.normal_layout)
    LinearLayout normalLayout;
    @BindView(R.id.dial_layout)
    LinearLayout dialLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_dial);
        ButterKnife.bind(this);
        callUtils = new CallUtils();
        switch ((String) SPUtils.get(this, "settings", "call_mode", "always")) {
            case "always":
                normalLayout.setVisibility(View.VISIBLE);
                dialLayout.setVisibility(View.VISIBLE);
                break;
            case "callback":
                dialLayout.setVisibility(View.VISIBLE);
                break;
            case "normal":
                normalLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    @OnClick({R.id.dial, R.id.cancel, R.id.root, R.id.normal})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dial:
                if (TextUtils.isEmpty(getIntent().getStringExtra("name")))
                    callUtils.judgeCallMode(this, getIntent().getStringExtra("number"));
                else
                    callUtils.judgeCallMode(this, getIntent().getStringExtra("number"), getIntent().getStringExtra("name"));
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.cancel:
                finish();
                break;
            case R.id.normal:
                Intent intent = new Intent(Intent.ACTION_CALL);
                Uri data = Uri.parse("tel:" + getIntent().getStringExtra("number"));
                intent.setData(data);
                startActivity(intent);
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.root:
                finish();
                break;
        }
    }
}
