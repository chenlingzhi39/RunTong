package com.callba.phone.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class CalllogDetailBean implements Parcelable{
	public static final int INCOMING_CALL = 0x10; // 来电
	public static final int OUTGOING_CALL = 0x11; // 去电
	public static final int MISSED_CALL = 0x12; // 未接

	public static final int LOCAL_CALLLOG = 0x20;
	public static final int SOFT_CALLLOG = 0x21;
	public static final int BACK_CALLLOG = 0x22;

	private String callLogNumber;
	private long callLogTime;
	private String date;

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public int getCallLogType() {
		return callLogType;
	}

	public void setCallLogType(int callLogType) {
		this.callLogType = callLogType;
	}

	private int callLogType;
	private int occurrenceNumber;
	private List<CalldaCalllogBean> calllogBean;

	public List<CalldaCalllogBean> getCalllogBean() {
		return calllogBean;
	}

	public void setCalllogBean(List<CalldaCalllogBean> calllogBean) {
		this.calllogBean = calllogBean;
	}

	public String getCallLogNumber() {
		return callLogNumber;
	}

	public void setCallLogNumber(String callLogNumber) {
		this.callLogNumber = callLogNumber;
	}

	public long getCallLogTime() {
		return callLogTime;
	}

	public void setCallLogTime(long callLogTime) {
		this.callLogTime = callLogTime;
	}

	public int getOccurrenceNumber() {
		return occurrenceNumber;
	}

	public void setOccurrenceNumber(int occurrenceNumber) {
		this.occurrenceNumber = occurrenceNumber;
	}

	@Override
	public String toString() {
		return " [callLogNumber=" + callLogNumber + ", callLogTime="
				+ callLogTime + ",date=" + date + ",occurrenceNumber="
				+ occurrenceNumber + ",calllogBean" + calllogBean + "]";
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.callLogNumber);
		dest.writeLong(this.callLogTime);
		dest.writeString(this.date);
		dest.writeInt(this.callLogType);
		dest.writeInt(this.occurrenceNumber);
		dest.writeList(this.calllogBean);
	}

	public CalllogDetailBean() {
	}

	protected CalllogDetailBean(Parcel in) {
		this.callLogNumber = in.readString();
		this.callLogTime = in.readLong();
		this.date = in.readString();
		this.callLogType = in.readInt();
		this.occurrenceNumber = in.readInt();
		this.calllogBean = new ArrayList<CalldaCalllogBean>();
		in.readList(this.calllogBean, CalldaCalllogBean.class.getClassLoader());
	}

	public static final Creator<CalllogDetailBean> CREATOR = new Creator<CalllogDetailBean>() {
		@Override
		public CalllogDetailBean createFromParcel(Parcel source) {
			return new CalllogDetailBean(source);
		}

		@Override
		public CalllogDetailBean[] newArray(int size) {
			return new CalllogDetailBean[size];
		}
	};
}
