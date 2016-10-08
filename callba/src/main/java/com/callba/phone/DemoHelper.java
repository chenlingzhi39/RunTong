package com.callba.phone;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.callba.R;
import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.ui.ChatActivity;
import com.callba.phone.ui.MainTabActivity;
import com.callba.phone.ui.NewFriendsMsgActivity;
import com.callba.phone.manager.UserProfileManager;
import com.callba.phone.bean.BaseUser;
import com.callba.phone.bean.EaseNotifier;
import com.callba.phone.bean.EaseNotifier.*;
import com.callba.phone.bean.EaseUser;
import com.callba.phone.controller.EaseUI;
import com.callba.phone.controller.EaseUI.*;
import com.callba.phone.db.DemoDBManager;
import com.callba.phone.db.InviteMessage;
import com.callba.phone.db.InviteMessage.*;
import com.callba.phone.db.InviteMessgeDao;
import com.callba.phone.db.UserDao;
import com.callba.phone.domain.RobotUser;
import com.callba.phone.manager.UserManager;
import com.callba.phone.receiver.CallReceiver;
import com.callba.phone.util.EaseCommonUtils;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.util.PreferenceManager;
import com.callba.phone.util.SimpleHandler;
import com.callba.phone.util.StringUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMContactListener;
import com.hyphenate.EMError;
import com.hyphenate.EMGroupChangeListener;
import com.hyphenate.EMMessageListener;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessage.ChatType;
import com.hyphenate.chat.EMMessage.Status;
import com.hyphenate.chat.EMMessage.Type;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.EMLog;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import okhttp3.Call;

public class DemoHelper {
    /**
     * 数据同步listener
     */
    static public interface DataSyncListener {
        /**
         * 同步完毕
         * @param success true：成功同步到数据，false失败
         */
        public void onSyncComplete(boolean success);
    }

    protected static final String TAG = "DemoHelper";
    
	private EaseUI easeUI;
	
    /**
     * EMEventListener
     */
    protected EMMessageListener messageListener = null;

	private Map<String, EaseUser> contactList;

	private Map<String, RobotUser> robotList;

    private UserProfileManager userProManager;

	private static DemoHelper instance = null;
	
	private DemoModel demoModel = null;
	
	/**
     * HuanXin sync groups status listener
     */
    private List<DataSyncListener> syncGroupsListeners;
    /**
     * HuanXin sync contacts status listener
     */
    private List<DataSyncListener> syncContactsListeners;
    /**
     * HuanXin sync blacklist status listener
     */
    private List<DataSyncListener> syncBlackListListeners;

    private boolean isSyncingGroupsWithServer = false;
    private boolean isSyncingContactsWithServer = false;
    private boolean isSyncingBlackListWithServer = false;
    private boolean isGroupsSyncedWithServer = false;
    private boolean isContactsSyncedWithServer = false;
    private boolean isBlackListSyncedWithServer = false;
    
    private boolean alreadyNotified = false;
	
	public boolean isVoiceCalling;
    public boolean isVideoCalling;

	private String username;

    private Context appContext;

    private CallReceiver callReceiver;

    private EMConnectionListener connectionListener;

    private InviteMessgeDao inviteMessgeDao;
    private UserDao userDao;

    private LocalBroadcastManager broadcastManager;

    private boolean isGroupAndContactListenerRegisted;
    Gson gson=new Gson();
	private DemoHelper() {
	}

	public synchronized static DemoHelper getInstance() {
		if (instance == null) {
			instance = new DemoHelper();
		}
		return instance;
	}

	/**
	 * init helper
	 * 
	 * @param context
	 *            application context
	 */
	public void init(Context context) {
	    demoModel = new DemoModel(context);
	    EMOptions options = initChatOptions();
	    //options传null则使用默认的
		if (EaseUI.getInstance().init(context, options)) {
		    appContext = context;
		    
		    //设为调试模式，打成正式包时，最好设为false，以免消耗额外的资源
		    EMClient.getInstance().setDebugMode(false);
		    //get easeui instance
		    easeUI = EaseUI.getInstance();
		    //调用easeui的api设置providers
		    setEaseUIProviders();
			//初始化PreferenceManager
			PreferenceManager.init(context);
			//初始化用户管理类
			getUserProfileManager().init(context);
			
			//设置全局监听
			setGlobalListeners();
			broadcastManager = LocalBroadcastManager.getInstance(appContext);
	        initDbDao();
		}
	}

	
	private EMOptions initChatOptions(){
        Log.d(TAG, "init HuanXin Options");
        // 获取到EMChatOptions对象
        EMOptions options = new EMOptions();
        // 默认添加好友时，是不需要验证的，改成需要验证
        options.setAcceptInvitationAlways(true);
        // 设置是否需要已读回执
        options.setRequireAck(true);
        // 设置是否需要已送达回执
        options.setRequireDeliveryAck(false);
        options.setAutoLogin(false);
        //使用gcm和mipush时，把里面的参数替换成自己app申请的
        //设置google推送，需要的GCM的app可以设置此参数
        //options.setGCMNumber("324169311137");
        //在小米手机上当app被kill时使用小米推送进行消息提示，同GCM一样不是必须的
        //options.setMipushConfig("2882303761517426801", "5381742660801");
        //集成华为推送时需要设置
//        options.setHuaweiPushAppId("10492024");
        
        options.allowChatroomOwnerLeave(getModel().isChatroomOwnerLeaveAllowed());
        options.setDeleteMessagesAsExitGroup(getModel().isDeleteMessagesAsExitGroup());
        options.setAutoAcceptGroupInvitation(getModel().isAutoAcceptGroupInvitation());
        
        return options;
    }

    protected void setEaseUIProviders() {
        //需要easeui库显示用户头像和昵称设置此provider
        easeUI.setUserProfileProvider(new EaseUserProfileProvider() {
            
            @Override
            public EaseUser getUser(String username) {
                return getUserInfo(username);
            }
        });
        
        //不设置，则使用easeui默认的
        easeUI.setSettingsProvider(new EaseSettingsProvider() {
            
            @Override
            public boolean isSpeakerOpened() {
                return demoModel.getSettingMsgSpeaker();
            }
            
            @Override
            public boolean isMsgVibrateAllowed(EMMessage message) {
                return demoModel.getSettingMsgVibrate();
            }
            
            @Override
            public boolean isMsgSoundAllowed(EMMessage message) {
                return demoModel.getSettingMsgSound();
            }
            
            @Override
            public boolean isMsgNotifyAllowed(EMMessage message) {
                if(message == null){
                    return demoModel.getSettingMsgNotification();
                }
                if(!demoModel.getSettingMsgNotification()){
                    return false;
                }else{
                    //如果允许新消息提示
                    //屏蔽的用户和群组不提示用户
                    String chatUsename = null;
                    List<String> notNotifyIds = null;
                    // 获取设置的不提示新消息的用户或者群组ids
                    if (message.getChatType() == ChatType.Chat) {
                        chatUsename = message.getFrom();
                        notNotifyIds = demoModel.getDisabledIds();
                    } else {
                        chatUsename = message.getTo();
                        notNotifyIds = demoModel.getDisabledGroups();
                    }

                    if (notNotifyIds == null || !notNotifyIds.contains(chatUsename)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        });
      /*  //设置表情provider
        easeUI.setEmojiconInfoProvider(new EaseEmojiconInfoProvider() {
            
            @Override
            public EaseEmojicon getEmojiconInfo(String emojiconIdentityCode) {
                EaseEmojiconGroupEntity data = EmojiconExampleGroupData.getData();
                for(EaseEmojicon emojicon : data.getEmojiconList()){
                    if(emojicon.getIdentityCode().equals(emojiconIdentityCode)){
                        return emojicon;
                    }
                }
                return null;
            }

            @Override
            public Map<String, Object> getTextEmojiconMapping() {
                //返回文字表情emoji文本和图片(resource id或者本地路径)的映射map
                return null;
            }
        });*/
        
        //不设置，则使用easeui默认的
        easeUI.getNotifier().setNotificationInfoProvider(new EaseNotificationInfoProvider() {
            
            @Override
            public String getTitle(EMMessage message) {
              //修改标题,这里使用默认
                if (message.getChatType() == ChatType.Chat)  // 单聊信息
                return "你有一条新消息";
                    else if (message.getChatType() == ChatType.GroupChat)return "你有一条群消息";
                else return "你有一条新消息";
            }
            
            @Override
            public int getSmallIcon(EMMessage message) {
              //设置小图标，这里为默认
                return R.drawable.logo_notification;
            }

            @Override
            public Bitmap getLargeIcon(EMMessage message) {
                EaseUser user=getUserInfo(message.getFrom());
                if(user!=null){
                    if(!user.getAvatar().equals(""))
                    try {
                        Bitmap theBitmap = Glide.
                                with(appContext).
                                load(user.getAvatar()).
                                asBitmap().
                                into(100, 100). // Width and height
                                get();
                        return theBitmap;
                    }catch (Exception e){

                    }
                  }
                if(message.getFrom().equals("admin"))
                    return BitmapFactory.decodeResource(appContext.getResources(),
                            R.drawable.system_message);
                return BitmapFactory.decodeResource(appContext.getResources(),
                        R.drawable.logo);
            }

            @Override
            public String getDisplayedText(EMMessage message) {
                // 设置状态栏的消息提示，可以根据message的类型做相应提示
                String ticker = EaseCommonUtils.getMessageDigest(message, appContext);
                if(message.getType() == Type.TXT){
                    ticker = ticker.replaceAll("\\[.{2,3}\\]", "[表情]");
                }
                EaseUser user = getUserInfo(message.getFrom());
                if(user != null){
                    return getUserInfo(message.getFrom()).getNick() + ": " + ticker;
                }else {
                    StringBuffer buffer = new StringBuffer(message.getFrom().length()>10?message.getFrom().substring(0,11):message.getFrom());
                    //buffer.replace(3,8,"****");
                    if(message.getFrom().equals("admin"))
                        buffer=new StringBuffer("系统消息");
                    return buffer + ": " + ticker;
                }
            }
            
            @Override
            public String getLatestText(EMMessage message, int fromUsersNum, int messageNum) {
                String ticker = EaseCommonUtils.getMessageDigest(message, appContext);
                if(message.getType() == Type.TXT){
                    ticker = ticker.replaceAll("\\[.{2,3}\\]", "[表情]");
                }
                EaseUser user = getUserInfo(message.getFrom());
                if(user != null){
                    return getUserInfo(message.getFrom()).getNick() + ": " + ticker;
                }else {

                    StringBuffer buffer = new StringBuffer(message.getFrom().length()>10?message.getFrom().substring(0,11):message.getFrom());
                    //buffer.replace(3,8,"****");
                    if(message.getFrom().equals("admin"))
                    buffer=new StringBuffer("系统消息");
                    return buffer + ": " + ticker;
                }
                //buffer.replace(3,8,"****");
                // return fromUsersNum + "个基友，发来了" + messageNum + "条消息";
            }
            
            @Override
            public Intent getLaunchIntent(EMMessage message) {
                Intent intent = new Intent(appContext, ChatActivity.class);
                ChatType chatType=message.getChatType();
                //设置点击通知栏跳转事件
                if (chatType == ChatType.Chat) { // 单聊信息
                    intent.putExtra("userId", message.getFrom());
                    intent.putExtra("chatType", Constant.CHATTYPE_SINGLE);
                } else { // 群聊信息
                    // message.getTo()为群聊id
                    intent.putExtra("userId", message.getTo());
                    if(chatType == ChatType.GroupChat){
                        intent.putExtra("chatType", Constant.CHATTYPE_GROUP);
                    }else{
                        intent.putExtra("chatType", Constant.CHATTYPE_CHATROOM);
                    }

                };
               /* //有电话时优先跳转到通话页面
                if(isVideoCalling){
                    intent = new Intent(appContext, VideoCallActivity.class);
                }else if(isVoiceCalling){
                    intent = new Intent(appContext, VoiceCallActivity.class);
                }else{
                    ChatType chatType = message.getChatType();
                    if (chatType == ChatType.Chat) { // 单聊信息
                        intent.putExtra("userId", message.getFrom());
                        intent.putExtra("chatType", Constant.CHATTYPE_SINGLE);
                    } else { // 群聊信息
                        // message.getTo()为群聊id
                        intent.putExtra("userId", message.getTo());
                        if(chatType == ChatType.GroupChat){
                            intent.putExtra("chatType", Constant.CHATTYPE_GROUP);
                        }else{
                            intent.putExtra("chatType", Constant.CHATTYPE_CHATROOM);
                        }
                        
                    }
                }*/
                return intent;
            }
        });
    }
    
    /**
     * 设置全局事件监听
     */
    protected void setGlobalListeners(){
        syncGroupsListeners = new ArrayList<DataSyncListener>();
        syncContactsListeners = new ArrayList<DataSyncListener>();
        syncBlackListListeners = new ArrayList<DataSyncListener>();
        
        isGroupsSyncedWithServer = demoModel.isGroupsSynced();
        isContactsSyncedWithServer = demoModel.isContactSynced();
        isBlackListSyncedWithServer = demoModel.isBacklistSynced();
        
        // create the global connection listener
        connectionListener = new EMConnectionListener() {
            @Override
            public void onDisconnected(final int error) {
                if (error == EMError.USER_REMOVED) {
                    onCurrentAccountRemoved();
                }else if (error == EMError.USER_LOGIN_ANOTHER_DEVICE) {
                    onConnectionConflict();
                }
             /*   SimpleHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(appContext,"服务器已断开",Toast.LENGTH_SHORT).show();
                    }
                });*/

            }

            @Override
            public void onConnected() {
             /*   SimpleHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(appContext,"服务器已连接",Toast.LENGTH_SHORT).show();
                    }
                });*/
                // in case group and contact were already synced, we supposed to notify sdk we are ready to receive the events
                if(isGroupsSyncedWithServer && isContactsSyncedWithServer){
                    new Thread(){
                        @Override
                        public void run(){
                            DemoHelper.getInstance().notifyForRecevingEvents();
                        }
                    }.start();
                }else{
                    if(!isGroupsSyncedWithServer){
                        asyncFetchGroupsFromServer(null);
                    }
                    
                    if(!isContactsSyncedWithServer){
                        Logger.i(TAG,"asyncFetchContactsFromServer");
                        asyncFetchContactsFromServer(null);
                    }
                    
                    if(!isBlackListSyncedWithServer){
                        asyncFetchBlackListFromServer(null);
                    }
                }
            }
        };
        
       /*
        IntentFilter callFilter = new IntentFilter(EMClient.getInstance().callManager().getIncomingCallBroadcastAction());
        if(callReceiver == null){
            callReceiver = new CallReceiver();
        }*/
        //注册通话广播接收者
       // appContext.registerReceiver(callReceiver, callFilter);
        //注册连接监听
        EMClient.getInstance().addConnectionListener(connectionListener);
        //注册群组和联系人监听
         registerGroupAndContactListener();
        //注册消息事件监听
        registerEventListener();
        
    }
    
    private void initDbDao() {
        inviteMessgeDao = new InviteMessgeDao(appContext);
        userDao = new UserDao(appContext);
    }
    
    /**
     * 注册群组和联系人监听，由于logout的时候会被sdk清除掉，再次登录的时候需要再注册一下
     */
    public void registerGroupAndContactListener(){
        if(!isGroupAndContactListenerRegisted){
            //注册群组变动监听
            EMClient.getInstance().groupManager().addGroupChangeListener(new MyGroupChangeListener());
            //注册联系人变动监听
            EMClient.getInstance().contactManager().setContactListener(new MyContactListener());
            isGroupAndContactListenerRegisted = true;
        }
        
    }
    
    /**
     * 群组变动监听
     */
    class MyGroupChangeListener implements EMGroupChangeListener {

        @Override
        public void onInvitationReceived(String groupId, String groupName, String inviter, String reason) {
            
            new InviteMessgeDao(appContext).deleteMessage(groupId);
            
            // 用户申请加入群聊
            InviteMessage msg = new InviteMessage();
            msg.setFrom(groupId);
            msg.setTime(System.currentTimeMillis());
            msg.setGroupId(groupId);
            msg.setGroupName(groupName);
            msg.setReason(reason);
            msg.setGroupInviter(inviter);
            Log.d(TAG, "收到邀请加入群聊：" + groupName);
            msg.setStatus(InviteMesageStatus.GROUPINVITATION);
            notifyNewIviteMessage(msg);
            broadcastManager.sendBroadcast(new Intent(Constant.ACTION_GROUP_CHANAGED));
        }

        @Override
        public void onInvitationAccepted(String groupId, String invitee, String reason) {
            new InviteMessgeDao(appContext).deleteMessage(groupId);

            // 对方同意加群邀请
            boolean hasGroup = false;
            EMGroup _group = null;
            for (EMGroup group : EMClient.getInstance().groupManager().getAllGroups()) {
                if (group.getGroupId().equals(groupId)) {
                    hasGroup = true;
                    _group = group;
                    break;
                }
            }
            if (!hasGroup)
                return;

            InviteMessage msg = new InviteMessage();
            msg.setFrom(groupId);
            msg.setTime(System.currentTimeMillis());
            msg.setGroupId(groupId);
            msg.setGroupName(_group == null ? groupId : _group.getGroupName());
            msg.setReason(reason);
            msg.setGroupInviter(invitee);
            Log.d(TAG, invitee + "同意加入群聊：" + _group == null ? groupId : _group.getGroupName());
            msg.setStatus(InviteMesageStatus.GROUPINVITATION_ACCEPTED);
            notifyNewIviteMessage(msg);
            broadcastManager.sendBroadcast(new Intent(Constant.ACTION_GROUP_CHANAGED));
        }

        @Override
        public void onInvitationDeclined(String groupId, String invitee, String reason) {
            
            new InviteMessgeDao(appContext).deleteMessage(groupId);
            
            // 对方同意加群邀请
            boolean hasGroup = false;
            EMGroup group = null;
            for (EMGroup _group : EMClient.getInstance().groupManager().getAllGroups()) {
                if (_group.getGroupId().equals(groupId)) {
                    group = _group;
                    hasGroup = true;
                    break;
                }
            }
            if (!hasGroup)
                return;
            
            InviteMessage msg = new InviteMessage();
            msg.setFrom(groupId);
            msg.setTime(System.currentTimeMillis());
            msg.setGroupId(groupId);
            msg.setGroupName(group == null ? groupId : group.getGroupName());
            msg.setReason(reason);
            msg.setGroupInviter(invitee);
            Log.d(TAG, invitee + "拒绝加入群聊：" + group == null ? groupId : group.getGroupName());
            msg.setStatus(InviteMesageStatus.GROUPINVITATION_DECLINED);
            notifyNewIviteMessage(msg);
            broadcastManager.sendBroadcast(new Intent(Constant.ACTION_GROUP_CHANAGED));
        }

        @Override
        public void onUserRemoved(String groupId,final String groupName) {
            //TODO 提示用户被T了，demo省略此步骤
            EMClient.getInstance().chatManager().deleteConversation(groupName,true);
            appContext.sendBroadcast(new Intent("message_num"));
            broadcastManager.sendBroadcast(new Intent(Constant.ACTION_GROUP_CHANAGED));
            SimpleHandler.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(appContext, "你已被群\""+groupName+"\"移除", 1).show();
                }
            });
            Logger.i("demohelper","用户被T了");
        }

        @Override
        public void onGroupDestroyed(String groupId,final String groupName) {
            // 群被解散
            //TODO 提示用户群被解散,demo省略
            EMClient.getInstance().chatManager().deleteConversation(groupName,true);
            appContext.sendBroadcast(new Intent("message_num"));
            broadcastManager.sendBroadcast(new Intent(Constant.ACTION_GROUP_CHANAGED));
            SimpleHandler.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(appContext, "群\""+groupName+"\"已被解散", 1).show();
                }
            });
            Logger.i("demohelper","群被解散");
        }

        @Override
        public void onApplicationReceived(String groupId, String groupName, String applyer, String reason) {
            
            // 用户申请加入群聊
            InviteMessage msg = new InviteMessage();
            msg.setFrom(applyer);
            msg.setTime(System.currentTimeMillis());
            msg.setGroupId(groupId);
            msg.setGroupName(groupName);
            msg.setReason(reason);
            Log.d(TAG, applyer + " 申请加入群聊：" + groupName);
            msg.setStatus(InviteMesageStatus.BEAPPLYED);
            notifyNewIviteMessage(msg);
            broadcastManager.sendBroadcast(new Intent(Constant.ACTION_GROUP_CHANAGED));
            EaseUser user=getUserInfo(applyer);
            Bitmap bitmap=  BitmapFactory.decodeResource(appContext.getResources(),
                    R.drawable.logo);
            if(user!=null){
                if(!user.getAvatar().equals(""))
                    try {
                        bitmap = Glide.
                                with(appContext).
                                load(user.getAvatar()).
                                asBitmap().
                                into(100, 100). // Width and height
                                get();
                    }catch (Exception e){

                    }
            }

            if ((MyApplication.activities.get(MyApplication.activities.size()-1).getClass()!= NewFriendsMsgActivity.class)&&demoModel.getSettingMsgNotification())
            {
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                        appContext)
                        .setSmallIcon(R.drawable.logo_notification)
                        .setLargeIcon(
                               bitmap)
                        .setContentTitle("群信息")
                        .setContentText((user!=null?user.getNick():msg.getFrom().substring(0,11))+"申请加入群"+"\""+msg.getGroupName()+"\"");
                Intent notificationIntent = new Intent(appContext, NewFriendsMsgActivity.class);
                notificationIntent.putExtra("username", username);
                // TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                // stackBuilder.addParentStack(clazz);
                // stackBuilder.addNextIntent(notificationIntent);
                // PendingIntent resultPendingIntent =
                // stackBuilder.getPendingIntent(
                // 0,
                // PendingIntent.FLAG_UPDATE_CURRENT
                // );
                PendingIntent contentIntent = PendingIntent.getActivity(
                        appContext, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                //mBuilder.setFullScreenIntent(contentIntent, false);
                mBuilder.setAutoCancel(true);
                mBuilder.setContentIntent(contentIntent);
                mBuilder.setPriority(Notification.PRIORITY_HIGH);
      /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mBuilder.setFullScreenIntent(contentIntent, true);*/
                mBuilder.setTicker((user!=null?user.getNick():msg.getFrom().substring(0,11))+"申请加入群"+"\""+msg.getGroupName()+"\"");
                Notification notification = mBuilder.build();
                notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
                //notification.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                final NotificationManager mNotificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(0526, notification);
             /*   Timer timer=new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                       mNotificationManager.cancel(0526);
                    }
                },5000);*/
            }
        }

        @Override
        public void onApplicationAccept(String groupId, String groupName, String accepter) {

            String st4 = appContext.getString(R.string.Agreed_to_your_group_chat_application);
            // 加群申请被同意
            EMMessage msg = EMMessage.createReceiveMessage(Type.TXT);
            msg.setChatType(ChatType.GroupChat);
            msg.setFrom(accepter);
            msg.setTo(groupId);
            msg.setMsgId(UUID.randomUUID().toString());
            StringBuffer buffer = new StringBuffer(accepter.substring(0,11));
            msg.addBody(new EMTextMessageBody((getUserInfo(accepter)!=null?getUserInfo(accepter).getNick():buffer.toString()) + " " +st4));
            msg.setStatus(Status.SUCCESS);
            // 保存同意消息
            EMClient.getInstance().chatManager().saveMessage(msg);
            // 提醒新消息
            //getNotifier().viberateAndPlayTone(msg);
            getNotifier().onNewMsg(msg);
            appContext.sendBroadcast(new Intent("message_num"));
            broadcastManager.sendBroadcast(new Intent(Constant.ACTION_GROUP_CHANAGED));
        }

        @Override
        public void onApplicationDeclined(String groupId, String groupName, String decliner, String reason) {
            // 加群申请被拒绝，demo未实现
        }

        @Override
        public void onAutoAcceptInvitationFromGroup(String groupId, String inviter, String inviteMessage) {
            // 被邀请
            String st3 = appContext.getString(R.string.Invite_you_to_join_a_group_chat);
            EMMessage msg = EMMessage.createReceiveMessage(Type.TXT);
            msg.setChatType(ChatType.GroupChat);
            msg.setFrom(inviter);
            msg.setTo(groupId);
            msg.setMsgId(UUID.randomUUID().toString());
            StringBuffer buffer = new StringBuffer(inviter.substring(0,11));
            //buffer.replace(3,8,"****");
            msg.addBody(new EMTextMessageBody((getUserInfo(inviter)!=null?getUserInfo(inviter).getNick():buffer.toString())+" " +st3));
            msg.setStatus(EMMessage.Status.SUCCESS);
            // 保存邀请消息
            EMClient.getInstance().chatManager().saveMessage(msg);
            // 提醒新消息
            //getNotifier().viberateAndPlayTone(msg);
            getNotifier().onNewMsg(msg);
            EMLog.d(TAG, "onAutoAcceptInvitationFromGroup groupId:" + groupId);
            //发送local广播
            broadcastManager.sendBroadcast(new Intent(Constant.ACTION_GROUP_CHANAGED));
            appContext.sendBroadcast(new Intent("message_num"));
        }
    }
    /***
     * 好友变化listener
     * 
     */
    public class MyContactListener implements EMContactListener {

        @Override
        public void onContactAdded(String username) {
            /*// 保存增加的联系人
            Map<String, EaseUser> localUsers = getContactList();
            Map<String, EaseUser> toAddUsers = new HashMap<String, EaseUser>();
            EaseUser user = new EaseUser(username);
            // 添加好友时可能会回调added方法两次
            if (!localUsers.containsKey(username)) {
                userDao.saveContact(user);
            }
            toAddUsers.put(username, user);
            localUsers.putAll(toAddUsers);*/
            OkHttpUtils
                    .post()
                    .url(Interfaces.GET_FRIENDS)
                    .addParams("loginName", UserManager.getUsername(appContext))
                    .addParams("loginPwd",  UserManager.getPassword(appContext))
                    .build().execute(new StringCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {
                 e.printStackTrace();
                }

                @Override
                public void onResponse(String response, int id) {
                    try{ Logger.i("get_result",response);
                    String[] result = response.split("\\|");
                    if (result[0].equals("0")) {
                        ArrayList<BaseUser> list;
                        list = gson.fromJson(result[1], new TypeToken<ArrayList<BaseUser>>() {
                        }.getType());
                        List<EaseUser> mList = new ArrayList<EaseUser>();
                        for (BaseUser baseUser : list) {
                            EaseUser user = new EaseUser(baseUser.getPhoneNumber()+"-callba");
                            user.setAvatar(baseUser.getUrl_head());
                            user.setNick(baseUser.getNickname());
                            user.setSign(baseUser.getSign());
                            user.setRemark(baseUser.getRemark());
                            EaseCommonUtils.setUserInitialLetter(user);
                            mList.add(user);
                        }
                        updateContactList(mList);
                        broadcastManager.sendBroadcast(new Intent(Constant.ACTION_CONTACT_CHANAGED));

                    }
                    }catch(Exception e){
                        e.printStackTrace();
                       Toast.makeText(appContext,R.string.getserverdata_exception,Toast.LENGTH_SHORT).show();
                    }
                }
            });
           //发送好友变动广播

          /*  RequestParams params = new RequestParams();
            params.addBodyParameter("loginName", GlobalConfig.getInstance().getUsername());
            params.addBodyParameter("loginPwd", GlobalConfig.getInstance().getPassword());
            params.addBodyParameter("phoneNumber",username.substring(0,11));
            Logger.i("add_url",Interfaces.ADD_FRIEND+"?loginName="+GlobalConfig.getInstance().getUsername()+"&loginPwd="+GlobalConfig.getInstance().getPassword()+"&phoneNumber="+username.substring(0,11));
            httpUtils.send(HttpRequest.HttpMethod.POST, Interfaces.ADD_FRIEND, params, new RequestCallBack<String>(){
                @Override
                public void onFailure(HttpException error, String msg) {
                    Logger.i("add_fail",msg);
                }

                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    Logger.i("add_result",responseInfo.result);
                    asyncFetchContactsFromServer(null);
                }
            });*/

        }

        @Override
        public void onContactDeleted(String username) {
            // 被删除
            Map<String, EaseUser> localUsers = DemoHelper.getInstance().getContactList();
            localUsers.remove(username);
            userDao.deleteContact(username);
            inviteMessgeDao.deleteMessage(username);
            EMClient.getInstance().chatManager().deleteConversation(username,false);
            appContext.sendBroadcast(new Intent("message_num"));
            Intent intent=new Intent(Constant.ACTION_CONTACT_CHANAGED);
            intent.putExtra("username",username);
            //发送好友变动广播
            broadcastManager.sendBroadcast(intent);

        }

        @Override
        public void onContactInvited(String username, String reason) {
            // 接到邀请的消息，如果不处理(同意或拒绝)，掉线后，服务器会自动再发过来，所以客户端不需要重复提醒
            List<InviteMessage> msgs = inviteMessgeDao.getMessagesList();

            for (InviteMessage inviteMessage : msgs) {
                if (inviteMessage.getGroupId() == null && inviteMessage.getFrom().equals(username)) {
                    inviteMessgeDao.deleteMessage(username);
                }
            }
            // 自己封装的javabean
            InviteMessage msg = new InviteMessage();
            msg.setFrom(username);
            msg.setTime(System.currentTimeMillis());
            msg.setReason(reason);
            Log.d(TAG, username + "请求加你为好友,reason: " + reason);
            // 设置相应status
            msg.setStatus(InviteMesageStatus.BEINVITEED);
            notifyNewIviteMessage(msg);
            broadcastManager.sendBroadcast(new Intent(Constant.ACTION_CONTACT_CHANAGED));
        }

        @Override
        public void onContactAgreed(String username) {
            List<InviteMessage> msgs = inviteMessgeDao.getMessagesList();
            for (InviteMessage inviteMessage : msgs) {
                if (inviteMessage.getFrom().equals(username)) {
                    return;
                }
            }
            // 自己封装的javabean
            InviteMessage msg = new InviteMessage();
            msg.setFrom(username);
            msg.setTime(System.currentTimeMillis());
            Log.d(TAG, username + "同意了你的好友请求");
            msg.setStatus(InviteMesageStatus.BEAGREED);
            notifyNewIviteMessage(msg);
            broadcastManager.sendBroadcast(new Intent(Constant.ACTION_CONTACT_CHANAGED));

        }

        @Override
        public void onContactRefused(String username) {
            // 参考同意，被邀请实现此功能,demo未实现
            Log.d(username, username + "拒绝了你的好友请求");
        }
    }
    
    /**
     * 保存并提示消息的邀请消息
     * @param msg
     */
    private void notifyNewIviteMessage(InviteMessage msg){
        if(inviteMessgeDao == null){
            inviteMessgeDao = new InviteMessgeDao(appContext);
        }
        inviteMessgeDao.saveMessage(msg);
        //保存未读数，这里没有精确计算
        inviteMessgeDao.saveUnreadMessageCount(1);
        // 提示有新消息
        getNotifier().viberateAndPlayTone(null);
    }
    
    /**
     * 账号在别的设备登录
     */
    protected void onConnectionConflict(){
        Intent intent = new Intent(appContext, MainTabActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constant.ACCOUNT_CONFLICT, true);
        appContext.startActivity(intent);
    }
    
    /**
     * 账号被移除
     */
    protected void onCurrentAccountRemoved(){
        Intent intent = new Intent(appContext, MainTabActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constant.ACCOUNT_REMOVED, true);
        appContext.startActivity(intent);
    }

	private EaseUser getUserInfo(String username){
	    //获取user信息，demo是从内存的好友列表里获取，
        //实际开发中，可能还需要从服务器获取用户信息,
        //从服务器获取的数据，最好缓存起来，避免频繁的网络请求
        EaseUser user = null;
        if(username.equals(EMClient.getInstance().getCurrentUser()))
            return getUserProfileManager().getCurrentUserInfo();
        user = getContactList().get(username);
        //TODO 获取不在好友列表里的群成员具体信息，即陌生人信息，demo未实现
       /* if(user == null && getRobotList() != null){
            user = getRobotList().get(username);
        }*/
        return user;
	}

	 /**
     * 全局事件监听
     * 因为可能会有UI页面先处理到这个消息，所以一般如果UI页面已经处理，这里就不需要再次处理
     * activityList.size() <= 0 意味着所有页面都已经在后台运行，或者已经离开Activity Stack
     */
    protected void registerEventListener() {
    	messageListener = new EMMessageListener() {
            private BroadcastReceiver broadCastReceiver = null;
			
			@Override
			public void onMessageReceived(List<EMMessage> messages) {
			    for (EMMessage message : messages) {
			        EMLog.d(TAG, "onMessageReceived id : " + message.getMsgId());
                    Intent intent = new Intent("com.callba.chat");
                    intent.putExtra("message",message);
                    appContext.sendBroadcast(intent);
                    appContext.sendBroadcast(new Intent("message_num"));
			        //应用在后台，不需要刷新UI,通知栏提示新消息
			        if(!easeUI.hasForegroundActivies()){
			            getNotifier().onNewMsg(message);
			        }
			    }
			}
			
			@Override
			public void onCmdMessageReceived(List<EMMessage> messages) {
			    for (EMMessage message : messages) {
                    EMLog.d(TAG, "收到透传消息");
                    //获取消息body
                    EMCmdMessageBody cmdMsgBody = (EMCmdMessageBody) message.getBody();
                    final String action = cmdMsgBody.action();//获取自定义action

                    //获取扩展属性 此处省略
                    //message.getStringAttribute("");
                    EMLog.d(TAG, String.format("透传消息：action:%s,message:%s", action,message.toString()));
                    final String str = appContext.getString(R.string.receive_the_passthrough);

                    final String CMD_TOAST_BROADCAST = "hyphenate.demo.cmd.toast";
                    IntentFilter cmdFilter = new IntentFilter(CMD_TOAST_BROADCAST);

                    if(broadCastReceiver == null){
                        broadCastReceiver = new BroadcastReceiver(){

                            @Override
                            public void onReceive(Context context, Intent intent) {
                                // TODO Auto-generated method stub
                                Toast.makeText(appContext, intent.getStringExtra("cmd_value"), Toast.LENGTH_SHORT).show();
                            }
                        };

                        //注册广播接收者
                        appContext.registerReceiver(broadCastReceiver,cmdFilter);
                    }

                    Intent broadcastIntent = new Intent(CMD_TOAST_BROADCAST);
                    broadcastIntent.putExtra("cmd_value", str+action);
                    appContext.sendBroadcast(broadcastIntent, null);
                }
			}

			@Override
			public void onMessageReadAckReceived(List<EMMessage> messages) {
			}
			
			@Override
			public void onMessageDeliveryAckReceived(List<EMMessage> message) {
			}
			
			@Override
			public void onMessageChanged(EMMessage message, Object change) {
				
			}
		};
		
        EMClient.getInstance().chatManager().addMessageListener(messageListener);
    }

	/**
	 * 是否登录成功过
	 * 
	 * @return
	 */
	public boolean isLoggedIn() {
		return EMClient.getInstance().isLoggedInBefore();
	}

	/**
	 * 退出登录
	 * 
	 * @param unbindDeviceToken
	 *            是否解绑设备token(使用GCM才有)
	 * @param callback
	 *            callback
	 */
	public void logout(boolean unbindDeviceToken, final EMCallBack callback) {
		//endCall();
		Log.d(TAG, "logout: " + unbindDeviceToken);
		EMClient.getInstance().logout(unbindDeviceToken, new EMCallBack() {

			@Override
			public void onSuccess() {
				Log.d(TAG, "logout: onSuccess");
			    reset();
				if (callback != null) {
					callback.onSuccess();
				}

			}

			@Override
			public void onProgress(int progress, String status) {
				if (callback != null) {
					callback.onProgress(progress, status);
				}
			}

			@Override
			public void onError(int code, String error) {
				Log.d(TAG, "logout: onSuccess");
                reset();
				if (callback != null) {
					callback.onError(code, error);
				}
			}
		});
	}
	
	/**
	 * 获取消息通知类
	 * @return
	 */
	public EaseNotifier getNotifier(){
	    return easeUI.getNotifier();
	}
	
	public DemoModel getModel(){
        return (DemoModel) demoModel;
    }
	
	/**
	 * 设置好友user list到内存中
	 * 
	 * @param contactList
	 */
	public void setContactList(Map<String, EaseUser> aContactList) {
		if(aContactList == null){
		    if (contactList != null) {
		        contactList.clear();
		    }
			return;
		}
		
		contactList = aContactList;
	}
	
	/**
     * 保存单个user 
     */
    public void saveContact(EaseUser user){
    	contactList.put(user.getUsername(), user);
    	demoModel.saveContact(user);
    }
    
    /**
     * 获取好友list
     *
     * @return
     */
    public Map<String, EaseUser> getContactList() {
       /* if (isLoggedIn() && contactList == null) {
            contactList = demoModel.getContactList();
        }*/
        contactList = demoModel.getContactList();
        // return a empty non-null object to avoid app crash
       /* if(contactList == null){
        	return new Hashtable<String, EaseUser>();
        }*/
        
        return contactList;
    }
    
    /**
     * 设置当前用户的环信id
     * @param username
     */
    public void setCurrentUserName(String username){
    	this.username = username;
    	demoModel.setCurrentUserName(username);
    }
    
    /**
     * 获取当前用户的环信id
     */
    public String getCurrentUserName(){

    		username = demoModel.getCurrentUsernName();

    	return username;
    }

	public void setRobotList(Map<String, RobotUser> robotList) {
		this.robotList = robotList;
	}
	public Map<String, RobotUser> getRobotList() {
		if (isLoggedIn() && robotList == null) {
			robotList = demoModel.getRobotList();
		}
		return robotList;
	}

	 /**
     * update user list to cach And db
     *
     * @param contactList
     */
    public void updateContactList(List<EaseUser> contactInfoList) {
        if(contactList==null)
            contactList=getContactList();
         for (EaseUser u : contactInfoList) {
            contactList.put(u.getUsername(), u);
         }
         ArrayList<EaseUser> mList = new ArrayList<EaseUser>();
         mList.addAll(contactList.values());
         demoModel.saveContactList(mList);
    }

	public UserProfileManager getUserProfileManager() {
		if (userProManager == null) {
			userProManager = new UserProfileManager();
		}
		return userProManager;
	}

	/*void endCall() {
		try {
			EMClient.getInstance().callManager().endCall();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
	
	  public void addSyncGroupListener(DataSyncListener listener) {
	        if (listener == null) {
	            return;
	        }
	        if (!syncGroupsListeners.contains(listener)) {
	            syncGroupsListeners.add(listener);
	        }
	    }

	    public void removeSyncGroupListener(DataSyncListener listener) {
	        if (listener == null) {
	            return;
	        }
	        if (syncGroupsListeners.contains(listener)) {
	            syncGroupsListeners.remove(listener);
	        }
	    }

	    public void addSyncContactListener(DataSyncListener listener) {
	        if (listener == null) {
	            return;
	        }
	        if (!syncContactsListeners.contains(listener)) {
	            syncContactsListeners.add(listener);
	        }
	    }

	    public void removeSyncContactListener(DataSyncListener listener) {
	        if (listener == null) {
	            return;
	        }
	        if (syncContactsListeners.contains(listener)) {
	            syncContactsListeners.remove(listener);
	        }
	    }

	    public void addSyncBlackListListener(DataSyncListener listener) {
	        if (listener == null) {
	            return;
	        }
	        if (!syncBlackListListeners.contains(listener)) {
	            syncBlackListListeners.add(listener);
	        }
	    }

	    public void removeSyncBlackListListener(DataSyncListener listener) {
	        if (listener == null) {
	            return;
	        }
	        if (syncBlackListListeners.contains(listener)) {
	            syncBlackListListeners.remove(listener);
	        }
	    }
	
	/**
    * 同步操作，从服务器获取群组列表
    * 该方法会记录更新状态，可以通过isSyncingGroupsFromServer获取是否正在更新
    * 和isGroupsSyncedWithServer获取是否更新已经完成
    * @throws EaseMobException
    */
   public synchronized void asyncFetchGroupsFromServer(final EMCallBack callback){
       if(isSyncingGroupsWithServer){
           return;
       }
       
       isSyncingGroupsWithServer = true;
       
       new Thread(){
           @Override
           public void run(){
               try {
                   EMClient.getInstance().groupManager().getJoinedGroupsFromServer();
                   
                   // in case that logout already before server returns, we should return immediately
                   if(!isLoggedIn()){
                       isGroupsSyncedWithServer = false;
                       isSyncingGroupsWithServer = false;
                       //通知listener同步群组完毕
                       noitifyGroupSyncListeners(false);
                       return;
                   }
                   
                   demoModel.setGroupsSynced(true);
                   
                   isGroupsSyncedWithServer = true;
                   isSyncingGroupsWithServer = false;
                   
                   //通知listener同步群组完毕
                   noitifyGroupSyncListeners(true);
                   if(isContactsSyncedWithServer()){
                       notifyForRecevingEvents();
                   }
                   if(callback != null){
                       callback.onSuccess();
                   }
               } catch (HyphenateException e) {
                   demoModel.setGroupsSynced(false);
                   isGroupsSyncedWithServer = false;
                   isSyncingGroupsWithServer = false;
                   noitifyGroupSyncListeners(false);
                   if(callback != null){
                       callback.onError(e.getErrorCode(), e.toString());
                   }
               }
           
           }
       }.start();
   }

   public void noitifyGroupSyncListeners(boolean success){
       for (DataSyncListener listener : syncGroupsListeners) {
           listener.onSyncComplete(success);
       }
   }
   
   public void asyncFetchContactsFromServer(final EMValueCallBack<List<String>> callback){
       if(isSyncingContactsWithServer){
           return;
       }
       isSyncingContactsWithServer = true;
       
       new Thread(){
           @Override
           public void run(){
               List<String> usernames = null;
             /*  try {
                   usernames = EMClient.getInstance().contactManager().getAllContactsFromServer();
                   // in case that logout already before server returns, we should return immediately
                   if(!isLoggedIn()){
                       isContactsSyncedWithServer = false;
                       isSyncingContactsWithServer = false;
                       //通知listeners联系人同步完毕
                       notifyContactsSyncListener(false);
                       return;
                   }*/
//                   Map<String, EaseUser> userlist = new HashMap<String, EaseUser>();
               /*    for (String username : usernames) {
                       EaseUser user = new EaseUser(username);
                       EaseCommonUtils.setUserInitialLetter(user);
                       userlist.put(username, user);
                   }*/
                   // 存入内存
//                   getContactList().clear();
//                   getContactList().putAll(userlist);
//                    // 存入db
//                   UserDao dao = new UserDao(appContext);
//                   List<EaseUser> users = new ArrayList<EaseUser>(userlist.values());
//                   dao.saveContactList(users);

                   demoModel.setContactSynced(true);
                   EMLog.d(TAG, "set contact syn status to true");
                   
                   isContactsSyncedWithServer = true;
                   isSyncingContactsWithServer = false;
                   
                   //通知listeners联系人同步完毕
                   notifyContactsSyncListener(true);
                   if(isGroupsSyncedWithServer()){
                       notifyForRecevingEvents();
                   }
                   getUserProfileManager().asyncFetchContactInfosFromServer(usernames,new EMValueCallBack<List<EaseUser>>() {

                       @Override
                       public void onSuccess(List<EaseUser> uList) {
                           updateContactList(uList);
                           getUserProfileManager().notifyContactInfosSyncListener(true);
                       }

                       @Override
                       public void onError(int error, String errorMsg) {
                       }
                   });
                   if(callback != null){
                       callback.onSuccess(usernames);
                   }
            /*   } catch (HyphenateException e) {
                   demoModel.setContactSynced(false);
                   isContactsSyncedWithServer = false;
                   isSyncingContactsWithServer = false;
                   notifyContactsSyncListener(false);
                   e.printStackTrace();
                   if(callback != null){
                       callback.onError(e.getErrorCode(), e.toString());
                   }
               }*/
           }
       }.start();
   }

   public void notifyContactsSyncListener(boolean success){
       for (DataSyncListener listener : syncContactsListeners) {
           listener.onSyncComplete(success);
       }
   }
   
   public void asyncFetchBlackListFromServer(final EMValueCallBack<List<String>> callback){
       
       if(isSyncingBlackListWithServer){
           return;
       }
       
       isSyncingBlackListWithServer = true;
       
       new Thread(){
           @Override
           public void run(){
               try {
                   List<String> usernames = EMClient.getInstance().contactManager().getBlackListFromServer();
                   
                   // in case that logout already before server returns, we should return immediately
                   if(!isLoggedIn()){
                       isBlackListSyncedWithServer = false;
                       isSyncingBlackListWithServer = false;
                       notifyBlackListSyncListener(false);
                       return;
                   }
                   
                   demoModel.setBlacklistSynced(true);
                   
                   isBlackListSyncedWithServer = true;
                   isSyncingBlackListWithServer = false;
                   
                   notifyBlackListSyncListener(true);
                   if(callback != null){
                       callback.onSuccess(usernames);
                   }
               } catch (HyphenateException e) {
                   demoModel.setBlacklistSynced(false);
                   
                   isBlackListSyncedWithServer = false;
                   isSyncingBlackListWithServer = true;
                   e.printStackTrace();
                   
                   if(callback != null){
                       callback.onError(e.getErrorCode(), e.toString());
                   }
               }
               
           }
       }.start();
   }
	
	public void notifyBlackListSyncListener(boolean success){
        for (DataSyncListener listener : syncBlackListListeners) {
            listener.onSyncComplete(success);
        }
    }
    
    public boolean isSyncingGroupsWithServer() {
        return isSyncingGroupsWithServer;
    }

    public boolean isSyncingContactsWithServer() {
        return isSyncingContactsWithServer;
    }

    public boolean isSyncingBlackListWithServer() {
        return isSyncingBlackListWithServer;
    }
    
    public boolean isGroupsSyncedWithServer() {
        return isGroupsSyncedWithServer;
    }

    public boolean isContactsSyncedWithServer() {
        return isContactsSyncedWithServer;
    }

    public boolean isBlackListSyncedWithServer() {
        return isBlackListSyncedWithServer;
    }
	
	public synchronized void notifyForRecevingEvents(){
        if(alreadyNotified){
            return;
        }
        
        // 通知sdk，UI 已经初始化完毕，注册了相应的receiver和listener, 可以接受broadcast了
        alreadyNotified = true;
    }
    synchronized void reset(){
        isSyncingGroupsWithServer = false;
        isSyncingContactsWithServer = false;
        isSyncingBlackListWithServer = false;
        
        demoModel.setGroupsSynced(false);
        demoModel.setContactSynced(false);
        demoModel.setBlacklistSynced(false);
        
        isGroupsSyncedWithServer = false;
        isContactsSyncedWithServer = false;
        isBlackListSyncedWithServer = false;
        
        alreadyNotified = false;
        isGroupAndContactListenerRegisted = false;
        
        setContactList(null);
        setRobotList(null);
        getUserProfileManager().reset();
        DemoDBManager.getInstance().closeDB();
    }

    public void pushActivity(Activity activity) {
        easeUI.pushActivity(activity);
    }

    public void popActivity(Activity activity) {
        easeUI.popActivity(activity);
    }

}
