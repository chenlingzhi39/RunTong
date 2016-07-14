package com.callba.phone.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.Selection;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import com.callba.phone.service.UpdateService;
import com.callba.phone.util.BitmapUtil;
import com.callba.phone.util.Logger;
import com.callba.phone.util.NumberAddressService;
import com.callba.phone.util.SharedPreferenceUtil;
import com.callba.phone.util.Utils;
import com.callba.phone.view.CircleTextView;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

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
    private static final int REQUESTCODE_PICK = 1;
    private static final int REQUESTCODE_CAMERA= 3;
    private static final int RESULT_CAMERA_CROP_PATH_RESULT=4;
    private Uri imageUri;
    private Uri imageCropUri;
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
                CalldaGlobalConfig.getInstance().setNickname(nickName.getHint().toString());
                CalldaGlobalConfig.getInstance().setSignature(signature.getHint().toString());
            }

            @Override
            public void failure(String msg) {
             toast(msg);
            }
        });
        String path = getSDCardPath();
        File file = new File(path + "/temp.jpg");
        imageUri = Uri.fromFile(file);
        File cropFile = new File(getSDCardPath() + "/temp_crop.jpg");
        imageCropUri = Uri.fromFile(cropFile);
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
        f=null;
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
      /*  PhotoPickerIntent intent = new PhotoPickerIntent(ChangeInfoActivity.this);
        intent.setPhotoCount(1);
        intent.setShowCamera(true);
        intent.setShowGif(true);
        startActivityForResult(intent, 0);*/
        uploadHeadPhoto();
    }
    @OnClick(R.id.change_nickname)
    public void change_nickname(){
    shownNicknameDialog();
    }
    @OnClick(R.id.change_signature)
    public void change_signature(){
    shownSignatureDialog();
    }
    private void uploadHeadPhoto() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("更改头像");
        builder.setItems(new String[] { "拍照","相册" },
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                Intent intent = null;
                                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//action is capture
                                intent.putExtra("return-data", false);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                                intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                                intent.putExtra("noFaceDetection", true);
                                startActivityForResult(intent, REQUESTCODE_CAMERA);
                                break;
                            case 1:
                                Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT,null);
                                pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                                startActivityForResult(pickIntent, REQUESTCODE_PICK);
                                break;
                            default:
                                break;
                        }
                    }
                });
        builder.create().show();
    }

   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
      *//*  switch (resultCode) {
            case RESULT_OK:
                Log.i("path",data.getStringExtra("path"));
                head.setImageBitmap(BitmapUtil.getLoacalBitmap(data.getStringExtra("path")));
                f= new File(data.getStringExtra("path"));
                break;
            default:
                break;

        }*//*
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
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.save:
                if(f!=null)
                userDao.changeHead(CalldaGlobalConfig.getInstance().getUsername(), CalldaGlobalConfig.getInstance().getPassword(),f);
                Logger.i("nickname",nickName.getHint().toString().equals(CalldaGlobalConfig.getInstance().getNickname())+"");
                Logger.i("sign",signature.getHint().toString().equals(CalldaGlobalConfig.getInstance().getSignature())+"");
                    userDao1.changeInfo(CalldaGlobalConfig.getInstance().getUsername(), CalldaGlobalConfig.getInstance().getPassword(),!nickName.getHint().toString().equals(CalldaGlobalConfig.getInstance().getNickname())?nickName.getHint().toString():null,!signature.getHint().toString().equals(CalldaGlobalConfig.getInstance().getSignature())?signature.getHint().toString():null);
                break;
               case android.R.id.home:
                Log.i("base", "finish");
                   if(!nickName.getHint().toString().equals(CalldaGlobalConfig.getInstance().getNickname())||!signature.getHint().toString().equals(CalldaGlobalConfig.getInstance().getSignature())||f!=null)
                   {android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                   builder.setMessage("当前信息未保存,确定退出？");
                   builder.setPositiveButton(R.string.ok,
                           new DialogInterface.OnClickListener() {
                               @Override
                               public void onClick(DialogInterface dialog,
                                                   int which) {
                                    finish();
                               }
                           });
                   builder.setNegativeButton(R.string.cancel,
                           new DialogInterface.OnClickListener() {
                               @Override
                               public void onClick(DialogInterface dialog,
                                                   int which) {
                                  dialog.dismiss();
                               }
                           });
                   android.app.AlertDialog alertDialog = builder.create();
                   alertDialog.setCanceledOnTouchOutside(true);
                   alertDialog.setCancelable(true);
                   alertDialog.show();}else finish();
                break;
        }
        return true;
    }

    public class DialogHelper implements DialogInterface.OnDismissListener {
        private Dialog mDialog;
        private View mView;
        private EditText change;

        public DialogHelper(int id) {
            mView = getLayoutInflater().inflate(id, null);
            change = (EditText) mView.findViewById(R.id.et_change);
            change.requestFocus();
            Timer timer = new Timer(); //设置定时器
            timer.schedule(new TimerTask() {
                @Override
                public void run() { //弹出软键盘的代码
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInputFromWindow(change.getWindowToken(), 0, InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }, 300); //设置300毫秒的时长
        }

        private String getText() {
            return change.getText().toString();
        }
        public void setText(String s){change.setText(s);
            Editable etext = change.getText();
            Selection.setSelection(etext, etext.length());
        }
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;
        switch (requestCode) {
            case REQUESTCODE_PICK:
                if (data == null || data.getData() == null) {
                    return;
                }
                cropImg(Uri.fromFile(new File(Utils.getPath(this,data))));
                break;
            case REQUESTCODE_CAMERA:
                cropImg(imageUri);
                break;
            case RESULT_CAMERA_CROP_PATH_RESULT:
                Bundle extras = data.getExtras();
                if (extras != null) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageCropUri));
                        head.setImageBitmap(bitmap);
                        f= new File(getSDCardPath() + "/temp_crop.jpg");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void cropImg(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX",150);
        intent.putExtra("outputY",150);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageCropUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, RESULT_CAMERA_CROP_PATH_RESULT);
    }
    public static String getSDCardPath() {
        String cmd = "cat /proc/mounts";
        Runtime run = Runtime.getRuntime();// 返回与当前 Java 应用程序相关的运行时对象
        try {
            Process p = run.exec(cmd);// 启动另一个进程来执行命令
            BufferedInputStream in = new BufferedInputStream(p.getInputStream());
            BufferedReader inBr = new BufferedReader(new InputStreamReader(in));

            String lineStr;
            while ((lineStr = inBr.readLine()) != null) {
                // 获得命令执行后在控制台的输出信息
                if (lineStr.contains("sdcard")
                        && lineStr.contains(".android_secure")) {
                    String[] strArray = lineStr.split(" ");
                    if (strArray != null && strArray.length >= 5) {
                        String result = strArray[1].replace("/.android_secure",
                                "");
                        return result;
                    }
                }
                // 检查命令是否执行失败。
                if (p.waitFor() != 0 && p.exitValue() == 1) {
                    // p.exitValue()==0表示正常结束，1：非正常结束
                }
            }
            inBr.close();
            in.close();
        } catch (Exception e) {

            return Environment.getExternalStorageDirectory().getPath();
        }

        return Environment.getExternalStorageDirectory().getPath();
    }
}
