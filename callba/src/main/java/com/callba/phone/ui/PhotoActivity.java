package com.callba.phone.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.callba.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.iwf.photopicker.PhotoPickerActivity;
import me.iwf.photopicker.adapter.PhotoPagerAdapter;

/**
 * Created by PC-20160514 on 2016/6/6.
 */
public class PhotoActivity extends AppCompatActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.vp_photos)
    ViewPager mViewPager;
    private PhotoPagerAdapter mPagerAdapter;
    private ArrayList<String> paths;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo);
        ButterKnife.bind(this);
        paths=getIntent().getStringArrayListExtra("path");
        mPagerAdapter=new PhotoPagerAdapter(Glide.with(this), paths);
        mViewPager.setAdapter(mPagerAdapter);
            mViewPager.setCurrentItem(getIntent().getIntExtra("position",0));
        mViewPager.setOffscreenPageLimit(5);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mViewPager.getCurrentItem()+1+"/"+mPagerAdapter.getCount());
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
             getSupportActionBar().setTitle(position+1+"/"+mPagerAdapter.getCount());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_photo, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent=new Intent();
                intent.putStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS,paths);
                setResult(RESULT_OK,intent);
                finish();
                break;
            case R.id.delete:
                paths.remove(mViewPager.getCurrentItem());
                mPagerAdapter.notifyDataSetChanged();
                getSupportActionBar().setTitle(mViewPager.getCurrentItem()+1+"/"+mPagerAdapter.getCount());
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}
