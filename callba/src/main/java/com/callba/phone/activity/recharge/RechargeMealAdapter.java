package com.callba.phone.activity.recharge;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.bean.RechargeMealSuiteBean;

/**
 * @author zhanghw
 * @version 创建时间：2013-9-30 下午2:42:21
 */
public class RechargeMealAdapter extends BaseAdapter {
	private int type = 0; //type 1 直拨， type 2回拨
	private Context context;
	private List<RechargeMealSuiteBean> mealBeans;
	
	public RechargeMealAdapter(int type, Context context,
			List<RechargeMealSuiteBean> mealBeans) {
		super();
		this.type = type;
		this.context = context;
		this.mealBeans = mealBeans;
	}

	@Override
	public int getCount() {
		return mealBeans.size();

	}

	@Override
	public Object getItem(int position) {
		return mealBeans.get(position);

	}

	@Override
	public long getItemId(int position) {
		return position;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		if(type == 1) {
			view = View.inflate(context, R.layout.recharge_meal_lv_zb_item, null);
		}else if(type == 2) {
			view = View.inflate(context, R.layout.recharge_meal_lv_hb_item, null);
		}
		TextView tv_title = (TextView) view.findViewById(R.id.tv_meal_title);
		TextView tv_info = (TextView) view.findViewById(R.id.tv_meal_info);
		
		RechargeMealSuiteBean bean = mealBeans.get(position);
		tv_title.setText(bean.getShowSuiteName());
		tv_info.setText(String.format(context.getString(R.string.recmeal_money), bean.getMoney(), bean.getDiscount()));
		
		return view;

	}

}
