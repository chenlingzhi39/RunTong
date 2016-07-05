package com.callba.phone.util;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.callba.R;
import com.callba.phone.bean.EaseUser;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.controller.EaseUI;
import com.callba.phone.controller.EaseUI.EaseUserProfileProvider;

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
        if(username.equals(CalldaGlobalConfig.getInstance().getUsername()+"-callba")){
            if(!CalldaGlobalConfig.getInstance().getUserhead().equals(""))
                Glide.with(context).load(CalldaGlobalConfig.getInstance().getUserhead()).into(imageView);
            return;
        }
        if(user != null && user.getAvatar() != null){
            try {
                int avatarResId = Integer.parseInt(user.getAvatar());
                Glide.with(context).load(avatarResId).into(imageView);
            } catch (Exception e) {
                //正常的string路径
                Glide.with(context).load(user.getAvatar()).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.logo).into(imageView);
            }
        }else{
            Glide.with(context).load(R.drawable.logo).into(imageView);
        }

    }
    
    /**
     * 设置用户昵称
     */
    public static void setUserNick(String username, TextView textView){
        if(textView != null){
            if(username.equals(CalldaGlobalConfig.getInstance().getUsername()+"-callba")){
                textView.setText(CalldaGlobalConfig.getInstance().getNickname());
                return;
            }
        	EaseUser user = getUserInfo(username);
        	if(user != null && user.getNick() != null){
        		textView.setText(user.getNick());
        	}else{
        		textView.setText(username);
        	}
        }
    }
    
}
