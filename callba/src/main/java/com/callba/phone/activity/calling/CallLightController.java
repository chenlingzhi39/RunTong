package com.callba.phone.activity.calling;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.callba.phone.util.Logger;

/** 
 * 通话界面屏幕灯光控制器
 * @Author  zhw
 * @Version V1.0  
 * @Createtime：2014年5月16日 下午1:51:07 
 */
public class CallLightController implements SensorEventListener {
	private static final String TAG = CallLightController.class.getCanonicalName();
	
	private Context mContext;
	
	private SensorManager mSensorManager;
	
	private float currSensorValue = -100;
	
	public CallLightController(Context context) {
		this.mContext = context;
		
		mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
	}
	
	/**
	 * 注册控制器
	 */
	public void registerLightController() {
		Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
		Logger.d(TAG, "register Proximity Sensor.");
	}
	
	/**
	 * 释放控制器
	 */
	public void releaseLightController() {
		mSensorManager.unregisterListener(this);
		Logger.d(TAG, "unregister Proximity Sensor.");
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float[] proximity = event.values;
		
		if(currSensorValue == proximity[0]) {
			return;
		}
		currSensorValue = proximity[0];
		
		if(event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
			if(proximity[0] == 0.0) {
				//靠近
				setProximitySensorNearby((Activity)mContext, true);
			} else {
				//远离
				setProximitySensorNearby((Activity)mContext, false);
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
	
	private void setProximitySensorNearby(Activity activity, boolean isNearby) {
		final Window window = activity.getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		View view = ((ViewGroup) window.getDecorView().findViewById(android.R.id.content)).getChildAt(0);
		if (isNearby) {
            params.screenBrightness = 0.1f;
            view.setVisibility(View.INVISIBLE);
		} else  {
			params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            view.setVisibility(View.VISIBLE);
		}
        window.setAttributes(params);
	}
	
//	private boolean isProximitySensorNearby(final SensorEvent event) {
//		float threshold = 4.001f; // <= 4 cm is near
//
//		final float distanceInCm = event.values[0];
//		final float maxDistance = event.sensor.getMaximumRange();
//		
//		if (maxDistance <= threshold) {
//			// Case binary 0/1 and short sensors
//			threshold = maxDistance;
//		}
//
//		return distanceInCm < threshold;
//	}
}
 