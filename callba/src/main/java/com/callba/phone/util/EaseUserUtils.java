package com.callba.phone.util;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.callba.R;
import com.callba.phone.MyApplication;
import com.callba.phone.bean.EaseUser;
import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.controller.EaseUI;
import com.callba.phone.controller.EaseUI.EaseUserProfileProvider;
import com.callba.phone.manager.UserManager;

public class EaseUserUtils {
    
    static EaseUserProfileProvider userProvider;
    
    static {
        userProvider = EaseUI.getInstance().getUserProfileProvider();
    }
    
    /**
     * 根据username获取相应user
     * @param username
     * @return
     */
    public static EaseUser getUserInfo(String username){
        if(userProvider != null)
            return userProvider.getUser(username);
        
        return null;
    }
    
    /**
     * 设置用户头像
     * @param username
     */
    public static void setUserAvatar(Context context, String username, ImageView imageView){
    	EaseUser user = getUserInfo(username);

        if(username.equals(UserManager.getUsername(context)+"-callba")){
            if(!UserManager.getUserAvatar(context).equals(""))
                Glide.with(context).load(UserManager.getUserAvatar(context)).into(imageView);
            return;
        }
        if(user!=null){
            if(!TextUtils.isEmpty(user.getAvatar())){
                Glide.with(context).load(user.getAvatar()).into(imageView);
            } else Glide.with(context).load(R.drawable.ease_default_avatar).into(imageView);
        }else {
            if(username.equals("admin")){
                Glide.with(context).load(R.drawable.system_message).into(imageView);
            }
            else Glide.with(context).load(R.drawable.ease_default_avatar).into(imageView);}

       /* if(user != null && user.getAvatar() != null){
            try {
                int avatarResId = Integer.parseInt(user.getAvatar());
                Glide.with(context).load(avatarResId).into(imageView);
            } catch (Exception e) {
                //正常的string路径
                Glide.with(context).load(user.getAvatar()).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.logo).into(imageView);
            }
        }else{
            Glide.with(context).load(R.drawable.logo).into(imageView);
        }*/

    }
    
    /**
     * 设置用户昵称
     */
    public static void setUserNick(String username, TextView textView){
        if(textView != null){
            if(username.equals(UserManager.getUsername(MyApplication.getInstance())+"-callba")){
                textView.setText(UserManager.getNickname(MyApplication.getInstance()));
                return;
            }
        	EaseUser user = getUserInfo(username);
        	if(user != null &&!TextUtils.isEmpty(user.getNick())){
        		textView.setText(user.getNick());

        	}else{
                if(username.length()>=11)
                {StringBuffer buffer = new StringBuffer(username.substring(0,11));
                textView.setText(buffer.toString());
                }else textView.setText(username);
               // buffer.replace(3,8,"****");

        	}
            if(user != null &&!TextUtils.isEmpty(user.getRemark()))
                    textView.setText(user.getRemark());
            if(username.equals("admin")){
                textView.setText("系统消息");
                textView.setTextColor(Color.parseColor("#dd191d"));
            }else textView.setTextColor(Color.parseColor("#000000"));
        }
    }
    public static String getUserNick(String username){
            String name=username;
            if(username.equals(UserManager.getUsername(MyApplication.getInstance())+"-callba")){
                name=UserManager.getNickname(MyApplication.getInstance());

            }
            EaseUser user = getUserInfo(username);
            if(user != null &&!TextUtils.isEmpty(user.getNick())){
                name=user.getNick();

            }else{
                if(username.length()>=11)
                {StringBuffer buffer = new StringBuffer(username.substring(0,11));
                    name=buffer.toString();
                }else  name=username;
                // buffer.replace(3,8,"****");

            }
            if(user != null &&!TextUtils.isEmpty(user.getRemark()))
                name=user.getRemark();
        return name;
    }
}
