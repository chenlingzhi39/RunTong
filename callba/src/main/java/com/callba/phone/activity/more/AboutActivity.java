package com.callba.phone.activity.more;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.callba.R;
import com.callba.phone.BaseActivity;

public class AboutActivity extends BaseActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.more_aboutus);
		super.onCreate(savedInstanceState);
		
		Button bn_back = (Button) findViewById(R.id.bn_about_back);
		bn_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AboutActivity.this.finish();
			}
		});
	}

	@Override
	public void refresh(Object... params) {
	}
}
