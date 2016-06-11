package com.callba.phone.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PC-20160514 on 2016/6/11.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.about,
        toolbarTitle = R.string.about_us,
        navigationId = R.drawable.press_back
)
public class AboutActivity extends BaseActivity {
    @InjectView(R.id.logo)
    ImageView logo;
    @InjectView(R.id.version_code)
    TextView versionCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
           versionCode.setText("版本号:"+getVersion());

    }
    /**
     * 获取版本号
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
    @Override
    public void init() {

    }

    @Override
    public void refresh(Object... params) {

    }
}
