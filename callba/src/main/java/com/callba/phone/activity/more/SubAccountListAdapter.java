package com.callba.phone.activity.more;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.activity.more.SubAccountActivity.SubAccountBean;
import com.callba.phone.bean.Task;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.service.MainService;
import com.callba.phone.view.CalldaToast;
import com.callba.phone.view.MyProgressDialog;

/**
 * 更多 子账户管理 子账户列表 适配器
 * @author Administrator
 *
 */
public class SubAccountListAdapter extends BaseAdapter {
	private List<SubAccountBean> subAccounts;
	private Context context;
	
	public SubAccountListAdapter(Context context, List<SubAccountBean> subAccounts) {
		super();
		this.context = context;
		this.subAccounts = subAccounts;
	}

	@Override
	public int getCount() {
		return subAccounts.size();
	}

	@Override
	public Object getItem(int position) {
		return subAccounts.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = View.inflate(context, R.layout.more_subaccount_lv_item, null);
		
		TextView tv_accountInfo = (TextView) view.findViewById(R.id.tv_subaccount_accinfo);
		TextView tv_delete = (TextView) view.findViewById(R.id.tv_subaccount_delete);
		TextView tv_changePass = (TextView) view.findViewById(R.id.tv_subaccount_chagepass);
		
		SubAccountBean bean = subAccounts.get(position);
		
		tv_accountInfo.setText(bean.getuName() + context.getString(R.string.sacc_mm) + bean.getuPass());
		
		tv_delete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				confirm2Delete(subAccounts.get(position));
			}
		});
		
		tv_changePass.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showChangePassDialog(subAccounts.get(position));
			}
		});
		
		return view;
	}
	
	/**
	 * 删除 确认
	 * @param bean
	 */
	private void confirm2Delete(final SubAccountBean bean) {
		new AlertDialog.Builder(context)
			.setTitle(context.getString(R.string.sacc_tip))
			.setMessage(context.getString(R.string.sacc_sfsczh))
			.setPositiveButton(context.getString(R.string.sacc_delete), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					deleteSubAccount(bean);
				}
				
			}).setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			}).create().show();
	}
	
	private MyProgressDialog progressDialog;
	
	protected void deleteSubAccount(SubAccountBean bean) {
		progressDialog = new MyProgressDialog(context, context.getString(R.string.sacc_zzsc));
		progressDialog.show();
		
		Task task = new Task(Task.TASK_DELETE_SUBACCOUNT);
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put("loginName", CalldaGlobalConfig.getInstance().getUsername());
		taskParams.put("loginPwd", CalldaGlobalConfig.getInstance().getPassword());
		taskParams.put("childPhoneNumber", bean.getuName());
		task.setTaskParams(taskParams);

		MainService.newTask(task);
	}
	/**
	 * 取消进度条对话框的显示
	 */
	public void dismissProDialog() {
		if(progressDialog.isShowing())
			progressDialog.dismiss();
	}
	
	/**
	 * 修改子账户密码 密码输入框
	 * @param subAccountBean
	 */
	private void showChangePassDialog(final SubAccountBean subAccountBean) {
		Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(context.getString(R.string.modify_pwd));
		
		View view = View.inflate(context, R.layout.more_subaccount_changepass_dialog, null);
		final EditText et_pass = (EditText) view.findViewById(R.id.et_subaccount_pass);
		
		dialog.setView(view);
		dialog.setPositiveButton(context.getString(R.string.modify), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String newPass = et_pass.getText().toString().trim();
				changeSubAccPass(subAccountBean, newPass);
			}
		});
		dialog.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		dialog.create().show();
	}

	/**
	 * 修改子账户密码
	 * @param subAccountBean
	 * @param newPass
	 */
	protected void changeSubAccPass(SubAccountBean bean,
			String newPass) {
		if("".equals(newPass) || newPass.length() < 1) {
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(context, R.string.sacc_xmmbnk);
			return;
		}
		//验证密码是否规范 6~16位的字母、数字或下划线
		Pattern p = Pattern.compile("\\w{6,16}");
		Matcher m = p.matcher(newPass);
		if(!m.matches()) {
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(context, R.string.pwd_type);
			return;
		}
		
		progressDialog = new MyProgressDialog(context, context.getString(R.string.sacc_zzxgmm));
		progressDialog.show();
		
		Task task = new Task(Task.TASK_DELETE_SUBACCOUNT);
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put("loginName", CalldaGlobalConfig.getInstance().getUsername());
		taskParams.put("loginPwd", CalldaGlobalConfig.getInstance().getPassword());
		taskParams.put("childPhoneNumber", bean.getuName());
		taskParams.put("childNewPwd", newPass);
		task.setTaskParams(taskParams);

		MainService.newTask(task);
	}
}