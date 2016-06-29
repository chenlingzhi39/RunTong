package com.callba.phone.activity.parse;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.callba.phone.Constant;
import com.callba.phone.DemoHelper;
import com.callba.phone.DemoHelper.*;
import com.callba.phone.bean.Advertisement;
import com.callba.phone.bean.BaseUser;
import com.callba.phone.bean.EaseUser;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.util.EaseCommonUtils;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.util.PreferenceManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;


import java.util.ArrayList;
import java.util.List;

public class UserProfileManager {

    /**
     * application context
     */
    protected Context appContext = null;

    /**
     * init flag: test if the sdk has been inited before, we don't need to init
     * again
     */
    private boolean sdkInited = false;

    /**
     * HuanXin sync contact nick and avatar listener
     */
    private List<DataSyncListener> syncContactInfosListeners;

    private boolean isSyncingContactInfosWithServer = false;

    private EaseUser currentUser;
    private HttpUtils httpUtils;
    private Gson gson;
    private LocalBroadcastManager broadcastManager;
    public UserProfileManager() {
    }

    public synchronized boolean init(Context context) {
        if (sdkInited) {
            return true;
        }
        syncContactInfosListeners = new ArrayList<DataSyncListener>();
        sdkInited = true;
        httpUtils = new HttpUtils(6 * 1000);
        httpUtils.configRequestRetryCount(3);
        gson=new Gson();
        broadcastManager = LocalBroadcastManager.getInstance(context);
        return true;
    }

    public void addSyncContactInfoListener(DataSyncListener listener) {
        if (listener == null) {
            return;
        }
        if (!syncContactInfosListeners.contains(listener)) {
            syncContactInfosListeners.add(listener);
        }
    }

    public void removeSyncContactInfoListener(DataSyncListener listener) {
        if (listener == null) {
            return;
        }
        if (syncContactInfosListeners.contains(listener)) {
            syncContactInfosListeners.remove(listener);
        }
    }

    public void asyncFetchContactInfosFromServer(List<String> usernames, final EMValueCallBack<List<EaseUser>> callback) {
       /* if (isSyncingContactInfosWithServer) {
            return;
        }*/
        RequestParams params = new RequestParams();
        params.addBodyParameter("loginName", CalldaGlobalConfig.getInstance().getUsername());
        params.addBodyParameter("loginPwd", CalldaGlobalConfig.getInstance().getPassword());
        Logger.i("get_friends",Interfaces.GET_FRIENDS+"?loginName="+ CalldaGlobalConfig.getInstance().getUsername()+"&loginPwd="+CalldaGlobalConfig.getInstance().getPassword());
        httpUtils.send(HttpRequest.HttpMethod.POST, Interfaces.GET_FRIENDS, params, new RequestCallBack<String>() {
            @Override
            public void onFailure(HttpException error, String msg) {

            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Logger.i("get_result",responseInfo.result);
                String[] result = responseInfo.result.split("\\|");
                if (result[0].equals("0")) {
                    ArrayList<BaseUser> list;
                    list = gson.fromJson(result[1], new TypeToken<List<BaseUser>>() {
                    }.getType());
                    List<EaseUser> mList = new ArrayList<EaseUser>();
                    for (BaseUser baseUser : list) {
                        EaseUser user = new EaseUser(baseUser.getPhoneNumber()+"-callba");
                        user.setAvatar(baseUser.getUrl_head());
                        user.setNick(baseUser.getNickname());
                        user.setSign(baseUser.getSign());
                        EaseCommonUtils.setUserInitialLetter(user);
                        mList.add(user);
                    }
                    callback.onSuccess(mList);
                }
                broadcastManager.sendBroadcast(new Intent(Constant.ACTION_CONTACT_CHANAGED));
            }
        });
        isSyncingContactInfosWithServer = true;


    }

    public void notifyContactInfosSyncListener(boolean success) {
        for (DataSyncListener listener : syncContactInfosListeners) {
            listener.onSyncComplete(success);
        }
    }

    public boolean isSyncingContactInfoWithServer() {
        return isSyncingContactInfosWithServer;
    }

    public synchronized void reset() {
        isSyncingContactInfosWithServer = false;
        currentUser = null;
        PreferenceManager.getInstance().removeCurrentUserInfo();
    }

    public synchronized EaseUser getCurrentUserInfo() {
        if (currentUser == null) {
            String username = EMClient.getInstance().getCurrentUser();
            currentUser = new EaseUser(username);
            String nick = getCurrentUserNick();
            currentUser.setNick((nick != null) ? nick : username);
            currentUser.setAvatar(getCurrentUserAvatar());
        }
        return currentUser;
    }


    private void setCurrentUserNick(String nickname) {
        getCurrentUserInfo().setNick(nickname);
        PreferenceManager.getInstance().setCurrentUserNick(nickname);
    }

    private void setCurrentUserAvatar(String avatar) {
        getCurrentUserInfo().setAvatar(avatar);
        PreferenceManager.getInstance().setCurrentUserAvatar(avatar);
    }

    private String getCurrentUserNick() {
        return PreferenceManager.getInstance().getCurrentUserNick();
    }

    private String getCurrentUserAvatar() {
        return PreferenceManager.getInstance().getCurrentUserAvatar();
    }

}