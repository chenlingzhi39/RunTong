package com.callba.phone.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.callba.R;
import com.callba.phone.ui.CallbackDisplayActivity;
import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.cfg.Constant;
import com.callba.phone.logic.contact.ContactPersonEntity;


public class CallUtils {
	protected static final String TAG = CallUtils.class.getCanonicalName();
	private static final int DEFAULT_PHONE_LENGTH = 8;

	private String callSeting;
	
	private Dialog callModeChooserDialog;
	
	/**
	 * 判断拨打方式
	 * @param context
	 * @param callNum
	 * @param callname
	 */
	public void judgeCallMode(Context context, String callNum, String callname,String id) {
		callSeting = GlobalConfig.getInstance().getCallSetting();
		callNum = PhoneUtils.formatAvailPhoneNumber(callNum);
		
		callNum = addQuHao(context, callNum);
		Log.i("callSetting",callSeting);
		if (Constant.CALL_SETTING_HUI_BO.equals(callSeting)) {
			dialCallback(context, callNum,callname,id);
		} else if (Constant.CALL_SETTING_ZHI_BO.equals(callSeting)) {
//			directDial(context, callNum,callname);
		} else if (Constant.CALL_SETTING_SHOU_DONG.equals(callSeting)) {
			//showChooseDialog(context, callNum,callname);
		} else if (Constant.CALL_SETTING_ZHI_NENG.equals(callSeting)) {
			if (NetWorkUtil.getNetworkInfoLevel(context, 6, false)) {
//				directDial(context, callNum,callname);
			} else {
				dialCallback(context, callNum,callname,id);
			}
		}
	}
	
	/**
	 * 自动选择呼叫方式呼叫 
	 * @param context
	 * @param callNumber
	 */
	public void judgeCallMode(Context context, String callNumber) {
		judgeCallMode(context, callNumber, queryNameByPhoneNumber(callNumber),queryIdByPhoneNumber(callNumber));
	}
	public void judgeCallMode(Context context,String callNumber, String name) {
		judgeCallMode(context, callNumber, name,queryIdByPhoneNumber(callNumber));
	}
	/**
	 * 自动选择呼叫方式呼叫 
	 * @param context
	 * @param callNumber
	 * @param callModeDialogDismissListener	呼叫选择对话框消失监听器
	 */
	public void judgeCallMode(Context context, String callNumber, final onCallModeDialogDismissListener callModeDialogDismissListener) {
		String callUserName = queryNameByPhoneNumber(callNumber);
		
		callSeting = GlobalConfig.getInstance().getCallSetting();
		callNumber = PhoneUtils.formatAvailPhoneNumber(callNumber);
		
		callNumber = addQuHao(context, callNumber);
		if (Constant.CALL_SETTING_HUI_BO.equals(callSeting)) {
			//回拨
			//dialCallback(context, callNumber, callUserName);
			callModeDialogDismissListener.onDialogDismiss();
		} else if (Constant.CALL_SETTING_ZHI_BO.equals(callSeting)) {
			//直拨
//			directDial(context, callNumber, callUserName);
			callModeDialogDismissListener.onDialogDismiss();
		} else if (Constant.CALL_SETTING_SHOU_DONG.equals(callSeting)) {
			//手动选择拨打方式
			if(callModeChooserDialog == null) {
				callModeChooserDialog = new Dialog(context, R.style.MyDialog);
			}
			callModeChooserDialog.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					callModeDialogDismissListener.onDialogDismiss();
				}
			});
			//showChooseDialog(context, callNumber, callUserName);
		} else if (Constant.CALL_SETTING_ZHI_NENG.equals(callSeting)) {
			
			if (NetWorkUtil.getNetworkInfoLevel(context, 6, false)) {
//				directDial(context, callNumber, callUserName);
			} else {
				//dialCallback(context, callNumber, callUserName);
			}
			//智能选择拨打方式
			callModeDialogDismissListener.onDialogDismiss();
		}
	}
	
	/**
	 * 根据电话号码获取联系人姓名
	 * @param phoneNumber 联系人号码
	 * @return 联系人姓名
	 */
	private String queryNameByPhoneNumber(String phoneNumber) {
		if(TextUtils.isEmpty(phoneNumber)) {
			return phoneNumber;
		}
		
		List<ContactPersonEntity> personEntities =
				GlobalConfig.getInstance().getContactBeans();
		
		if(personEntities == null || personEntities.isEmpty()) {
			return phoneNumber;
		}
		
		for(ContactPersonEntity entity : personEntities) {
			String phoneNum = entity.getPhoneNumber();
			if(TextUtils.isEmpty(phoneNum)) {
				continue;
			}
			
			if(phoneNumber.equals(phoneNum)) {
				return entity.getDisplayName();
			}
		}
		
		return phoneNumber;
	}
	private String queryIdByPhoneNumber(String phoneNumber) {
		if(TextUtils.isEmpty(phoneNumber)) {
			return "";
		}

		List<ContactPersonEntity> personEntities =
				GlobalConfig.getInstance().getContactBeans();

		if(personEntities == null || personEntities.isEmpty()) {
			return "";
		}

		for(ContactPersonEntity entity : personEntities) {
			String phoneNum = entity.getPhoneNumber();
			if(TextUtils.isEmpty(phoneNum)) {
				continue;
			}

			if(phoneNumber.equals(phoneNum)) {
				return entity.get_id();
			}
		}

		return "";
	}
	/**
	 * 添加区号
	 * @param quhao
	 * @param callnum
	 * @return
	 */
	private String addQuHao(Context context, String callnum) {
		SharedPreferenceUtil mPreferenceUtil = SharedPreferenceUtil.getInstance(context);
		String quhao = mPreferenceUtil.getString(Constant.QU_HAO);
		if ("".equals(quhao) || null == quhao) {
			return callnum;
		}
		if (callnum.length() == DEFAULT_PHONE_LENGTH || callnum.length() == DEFAULT_PHONE_LENGTH - 1) {
			if (!callnum.startsWith("0")) {
				return quhao + callnum;
			}
		}
		return callnum;
	}

	/*private void showChooseDialog(final Context context, final String callNum,final String callname) {
		if(callModeChooserDialog == null) {
			callModeChooserDialog = new Dialog(context, R.style.MyDialog);
		}
		
		View view = View.inflate(context, R.layout.choose_callway_dialog, null);
		callModeChooserDialog.setContentView(view);
		
		Logger.v("手动选择", "callname:"+callname);

		TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
		TextView tv_close = (TextView) view.findViewById(R.id.tv_close);
		LinearLayout tv_zhibo_item = (LinearLayout) view
				.findViewById(R.id.tv_delete_item);
		LinearLayout tv_huibo_all = (LinearLayout) view
				.findViewById(R.id.tv_delete_all);
		tv_name.setText("");
		// 关闭
		tv_close.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				callModeChooserDialog.dismiss();
			}
		});

		// 直拨
		tv_zhibo_item.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				callModeChooserDialog.dismiss();
//				directDial(context, callNum, callname);
			}
		});

		// 回拨
		tv_huibo_all.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				callModeChooserDialog.dismiss();
				dialCallback(context, callNum, callname);
			}
		});

		callModeChooserDialog.show();

	}*/
	
	/*private void directDial(Context context, String callNum,String callname) {
		if(!CalldaPhoneService.isReady()) {
			//如果服务未启动，则启动该服务
			context.startService(new Intent(context, CalldaPhoneService.class));
		}
		
		Intent intent = new Intent();
		intent.setClass(context, CallingActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		intent.putExtra("callnum", callNum);
		intent.putExtra("callname", callname);
		
		context.startActivity(intent);
		
		//统计用户呼叫数据
		String time = TimeFormatUtil.formatTimeRange();
		String week = TimeFormatUtil.formatWeekRange();
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put("callTime", time);
		paramsMap.put("callWeek", week);
		paramsMap.put("callType", "直拨");
		paramsMap.put("mobile", Build.MODEL);
		MobclickAgent.onEventValue(context, "call_event", paramsMap, 1);
	}*/
	
	/**
	 * 回拨
	 */
	private void dialCallback(final Context context, final String callNum,String name,String id) {
		Intent intent=new Intent();
		intent.setClass(context, CallbackDisplayActivity.class);
		Bundle bundle=new Bundle();
		bundle.putString("name", name);
		bundle.putString("number", callNum);
		bundle.putString("callsetting", callSeting);
		bundle.putString("id",id);
		intent.putExtras(bundle);
		context.startActivity(intent);
		
		
		//统计用户呼叫数据
		String time = TimeFormatUtil.formatTimeRange();
		String week = TimeFormatUtil.formatWeekRange();
		
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put("callTime", time);
		paramsMap.put("callWeek", week);
		paramsMap.put("callType", "回拨");
		paramsMap.put("mobile", Build.MODEL);
		//MobclickAgent.onEventValue(context, "call_event", paramsMap, 1);
	}

	/**
	 * 手动呼叫选择对话框消失监听器
	 * @author zhw
	 */
	public interface onCallModeDialogDismissListener {
		void onDialogDismiss();
	}
}
