package com.callba.phone.activity.recharge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.activity.more.FeeQueryActivity;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.view.CornerListView;

public class RechargeWayActivity extends BaseActivity implements OnClickListener, OnItemClickListener {
//	private Button bn_back;
	private TextView tv_more_zifei, tv_account;
	private CornerListView clv_recharge_way;
	private LinearLayout ll_root;
	
	private boolean isTabPage = true;	//判断当前页面是否为TAB
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recharge_way);
		
		isTabPage = getIntent().getBooleanExtra("isTabPage", true);
		initView();
		
		ll_root = (LinearLayout) findViewById(R.id.ll_recharge_root);
		if(!isTabPage) {
			LayoutParams params = (LayoutParams) ll_root.getLayoutParams();
			params.bottomMargin = 0;
			ll_root.setLayoutParams(params);
		}
	}
	@Override
		protected void onResume() {
			super.onResume();
			tv_account.setText(CalldaGlobalConfig.getInstance().getUsername());
		}
	/**
	 * 初始化VIew
	 */
	private void initView() {
		tv_account = (TextView) findViewById(R.id.tv_current_account);
		
//		bn_back = (Button) findViewById(R.id.bn_rechargeway_back);
//		bn_back.setOnClickListener(this);
		
		tv_more_zifei = (TextView) findViewById(R.id.tv_more_zifei);
		tv_more_zifei.setOnClickListener(this);
		
		clv_recharge_way = (CornerListView) findViewById(R.id.lv_recharge_way);
		SimpleAdapter adapter = new SimpleAdapter(this, getData(),
				R.layout.more_list_item, new String[] { "icon", "text" },
				new int[] { R.id.iv_more_item_icon, R.id.tv_more_item_text });
		clv_recharge_way.setAdapter(adapter);
		clv_recharge_way.setOnItemClickListener(this);
	}

	private List<? extends Map<String, ?>> getData() {
		List<Map<String, Object>> lists = new ArrayList<Map<String, Object>>();
		Map<String, Object> map1 = new HashMap<String, Object>();
//		map1.put("icon", R.drawable.alipay);
//		map1.put("text", getString(R.string.recmeal_zfbwy));
//		lists.add(map1);
//
//		map1 = new HashMap<String, Object>();
//		map1.put("icon", R.drawable.alipay);
//		map1.put("text", getString(R.string.recmeal_zfbkhd));
//		lists.add(map1);
//
//		map1 = new HashMap<String, Object>();
		map1.put("icon", R.drawable.callda);
		map1.put("text", getString(R.string.recallda_kdczk));
		lists.add(map1);
//
//		map1 = new HashMap<String, Object>();
//		map1.put("icon", R.drawable.china_mobile);
//		map1.put("text", getString(R.string.rechway_ydk));
//		lists.add(map1);
//
//		map1 = new HashMap<String, Object>();
//		map1.put("icon", R.drawable.china_unicom);
//		map1.put("text", getString(R.string.rechway_ltk));
//		lists.add(map1);
//		
//		map1 = new HashMap<String, Object>();
//		map1.put("icon", R.drawable.china_telecom);
//		map1.put("text", getString(R.string.rechway_dxk));
//		lists.add(map1);
//
//		map1 = new HashMap<String, Object>();
//		map1.put("icon", R.drawable.callda);
//		map1.put("text", getString(R.string.rech_cztc));
//		lists.add(map1);
		
		return lists;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
//		case R.id.bn_rechargeway_back:
//			finish();
//			break;
		
		case R.id.tv_more_zifei:
			Intent intent = new Intent(this, FeeQueryActivity.class);
			startActivity(intent);
			break;
			
		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
//		if(position == 0) {
//			//支付宝网页
//			Intent intent_wap = new Intent(this, RechargeActivity.class);
//			intent_wap.putExtra("rechargeWay", RechargeActivity.RECHARGE_ALIPAY_WAP);
//			startActivity(intent_wap);
//		}else if(position == 1) {
//			//支付宝客户端
//			Intent intent_client = new Intent(this, RechargeActivity.class);
//			intent_client.putExtra("rechargeWay", RechargeActivity.RECHARGE_ALIPAY_CLIENT);
//			startActivity(intent_client);
//		}else 
			if(position == 0) {
			//闰通充值卡
			Intent intent_client = new Intent(this, RechargeCalldaActivity.class);
			startActivity(intent_client);
		}
//			else if(position == 3) {
//			//移动
//			Intent intent_client = new Intent(this, RechargeActivity.class);
//			intent_client.putExtra("rechargeWay", RechargeActivity.RECHARGE_CARD_YIDONG);
//			startActivity(intent_client);
//		}else if(position == 4) {
//			//联通
//			Intent intent_client = new Intent(this, RechargeActivity.class);
//			intent_client.putExtra("rechargeWay", RechargeActivity.RECHARGE_CARD_LIANTONG);
//			startActivity(intent_client);
//		}else if(position == 5) {
//			//电信
//			Intent intent_client = new Intent(this, RechargeActivity.class);
//			intent_client.putExtra("rechargeWay", RechargeActivity.RECHARGE_CARD_DIANXIN);
//			startActivity(intent_client);
//		}else if(position == 6) {
//			//套餐
//			Intent intent_client = new Intent(this, RechargeMealActivity.class);
////			intent_client.putExtra("rechargeWay", RechargeActivity.RECHARGE_CARD_DIANXIN);
//			startActivity(intent_client);
//		}
	}
	
	/**
	 * 重写onkeyDown 捕捉返回键
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isTabPage) {
				//转到后台运行
				ActivityUtil.moveAllActivityToBack();
			} else {
				finish();
			}
			return true;
		}
		if(isTabPage) 
			return false;
		else
			return false;
	}

	@Override
	public void refresh(Object... params) {
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.getMenuInflater().inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.menu_exit) {
			super.exitApp();
		}
		return true;
	}
}
