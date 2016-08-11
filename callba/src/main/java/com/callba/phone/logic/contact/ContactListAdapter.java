package com.callba.phone.logic.contact;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.manager.ContactsManager;

import de.hdodenhof.circleimageview.CircleImageView;

/** 
 * 联系人页面ListView适配器
 * @Author  zhw
 * @Version V1.0  
 * @Createtime：2014年5月23日 下午4:18:37 
 */
public class ContactListAdapter extends BaseAdapter {
	private List<ContactEntity> mContactEntities;
	private Context mContext;
	
	public ContactListAdapter(Context context, List<ContactEntity> contactEntities) {
		this.mContext = context;
		this.mContactEntities = contactEntities;
	}
	
	@Override
	public int getCount() {
		return mContactEntities.size();
	}

	@Override
	public Object getItem(int position) {
		return mContactEntities.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean isEnabled(int position) {
		ContactEntity bean = (ContactEntity) getItem(position);
		if (bean.getType() == ContactEntity.CONTACT_TYPE_INDEX) {
			return false;
		}
		return super.isEnabled(position);
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
				convertView = View.inflate(mContext,
						R.layout.contact_listitem_abc, null);

				viewHolder_ABC = new ViewHolder_ABC();
				viewHolder_ABC.tvABC = (TextView) convertView
						.findViewById(R.id.tv_indexword);

				convertView.setTag(viewHolder_ABC);
			} else {

				convertView = View.inflate(mContext,
						R.layout.contact_listitem, null);

				viewHolder_Item = new ViewHolder_Item();
				viewHolder_Item.tvDisplayName = (TextView) convertView
						.findViewById(R.id.tv_displayname);
                viewHolder_Item.avatar=(CircleImageView) convertView.findViewById(R.id.avatar);

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
			new Thread(new Runnable() {
				@Override
				public void run() {

				}
			}).start();
			Bitmap bitmap=ContactsManager.getAvatar(mContext,contactPersonEntity.get_id(),false);
			if(bitmap!=null)
				viewHolder_Item.avatar.setImageBitmap(bitmap);
			else viewHolder_Item.avatar.setImageResource(R.drawable.head_portrait);
		}
		
		return convertView;
	}

	static class ViewHolder_ABC {
		TextView tvABC;
	}

	static class ViewHolder_Item {
		TextView tvDisplayName;
		ImageView avatar;
	}
}
 