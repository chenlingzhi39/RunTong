package com.callba.phone.bean;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

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

import java.util.ArrayList;
import java.util.List;

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
   public  interface PostListener{
        void start();
        void success(String msg);
        void failure(String msg);
    }
    public UserDao(){
        httpUtils = new HttpUtils(6 * 1000);
        httpUtils.configRequestRetryCount(3);
    }
    public UserDao(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
        httpUtils = new HttpUtils(6 * 1000);
        httpUtils.configRequestRetryCount(3);
    }
    public UserDao(Context context,PostListener postListener){
        this.context=context;
        this.postListener=postListener;
        httpUtils = new HttpUtils(6 * 1000);
        httpUtils.configRequestRetryCount(3);
    }
    public void login(String loginSign, String loginType) {
        RequestParams params = new RequestParams();
        params.addBodyParameter("loginSign", loginSign);
        params.addBodyParameter("loginType", loginType);
        params.addBodyParameter("softType", "android");
        params.addBodyParameter("callType", "all");
        httpUtils.send(HttpRequest.HttpMethod.POST, Interfaces.Login, params, new RequestCallBack<String>() {
            @Override
            public void onStart() {
                Log.i("url", Interfaces.Login);
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i("login_success", responseInfo.result);
            }

            @Override
            public void onFailure(HttpException error, String msg) {

            }
        });

    }
     public void getMessage(int code){
         message = handler.obtainMessage();
         message.what =code;
     }
    public void getRegisterKey(final String phoneNumber) {
        RequestParams params = new RequestParams();
        params.addBodyParameter("phoneNumber", phoneNumber);
        params.addBodyParameter("softType", "android");
        httpUtils.send(HttpRequest.HttpMethod.POST, Interfaces.Send_SMS, params, new RequestCallBack<String>() {
            @Override
            public void onStart() {
                Log.i("url", Interfaces.Send_SMS);
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i("send_success", responseInfo.result);
                getMessage( Interfaces.GET_KEY_SUCCESS);
                result = responseInfo.result.split("\\|");
                Log.i("result",responseInfo.result);
                message.obj=result[1];
                Log.i("key", result[1]);
                handler.sendMessage(message);
                String phoneNumber2;
                try {
                    phoneNumber2 = DesUtil.encrypt(phoneNumber, result[1]);
                    Log.i("phoneNumber", phoneNumber2);
                    getCode(phoneNumber2, result[2]);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(HttpException error, String msg) {

            }
        });
    }
   public void getFindKey(final String phoneNumber){
       RequestParams params = new RequestParams();
       params.addBodyParameter("phoneNumber", phoneNumber);
       params.addBodyParameter("softType", "android");
       httpUtils.send(HttpRequest.HttpMethod.POST, Interfaces.Send_SMS, params, new RequestCallBack<String>() {
           @Override
           public void onStart() {
               Log.i("url", Interfaces.Send_SMS);
           }

           @Override
           public void onSuccess(ResponseInfo<String> responseInfo) {
               Log.i("send_success", responseInfo.result);
               result = responseInfo.result.split("\\|");
               String phoneNumber2;
               Log.i("key", result[1]);
               try {
                   phoneNumber2 = DesUtil.encrypt(phoneNumber, result[1]);
                   Log.i("phoneNumber", phoneNumber2);
                   getPassword(phoneNumber2, result[2]);
               } catch (Exception e) {
                   e.printStackTrace();
               }

           }

           @Override
           public void onFailure(HttpException error, String msg) {

           }
       });
   }
    public void getPassword(String phoneNumber, String sign){
        RequestParams params = new RequestParams();
        params.addBodyParameter("phoneNumber", phoneNumber);
        params.addBodyParameter("sign", sign);
        params.addBodyParameter("softType", "android");
        httpUtils.send(HttpRequest.HttpMethod.POST, Interfaces.Retrieve_Pass, params, new RequestCallBack<String>() {
            @Override
            public void onStart() {
                Log.i("url", Interfaces.Retrieve_Pass);
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i("get_success", responseInfo.result);

                result = responseInfo.result.split("\\|");
                if(result[0].equals("0"))
                getMessage(Interfaces.GET_CODE_SUCCESS);
                else getMessage(Interfaces.GET_CODE_FAILURE);
                message.obj = result[1];
                handler.sendMessage(message);

            }

            @Override
            public void onFailure(HttpException error, String msg) {
                getMessage(Interfaces.GET_CODE_FAILURE);
                message.obj=msg;
                handler.sendMessage(message);
            }
        });
    }
    public void getCode(String phoneNumber, String sign) {
        RequestParams params = new RequestParams();
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
                result = responseInfo.result.split("\\|");
                if(result[0].equals("0"))
                 getMessage(Interfaces.GET_CODE_SUCCESS);
                else getMessage(Interfaces.GET_CODE_FAILURE);
                message.obj = result[1];
                handler.sendMessage(message);

            }

            @Override
            public void onFailure(HttpException error, String msg) {
                getMessage(Interfaces.GET_CODE_FAILURE);
                message.obj =msg;
                handler.sendMessage(message);
            }

            @Override
            public void onCancelled() {
                Log.i("get_code","cancel");
            }
        });
    }

    public void recharge(String number,String card,String from){
        RequestParams params=new RequestParams();
        params.addBodyParameter("phoneNumber",number);
        params.addBodyParameter("cardNumber",card);
        params.addBodyParameter("fromtel",from);
        params.addBodyParameter("softType","android");
        httpUtils.send(HttpRequest.HttpMethod.POST, Interfaces.CALLDA_PAY,params, new RequestCallBack<String>() {
            @Override
            public void onStart() {
            postListener.start();
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i("recharge_success", responseInfo.result);
                result=responseInfo.result.split("\\|");
                if(result[0].equals("0"))
                    postListener.success(result[1]);
                else  postListener.failure(result[1]);
            }

            @Override
            public void onFailure(HttpException error, String msg) {
           postListener.failure(msg);
            }

        });
    }
    public void getNearBy(String loginName,String password,double latitude,double longitude,int radius){
        RequestParams params=new RequestParams();
        Log.i("password",password);
        params.addBodyParameter("loginName",loginName);
        params.addBodyParameter("loginPwd",password);
        params.addBodyParameter("latitude",latitude+"");
        params.addBodyParameter("longitude",longitude+"");
        params.addBodyParameter("radius",radius+"");
        httpUtils.send(HttpRequest.HttpMethod.POST, Interfaces.GET_NEARBY,params, new RequestCallBack<String>() {
            @Override
            public void onFailure(HttpException error, String msg) {
            postListener.failure(msg);
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                result=responseInfo.result.split("\\|");
                Log.i("getNearby_success",responseInfo.result);
                if(result[0].equals("0"))
                {
                Gson gson=new Gson();
                List<NearByUser> list=new ArrayList<>();
                    try {
                        list=gson.fromJson(result[1], new TypeToken<List<NearByUser>>(){}.getType());
                    }catch (Exception e){

                    }

                }else postListener.failure(result[1]);
            }

            @Override
            public void onStart() {
                postListener.start();
            }

        });
    }
public void saveLocation(String loginName,String password,double latitude,double longitude){
    RequestParams params=new RequestParams();
    params.addBodyParameter("loginName",loginName);
    params.addBodyParameter("loginPwd",password);
    params.addBodyParameter("latitude",latitude+"");
    params.addBodyParameter("longitude",longitude+"");
    httpUtils.send(HttpRequest.HttpMethod.POST, Interfaces.GET_NEARBY,params, new RequestCallBack<String>() {
        @Override
        public void onSuccess(ResponseInfo<String> responseInfo) {
            Log.i("save","success");
        }

        @Override
        public void onFailure(HttpException error, String msg) {
            Log.i("save","failure");
        }
    });
}
    public void getRechargeMeal(String loginName,String password){
        RequestParams params=new RequestParams();
        Log.i("password",password);
        params.addBodyParameter("loginName",loginName);
        params.addBodyParameter("loginPwd",password);
        params.addBodyParameter("softType","android");
        httpUtils.send(HttpRequest.HttpMethod.POST, Interfaces.GET_RECHARGE_MEAL,params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i("get_recharge_meal",responseInfo.result);
                result=responseInfo.result.split("\\|");
                postListener.success(result[1]);
            }

            @Override
            public void onFailure(HttpException error, String msg) {
              postListener.failure(msg);
            }
        });
    }
}