package com.callba.phone.logic.contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;

import com.callba.phone.activity.contact.ContactMutliNumBean;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.util.Logger;
import com.umeng.socialize.utils.Log;

/** 
 * 联系人业务逻辑管理
 * @Author  zhw
 * @Version V1.0  
 * @Createtime：2014年5月23日 上午11:58:00 
 */
public class ContactController {
	private static final String[] SEARCH_LETTER_ARRAY = 
		{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
		 "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
		 "U", "V", "W", "X", "Y", "Z", "#"};
	
	//所有的联系人集合
	private List<ContactPersonEntity> mAllContactPersonEntities;
	
	//检索的字母、位置索引表
	private Map<String, Integer> letterSearchMap;
	
	public ContactController() {
		mAllContactPersonEntities = CalldaGlobalConfig.getInstance().getContactBeans();
	}
	
	/**
	 * 获取ListView列表的搜索索引
	 * @return
	 */
	public Map<String, Integer> getSearchMap() {
		if(letterSearchMap == null) {
			sortContactByLetter(mAllContactPersonEntities);
		}
		
		return letterSearchMap;
	}
	
	/**
	 * 获取用于ListView显示的数据(默认数据为当前联系人)
	 * @return
	 */
	public List<ContactEntity> getFilterListContactEntities() {
		return sortContactByLetter(mAllContactPersonEntities);
	}
	
	/**
	 * 获取用于ListView显示的数据(默认数据为当前联系人，根据姓名排列，多个号码只显示一条数据)
	 * @return
	 */
	public List<ContactEntity> getFilterListContactEntitiesNoDuplicate() {
		List<ContactMutliNumBean> personEntities = new ArrayList<ContactMutliNumBean>();
		String lastName = "";
		List<String> contactPhones=new ArrayList<>();
		Logger.i("contact_size",mAllContactPersonEntities.size()+"");
		for(int i=0;i<mAllContactPersonEntities.size();i++){
			Logger.i("contact_number",mAllContactPersonEntities.get(i).getPhoneNumber());
			if(i==0)
			{personEntities.add(new ContactMutliNumBean(mAllContactPersonEntities.get(0)));
				contactPhones.add(mAllContactPersonEntities.get(0).getPhoneNumber());
				personEntities.get(0).setContactPhones(contactPhones);
			continue;}
			if(!mAllContactPersonEntities.get(i).getDisplayName().equals(mAllContactPersonEntities.get(i-1).getDisplayName())){
				contactPhones=new ArrayList<>();
				contactPhones.add(mAllContactPersonEntities.get(i).getPhoneNumber());
				personEntities.add(new ContactMutliNumBean(mAllContactPersonEntities.get(i)));
			}else{
				contactPhones.add(mAllContactPersonEntities.get(i).getPhoneNumber());
				}
			personEntities.get(personEntities.size()-1).setContactPhones(contactPhones);
		}
		/*for(ContactPersonEntity contactPersonEntity : mAllContactPersonEntities) {

			if(!TextUtils.isEmpty(lastName) && lastName.equals(contactPersonEntity.getDisplayName())) {
				Logger.i("name_number",contactPersonEntity.getDisplayName()+contactPersonEntity.getPhoneNumber());
				if(personEntities.size() > 1) {

					List<String> contactPhones = personEntities.get(personEntities.size() - 1).getContactPhones();
					contactPhones.add(contactPersonEntity.getPhoneNumber());
				}
				continue;
			}

			lastName = contactPersonEntity.getDisplayName();
			
			personEntities.add(new ContactMutliNumBean(contactPersonEntity));
		}*/
		return sortContactByLetter(personEntities);
	}
	
	/**
	 * 获取用于ListView显示的数据
	 * @param contactPersonEntities
	 * @return
	 */
	public List<ContactEntity> getFilterListContactEntities(List<ContactPersonEntity> contactPersonEntities) {
		return sortContactByLetter(contactPersonEntities);
	}
	
	/**
	 * 根据关键字检索联系人（模糊检索手机号码和名字）
	 * @param searchWord
	 * @return
	 */
	public List<ContactEntity> searchContact(String searchWord, List<ContactEntity> searchSrcEntities) {
		if(TextUtils.isEmpty(searchWord)) {
			return searchSrcEntities;
		}
		
		//检索到的联系人
		List<ContactPersonEntity> searchedPersonEntities = new ArrayList<ContactPersonEntity>();
		
		for(ContactEntity contactEntity : searchSrcEntities) {
			if(contactEntity.getType() == ContactEntity.CONTACT_TYPE_CONTACT) {
				ContactPersonEntity personEntity = (ContactPersonEntity) contactEntity;
				
				if(personEntity.getDisplayName() == null
						|| personEntity.getPhoneNumber() == null) {
					continue;
				}
				
				if(personEntity.getDisplayName().contains(searchWord)
						|| personEntity.getPhoneNumber().contains(searchWord)) {
					searchedPersonEntities.add(personEntity);
				}
			}
		}
		
		return sortContactByLetter(searchedPersonEntities);
	}
	
	
	/**
	 * 联系人按字母排序
	 * @author zhw
	 */
	private List<ContactEntity> sortContactByLetter(List<? extends ContactPersonEntity> personEntities) {
		List<ContactEntity> entities = new ArrayList<ContactEntity>();
		letterSearchMap = new HashMap<String, Integer>();
		
		if(personEntities.isEmpty()) {
			return entities;
		}
		
		for(String letter : SEARCH_LETTER_ARRAY) {
			boolean isLetterAdd = false;
			for(int i=0; i<personEntities.size(); i++) {
				ContactPersonEntity contactPersonEntity = personEntities.get(i);
				if(letter.equals(contactPersonEntity.getTypeName())) {
					if(!isLetterAdd) {
						//添加字母索引
						ContactIndexEntity contactIndexEntity = new ContactIndexEntity();
						contactIndexEntity.setIndexName(letter);
						entities.add(contactIndexEntity);
						
						letterSearchMap.put(letter, entities.size()-1);
						
						isLetterAdd = true;
					}
					entities.add(contactPersonEntity);
				}
			}
		}
		
		return entities;
	}
}
 