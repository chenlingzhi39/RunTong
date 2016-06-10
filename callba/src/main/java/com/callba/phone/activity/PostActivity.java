package com.callba.phone.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.adapter.PhotoAdapter;
import com.callba.phone.adapter.RecyclerArrayAdapter;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.UserDao;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.view.AlwaysMarqueeTextView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import me.iwf.photopicker.PhotoPickerActivity;
import me.iwf.photopicker.utils.PhotoPickerIntent;


/**
 * Created by PC-20160514 on 2016/5/25.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.post,
        navigationId = R.drawable.press_back,
        menuId = R.menu.menu_post
)
public class PostActivity extends BaseActivity implements UserDao.UploadListener{
    @InjectView(R.id.content)
    EditText content;
    @InjectView(R.id.photos)
    RecyclerView photos;
    @InjectView(R.id.location)
    TextView location;
    @InjectView(R.id.is_located)
    CheckBox isLocated;
    private PhotoAdapter photoAdapter;
    private View footerView;
    private ArrayList<String> photoList;
    private UserDao userDao;
    private ProgressDialog dialog;
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
        location.setText(CalldaGlobalConfig.getInstance().getAddress());
        footerView = getLayoutInflater().inflate(R.layout.add_photo, null);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(metrics.widthPixels/5,metrics.widthPixels/5);
        footerView.setLayoutParams(params);
        photoAdapter = new PhotoAdapter(this);
        photoAdapter.addFooter(new RecyclerArrayAdapter.ItemView() {
            @Override
            public void onBindView(View footerView) {
                footerView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PhotoPickerIntent intent = new PhotoPickerIntent(PostActivity.this);
                        intent.setPhotoCount(9 - photoAdapter.getCount());
                        intent.setShowCamera(true);
                        intent.setShowGif(true);
                        startActivityForResult(intent, 0);
                    }
                });
            }

            @Override
            public View onCreateView(ViewGroup parent) {
                return footerView;
            }
        });
        photoAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent=new Intent(PostActivity.this,PhotoActivity.class);
                intent.putExtra("position",position);
                intent.putStringArrayListExtra("path",(ArrayList<String>) photoAdapter.getData());
                startActivityForResult(intent,1);
            }
        });
        photos.setLayoutManager(new GridLayoutManager(this, 5));
        photos.setAdapter(photoAdapter);
        userDao=new UserDao(this,this);
    }

    @Override
    public void failure(String msg) {
        toast(msg);
        dialog.dismiss();
    }

    @Override
    public void start() {
        dialog = new ProgressDialog(PostActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setTitle("上传中...");
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    public void success(String msg) {
        toast(msg);
        dialog.dismiss();
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void loading(long total, long current, boolean isUploading) {
        dialog.setProgress((int)(current/total)*100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 0) {
            if (data != null) {
                ArrayList<String> photos =
                        data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS);
                photoAdapter.addAll(photos);
                if(photoAdapter.getCount()==9)
                    footerView.setVisibility(View.GONE);
            }
        }
        if(resultCode == RESULT_OK && requestCode == 1){
            if (data != null) {
                ArrayList<String> photos =
                        data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS);
                photoAdapter.clear();
                photoAdapter.addAll(photos);
                if(photoAdapter.getCount()<9)
                    footerView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.post:
                userDao.sendMood(CalldaGlobalConfig.getInstance().getUsername(),CalldaGlobalConfig.getInstance().getPassword(),content.getText().toString(),(ArrayList<String>) photoAdapter.getData());
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
