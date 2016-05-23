package com.callba.phone.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.callba.R;

public class MyProgressDialog extends ProgressDialog {
	private String message;
	private TextView define_progress_msg;
	private int progressDialogShowCount = 0;	//记录ProgressDailog show次数

	public MyProgressDialog(Context context) {
		super(context);
		message = context.getString(R.string.loading);
	}

	public MyProgressDialog(Context context, String message) {
		super(context);
		this.message = message;
		this.setCanceledOnTouchOutside(false);
	}

	public void setProgressMessage(String msg) {
		if(define_progress_msg != null)
			define_progress_msg.setText(msg);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.myprogressdialog);
		define_progress_msg = (TextView) findViewById(R.id.define_progress_msg);
		define_progress_msg.setText(message);
	}
	
	@Override
	public void dismiss() {
		progressDialogShowCount--;
		if(this != null && progressDialogShowCount <= 0
				&& this.isShowing()) {
			if(isShowing()) {
				try {
					super.dismiss();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public void show() {
		if(!isShowing()) {
			super.show();
		}
		progressDialogShowCount++;
	}
}