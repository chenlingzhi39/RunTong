package com.callba.phone.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.callba.phone.logic.contact.ContactPersonEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 联系人详情中 同一个联系人 允许存在多个电话号码
 * 
 * @author Administrator
 */
public class ContactMultiNumBean extends ContactPersonEntity implements Parcelable{

	private List<String> contactPhones;
	
	public ContactMultiNumBean(){}
	
	public ContactMultiNumBean(ContactPersonEntity contactPersonEntity){
		this.set_id(contactPersonEntity.get_id());
		this.setType(contactPersonEntity.getType());
		this.setTypeName(contactPersonEntity.getTypeName());
		this.setLocation(contactPersonEntity.getLocation());
		this.setDisplayName(contactPersonEntity.getDisplayName());
		this.setPhoneNumber(contactPersonEntity.getPhoneNumber());
		this.setShowDisplayName(contactPersonEntity.getShowDisplayName());
		this.setShowPhoneNumber(contactPersonEntity.getShowPhoneNumber());
		this.setSearchSortKeyBean(contactPersonEntity.getSearchSortKeyBean());
		this.setShowSortPinYin(contactPersonEntity.getShowSortPinYin());
		
		List<String> phoneNumbes = new ArrayList<String>();
		phoneNumbes.add(contactPersonEntity.getPhoneNumber());
		this.setContactPhones(phoneNumbes);
	}

	public List<String> getContactPhones() {
		return contactPhones;
	}

	public void setContactPhones(List<String> contactPhones) {
		this.contactPhones = contactPhones;
	}


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeStringList(this.contactPhones);
	}

	protected ContactMultiNumBean(Parcel in) {
		super(in);
		this.contactPhones = in.createStringArrayList();
	}

	public static final Creator<ContactMultiNumBean> CREATOR = new Creator<ContactMultiNumBean>() {
		@Override
		public ContactMultiNumBean createFromParcel(Parcel source) {
			return new ContactMultiNumBean(source);
		}

		@Override
		public ContactMultiNumBean[] newArray(int size) {
			return new ContactMultiNumBean[size];
		}
	};
}
