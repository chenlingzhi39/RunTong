package com.callba.phone.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.UserDao;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.cfg.Constant;
import com.callba.phone.util.BitmapUtil;
import com.callba.phone.util.SharedPreferenceUtil;
import com.callba.phone.view.CircleTextView;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by PC-20160514 on 2016/5/19.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.change_info,
        toolbarTitle = R.string.change,
        navigationId = R.drawable.press_back,
        menuId =R.menu.menu_save
)
public class ChangeInfoActivity extends BaseActivity implements UserDao.UploadListener{
    @InjectView(R.id.head)
    CircleImageView head;
    @InjectView(R.id.change_head)
    RelativeLayout changeHead;
    @InjectView(R.id.nick_name)
    TextView nickName;
    @InjectView(R.id.change_nickname)
    RelativeLayout changeNickname;
    @InjectView(R.id.signature)
    TextView signature;
    @InjectView(R.id.change_signature)
    RelativeLayout changeSignature;
    private Bitmap photo;
    private File f;
    private UserDao userDao;
    private ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        if(!CalldaGlobalConfig.getInstance().getUserhead().equals("")){
            Glide.with(this).load(CalldaGlobalConfig.getInstance().getUserhead()).into(head);
        }
        nickName.setText(CalldaGlobalConfig.getInstance().getNickname());
        userDao=new UserDao(this,this);
    }

    @Override
    public void start() {
        dialog = new ProgressDialog(ChangeInfoActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setTitle("上传中...");
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    public void success(String msg) {
        toast(getString(R.string.change_success));
        CalldaGlobalConfig.getInstance().setUserhead(msg);
        dialog.dismiss();
    }

    @Override
    public void failure(String msg) {
        toast(msg);
        dialog.dismiss();
    }

    @Override
    public void loading(long total, long current, boolean isUploading) {
       dialog.setProgress((int)(current/total));
    }

    @OnClick(R.id.change_head)
    public void change_head(){
        Intent intent=new Intent(ChangeInfoActivity.this,SelectPicPopupWindow.class);
        intent.putExtra("isCrop",true);
        startActivityForResult(intent, 0);
    }
    @OnClick(R.id.change_nickname)
    public void change_nickname(){

    }
    @OnClick(R.id.change_signature)
    public void change_signature(){

    }
    @Override
    public void init() {

    }

    @Override
    public void refresh(Object... params) {

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case RESULT_OK:
                Bundle b = data.getBundleExtra("photo_item");
                photo = b.getParcelable("data");
                head.setImageBitmap(photo);
                f= BitmapUtil.saveBitmap(ChangeInfoActivity.this, photo, Constant.PHOTO_PATH, "head.jpg");
                break;
            default:
                break;

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.save:
                if(f.exists())
                userDao.changeHead(CalldaGlobalConfig.getInstance().getUsername(), CalldaGlobalConfig.getInstance().getPassword(),f);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
