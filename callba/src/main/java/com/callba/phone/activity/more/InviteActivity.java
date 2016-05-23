package com.callba.phone.activity.more;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.activity.contact.ContactChooseActivity;
import com.callba.phone.bean.Task;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.view.CalldaToast;
import com.callba.phone.view.MyProgressDialog;
/**
 * 短信邀请
 * @author Administrator
 *
 */
public class InviteActivity extends BaseActivity implements OnClickListener {
	private Button bn_back, bn_invite;
	private MyProgressDialog progressDialog;
	
	@Override
	public void init() {
		bn_back = (Button) findViewById(R.id.bn_invite_back);
		bn_invite = (Button) findViewById(R.id.bn_invite_submit);
		bn_back.setOnClickListener(this);
		bn_invite.setOnClickListener(this);
	}

	@Override
	public void refresh(Object... params) {
		Message msg = (Message) params[0];
		if (progressDialog!=null&&progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		
		if(msg.arg1 == Task.TASK_SUCCESS) {
			String result = (String) msg.obj;
			String[] content = result.split("\\|");
			if("1".equals(content[0])) {	
				//fail
				CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(), content[1]);
			}else if("0".equals(content[0])) {
				//ok
				CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(), content[1]);
				this.finish();
			}else {
				CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(), R.string.server_error);
			}
		}else {
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.getdata_fail);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.more_invite);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bn_invite_back:
			finish();
			break;

		case R.id.bn_invite_submit:
			Intent intent = new Intent(this, ContactChooseActivity.class);
			startActivityForResult(intent, 20);
			break;
			
		default:
			break;
		}
	}
	/**
	 * 邀请好友
	 */
//	private void invite() {
//		Task task = new Task(Task.TASK_INVITE_FRIEND);
//		Map<String, Object> taskParams = new HashMap<String, Object>();
//		taskParams.put("loginName", Constant.USERNAME);
//		taskParams.put("loginPwd", Constant.PASSWORD);
//		taskParams.put("softType", "android");
//		task.setTaskParams(taskParams);
//
//		MainService.alltasks.add(task);
//		
//		progressDialog = new MyProgressDialog(this, "正在邀请");
//		progressDialog.show();
//	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 20 && resultCode == RESULT_OK) {
			String contacts = data.getStringExtra("contacts");
			if("".equals(contacts.trim()) || contacts.trim().length() < 1) {
				CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(), R.string.invite_qxxzlxr);
				return;
			}
			
			Uri smsToUri = Uri.parse("smsto:" + contacts);
		    Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
		    intent.putExtra("sms_body", (getString(R.string.share_content)
		    		+ CalldaGlobalConfig.getInstance().getUsername()));
		    startActivity(intent);
		}
	}
}
