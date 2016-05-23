package com.callba.phone.activity.contact;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.logic.contact.ContactEntity;
import com.callba.phone.logic.contact.ContactIndexEntity;
import com.callba.phone.logic.contact.ContactPersonEntity;

/** 
 * 选择联系人页面listview适配器
 * @Author  zhw
 * @Version V1.0  
 * @Createtime：2014年5月23日 下午5:31:34 
 */
public class ContactChooserAdapter extends BaseAdapter {
	private Context context;
	private List<ContactEntity> mContactList;
	
	public Map<Integer, Boolean> isSelected;// 记录该项是否被选中

	public ContactChooserAdapter(Context context, List<ContactEntity> mContactList) {
		this.context = context;
		this.mContactList = mContactList;

		isSelected = new HashMap<Integer, Boolean>();
		// 初始化数据
		for (int i = 0; i < mContactList.size(); i++) {
			isSelected.put(i, false);
		}
	}

	@Override
	public int getCount() {
		return mContactList.size();
	}

	@Override
	public boolean isEnabled(int position) {
		ContactEntity contactBean = (ContactEntity) getItem(position);
		if(contactBean.getType() == ContactEntity.CONTACT_TYPE_INDEX) {
			return false;
		}
		return super.isEnabled(position);
	}

	@Override
	public Object getItem(int position) {
		return mContactList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public int getItemViewType(int position) {
		ContactEntity bean = (ContactEntity) getItem(position);
		return bean.getType();
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ContactEntity bean = (ContactEntity) getItem(position);

		ViewHolder_ABC viewHolder_ABC = null;
		ViewHolder_Item viewHolder_Item = null;

		if (convertView == null) {
			if (bean.getType() == ContactEntity.CONTACT_TYPE_INDEX) {
				convertView = View.inflate(context,
						R.layout.contact_listitem_abc, null);

				viewHolder_ABC = new ViewHolder_ABC();
				viewHolder_ABC.tvABC = (TextView) convertView
						.findViewById(R.id.tv_indexword);

				convertView.setTag(viewHolder_ABC);
			} else {

				convertView = View.inflate(context,
						R.layout.choose_contact_lv_item, null);
				
				viewHolder_Item = new ViewHolder_Item();
				viewHolder_Item.tvDisplayName = (TextView) 
						convertView.findViewById(R.id.tv_choose_name);
				viewHolder_Item.tvPhoneNumber = (TextView) 
						convertView.findViewById(R.id.tv_choose_phone);
				viewHolder_Item.cbSelected = (CheckBox) 
						convertView.findViewById(R.id.cb_choose_check);
				
				convertView.setTag(viewHolder_Item);
			}
		} else {
			if (bean.getType() == ContactEntity.CONTACT_TYPE_INDEX) {
				viewHolder_ABC = (ViewHolder_ABC) convertView.getTag();
			} else {
				viewHolder_Item = (ViewHolder_Item) convertView.getTag();
			}
		}
		
		if (bean.getType() == ContactEntity.CONTACT_TYPE_INDEX) {
			ContactIndexEntity contactIndexEntity = (ContactIndexEntity) bean;
			viewHolder_ABC.tvABC.setText(contactIndexEntity.getIndexName());
		} else {
			ContactPersonEntity contactPersonEntity = (ContactPersonEntity) bean;
			viewHolder_Item.tvDisplayName.setText(contactPersonEntity.getDisplayName());
			viewHolder_Item.tvPhoneNumber.setText(contactPersonEntity.getPhoneNumber());
			viewHolder_Item.cbSelected.setChecked(isSelected.get(position));
		}
		
		return convertView;
	}
	
	static class ViewHolder_ABC {
		TextView tvABC;
	}

	static class ViewHolder_Item {
		TextView tvDisplayName;
		TextView tvPhoneNumber;
		CheckBox cbSelected;
	}
}
 