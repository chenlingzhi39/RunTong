package com.callba.phone.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.callba.R;
import com.callba.phone.cfg.Constant;
import com.callba.phone.util.BitmapUtil;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.umeng.socialize.utils.Log;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import me.iwf.photopicker.PhotoPickerActivity;
/**
 * Created by PC-20160514 on 2016/6/8.
 */
public class CropActivity extends AppCompatActivity {
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.cropImageView)
    CropImageView cropImageView;
    Uri uri;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crop);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("裁剪图片");
        uri=getIntent().getParcelableExtra("uri");
        Log.i("path",uri.getPath());
        cropImageView.setImageUriAsync(uri);
        cropImageView.setFixedAspectRatio(true);
        cropImageView.setAspectRatio(1,1);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_crop, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.i("onclick","home");
                finish();
                break;
            case R.id.ok:
                Log.i("onclick","ok");
                cropImageView.setOnGetCroppedImageCompleteListener(new CropImageView.OnGetCroppedImageCompleteListener() {
                    @Override
                    public void onGetCroppedImageComplete(CropImageView view, Bitmap bitmap, Exception error) {
                        File f=BitmapUtil.saveBitmap(CropActivity.this, bitmap, Constant.PHOTO_PATH, "head.jpg");
                        Intent intent=new Intent();
                        intent.putExtra("path",f.getPath());
                        setResult(RESULT_OK,intent);
                        finish();
                    }
                });
                cropImageView.getCroppedImageAsync();



                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
