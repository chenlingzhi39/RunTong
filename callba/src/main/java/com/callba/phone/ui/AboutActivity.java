package com.callba.phone.ui;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.callba.BuildConfig;
import com.callba.R;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.ui.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by PC-20160514 on 2016/6/11.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.about,
        toolbarTitle = R.string.about_us,
        navigationId = R.drawable.press_back
)
public class AboutActivity extends BaseActivity {
    @BindView(R.id.logo)
    ImageView logo;
    @BindView(R.id.version_code)
    TextView versionCode;
    @BindView(R.id.invite)
    TextView invite;
    @BindView(R.id.url)
    TextView url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        versionCode.setText("版本号:" + BuildConfig.VERSION_NAME);

    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    public String getVersion() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    @OnClick({R.id.invite,R.id.url})
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.invite:
                Intent intent = new Intent(Intent.ACTION_SEND); // 启动分享发送的属性
                intent.setType("text/plain"); // 分享发送的数据类型
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Callba分享"); // 分享的主题
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_content)); // 分享的内容
                startActivity(Intent.createChooser(intent, "选择分享"));// 目标应用选择对话框的标题*/
                break;
            case R.id.url:
                Intent intent1 = new Intent(Intent.ACTION_VIEW);
                intent1.setData(Uri.parse("http://"+url.getText().toString()));
                startActivity(intent1);
                break;
        }

    }


}
