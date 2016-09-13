package com.callba.phone.ui;

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
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.ui.adapter.PhotoAdapter;
import com.callba.phone.ui.adapter.RecyclerArrayAdapter;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.manager.UserManager;
import com.callba.phone.util.Interfaces;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.PostFormBuilder;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import me.iwf.photopicker.PhotoPickerActivity;
import me.iwf.photopicker.utils.PhotoPickerIntent;
import okhttp3.Call;
import okhttp3.Request;


/**
 * Created by PC-20160514 on 2016/5/25.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.post,
        navigationId = R.drawable.press_back,
        menuId = R.menu.menu_post
)
public class PostActivity extends BaseActivity{
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
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.inject(this);
        location.setText(UserManager.getAddress(this));
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
                PostFormBuilder postFormBuilder=OkHttpUtils.post().url(Interfaces.SEND_MOODS)
                        .addParams("loginName",getUsername())
                        .addParams("loginPwd",getPassword())
                        .addParams("content",content.getText().toString());
                for (String path : photoAdapter.getData())
                {   File file=new File(path);
                    postFormBuilder.addFile("file",file.getName(),file);
                }
                postFormBuilder.build().execute(new StringCallback() {
                    @Override
                    public void onAfter(int id) {
                        dialog.dismiss();
                    }

                    @Override
                    public void onBefore(Request request, int id) {
                       dialog=ProgressDialog.show(PostActivity.this,"","上传中");
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        showException(e);
                    }

                    @Override
                    public void onResponse(String response, int id) {
                       try{
                           String[] result=response.split("\\|");
                           toast(result[1]);
                           if(result[0].equals("0")){
                               setResult(RESULT_OK);
                               finish();
                           }
                       }catch (Exception e){
                           showException(e);
                       }
                    }

                });
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
