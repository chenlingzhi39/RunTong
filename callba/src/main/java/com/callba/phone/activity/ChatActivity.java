package com.callba.phone.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.EaseConstant;
import com.callba.phone.adapter.ChatAdapter;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.EaseEmojicon;
import com.callba.phone.util.EaseCommonUtils;
import com.callba.phone.widget.EaseChatExtendMenu;
import com.callba.phone.widget.EaseChatInputMenu;
import com.callba.phone.widget.EaseChatInputMenu.ChatInputMenuListener;
import com.callba.phone.widget.refreshlayout.EasyRecyclerView;
import com.callba.phone.widget.refreshlayout.RefreshLayout;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.util.PathUtil;

import java.io.File;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by PC-20160514 on 2016/5/27.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.chat,
        navigationId = R.drawable.press_back
)
public class ChatActivity extends BaseActivity implements RefreshLayout.OnRefreshListener {
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.input_menu)
    EaseChatInputMenu inputMenu;
    @InjectView(R.id.messages)
    EasyRecyclerView list;

    private ChatAdapter chatAdapter;
    private ArrayList<EMMessage> messages;
    private String userName;
    private ChatReceiver chatReceiver;
    protected File cameraFile;
    static final int ITEM_TAKE_PICTURE = 1;
    static final int ITEM_PICTURE = 2;
    static final int ITEM_LOCATION = 3;
    protected static final int REQUEST_CODE_MAP = 1;
    protected static final int REQUEST_CODE_CAMERA = 2;
    protected static final int REQUEST_CODE_LOCAL = 3;
    protected int[] itemStrings = { R.string.attach_take_pic, R.string.attach_picture, R.string.attach_location };
    protected int[] itemdrawables = { R.drawable.ease_chat_takepic_selector, R.drawable.ease_chat_image_selector,
            R.drawable.ease_chat_location_selector };
    protected int[] itemIds = { ITEM_TAKE_PICTURE, ITEM_PICTURE, ITEM_LOCATION };
    protected EaseChatFragmentListener chatFragmentListener;
    protected MyItemClickListener extendMenuItemClickListener;
    protected String toChatUsername;
    protected int chatType;
    protected InputMethodManager inputManager;
    protected ClipboardManager clipboard;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        chatAdapter = new ChatAdapter(this);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setRefreshEnabled(true);
        list.setFooterEnabled(false);
        userName = getIntent().getStringExtra("username");
        toChatUsername=userName;
        title.setText(userName);
        list.setAdapter(chatAdapter);
        messages = (ArrayList<EMMessage>) EMClient.getInstance().chatManager().getConversation(userName).getAllMessages();
        chatAdapter.addAll(messages);
        list.showRecycler();
        list.scrollToPosition(messages.size() - 1);
        list.setRefreshListener(this);
        IntentFilter filter = new IntentFilter(
                "com.callba.chat");
        chatReceiver = new ChatReceiver();
        registerReceiver(chatReceiver, filter);
        EMClient.getInstance().chatManager().getConversation(userName).markAllMessagesAsRead();
        Intent intent = new Intent("com.callba.asread");
        sendBroadcast(intent);
        extendMenuItemClickListener = new MyItemClickListener();
        registerExtendMenuItem();
        inputMenu.init(null);
        inputMenu.setChatInputMenuListener(new ChatInputMenuListener() {

            @Override
            public void onSendMessage(String content) {
                // 发送文本消息
                sendTextMessage(content);
            }

            @Override
            public boolean onPressToSpeakBtnTouch(View v, MotionEvent event) {
               /* return voiceRecorderView.onPressToSpeakBtnTouch(v, event, new EaseVoiceRecorderCallback() {

                    @Override
                    public void onVoiceRecordComplete(String voiceFilePath, int voiceTimeLength) {
                        // 发送语音消息
                        sendVoiceMessage(voiceFilePath, voiceTimeLength);
                    }
                });*/
                return false;
            }

            @Override
            public void onBigExpressionClicked(EaseEmojicon emojicon) {
                //发送大表情(动态表情)
                sendBigExpressionMessage(emojicon.getName(), emojicon.getIdentityCode());
            }
        });
        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
    //发送消息方法
    //==========================================================================
    protected void sendTextMessage(String content) {
        System.out.println(content);
        EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
        sendMessage(message);
    }

    protected void sendBigExpressionMessage(String name, String identityCode){
        EMMessage message = EaseCommonUtils.createExpressionMessage(toChatUsername, name, identityCode);
        sendMessage(message);
    }

    protected void sendVoiceMessage(String filePath, int length) {
        EMMessage message = EMMessage.createVoiceSendMessage(filePath, length, toChatUsername);
        sendMessage(message);
    }

    protected void sendImageMessage(String imagePath) {
        EMMessage message = EMMessage.createImageSendMessage(imagePath, false, toChatUsername);
        sendMessage(message);
    }

    protected void sendLocationMessage(double latitude, double longitude, String locationAddress) {
        EMMessage message = EMMessage.createLocationSendMessage(latitude, longitude, locationAddress, toChatUsername);
        sendMessage(message);
    }

    protected void sendVideoMessage(String videoPath, String thumbPath, int videoLength) {
        EMMessage message = EMMessage.createVideoSendMessage(videoPath, thumbPath, videoLength, toChatUsername);
        sendMessage(message);
    }

    protected void sendFileMessage(String filePath) {
        EMMessage message = EMMessage.createFileSendMessage(filePath, toChatUsername);
        sendMessage(message);
    }

    protected void sendMessage(EMMessage message){
        if (message == null) {
            return;
        }
        if(chatFragmentListener != null){
            //设置扩展属性
            chatFragmentListener.onSetMessageAttributes(message);
        }
        // 如果是群聊，设置chattype,默认是单聊
        if (chatType == EaseConstant.CHATTYPE_GROUP){
            message.setChatType(EMMessage.ChatType.GroupChat);
        }else if(chatType == EaseConstant.CHATTYPE_CHATROOM){
            message.setChatType(EMMessage.ChatType.ChatRoom);
        }
        //发送消息
        EMClient.getInstance().chatManager().sendMessage(message);
        //刷新ui
      /*  if(isMessageListInited) {
            messageList.refreshSelectLast();
        }*/


        chatAdapter.addAll(message);
        chatAdapter.notifyItemChanged(chatAdapter.getCount() - 1);
        list.scrollToPosition(chatAdapter.getCount() - 1);
    }


    public void resendMessage(EMMessage message){
        message.setStatus(EMMessage.Status.CREATE);
        EMClient.getInstance().chatManager().sendMessage(message);
        //messageList.refresh();
    }

    //===================================================================================


    /**
     * 根据图库图片uri发送图片
     *
     * @param selectedImage
     */
    protected void sendPicByUri(Uri selectedImage) {
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
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

    /**
     * 根据uri发送文件
     * @param uri
     */
    protected void sendFileByUri(Uri uri){
        String filePath = null;
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = null;

            try {
                cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    filePath = cursor.getString(column_index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            filePath = uri.getPath();
        }
        File file = new File(filePath);
        if (file == null || !file.exists()) {
            Toast.makeText(this, R.string.File_does_not_exist, 0).show();
            return;
        }
        //大于10M不让发送
        if (file.length() > 10 * 1024 * 1024) {
            Toast.makeText(this, R.string.The_file_is_not_greater_than_10_m, 0).show();
            return;
        }
        sendFileMessage(filePath);
    }
    @Override
    public void onHeaderRefresh() {
        messages = (ArrayList<EMMessage>) EMClient.getInstance().chatManager().getConversation(userName).getAllMessages();
        chatAdapter.clear();
        chatAdapter.addAll(messages);
        list.scrollToPosition(messages.size() - 1);
        list.setHeaderRefreshing(false);
    }

    @Override
    public void onFooterRefresh() {

    }

    @Override
    public void init() {

    }

    @Override
    public void refresh(Object... params) {

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(chatReceiver);
        super.onDestroy();
    }

  /*  @OnClick(R.id.btn_send)
    public void onClick() {
        Log.i("submit", userName);
        if (content.getText().toString().equals(""))
            return;
        EMMessage message = EMMessage.createTxtSendMessage(content.getText().toString(), userName);
        //发送消息
        EMClient.getInstance().chatManager().sendMessage(message);
        chatAdapter.addAll(message);
        chatAdapter.notifyItemChanged(chatAdapter.getCount() - 1);
        list.scrollToPosition(chatAdapter.getCount() - 1);


    }*/

    class ChatReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra("username").equals((userName))) {
                messages = (ArrayList<EMMessage>) EMClient.getInstance().chatManager().getConversation(userName).getAllMessages();
                chatAdapter.clear();
                chatAdapter.addAll(messages);
                list.scrollToPosition(messages.size() - 1);
            }
        }
    }
    /**
     * 注册底部菜单扩展栏item; 覆盖此方法时如果不覆盖已有item，item的id需大于3
     */
    protected void registerExtendMenuItem(){
        for(int i = 0; i < itemStrings.length; i++){
            inputMenu.registerExtendMenuItem(itemStrings[i], itemdrawables[i], itemIds[i], extendMenuItemClickListener);
        }
    }
    /**
     * 扩展菜单栏item点击事件
     *
     */
    class MyItemClickListener implements EaseChatExtendMenu.EaseChatExtendMenuItemClickListener{

        @Override
        public void onClick(int itemId, View view) {
            if(chatFragmentListener != null){
                if(chatFragmentListener.onExtendMenuItemClick(itemId, view)){
                    return;
                }
            }
            switch (itemId) {
                case ITEM_TAKE_PICTURE: // 拍照
                    selectPicFromCamera();
                    break;
                case ITEM_PICTURE:
                    selectPicFromLocal(); // 图库选择图片
                    break;
                case ITEM_LOCATION: // 位置
                   // startActivityForResult(new Intent(this, EaseBaiduMapActivity.class), REQUEST_CODE_MAP);
                    break;

                default:
                    break;
            }
        }
}
    /**
     * 照相获取图片
     */
    protected void selectPicFromCamera() {
        if (!EaseCommonUtils.isExitsSdcard()) {
            Toast.makeText(this, R.string.sd_card_does_not_exist, 0).show();
            return;
        }

        cameraFile = new File(PathUtil.getInstance().getImagePath(), EMClient.getInstance().getCurrentUser()
                + System.currentTimeMillis() + ".jpg");
        cameraFile.getParentFile().mkdirs();
        startActivityForResult(
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile)),
                REQUEST_CODE_CAMERA);
    }

    /**
     * 从图库获取图片
     */
    protected void selectPicFromLocal() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");

        } else {
            intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, REQUEST_CODE_LOCAL);
    }
    public interface EaseChatFragmentListener{
        /**
         * 设置消息扩展属性
         */
        void onSetMessageAttributes(EMMessage message);

        /**
         * 进入会话详情
         */
        void onEnterToChatDetails();

        /**
         * 用户头像点击事件
         * @param username
         */
        void onAvatarClick(String username);

        /**
         * 消息气泡框点击事件
         */
        boolean onMessageBubbleClick(EMMessage message);

        /**
         * 消息气泡框长按事件
         */
        void onMessageBubbleLongClick(EMMessage message);

        /**
         * 扩展输入栏item点击事件,如果要覆盖EaseChatFragment已有的点击事件，return true
         * @param view
         * @param itemId
         * @return
         */
        boolean onExtendMenuItemClick(int itemId, View view);

        /**
         * 设置自定义chatrow提供者
         * @return
         */
        //EaseCustomChatRowProvider onSetCustomChatRowProvider();
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
}
