package com.callba.phone.logic.contact;
/** 
 * 联系人实体类 (两个子类)
 * 1、ContactPersonEntity	联系人
 * 2、ContactIndexEntity		搜索索引
 * @Author  zhw
 * @Version V1.0  
 * @Createtime：2014年5月23日 下午12:04:02 
 */
public class ContactEntity {
	public static final int CONTACT_TYPE_CONTACT = 0;
	public static final int CONTACT_TYPE_INDEX	 = 1;
	
	private int type;

	public int getType() {
		return type;
	}
	
	protected void setType(int contactType) {
		this.type = contactType;
	}
}
 