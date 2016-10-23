package com.callba.phone.util;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;

import com.callba.phone.cfg.Constant;

public class KeyboardUtil {
	private static final int TONE_RELATIVE_VOLUME = 80;
	private static final int TONE_LENGTH_MS = 150;// 延迟时间

	private Object mToneGeneratorLock = new Object();// 监视器对象锁
	private ToneGenerator mDTMFPlayer;
	private String TAG = "KeyboardUtil";
	private boolean keyflag;

	public void startDTMF(int number, Context context) {
		keyflag= (boolean)SPUtils.get(context, Constant.PACKAGE_NAME,Constant.KeyboardSetting,true);
		Logger.i(TAG, keyflag+"");
		if (!keyflag) {
			return;
		}
		@SuppressWarnings("static-access")
		AudioManager mAudioManager = (AudioManager)context.getSystemService(context.AUDIO_SERVICE); 
		int statusFlag = (mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) ? 1: 0; 
		if(statusFlag == 1) 
		{ 
		return; 
		} 
		synchronized (mToneGeneratorLock) {
			if (mDTMFPlayer == null) {
				try {
					mDTMFPlayer = new ToneGenerator(AudioManager.STREAM_MUSIC,
							TONE_RELATIVE_VOLUME);
					
					((Activity) context)
							.setVolumeControlStream(AudioManager.STREAM_MUSIC);
				} catch (RuntimeException e) {
					Logger.w(TAG,
							"Exception caught while creating local tone generator: "
									+ e);
					mDTMFPlayer = null;
				}
			}
		}

		if (mDTMFPlayer != null) {
			synchronized (mDTMFPlayer) {
				switch (number) {
				case 0:
					mDTMFPlayer.startTone(ToneGenerator.TONE_DTMF_0,
							TONE_LENGTH_MS);
					break;
				case 1:
					mDTMFPlayer.startTone(ToneGenerator.TONE_DTMF_1,
							TONE_LENGTH_MS);
					break;
				case 2:
					mDTMFPlayer.startTone(ToneGenerator.TONE_DTMF_2,
							TONE_LENGTH_MS);
					break;
				case 3:
					mDTMFPlayer.startTone(ToneGenerator.TONE_DTMF_3,
							TONE_LENGTH_MS);
					break;
				case 4:
					mDTMFPlayer.startTone(ToneGenerator.TONE_DTMF_4,
							TONE_LENGTH_MS);
					break;
				case 5:
					mDTMFPlayer.startTone(ToneGenerator.TONE_DTMF_5,
							TONE_LENGTH_MS);
					break;
				case 6:
					mDTMFPlayer.startTone(ToneGenerator.TONE_DTMF_6,
							TONE_LENGTH_MS);
					break;
				case 7:
					mDTMFPlayer.startTone(ToneGenerator.TONE_DTMF_7,
							TONE_LENGTH_MS);
					break;
				case 8:
					mDTMFPlayer.startTone(ToneGenerator.TONE_DTMF_8,
							TONE_LENGTH_MS);
					break;
				case 9:
					mDTMFPlayer.startTone(ToneGenerator.TONE_DTMF_9,
							TONE_LENGTH_MS);
					break;
				case 10:
					mDTMFPlayer.startTone(ToneGenerator.TONE_DTMF_S,
							TONE_LENGTH_MS);
					break;
				case 11:
					mDTMFPlayer.startTone(ToneGenerator.TONE_DTMF_P,
							TONE_LENGTH_MS);
					break;
				case 12:
					mDTMFPlayer.startTone(ToneGenerator.TONE_DTMF_D,
							TONE_LENGTH_MS);
					break;
				}
			}
		}
	}
}
