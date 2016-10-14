package com.callba.phone.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.callba.R;
import com.callba.phone.bean.ContactMultiNumBean;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.CalllogDetailBean;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Administrator on 2016/7/10.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.calllog_detail,
        navigationId = R.drawable.press_back,
        menuId = R.menu.menu_calllog_detail
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
            ContactMultiNumBean bean=new ContactMultiNumBean();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("保存号码");
                builder.setItems(new String[] { getString(R.string.add_contact), getString(R.string.save_contact) },
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                switch (which) {
                                    case 0:
                                        Intent intent = new Intent(Intent.ACTION_INSERT);
                                        intent.setType("vnd.android.cursor.dir/person");
                                        intent.setType("vnd.android.cursor.dir/contact");
                                        intent.setType("vnd.android.cursor.dir/raw_contact");
                                        intent.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE, calllogBean.getCallLogNumber());
                                        startActivity(intent);
                                        break;
                                    case 1:
                                        Intent intent1 = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                                        intent1.setType("vnd.android.cursor.item/person");
                                        intent1.setType("vnd.android.cursor.item/contact");
                                        intent1.setType("vnd.android.cursor.item/raw_contact");
                                        //    intent.putExtra(android.provider.ContactsContract.Intents.Insert.NAME, name);
                                        intent1.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE, calllogBean.getCallLogNumber());
                                        intent1.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE_TYPE, 2);
                                        startActivity(intent1);
                                        break;
                                    default:
                                        break;
                                }
                            }
                        });
                builder.create().show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
