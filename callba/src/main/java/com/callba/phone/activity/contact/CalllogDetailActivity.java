package com.callba.phone.activity.contact;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.CalldaCalllogBean;
import com.callba.phone.bean.CalllogDetailBean;
import com.callba.phone.cfg.CalldaGlobalConfig;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Administrator on 2016/7/10.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.calllog_detail,
        navigationId = R.drawable.press_back
)
public class CalllogDetailActivity extends BaseActivity {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.layout_tab)
    TabLayout tabs;
    @InjectView(R.id.viewpager)
    ViewPager viewpager;
   private CalllogDetailBean calllogBean;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.inject(this);
        calllogBean=(CalllogDetailBean) getIntent().getExtras().get("log");
        getSupportActionBar().setTitle(calllogBean.getCallLogNumber());
        viewpager.setAdapter(new SimpleFragmentPagerAdapter(getSupportFragmentManager(), this));
        tabs.setupWithViewPager(viewpager);
    }
    public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {

        final int PAGE_COUNT = 2;
        private String tabTitles[] = new String[]{"通话记录", "详细信息"};
        private Context context;

        public SimpleFragmentPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;

        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            ContactMutliNumBean bean=new ContactMutliNumBean();
            ArrayList<String> numbers=new ArrayList<>();
            switch (position) {

                case 0:
                    CalllogFragment calllogFragment = new CalllogFragment();
                    numbers.add(calllogBean.getCallLogNumber());
                    bean.setContactPhones(numbers);
                    bundle.putSerializable("contact", bean);
                    calllogFragment.setArguments(bundle);
                    return calllogFragment;
                case 1:
                    ContactDetailFragment contactDetailFragment = new ContactDetailFragment();
                    numbers.add(calllogBean.getCallLogNumber());
                    bean.setContactPhones(numbers);
                    bean.setDisplayName(calllogBean.getCallLogNumber());
                    bundle.putSerializable("contact", bean);
                    contactDetailFragment.setArguments(bundle);
                    return contactDetailFragment;
                default:
                    return null;

            }

        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.invite:
                Dialog dialog=new android.support.v7.app.AlertDialog.Builder(this).setTitle("是否邀请此好友？").setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri smsToUri = Uri.parse("smsto://" + calllogBean.getCallLogNumber());
                        Intent mIntent = new Intent(android.content.Intent.ACTION_SENDTO, smsToUri);
                        mIntent.putExtra("sms_body", "我是"+ CalldaGlobalConfig.getInstance().getNickname()+"，我正在使用CALL吧！ CALL吧“0月租”“0漫游”“通话不计分钟”，赶快加入我们吧！");
                        startActivity(mIntent);
                        dialog.dismiss();
                    }
                }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
                dialog.show();
                break;
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
