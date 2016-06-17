package com.callba.phone.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by PC-20160514 on 2016/5/26.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.post_message,
        toolbarTitle = R.string.post_message,
        navigationId = R.drawable.press_back
)
public class PostMessageActivity extends BaseActivity {

    @InjectView(R.id.number)
    EditText number;
    @InjectView(R.id.content)
    EditText content;
    @InjectView(R.id.submit)
    ImageButton submit;
    protected File cameraFile;
    protected static final int REQUEST_CODE_CAMERA = 2;
    protected static final int REQUEST_CODE_LOCAL = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
    }

    @Override
    public void init() {

    }

    @Override
    public void refresh(Object... params) {

    }


    @OnClick({R.id.submit, R.id.submit_image})
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.submit:
                if (number.getText().toString().equals("") || number.getText().toString().equals(CalldaGlobalConfig.getInstance().getUsername()))
                    break;
                EMMessage message = EMMessage.createTxtSendMessage(content.getText().toString(), number.getText().toString() + "-callba");
//发送消息
                EMClient.getInstance().chatManager().sendMessage(message);
                intent = new Intent(PostMessageActivity.this, ChatActivity.class);
                intent.putExtra("username", number.getText().toString() + "-callba");
                Intent intent1 = new Intent("com.callba.chat");
                sendBroadcast(intent1);
                startActivity(intent);
                finish();
                break;
            case R.id.submit_image:
                if (number.getText().toString().equals("") || number.getText().toString().equals(CalldaGlobalConfig.getInstance().getUsername()))
                    break;
                if (Build.VERSION.SDK_INT < 19) {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");

                } else {
                    intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                }
                startActivityForResult(intent, REQUEST_CODE_LOCAL);
                break;
        }
    }

    protected void sendImageMessage(String imagePath) {
        EMMessage message = EMMessage.createImageSendMessage(imagePath, false, number.getText().toString() + "-callba");
        EMClient.getInstance().chatManager().sendMessage(message);
        Intent intent = new Intent(PostMessageActivity.this, ChatActivity.class);
        intent.putExtra("username", number.getText().toString() + "-callba");
        Intent intent1 = new Intent("com.callba.chat");
        sendBroadcast(intent1);
        startActivity(intent);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAMERA) { // 发送照片
                if (cameraFile != null && cameraFile.exists())
                    sendImageMessage(cameraFile.getAbsolutePath());
            } else if (requestCode == REQUEST_CODE_LOCAL) { // 发送本地图片
                if (data != null) {
                    Uri selectedImage = data.getData();
                    if (selectedImage != null) {
                        sendPicByUri(selectedImage);
                    }
                }
            }/* else if (requestCode == REQUEST_CODE_MAP) { // 地图
                double latitude = data.getDoubleExtra("latitude", 0);
                double longitude = data.getDoubleExtra("longitude", 0);
                String locationAddress = data.getStringExtra("address");
                if (locationAddress != null && !locationAddress.equals("")) {
                    sendLocationMessage(latitude, longitude, locationAddress);
                } else {
                    Toast.makeText(getActivity(), R.string.unable_to_get_loaction, 0).show();
                }

            }*/
        }
    }

    /**
     * 根据图库图片uri发送图片
     *
     * @param selectedImage
     */
    protected void sendPicByUri(Uri selectedImage) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            cursor = null;

            if (picturePath == null || picturePath.equals("null")) {
                Toast toast = Toast.makeText(this, R.string.cant_find_pictures, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }
            sendImageMessage(picturePath);
        } else {
            File file = new File(selectedImage.getPath());
            if (!file.exists()) {
                Toast toast = Toast.makeText(this, R.string.cant_find_pictures, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;

            }
            sendImageMessage(file.getAbsolutePath());
        }

    }
}
