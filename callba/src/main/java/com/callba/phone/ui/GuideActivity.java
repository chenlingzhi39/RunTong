package com.callba.phone.ui;

import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.callba.R;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.cfg.Constant;
import com.callba.phone.util.SharedPreferenceUtil;
@ActivityFragmentInject(
		contentViewId = R.layout.guide
)
public class GuideActivity extends BaseActivity implements OnClickListener {
	private Button bn_manual, bn_auto, bn_login;
	
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferenceUtil mPreferenceUtil = SharedPreferenceUtil.getInstance(this);
		mPreferenceUtil.putBoolean(Constant.IS_FROMGUIDE, true, true);
		
		bn_manual = (Button) this.findViewById(R.id.bn_guide_manual);
		bn_login = (Button) this.findViewById(R.id.bn_guide_login);
		
		bn_manual.setOnClickListener(this);
		bn_login.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
	/*	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			Window window = getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
			);
			window.getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_FULLSCREEN
					| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}*/
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bn_guide_manual:
			gotoActivity(RegisterActivity.class);
			break;

		case R.id.bn_guide_login:
			gotoActivity(LoginActivity.class);
			break;

		default:
			break;
		}
	}
	/**
	 * Activity 跳转
	 * @param clazz
	 */
	private void gotoActivity(Class<?> clazz) {
		Intent intent = new Intent(this, clazz);
		startActivity(intent);
	}



//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if(keyCode == KeyEvent.KEYCODE_BACK){
//			
//			return true;
//		}
//		return true;
//	}
}
