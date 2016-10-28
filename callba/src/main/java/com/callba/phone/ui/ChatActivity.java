package com.callba.phone.ui;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.callba.R;
import com.callba.phone.Constant;
import com.callba.phone.DemoHelper;
import com.callba.phone.EaseConstant;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.EaseEmojicon;
import com.callba.phone.bean.EaseUser;
import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.controller.EaseUI;
import com.callba.phone.manager.UserManager;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.ui.ease.EaseChatFragmentListener;
import com.callba.phone.ui.ease.EaseGroupRemoveListener;
import com.callba.phone.util.EaseCommonUtils;
import com.callba.phone.util.EaseUserUtils;
import com.callba.phone.util.Logger;
import com.callba.phone.util.SimpleHandler;
import com.callba.phone.widget.EaseAlertDialog;
import com.callba.phone.widget.EaseChatExtendMenu;
import com.callba.phone.widget.EaseChatInputMenu;
import com.callba.phone.widget.EaseChatInputMenu.ChatInputMenuListener;
import com.callba.phone.widget.EaseChatMessageList;
import com.callba.phone.widget.EaseVoiceRecorderView;
import com.callba.phone.widget.EaseVoiceRecorderView.EaseVoiceRecorderCallback;
import com.callba.phone.widget.chatrow.EaseCustomChatRowProvider;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.util.PathUtil;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by PC-20160514 on 2016/5/27.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.chat,
        navigationId = R.drawable.press_back
)
public class ChatActivity extends BaseActivity implements EaseChatFragmentListener {
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.input_menu)
    EaseChatInputMenu inputMenu;
    @BindView(R.id.message_list)
    EaseChatMessageList messageList;
    protected ListView listView;
    protected SwipeRefreshLayout swipeRefreshLayout;
    private String userName;
    protected File cameraFile;
    static final int ITEM_TAKE_PICTURE = 1;
    static final int ITEM_PICTURE = 2;
    static final int ITEM_LOCATION = 3;
    protected static final int REQUEST_CODE_MAP = 1;
    protected static final int REQUEST_CODE_CAMERA = 2;
    protected static final int REQUEST_CODE_LOCAL = 3;
    protected static final int USER_INFO=4;
    private static final int REQUEST_CODE_SELECT_VIDEO = 11;
    private static final int REQUEST_CODE_SELECT_FILE = 12;
    private static final int REQUEST_CODE_GROUP_DETAIL = 13;
    private static final int REQUEST_CODE_CONTEXT_MENU = 14;
    protected int[] itemStrings = {R.string.attach_take_pic, R.string.attach_picture, R.string.attach_location};
    protected int[] itemdrawables = {R.drawable.ease_chat_takepic_selector, R.drawable.ease_chat_image_selector,
            R.drawable.ease_chat_location_selector};
    protected int[] itemIds = {ITEM_TAKE_PICTURE, ITEM_PICTURE, ITEM_LOCATION};
    protected EaseChatFragmentListener chatFragmentListener;
    protected MyItemClickListener extendMenuItemClickListener;
    protected String toChatUsername;
    protected int chatType = EaseConstant.CHATTYPE_SINGLE;
    protected InputMethodManager inputManager;
    protected ClipboardManager clipboard;
    protected boolean isMessageListInited;
    protected EMMessage contextMenuMessage;
    protected EMConversation conversation;
    protected int pagesize = 10;
    protected boolean isloading;
    protected boolean haveMoreData = true;
    private EaseVoiceRecorderView voiceRecorderView;
    private GroupListener groupListener;
    private NotificationManager notificationManager;
    private BroadcastReceiver broadcastReceiver;
    private LocalBroadcastManager broadcastManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        init();
    }

    protected void init() {
        broadcastManager=LocalBroadcastManager.getInstance(this);
        chatFragmentListener = this;
        chatType = getIntent().getIntExtra("chatType", 1);
        if (chatType != EaseConstant.CHATTYPE_SINGLE)
            messageList.setShowUserNick(true);
        voiceRecorderView = (EaseVoiceRecorderView) findViewById(R.id.voice_recorder);
        listView = messageList.getListView();
        userName = getIntent().getStringExtra(Constant.EXTRA_USER_ID);
        toChatUsername = userName;
        Logger.i("userName",userName);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (chatType == EaseConstant.CHATTYPE_SINGLE) { // 单聊
            // 设置标题
                EaseUserUtils.setUserNick(toChatUsername, title);
            //titleBar.setRightImageResource(R.drawable.ease_mm_title_remove);
            if(GlobalConfig.getInstance().getMessage()!=null){
                Logger.i("username",GlobalConfig.getInstance().getMessage().getFrom());
                if(GlobalConfig.getInstance().getMessage().getFrom().equals(toChatUsername)&&GlobalConfig.getInstance().getMessage().getChatType()==  EMMessage.ChatType.Chat);
                notificationManager.cancel(0525);
            }
        } else {
            //titleBar.setRightImageResource(R.drawable.ease_to_group_details_normal);
            if (chatType == EaseConstant.CHATTYPE_GROUP) {
                // 群聊
                EMGroup group = EMClient.getInstance().groupManager().getGroup(toChatUsername);
                if (group != null)
                    title.setText(group.getGroupName());
                // 监听当前会话的群聊解散被T事件
                groupListener = new GroupListener();
                EMClient.getInstance().groupManager().addGroupChangeListener(groupListener);
                if(GlobalConfig.getInstance().getMessage()!=null){
                    if(GlobalConfig.getInstance().getMessage().getTo().equals(toChatUsername)&&GlobalConfig.getInstance().getMessage().getChatType()==  EMMessage.ChatType.GroupChat);
                    notificationManager.cancel(0525);
                }
            } else {
                //onChatRoomViewCreation();
            }

        }
        if (EMClient.getInstance().chatManager().getConversation(userName) != null)
            EMClient.getInstance().chatManager().getConversation(userName).markAllMessagesAsRead();

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
                return voiceRecorderView.onPressToSpeakBtnTouch(v, event, new EaseVoiceRecorderCallback() {

                    @Override
                    public void onVoiceRecordComplete(String voiceFilePath, int voiceTimeLength) {
                        // 发送语音消息
                        sendVoiceMessage(voiceFilePath, voiceTimeLength);
                    }
                });

            }

            @Override
            public void onBigExpressionClicked(EaseEmojicon emojicon) {
                //发送大表情(动态表情)
                sendBigExpressionMessage(emojicon.getName(), emojicon.getIdentityCode());
            }
        });
        swipeRefreshLayout = messageList.getSwipeRefreshLayout();
        setRefreshLayoutListener();
        swipeRefreshLayout.setColorSchemeResources(R.color.holo_blue_bright, R.color.holo_green_light,
                R.color.holo_orange_light, R.color.holo_red_light);
        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (chatType != EaseConstant.CHATTYPE_CHATROOM) {
            onConversationInit();
            onMessageListInit();
            String forward_msg_id = getIntent().getStringExtra("forward_msg_id");
            if (forward_msg_id != null) {
                forwardMessage(forward_msg_id);
            }
        }

        broadcastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //messages = (ArrayList<EMMessage>) EMClient.getInstance().chatManager().getConversation(userName).getAllMessages();
              /*  chatAdapter.clear();
                chatAdapter.addAll(messages);
                list.scrollToPosition(messages.size() - 1);*/
                Logger.i("chatActivity", "receive");
                if(intent.getExtras()!=null){
                EMMessage message = (EMMessage) intent.getExtras().get("message");
                if(message!=null){
                String username = null;
                // 群组消息
                if (message.getChatType() == EMMessage.ChatType.GroupChat || message.getChatType() == EMMessage.ChatType.ChatRoom) {
                    username = message.getTo();
                } else {
                    // 单聊消息
                    username = message.getFrom();
                }

                // 如果是当前会话的消息，刷新聊天页面
                if (username.equals(toChatUsername)) {
                    messageList.refreshSelectLast();
                    // 声音和震动提示有新消息
                    EaseUI.getInstance().getNotifier().viberateAndPlayTone(message);
                } else {
                    // 如果消息不是和当前聊天ID的消息
                    EaseUI.getInstance().getNotifier().onNewMsg(message);
                }
                SimpleHandler.getInstance().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        broadcastManager.sendBroadcast(new Intent((Constant.ACTION_MESSAGR_NUM_CHANGED)));
                    }
                }, 100);
            }}}
        };
        broadcastManager.sendBroadcast(new Intent((Constant.ACTION_MESSAGR_NUM_CHANGED)));
        broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(Constant.ACTION_MESSAGE_CHANGED));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isMessageListInited) {
            messageList.refreshSelectLast();
            DemoHelper.getInstance().pushActivity(this);
            if (chatType == EaseConstant.CHATTYPE_SINGLE) { // 单聊
                // 设置标题
                    EaseUserUtils.setUserNick(toChatUsername, title);
                //titleBar.setRightImageResource(R.drawable.ease_mm_title_remove);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // unregister this event listener when this activity enters the
        // background
        // 把此activity 从foreground activity 列表里移除
        DemoHelper.getInstance().popActivity(this);
    }

    @Override
    protected void onDestroy() {
        if (groupListener != null)
            EMClient.getInstance().groupManager().removeGroupChangeListener(groupListener);
        broadcastManager.unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    protected void onConversationInit() {
        // 获取当前conversation对象

        conversation = EMClient.getInstance().chatManager().getConversation(toChatUsername, EaseCommonUtils.getConversationType(chatType), true);
        // 把此会话的未读数置为0
        conversation.markAllMessagesAsRead();
        // 初始化db时，每个conversation加载数目是getChatOptions().getNumberOfMessagesLoaded
        // 这个数目如果比用户期望进入会话界面时显示的个数不一样，就多加载一些
        final List<EMMessage> msgs = conversation.getAllMessages();
        int msgCount = msgs != null ? msgs.size() : 0;
        if (msgCount < conversation.getAllMsgCount() && msgCount < pagesize) {
            String msgId = null;
            if (msgs != null && msgs.size() > 0) {
                msgId = msgs.get(0).getMsgId();
            }
            conversation.loadMoreMsgFromDB(msgId, pagesize - msgCount);
        }

    }

    protected void onMessageListInit() {
        messageList.init(toChatUsername, chatType, chatFragmentListener != null ?
                chatFragmentListener.onSetCustomChatRowProvider() : null);
        //设置list item里的控件的点击事件
        setListItemClickListener();

        messageList.getListView().setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                inputMenu.hideExtendMenuContainer();
                return false;
            }
        });

        isMessageListInited = true;
    }

    protected void setRefreshLayoutListener() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (listView.getFirstVisiblePosition() == 0 && !isloading && haveMoreData) {
                            List<EMMessage> messages;
                            try {
                                if (chatType == EaseConstant.CHATTYPE_SINGLE) {
                                    messages = conversation.loadMoreMsgFromDB(messageList.getItem(0).getMsgId(),
                                            pagesize);
                                } else {
                                    messages = conversation.loadMoreMsgFromDB(messageList.getItem(0).getMsgId(),
                                            pagesize);
                                }
                            } catch (Exception e1) {
                                swipeRefreshLayout.setRefreshing(false);
                                return;
                            }
                            if (messages.size() > 0) {
                                messageList.refreshSeekTo(messages.size() - 1);
                                if (messages.size() != pagesize) {
                                    haveMoreData = false;
                                }
                            } else {
                                haveMoreData = false;
                            }

                            isloading = false;

                        } else {
                            Toast.makeText(ChatActivity.this, getResources().getString(R.string.no_more_messages),
                                    Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 600);
            }
        });
    }

    protected void setListItemClickListener() {
        messageList.setItemClickListener(new EaseChatMessageList.MessageListItemClickListener() {

            @Override
            public void onUserAvatarClick(String username) {
                if (chatFragmentListener != null) {
                    chatFragmentListener.onAvatarClick(username);
                }
            }

            @Override
            public void onResendClick(final EMMessage message) {
                new EaseAlertDialog(ChatActivity.this, R.string.resend, R.string.confirm_resend, null, new EaseAlertDialog.AlertDialogUser() {
                    @Override
                    public void onResult(boolean confirmed, Bundle bundle) {
                        if (!confirmed) {
                            return;
                        }
                        resendMessage(message);
                    }
                }, true).show();
            }

            @Override
            public void onBubbleLongClick(EMMessage message) {
                contextMenuMessage = message;
                if (chatFragmentListener != null) {
                    chatFragmentListener.onMessageBubbleLongClick(message);
                }
                startActivityForResult((new Intent(ChatActivity.this, ContextMenuActivity.class)).putExtra("message",message)
                                .putExtra("ischatroom", chatType == EaseConstant.CHATTYPE_CHATROOM),
                        REQUEST_CODE_CONTEXT_MENU);
            }

            @Override
            public boolean onBubbleClick(EMMessage message) {
                if (chatFragmentListener != null) {
                    return chatFragmentListener.onMessageBubbleClick(message);
                }
                return false;
            }
        });
    }

    /**
     * 点击进入群组详情
     */
    protected void toGroupDetails() {
        if (chatType == EaseConstant.CHATTYPE_GROUP) {
            EMGroup group = EMClient.getInstance().groupManager().getGroup(toChatUsername);
            if (group == null) {
                Toast.makeText(this, R.string.gorup_not_found, Toast.LENGTH_SHORT).show();
                return;
            }
            if (chatFragmentListener != null) {
                chatFragmentListener.onEnterToChatDetails();
            }
        } else if (chatType == EaseConstant.CHATTYPE_CHATROOM) {
            if (chatFragmentListener != null) {
                chatFragmentListener.onEnterToChatDetails();
            }
        }
    }

    /**
     * 隐藏软键盘
     */
    protected void hideKeyboard() {
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    //发送消息方法
    //==========================================================================
    protected void sendTextMessage(String content) {
        System.out.println(content);
        EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
        sendMessage(message);
    }

    protected void sendBigExpressionMessage(String name, String identityCode) {
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

    protected void sendMessage(EMMessage message) {
        if (message == null) {
            return;
        }
        if (chatFragmentListener != null) {
            //设置扩展属性
            chatFragmentListener.onSetMessageAttributes(message);
        }
        // 如果是群聊，设置chattype,默认是单聊
        if (chatType == EaseConstant.CHATTYPE_GROUP) {
            message.setChatType(EMMessage.ChatType.GroupChat);
        } else if (chatType == EaseConstant.CHATTYPE_CHATROOM) {
            message.setChatType(EMMessage.ChatType.ChatRoom);
        }
        //发送消息
        EMClient.getInstance().chatManager().sendMessage(message); //刷新ui
        if (isMessageListInited) {
            messageList.refreshSelectLast();
        }

        //刷新ui
      /*  if(isMessageListInited) {
            messageList.refreshSelectLast();
        }*/


    /*    chatAdapter.addAll(message);
        chatAdapter.notifyItemChanged(chatAdapter.getCount() - 1);
        list.scrollToPosition(chatAdapter.getCount() - 1);*/
    }


    public void resendMessage(EMMessage message) {
        message.setStatus(EMMessage.Status.CREATE);
        EMClient.getInstance().chatManager().sendMessage(message);
        messageList.refresh();
    }

    //===================================================================================


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

    /**
     * 根据uri发送文件
     *
     * @param uri
     */
    protected void sendFileByUri(Uri uri) {
        String filePath = null;
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
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
            Toast.makeText(this, R.string.File_does_not_exist, Toast.LENGTH_SHORT).show();
            return;
        }
        //大于10M不让发送
        if (file.length() > 10 * 1024 * 1024) {
            Toast.makeText(this, R.string.The_file_is_not_greater_than_10_m, Toast.LENGTH_SHORT).show();
            return;
        }
        sendFileMessage(filePath);
    }


    /**
     * 注册底部菜单扩展栏item; 覆盖此方法时如果不覆盖已有item，item的id需大于3
     */
    protected void registerExtendMenuItem() {
        for (int i = 0; i < itemStrings.length; i++) {
            inputMenu.registerExtendMenuItem(itemStrings[i], itemdrawables[i], itemIds[i], extendMenuItemClickListener);
        }
    }

    /**
     * 扩展菜单栏item点击事件
     */
    class MyItemClickListener implements EaseChatExtendMenu.EaseChatExtendMenuItemClickListener {

        @Override
        public void onClick(int itemId, View view) {
            if (chatFragmentListener != null) {
                if (chatFragmentListener.onExtendMenuItemClick(itemId, view)) {
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
                    Intent intent = new Intent(ChatActivity.this, GeocoderActivity.class);
                    intent.putExtra("latitude", UserManager.getLatitude(ChatActivity.this));
                    intent.putExtra("longitude", UserManager.getLongitude(ChatActivity.this));
                    startActivityForResult(new Intent(ChatActivity.this, GeocoderActivity.class), REQUEST_CODE_MAP);
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
            Toast.makeText(this, R.string.sd_card_does_not_exist, Toast.LENGTH_SHORT).show();
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
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, REQUEST_CODE_LOCAL);
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
            } else if (requestCode == REQUEST_CODE_MAP) { // 地图
                double latitude = data.getDoubleExtra("latitude", 0);
                double longitude = data.getDoubleExtra("longitude", 0);
                String locationAddress = data.getStringExtra("address");
                if (locationAddress != null && !locationAddress.equals("")) {
                    sendLocationMessage(latitude, longitude, locationAddress);
                } else {
                    Toast.makeText(this, R.string.unable_to_get_loaction, Toast.LENGTH_SHORT).show();
                }
            }else if(requestCode== USER_INFO){
                setResult(RESULT_OK);
                finish();
            }

        }
        if (resultCode == 5) title.setText(data.getStringExtra("data"));
        if (requestCode == REQUEST_CODE_CONTEXT_MENU) {
            switch (resultCode) {
                case ContextMenuActivity.RESULT_CODE_COPY: // copy
                    clipboard.setPrimaryClip(ClipData.newPlainText(null,
                            ((EMTextMessageBody) contextMenuMessage.getBody()).getMessage()));
                    break;
                case ContextMenuActivity.RESULT_CODE_DELETE: // delete
                    conversation.removeMessage(contextMenuMessage.getMsgId());
                    messageList.refresh();
                    break;

                case ContextMenuActivity.RESULT_CODE_FORWARD: // forward
                    Intent intent = new Intent(this, PickContactNoCheckboxActivity.class);
                    intent.putExtra("forward_msg_id", contextMenuMessage.getMsgId());
                    startActivity(intent);

                    break;

                default:
                    break;
            }}

    }

    protected void emptyHistory() {
        String msg = getResources().getString(R.string.Whether_to_empty_all_chats);
        new EaseAlertDialog(this, null, msg, null, new EaseAlertDialog.AlertDialogUser() {

            @Override
            public void onResult(boolean confirmed, Bundle bundle) {
                if (confirmed) {
                    // 清空会话
                    EMClient.getInstance().chatManager().deleteConversation(toChatUsername, true);
                    messageList.refresh();
                    LocalBroadcastManager.getInstance(ChatActivity.this).sendBroadcast(new Intent(Constant.ACTION_MESSAGE_CHANGED));
                }
            }
        }, true).show();
        ;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (chatType == EaseConstant.CHATTYPE_SINGLE&&toChatUsername.length()>=11)
            getMenuInflater().inflate(R.menu.menu_chat, menu);
        else if(chatType == EaseConstant.CHATTYPE_SINGLE&&toChatUsername.length()<11){
            getMenuInflater().inflate(R.menu.clear, menu);
        }
        if (chatType == EaseConstant.CHATTYPE_GROUP)
            getMenuInflater().inflate(R.menu.menu_group_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.detail:
                toGroupDetails();
                break;
            case R.id.person:
                Intent intent = new Intent(this, UserInfoActivity.class);
                intent.putExtra("username", userName);
                startActivityForResult(intent,USER_INFO);
                break;
            case R.id.callback:
                String name;
                if(toChatUsername.length()>10){
                EaseUser user=EaseUserUtils.getUserInfo(toChatUsername);
                name=toChatUsername;
                if (user != null) {
                    if(!TextUtils.isEmpty(user.getNick()))
                    name=user.getNick();
                    if(!TextUtils.isEmpty(user.getRemark()))
                        name=user.getRemark();
                }
                Intent intent1 = new Intent(this, SelectDialPopupWindow.class);
                intent1.putExtra("name",name);
                intent1.putExtra("number", toChatUsername.substring(0,11));

                startActivity(intent1);}else toast("不支持的用户类型");
                break;
            case R.id.clear:
                emptyHistory();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        init();
    }

    /**
     * 监测群组解散或者被T事件
     */
    class GroupListener extends EaseGroupRemoveListener {

        @Override
        public void onUserRemoved(final String groupId, final String groupName) {
            runOnUiThread(new Runnable() {

                public void run() {
                    if (toChatUsername.equals(groupId)) {
                        // Toast.makeText(ChatActivity.this, R.string.you_are_group, 1).show();
                        // Toast.makeText(ChatActivity.this, "你已被群\""+groupName+"\"移除", 1).show();
                        finish();
                    }
                }
            });
        }

        @Override
        public void onGroupDestroyed(final String groupId, final String groupName) {
            // 群组解散正好在此页面，提示群组被解散，并finish此页面
            runOnUiThread(new Runnable() {
                public void run() {
                    if (toChatUsername.equals(groupId)) {
                        // Toast.makeText(ChatActivity.this, R.string.the_current_group, 1).show();
                        //Toast.makeText(ChatActivity.this, "群\""+groupName+"\"已被解散", 1).show();
                        finish();
                    }
                }
            });
        }

    }

    @Override
    public void onAvatarClick(String username) {
        if(!username.equals(EMClient.getInstance().getCurrentUser())||!username.equals("admin"))
        {Intent intent = new Intent(this, UserInfoActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);}
    }

    @Override
    public void onSetMessageAttributes(EMMessage message) {

    }

    @Override
    public void onEnterToChatDetails() {
        if (chatType == Constant.CHATTYPE_GROUP) {
            EMGroup group = EMClient.getInstance().groupManager().getGroup(toChatUsername);
            if (group == null) {
                Toast.makeText(this, R.string.gorup_not_found, Toast.LENGTH_SHORT).show();
                return;
            }
            startActivityForResult(
                    (new Intent(this, GroupDetailActivity.class).putExtra("groupId", toChatUsername)),
                    REQUEST_CODE_GROUP_DETAIL);
        } else if (chatType == Constant.CHATTYPE_CHATROOM) {
            //startActivityForResult(new Intent(getActivity(), ChatRoomDetailsActivity.class).putExtra("roomId", toChatUsername), REQUEST_CODE_GROUP_DETAIL);
        }
    }

    @Override
    public boolean onMessageBubbleClick(EMMessage message) {
        return false;
    }

    @Override
    public void onMessageBubbleLongClick(EMMessage message) {

    }

    @Override
    public boolean onExtendMenuItemClick(int itemId, View view) {
        return false;
    }

    @Override
    public EaseCustomChatRowProvider onSetCustomChatRowProvider() {
        return null;
    }
    protected void forwardMessage(String forward_msg_id) {
        final EMMessage forward_msg = EMClient.getInstance().chatManager().getMessage(forward_msg_id);
        EMMessage.Type type = forward_msg.getType();
        switch (type) {
            case TXT:
                if(forward_msg.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION, false)){
                    sendBigExpressionMessage(((EMTextMessageBody) forward_msg.getBody()).getMessage(),
                            forward_msg.getStringAttribute(EaseConstant.MESSAGE_ATTR_EXPRESSION_ID, null));
                }else{
                    // get the content and send it
                    String content = ((EMTextMessageBody) forward_msg.getBody()).getMessage();
                    sendTextMessage(content);
                }
                break;
            case IMAGE:
                // send image
                String filePath = ((EMImageMessageBody) forward_msg.getBody()).getLocalUrl();
                if (filePath != null) {
                    File file = new File(filePath);
                    if (!file.exists()) {
                        // send thumb nail if original image does not exist
                        filePath = ((EMImageMessageBody) forward_msg.getBody()).thumbnailLocalPath();
                    }
                    sendImageMessage(filePath);
                }
                break;
            default:
                break;
        }

        if(forward_msg.getChatType() == EMMessage.ChatType.ChatRoom){
            EMClient.getInstance().chatroomManager().leaveChatRoom(forward_msg.getTo());
        }
    }
}
