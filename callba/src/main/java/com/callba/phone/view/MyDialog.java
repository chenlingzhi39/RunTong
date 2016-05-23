package com.callba.phone.view;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.callba.R;

public class MyDialog {
	private static Dialog dialog;
	
	private MyDialog(){}
	/**
	 * 显示Dialog
	 * @param context
	 * @param text
	 */
	public static void showDialog(Context context, String text, OnClickListener listener) {
		dialog = new Dialog(context, R.style.MyDialog);
		View view = View.inflate(context, R.layout.mydialog, null);
		TextView tv = (TextView) view.findViewById(R.id.tv_dialog_message);
		tv.setText(text);
		Button bn_ok = (Button) view.findViewById(R.id.bn_ok);
		Button bn_cancel = (Button) view.findViewById(R.id.bn_cancel);
		if(listener != null) {
			bn_ok.setOnClickListener(listener);
			bn_cancel.setOnClickListener(listener);
		}
		
		dialog.setContentView(view);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}
	/**
	 * 取消dialog显示
	 */
	public static void dismissDialog() {
		try {
			if(dialog != null&&dialog.isShowing())
				dialog.dismiss();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
