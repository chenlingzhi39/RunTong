package com.callba.phone.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 封装闰通通话记录
 * 
 * @author zhw
 */
public class CalldaCalllogBean implements Parcelable{
	
	public static final int INCOMING_CALL = 0x10; // 来电
	public static final int OUTGOING_CALL = 0x11; // 去电
	public static final int MISSED_CALL = 0x12; // 未接

	public static final int LOCAL_CALLLOG = 0x20;
	public static final int SOFT_CALLLOG = 0x21;
	public static final int BACK_CALLLOG = 0x22;

	private int id;
	private String callLogNumber;
	private String displayName;
	private String location;
	private long callLogTime;
	private long calllogDuration;
	private int callLogType;
	private int callLogMIME;
	private SearchSortKeyBean searchSortKeyBean;
	private int occurrenceNumber;
	private int index;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCallLogNumber() {
		return callLogNumber;
	}

	public void setCallLogNumber(String callLogNumber) {
		this.callLogNumber = callLogNumber;
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

	public long getCallLogTime() {
		return callLogTime;
	}

	public void setCallLogTime(long callLogTime) {
		this.callLogTime = callLogTime;
	}

	public int getCallLogType() {
		return callLogType;
	}

	public void setCallLogType(int callLogType) {
		this.callLogType = callLogType;
	}

	public int getCallLogMIME() {
		return callLogMIME;
	}

	public void setCallLogMIME(int callLogMIME) {
		this.callLogMIME = callLogMIME;
	}



	public SearchSortKeyBean getSearchSortKeyBean() {
		return searchSortKeyBean;
	}

	public void setSearchSortKeyBean(SearchSortKeyBean searchSortKeyBean) {
		this.searchSortKeyBean = searchSortKeyBean;
	}
	
	public int getOccurrenceNumber() {
		return occurrenceNumber;
	}
	
	public void setOccurrenceNumber(int occurrenceNumber) {
		this.occurrenceNumber = occurrenceNumber;
	}

	public long getCalllogDuration() {
		return calllogDuration;
	}

	public void setCalllogDuration(long calllogDuration) {
		this.calllogDuration = calllogDuration;
	}

	@Override
	public String toString() {
		return "CalldaCalllogBean [id=" + id + ", callLogNumber="
				+ callLogNumber + ", displayName=" + displayName
				+ ", callLogTime=" + callLogTime + ", callLogType="
				+ callLogType + ", callLogMIME=" + callLogMIME
				+ ", searchSortKeyBean=" + searchSortKeyBean
				+ ", location=" + location
				+ ",occurrenceNumber=" + occurrenceNumber + "]";
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.id);
		dest.writeString(this.callLogNumber);
		dest.writeString(this.displayName);
		dest.writeString(this.location);
		dest.writeLong(this.callLogTime);
		dest.writeLong(this.calllogDuration);
		dest.writeInt(this.callLogType);
		dest.writeInt(this.callLogMIME);
		dest.writeParcelable(this.searchSortKeyBean, flags);
		dest.writeInt(this.occurrenceNumber);
		dest.writeInt(this.index);
	}

	public CalldaCalllogBean() {
	}

	protected CalldaCalllogBean(Parcel in) {
		this.id = in.readInt();
		this.callLogNumber = in.readString();
		this.displayName = in.readString();
		this.location = in.readString();
		this.callLogTime = in.readLong();
		this.calllogDuration = in.readLong();
		this.callLogType = in.readInt();
		this.callLogMIME = in.readInt();
		this.searchSortKeyBean = in.readParcelable(SearchSortKeyBean.class.getClassLoader());
		this.occurrenceNumber = in.readInt();
		this.index = in.readInt();
	}

	public static final Creator<CalldaCalllogBean> CREATOR = new Creator<CalldaCalllogBean>() {
		@Override
		public CalldaCalllogBean createFromParcel(Parcel source) {
			return new CalldaCalllogBean(source);
		}

		@Override
		public CalldaCalllogBean[] newArray(int size) {
			return new CalldaCalllogBean[size];
		}
	};
}
