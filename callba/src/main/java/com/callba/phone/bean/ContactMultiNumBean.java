package com.callba.phone.bean;

import com.callba.phone.logic.contact.ContactPersonEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 联系人详情中 同一个联系人 允许存在多个电话号码
 * 
 * @author Administrator
 */
public class ContactMultiNumBean extends ContactPersonEntity{
	private static final long serialVersionUID = 1L;

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

}
