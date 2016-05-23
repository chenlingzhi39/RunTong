package com.callba.phone.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.callba.R;

/** 
 * 自定义Toast
 * @author  zhw
 * @version V1.0  
 * @createtime：2014年6月9日 下午2:36:55 
 */
public class CalldaToast {
	/**
	 * 显示自定义Toast(默认显示时长 LENGTH_SHORT)
	 * @author zhw
	 *
	 * @param context 上下文
	 * @param showMessage 显示的消息内容
	 */
	public void showToast(Context context, String showMessage) {
		showToast(context, showMessage, Toast.LENGTH_SHORT);
	}
	
	/**
	 * 显示自定义Toast(默认显示时长 LENGTH_SHORT)
	 * @author zhw
	 *
	 * @param context 上下文
	 * @param resId 显示消息的资源id
	 */
	public void showToast(Context context, int msgId) {
		showToast(context, context.getString(msgId));
	}
	
	/**
	 * 显示自定义Toast
	 * @author zhw
	 *
	 * @param context 上下文
	 * @param showMessage 显示的消息内容
	 * @param showTime 显示时长(Toast.LENGTH_SHORT Toast.LENGTH_LONG)
	 */
	public void showToast(Context context, String showMessage, int showTime) {
		View view = View.inflate(context, R.layout.callda_toast, null);
		TextView tvMessage = (TextView) view.findViewById(R.id.tv_toast_msg);
		
		tvMessage.setText(showMessage);
		
		Toast mToast = new Toast(context);
		mToast.setView(view);
		mToast.setGravity(Gravity.CENTER, 0, 0);
		mToast.setDuration(showTime);
		mToast.show();
	}
	
	/**
	 * 显示自定义Toast
	 * @author zhw
	 *
	 * @param context 上下文
	 * @param msgId 显示消息的资源id
	 * @param showTime 显示时长(Toast.LENGTH_SHORT Toast.LENGTH_LONG)
	 */
	public void showToast(Context context, int msgId, int showTime) {
		showToast(context, context.getString(msgId), showTime);
	}
	
	/**
	 * 显示自定义图标的Toast
	 * @author zhw
	 *
	 * @param context 上下文
	 * @param showMessage  显示消息的内容
	 * @param drawable 自定义的图标
	 * @param showTime 显示时长(Toast.LENGTH_SHORT Toast.LENGTH_LONG)
	 */
	public void showImageToast(Context context, String showMessage, Drawable drawable, int showTime) {
		View view = View.inflate(context, R.layout.callda_toast, null);
		TextView tvMessage = (TextView) view.findViewById(R.id.tv_toast_msg);
		ImageView ivIcon   = (ImageView) view.findViewById(R.id.iv_toast_img);
		
		tvMessage.setText(showMessage);
		ivIcon.setImageDrawable(drawable);
		
		Toast mToast = new Toast(context);
		mToast.setView(view);
		mToast.setGravity(Gravity.CENTER, 0, 0);
		mToast.setDuration(showTime);
		mToast.show();
	}
	
	/**
	 * 显示自定义图标的Toast
	 * @author zhw
	 *
	 * @param context	上下文
	 * @param msgId		资源内容id
	 * @param imageId	图标id
	 * @param showTime	显示时长(Toast.LENGTH_SHORT Toast.LENGTH_LONG)
	 */
	public void showImageToast(Context context, int msgId, int imageId, int showTime) {
		showImageToast(context, context.getString(msgId), context.getResources().getDrawable(imageId), showTime);
	}
	
	/**
	 * 显示自定义图标的Toast
	 * @author zhw
	 *
	 * @param context
	 * @param showMessage
	 * @param drawable
	 */
	public void showImageToast(Context context, String showMessage, Drawable drawable) {
		showImageToast(context, showMessage, drawable, Toast.LENGTH_SHORT);
	}
	
	/**
	 * 显示自定义图标的Toast
	 * @author zhw
	 *
	 * @param context	上下文
	 * @param msgId		资源内容id
	 * @param imageId	图标id
	 */
	public void showImageToast(Context context, int msgId, int imageId) {
		showImageToast(context, msgId, imageId, Toast.LENGTH_SHORT);
	}
}
 