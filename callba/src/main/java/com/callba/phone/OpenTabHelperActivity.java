package com.callba.phone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.callba.phone.ui.MainTabActivity;

/** 
 * 用于解决部分手机点击通知栏时无法打开MainTab页面的问题
 * @author  zhw
 * @version V1.0  
 * @createtime：2014年6月16日 下午6:33:06 
 */
public class OpenTabHelperActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startActivity(new Intent(this, MainTabActivity.class));
		
		finish();
	}
}