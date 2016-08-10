package com.callba.phone.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
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
import com.callba.phone.util.Logger;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;


import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Subscription;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
/**
 * Created by PC-20160514 on 2016/6/8.
 */
public class CropActivity extends AppCompatActivity {
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.cropImageView)
    CropImageView cropImageView;
    Uri uri;
    public Subscription subscription;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crop);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("裁剪图片");
        uri=getIntent().getParcelableExtra("uri");
        Logger.i("path",uri.getPath());
        cropImageView.setImageUriAsync(uri);
        cropImageView.setFixedAspectRatio(true);
        cropImageView.setAspectRatio(1,1);

        cropImageView.setOnGetCroppedImageCompleteListener(new CropImageView.OnGetCroppedImageCompleteListener() {
            @Override
            public void onGetCroppedImageComplete(CropImageView view, final Bitmap bitmap, Exception error) {
               progressDialog = ProgressDialog.show(CropActivity.this, null, "正在压缩图片");
                subscription=Observable.create(new Observable.OnSubscribe<Bitmap>() {
                    @Override
                    public void call(Subscriber<? super Bitmap> subscriber) {
                        if(bitmap.getHeight()>480||bitmap.getWidth()>480)
                        {Bitmap image=ThumbnailUtils.extractThumbnail(bitmap, 480, 480);
                        subscriber.onNext(image);}
                        else subscriber.onNext(bitmap);
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Bitmap>() {
                    @Override
                    public void call(Bitmap bitmap) {
                        progressDialog.dismiss();
                        File f=BitmapUtil.saveBitmap(CropActivity.this,bitmap, Constant.PHOTO_PATH, "head.jpg");
                        Intent intent=new Intent();
                        intent.putExtra("path",f.getPath());
                        setResult(RESULT_OK,intent);
                        finish();
                    }
                });
               /* File f=BitmapUtil.saveBitmap(CropActivity.this,bitmap, Constant.PHOTO_PATH, "head.jpg");
                Intent intent=new Intent();
                intent.putExtra("path",f.getPath());
                setResult(RESULT_OK,intent);
                finish();*/
            }
        });

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
                Logger.i("onclick","home");
                finish();
                break;
            case R.id.ok:
                Logger.i("onclick","ok");

                cropImageView.getCroppedImageAsync();



                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        if(subscription!=null)
        subscription.unsubscribe();
        super.onDestroy();
    }
}
