package com.callba.phone.manager;

import android.content.Context;

import com.callba.phone.cfg.Constant;
import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.util.SPUtils;

/**
 * Created by PC-20160514 on 2016/7/20.
 */
public class UserManager {
    public static String getUsername(Context context) {
       return (String) SPUtils.get(context, Constant.PACKAGE_NAME, Constant.LOGIN_USERNAME, "");
       // return GlobalConfig.getInstance().getUsername();
    }

    public static String getPassword(Context context) {
        return (String) SPUtils.get(context, Constant.PACKAGE_NAME, Constant.LOGIN_ENCODED_PASSWORD, "");
       // return  GlobalConfig.getInstance().getPassword();
    }
    public static String getUserAvatar(Context context){
        return (String)SPUtils.get(context,Constant.PACKAGE_NAME,Constant.USER_AVATAR,"");
    }
    public static Double getLatitude(Context context){
        return (Double)SPUtils.get(context,Constant.PACKAGE_NAME,Constant.LATITUDE,0);
    }
    public static Double getLongitude(Context context){
        return (Double)SPUtils.get(context,Constant.PACKAGE_NAME,Constant.LONGITUDE,0);
    }
    public static String getCommission(Context context){
        return (String)SPUtils.get(context,Constant.PACKAGE_NAME,Constant.COMMISSION,"");
    }
    public static String getAddress(Context context){
        return (String)SPUtils.get(context,Constant.PACKAGE_NAME,Constant.ADDRESS,"");
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
        SPUtils.put(context,Constant.PACKAGE_NAME,Constant.USER_AVATAR,"");
    }
    public static void putLatitude(Context context,Double latitude){
        SPUtils.put(context,Constant.PACKAGE_NAME,Constant.LATITUDE,latitude);
    }
    public static void putLongitude(Context context,Double longitude){
        SPUtils.put(context,Constant.PACKAGE_NAME,Constant.LONGITUDE,longitude);
    }
    public static void putCommission(Context context,String commission){
        SPUtils.put(context,Constant.PACKAGE_NAME,Constant.COMMISSION,commission);
    }
    public static void putAddress(Context context,String address){
        SPUtils.put(context,Constant.PACKAGE_NAME,Constant.ADDRESS,address);
    }

}
