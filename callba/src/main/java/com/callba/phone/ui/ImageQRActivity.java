package com.callba.phone.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.callba.R;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.cfg.Constant;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.util.BitmapUtil;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.util.SimpleHandler;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by PC-20160514 on 2016/7/18.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.image_qr,
        toolbarTitle = R.string.qr_code,
        navigationId = R.drawable.press_back,
        menuId = R.menu.menu_image_qr
)
public class ImageQRActivity extends BaseActivity {
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.retry)
    TextView retry;
    @BindView(R.id.hint)
    TextView hint;
    File f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        getImage();
    }

    public void getImage() {
        addRequestCall(OkHttpUtils.post().url(Interfaces.IMAGE_QR)
                .addParams("loginPwd", getPassword())
                .addParams("loginName",getUsername())
                .build())
                .execute(new StringCallback() {
                    @Override
                    public void onBefore(Request request, int id) {
                        retry.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAfter(int id) {
                        progressBar.setVisibility(View.GONE);

                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        toast(R.string.network_error);
                        retry.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        retry.setVisibility(View.GONE);
                        try{  Logger.i("order_result", response);
                        final String[] result = response.split("\\|");
                        if (result[0].equals("0")) {

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        final Bitmap bitmap = Glide.with(ImageQRActivity.this)
                                                .load(result[1])
                                                .asBitmap()
                                                .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                                .get();
                                        f = BitmapUtil.saveImage(ImageQRActivity.this, bitmap, Constant.PHOTO_PATH, result[1].substring(result[1].lastIndexOf("/")));
                                        SimpleHandler.getInstance().post(new Runnable() {
                                            @Override
                                            public void run() {
                                                image.setImageBitmap(bitmap);
                                            }
                                        });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            }).start();


                        } else {
                            hint.setText(result[1]);
                            hint.setVisibility(View.VISIBLE);
                        }
                        }catch(Exception e){
                            toast(R.string.getserverdata_exception);
                        }
                    }
                });
    }

    @OnClick(R.id.retry)
    public void onClick() {
        getImage();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share:
                if (f != null) {
                    Intent intent = new Intent(Intent.ACTION_SEND); // 启动分享发送的属性
                    intent.setType("image/jpg"); // 分享发送的数据类型
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Call吧分享"); // 分享的主题
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f)); // 分享的内容
                    startActivity(Intent.createChooser(intent, "选择分享"));// 目标应用选择对话框的标题*/
                } else toast("没有图片不能分享");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
