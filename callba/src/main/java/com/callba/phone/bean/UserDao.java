package com.callba.phone.bean;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.callba.R;
import com.callba.phone.logic.login.UserLoginErrorMsg;
import com.callba.phone.util.Logger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.callba.phone.util.DesUtil;
import com.callba.phone.util.Interfaces;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by PC-20160514 on 2016/5/19.
 */
public class UserDao {
    private Context context;
    private Handler handler;
    private Gson gson = new Gson();
    private ProgressDialog pd;
    private HttpUtils httpUtils;
    private Message message;
    private String[] result;
    private PostListener postListener;
    private UploadListener uploadListener;

    public interface PostListener {
        void start();

        void success(String msg);

        void failure(String msg);
    }

    public interface UploadListener {
        void start();

        void success(String msg);

        void failure(String msg);

        void loading(long total, long current, boolean isUploading);
    }

    public UserDao() {
        httpUtils = new HttpUtils(6 * 1000);
        httpUtils.configRequestRetryCount(3);
    }

    public UserDao(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
        httpUtils = new HttpUtils(6 * 1000);
        httpUtils.configRequestRetryCount(3);
    }

    public UserDao(Context context, PostListener postListener) {
        this.context = context;
        this.postListener = postListener;
        httpUtils = new HttpUtils(6 * 1000);
        httpUtils.configRequestRetryCount(3);
        httpUtils.configSoTimeout(30000);
    }

    public void getMessage(int code) {
        message = handler.obtainMessage();
        message.what = code;
    }

    public void getRegisterKey(final String phoneNumber) {
        final RequestParams params = new RequestParams();
        Log.i("phoneNumber", phoneNumber);
        params.addBodyParameter("phoneNumber", phoneNumber);
        params.addBodyParameter("softType", "android");
        httpUtils.send(HttpRequest.HttpMethod.POST, Interfaces.Send_SMS, params, new RequestCallBack<String>() {
            @Override
            public void onStart() {
                Log.i("url", Interfaces.Send_SMS);
                getMessage(Interfaces.GET_KEY_START);
                handler.sendMessage(message);
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                try {
                    Log.i("send_success", responseInfo.result);
                    result = responseInfo.result.split("\\|");
                    if (result[0].equals("0")) {
                        getMessage(Interfaces.GET_KEY_SUCCESS);
                        message.obj = result[1];
                        String phoneNumber2;
                        try {
                            phoneNumber2 = DesUtil.encrypt(phoneNumber, result[1]);
                            Log.i("phoneNumber", phoneNumber2);
                            getCode(phoneNumber2, result[2]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        getMessage(Interfaces.GET_KEY_FAILURE);
                        message.obj = result[1];
                    }
                    handler.sendMessage(message);
                } catch (Exception e) {
                    getMessage(Interfaces.GET_KEY_FAILURE);
                    message.obj = context.getString(R.string.network_error);
                    handler.sendMessage(message);
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                getMessage(Interfaces.GET_KEY_FAILURE);
                message.obj = context.getString(R.string.network_error);
                handler.sendMessage(message);
            }
        });
    }

    public void getFindKey(final String phoneNumber) {
        final RequestParams params = new RequestParams();
        params.addBodyParameter("phoneNumber", phoneNumber);
        params.addBodyParameter("softType", "android");
        httpUtils.send(HttpRequest.HttpMethod.POST, Interfaces.Send_SMS, params, new RequestCallBack<String>() {
            @Override
            public void onStart() {
                Log.i("url", Interfaces.Send_SMS);
                getMessage(Interfaces.GET_KEY_START);
                handler.sendMessage(message);
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i("send_success", responseInfo.result);
                try {
                    result = responseInfo.result.split("\\|");
                    if (result[0].equals("0")) {
                        getMessage(Interfaces.GET_KEY_SUCCESS);
                        String phoneNumber2;
                        try {
                            phoneNumber2 = DesUtil.encrypt(phoneNumber, result[1]);
                            Log.i("phoneNumber", phoneNumber2);
                            getPassword(phoneNumber2, result[2]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        getMessage(Interfaces.GET_KEY_FAILURE);
                        message.obj = result[1];
                    }
                    handler.sendMessage(message);
                } catch (Exception e) {
                    getMessage(Interfaces.GET_KEY_FAILURE);
                    message.obj = context.getString(R.string.network_error);
                    handler.sendMessage(message);
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                getMessage(Interfaces.GET_KEY_FAILURE);
                message.obj = context.getString(R.string.network_error);
                handler.sendMessage(message);
            }
        });
    }

    public void getPassword(String phoneNumber, String sign) {
        final RequestParams params = new RequestParams();
        params.addBodyParameter("phoneNumber", phoneNumber);
        params.addBodyParameter("sign", sign);
        params.addBodyParameter("softType", "android");
        httpUtils.send(HttpRequest.HttpMethod.POST, Interfaces.Retrieve_Pass, params, new RequestCallBack<String>() {
            @Override
            public void onStart() {
                Log.i("url", Interfaces.Retrieve_Pass);
                getMessage(Interfaces.GET_CODE_START);
                handler.sendMessage(message);
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i("get_success", responseInfo.result);
                try {
                    result = responseInfo.result.split("\\|");
                    if (result[0].equals("0"))
                        getMessage(Interfaces.GET_CODE_SUCCESS);
                    else getMessage(Interfaces.GET_CODE_FAILURE);
                    message.obj = result[1];
                    handler.sendMessage(message);
                } catch (Exception e) {
                    getMessage(Interfaces.GET_CODE_FAILURE);
                    message.obj = context.getString(R.string.network_error);
                    ;
                    handler.sendMessage(message);
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                getMessage(Interfaces.GET_CODE_FAILURE);
                message.obj = context.getString(R.string.network_error);
                handler.sendMessage(message);
            }
        });
    }

    public void getCode(String phoneNumber, String sign) {
        final RequestParams params = new RequestParams();
        params.addBodyParameter("phoneNumber", phoneNumber);
        params.addBodyParameter("sign", sign);
        params.addBodyParameter("softType", "android");
        httpUtils.send(HttpRequest.HttpMethod.POST, Interfaces.Verification_Code, params, new RequestCallBack<String>() {
            @Override
            public void onStart() {
                getMessage(Interfaces.GET_CODE_START);
                Log.i("url", Interfaces.Verification_Code);
                handler.sendMessage(message);
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i("get_success", responseInfo.result);
                try {
                    result = responseInfo.result.split("\\|");
                    if (result[0].equals("0"))
                        getMessage(Interfaces.GET_CODE_SUCCESS);
                    else getMessage(Interfaces.GET_CODE_FAILURE);
                    message.obj = result[1];
                    handler.sendMessage(message);
                } catch (Exception e) {
                    getMessage(Interfaces.GET_CODE_FAILURE);
                    message.obj = context.getString(R.string.network_error);
                    handler.sendMessage(message);
                }

            }

            @Override
            public void onFailure(HttpException error, String msg) {
                getMessage(Interfaces.GET_CODE_FAILURE);
                message.obj = context.getString(R.string.network_error);
                handler.sendMessage(message);
            }

            @Override
            public void onCancelled() {
                Log.i("get_code", "cancel");
            }
        });
    }
}