package com.callba.phone.logic.contact;


import android.os.Parcel;
import android.os.Parcelable;

import com.callba.phone.bean.SearchSortKeyBean;

/**
 * 联系人
 * 
 * @Author zhw
 * @Version V1.0
 * @Createtime：2014年5月23日 下午12:07:10
 */
public class ContactPersonEntity extends ContactEntity implements Parcelable{

	private String _id; // 本地系统中数联系人 lookup_key （编辑联系人）
	private String displayName; // 名字
	private String phoneNumber;// 号码
	private String location;// 归属地
	private String typeName;// 首字母英文名称

	private SearchSortKeyBean searchSortKeyBean; // 拼音搜索实体

	private String showSortPinYin; // 格式化显示的拼音
	private String showPhoneNumber; // 格式化显示的号码
	private String showDisplayName; // 格式化显示的姓名
	public ContactPersonEntity() {
		setType(CONTACT_TYPE_CONTACT);
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public SearchSortKeyBean getSearchSortKeyBean() {
		return searchSortKeyBean;
	}

	public void setSearchSortKeyBean(SearchSortKeyBean searchSortKeyBean) {
		this.searchSortKeyBean = searchSortKeyBean;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getShowSortPinYin() {
		return showSortPinYin;
	}

	public void setShowSortPinYin(String showSortPinYin) {
		this.showSortPinYin = showSortPinYin;
	}

	public String getShowPhoneNumber() {
		return showPhoneNumber;
	}

	public void setShowPhoneNumber(String showPhoneNumber) {
		this.showPhoneNumber = showPhoneNumber;
	}

	public String getShowDisplayName() {
		return showDisplayName;
	}

	public void setShowDisplayName(String showDisplayName) {
		this.showDisplayName = showDisplayName;
	}

	@Override
	public String toString() {
		return "ContactPersonEntity [_id=" + _id + ", displayName="
				+ displayName + ", phoneNumber=" + phoneNumber + ", typeName="
				+ typeName + ", searchSortKeyBean=" + searchSortKeyBean
				+ ", location=" + location + ", showSortPinYin="
				+ showSortPinYin + ", showPhoneNumber=" + showPhoneNumber
				+ ", showDisplayName=" + showDisplayName + "]";
	}


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this._id);
		dest.writeString(this.displayName);
		dest.writeString(this.phoneNumber);
		dest.writeString(this.location);
		dest.writeString(this.typeName);
		dest.writeParcelable(this.searchSortKeyBean, flags);
		dest.writeString(this.showSortPinYin);
		dest.writeString(this.showPhoneNumber);
		dest.writeString(this.showDisplayName);
	}

	protected ContactPersonEntity(Parcel in) {
		this._id = in.readString();
		this.displayName = in.readString();
		this.phoneNumber = in.readString();
		this.location = in.readString();
		this.typeName = in.readString();
		this.searchSortKeyBean = in.readParcelable(SearchSortKeyBean.class.getClassLoader());
		this.showSortPinYin = in.readString();
		this.showPhoneNumber = in.readString();
		this.showDisplayName = in.readString();
	}

	public static final Creator<ContactPersonEntity> CREATOR = new Creator<ContactPersonEntity>() {
		@Override
		public ContactPersonEntity createFromParcel(Parcel source) {
			return new ContactPersonEntity(source);
		}

		@Override
		public ContactPersonEntity[] newArray(int size) {
			return new ContactPersonEntity[size];
		}
	};
}
