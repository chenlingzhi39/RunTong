package com.callba.phone.manager;

import android.content.Context;
import android.widget.Toast;

import com.callba.R;
import com.callba.phone.DemoHelper.*;
import com.callba.phone.bean.BaseUser;
import com.callba.phone.bean.EaseUser;
import com.callba.phone.cfg.Constant;
import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.manager.UserManager;
import com.callba.phone.util.EaseCommonUtils;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.util.PreferenceManager;
import com.callba.phone.util.SPUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;


import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

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
    private Gson gson;

    public UserProfileManager() {
    }

    public synchronized boolean init(Context context) {
        if (sdkInited) {
            return true;
        }
        syncContactInfosListeners = new ArrayList<DataSyncListener>();
        sdkInited = true;
        gson = new Gson();
        appContext = context;
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
     /*   RequestParams params = new RequestParams();
        params.addBodyParameter("loginName", GlobalConfig.getInstance().getUsername());
        params.addBodyParameter("loginPwd", GlobalConfig.getInstance().getPassword());
        Logger.i("get_friends",Interfaces.GET_FRIENDS+"?loginName="+ GlobalConfig.getInstance().getUsername()+"&loginPwd="+GlobalConfig.getInstance().getPassword());
        httpUtils.send(HttpRequest.HttpMethod.POST, Interfaces.GET_FRIENDS, params, new RequestCallBack<String>() {
            @Override
            public void onFailure(HttpException error, String msg) {
                error.printStackTrace();
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {

            }
        });*/
        OkHttpUtils
                .post()
                .url(Interfaces.GET_FRIENDS)
                .addParams("loginName", (String) SPUtils.get(appContext, Constant.PACKAGE_NAME, Constant.LOGIN_USERNAME, ""))
                .addParams("loginPwd", (String) SPUtils.get(appContext, Constant.PACKAGE_NAME, Constant.LOGIN_ENCODED_PASSWORD, ""))
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(String response, int id) {
                try {
                    Logger.i("get_result", response);
                    String[] result = response.split("\\|");
                    if (result[0].equals("0")) {
                        ArrayList<BaseUser> list;
                        list = gson.fromJson(result[1], new TypeToken<ArrayList<BaseUser>>() {
                        }.getType());
                        List<EaseUser> mList = new ArrayList<>();
                        for (BaseUser baseUser : list) {
                            EaseUser user = new EaseUser(baseUser.getPhoneNumber() + "-callba");
                            user.setAvatar(baseUser.getUrl_head());
                            user.setNick(baseUser.getNickname());
                            user.setSign(baseUser.getSign());
                            user.setRemark(baseUser.getRemark());
                            EaseCommonUtils.setUserInitialLetter(user);
                            mList.add(user);
                        }
                        callback.onSuccess(mList);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //Toast.makeText(appContext, appContext.getString(R.string.getserverdata_exception), Toast.LENGTH_SHORT).show();
                }
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


    public void setCurrentUserNick(String nickname) {
        getCurrentUserInfo().setNick(nickname);
        PreferenceManager.getInstance().setCurrentUserNick(nickname);
    }

    public void setCurrentUserAvatar(String avatar) {
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
