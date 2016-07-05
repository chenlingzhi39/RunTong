
package com.callba.phone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;

@ActivityFragmentInject(
		contentViewId = R.layout.em_activity_edit,
		navigationId = R.drawable.press_back,
		toolbarTitle = R.string.Change_the_group_name,
		menuId = R.menu.menu_save
)
public class EditActivity extends BaseActivity {
	private EditText editText;

	@Override
	public void refresh(Object... params) {

	}

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		editText = (EditText) findViewById(R.id.edittext);
		String title = getIntent().getStringExtra("title");
		String data = getIntent().getStringExtra("data");
		if(title != null)
			((TextView)findViewById(R.id.tv_title)).setText(title);
		if(data != null)
			editText.setText(data);
		editText.setSelection(editText.length());
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.save:
				save();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void save(){
		setResult(RESULT_OK,new Intent().putExtra("data", editText.getText().toString()));
		finish();
	}
}
