package com.callba.phone.logic.contact;
/** 
 * 
 * @Author  zhw
 * @Version V1.0  
 * @Createtime：2014年5月23日 下午12:07:47 
 */
public class ContactIndexEntity extends ContactEntity {
	private String indexName;	//索引名称
	
	public ContactIndexEntity() {
		setType(CONTACT_TYPE_INDEX);
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}
}
 