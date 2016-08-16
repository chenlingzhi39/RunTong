package com.callba.phone.manager;

import android.content.Context;

import com.callba.phone.bean.Contact;
import com.callba.phone.cfg.Constant;
import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.util.SPUtils;

/**
 * Created by PC-20160514 on 2016/7/20.
 */
public class UserManager {
    public static String getToken(Context context){
        return (String)SPUtils.get(context,Constant.PACKAGE_NAME,Constant.LOGIN_TOKEN,"");
    }
    public static String getSecretKey(Context context){
        return (String)SPUtils.get(context,Constant.PACKAGE_NAME,Constant.SECRET_KEY,"");
    }
    public static String getUsername(Context context) {
       return (String) SPUtils.get(context, Constant.PACKAGE_NAME, Constant.LOGIN_USERNAME, "");
       // return GlobalConfig.getInstance().getUsername();
    }

    public static String getPassword(Context context) {
        return (String) SPUtils.get(context, Constant.PACKAGE_NAME, Constant.LOGIN_ENCODED_PASSWORD, "");
       // return  GlobalConfig.getInstance().getPassword();
    }
    public static String getOriginalPassword(Context context){
        return (String)SPUtils.get(context,Constant.PACKAGE_NAME,Constant.LOGIN_PASSWORD,"");
    }
    public static String getUserAvatar(Context context){
        return (String)SPUtils.get(context,Constant.PACKAGE_NAME,Constant.USER_AVATAR,"");
    }
    public static String getNickname(Context context){
        return (String)SPUtils.get(context,Constant.PACKAGE_NAME,Constant.NICKNAME,"");
    }
    public static String getSignature(Context context){
        return (String)SPUtils.get(context,Constant.PACKAGE_NAME,Constant.SIGNATURE,"");
    }
    public static String getLatitude(Context context){
        return (String) SPUtils.get(context,Constant.PACKAGE_NAME,Constant.LATITUDE,"0");
    }
    public static String getLongitude(Context context){
        return (String) SPUtils.get(context,Constant.PACKAGE_NAME,Constant.LONGITUDE,"0");
    }
    public static String getCommission(Context context){
        return (String) SPUtils.get(context,Constant.PACKAGE_NAME,Constant.COMMISSION,"0");
    }
    public static String getAddress(Context context){
        return (String)SPUtils.get(context,Constant.PACKAGE_NAME,Constant.ADDRESS,"");
    }
    public static int getGold(Context context){
        return (int)SPUtils.get(context,Constant.PACKAGE_NAME,Constant.GOLD,0);
    }
    public static String getBalnace(Context context){
        return (String)SPUtils.get(context,Constant.PACKAGE_NAME,Constant.BALANCE,"");
    }
    public static String getCreateTime(Context context){
        return (String)SPUtils.get(context,Constant.PACKAGE_NAME,Constant.CREATE_TIME,"");
    }
    public static int getProportion(Context context){
        return (int)SPUtils.get(context,Constant.PACKAGE_NAME,Constant.PROPORTION,0);
    }
    public static void putLoginToken(Context context,String loginToken){
        SPUtils.put(context,Constant.PACKAGE_NAME,Constant.LOGIN_TOKEN,loginToken);
    }
    public  static void putSecretKey(Context context,String secretkey){
        SPUtils.put(context,Constant.PACKAGE_NAME,Constant.SECRET_KEY,secretkey);
    }
    public static void putUsername(Context context,String username){
        SPUtils.put(context,Constant.PACKAGE_NAME,Constant.LOGIN_USERNAME,username);
    }
    public static void putPassword(Context context,String password){
        SPUtils.put(context,Constant.PACKAGE_NAME,Constant.LOGIN_ENCODED_PASSWORD,password);
    }
    public static void putOriginalPassword(Context context,String password){
        SPUtils.put(context,Constant.PACKAGE_NAME,Constant.LOGIN_PASSWORD,password);
    }
    public static void putUserAvatar(Context context,String avatar){
        SPUtils.put(context,Constant.PACKAGE_NAME,Constant.USER_AVATAR,avatar);
    }
    public static void putNickname(Context context,String nickname){
        SPUtils.put(context,Constant.PACKAGE_NAME,Constant.NICKNAME,nickname);
    }
    public static void putSignature(Context context,String signature){
        SPUtils.put(context,Constant.PACKAGE_NAME,Constant.SIGNATURE,signature);
    }
    public static void putLatitude(Context context,String latitude){
        SPUtils.put(context,Constant.PACKAGE_NAME,Constant.LATITUDE,latitude);
    }
    public static void putLongitude(Context context,String longitude){
        SPUtils.put(context,Constant.PACKAGE_NAME,Constant.LONGITUDE,longitude);
    }
    public static void putCommission(Context context,String commission){
        SPUtils.put(context,Constant.PACKAGE_NAME,Constant.COMMISSION,commission);
    }
    public static void putAddress(Context context,String address){
        SPUtils.put(context,Constant.PACKAGE_NAME,Constant.ADDRESS,address);
    }
    public static void putGold(Context context,int gold){
        SPUtils.put(context,Constant.PACKAGE_NAME,Constant.GOLD,gold);
    }
    public static void putBalance(Context context,String balance){
        SPUtils.put(context,Constant.PACKAGE_NAME,Constant.BALANCE,balance);
    }
    public static void putCreateTime(Context context,String createTime){
        SPUtils.put(context,Constant.PACKAGE_NAME,Constant.CREATE_TIME,createTime);
    }
    public static void putProportion(Context context,int proportion){
        SPUtils.put(context,Constant.PACKAGE_NAME,Constant.PROPORTION,proportion);
    }
}
