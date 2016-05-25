package com.callba.phone.activity;

import android.content.Intent;
import android.view.MenuItem;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;

/**
 * Created by PC-20160514 on 2016/5/25.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.community,
        toolbarTitle =R.string.community,
        navigationId = R.drawable.press_back,
        menuId = R.menu.menu_community
)
public class CommunityActivity extends BaseActivity{

    @Override
    public void init() {

    }

    @Override
    public void refresh(Object... params) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case  R.id.camera:
                Intent intent=new Intent(CommunityActivity.this,PostActivity.class);
                startActivityForResult(intent,0);
            break;
        }
        return super.onOptionsItemSelected(item);
    }
}
