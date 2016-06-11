package com.callba.phone.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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
import com.callba.phone.util.NumberAddressService;
import com.callba.phone.util.SharedPreferenceUtil;
import com.callba.phone.view.CircleTextView;
import com.umeng.socialize.utils.Log;

import java.io.File;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import me.iwf.photopicker.PhotoPickerActivity;
import me.iwf.photopicker.utils.PhotoPickerIntent;

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
    private File f;
    private UserDao userDao,userDao1;
    private ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        if(!CalldaGlobalConfig.getInstance().getUserhead().equals("")){
            Glide.with(this).load(CalldaGlobalConfig.getInstance().getUserhead()).into(head);
        }
        nickName.setHint(CalldaGlobalConfig.getInstance().getNickname());
        signature.setHint(CalldaGlobalConfig.getInstance().getSignature());
        userDao=new UserDao(this,this);
        userDao1=new UserDao(this, new UserDao.PostListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(String msg) {
            toast(msg);
            }

            @Override
            public void failure(String msg) {
             toast(msg);
            }
        });
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
       dialog.setProgress((int)(current/total)*100);
    }

    @OnClick(R.id.change_head)
    public void change_head(){
        /*Intent intent=new Intent(ChangeInfoActivity.this,SelectPicPopupWindow.class);
        intent.putExtra("isCrop",true);
        startActivityForResult(intent, 1);*/
        PhotoPickerIntent intent = new PhotoPickerIntent(ChangeInfoActivity.this);
        intent.setPhotoCount(1);
        intent.setShowCamera(true);
        intent.setShowGif(true);
        startActivityForResult(intent, 0);
    }
    @OnClick(R.id.change_nickname)
    public void change_nickname(){
    shownNicknameDialog();
    }
    @OnClick(R.id.change_signature)
    public void change_signature(){
    shownSignatureDialog();
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
      /*  switch (resultCode) {
            case RESULT_OK:
                Log.i("path",data.getStringExtra("path"));
                head.setImageBitmap(BitmapUtil.getLoacalBitmap(data.getStringExtra("path")));
                f= new File(data.getStringExtra("path"));
                break;
            default:
                break;

        }*/
        if (resultCode == RESULT_OK && requestCode == 0) {
            if (data != null) {
                ArrayList<String> photos =
                        data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS);
                Intent intent = new Intent(ChangeInfoActivity.this, CropActivity.class);
                intent.putExtra("uri", Uri.fromFile(new File(photos.get(0))));
                startActivityForResult(intent,1);
            }
        }
        if (resultCode == RESULT_OK && requestCode == 1) {
            if (data != null) {
                head.setImageBitmap(BitmapUtil.getLoacalBitmap(data.getStringExtra("path")));
                f= new File(data.getStringExtra("path"));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.save:
                if(f!=null)
                userDao.changeHead(CalldaGlobalConfig.getInstance().getUsername(), CalldaGlobalConfig.getInstance().getPassword(),f);
                    userDao1.changeInfo(CalldaGlobalConfig.getInstance().getUsername(), CalldaGlobalConfig.getInstance().getPassword(),!nickName.getHint().toString().equals(CalldaGlobalConfig.getInstance().getNickname())?nickName.getHint().toString():null,!(signature.getHint().toString()).equals(CalldaGlobalConfig.getInstance().getNickname())?signature.getHint().toString():null);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    public class DialogHelper implements DialogInterface.OnDismissListener {
        private Dialog mDialog;
        private View mView;
        private EditText change;

        public DialogHelper(int id) {
            mView = getLayoutInflater().inflate(id, null);
            change = (EditText) mView.findViewById(R.id.et_change);
        }

        private String getText() {
            return change.getText().toString();
        }
        public void setText(String s){change.setText(s);}
        @Override
        public void onDismiss(DialogInterface dialogInterface) {
            mDialog = null;
        }

        public void setDialog(Dialog mDialog) {
            this.mDialog = mDialog;
        }

        public View getView() {
            return mView;
        }

    }

    public void shownNicknameDialog() {
        final DialogHelper helper = new DialogHelper(R.layout.dialog_change_number);
        Dialog dialog = new AlertDialog.Builder(this)
                .setView(helper.getView()).setTitle(getString(R.string.nick_name))
                .setOnDismissListener(helper)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                     if(!helper.getText().equals("")){
                         nickName.setHint(helper.getText());
                     }
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        helper.setText(nickName.getHint().toString());
        helper.setDialog(dialog);
        dialog.show();
    }
    public void shownSignatureDialog() {
        final DialogHelper helper = new DialogHelper(R.layout.dialog_change_signature);
        Dialog dialog = new AlertDialog.Builder(this)
                .setView(helper.getView()).setTitle(getString(R.string.signature))
                .setOnDismissListener(helper)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!helper.getText().equals("")){
                            signature.setHint(helper.getText());
                        }
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        helper.setText(signature.getHint().toString());
        helper.setDialog(dialog);
        dialog.show();
    }

}
