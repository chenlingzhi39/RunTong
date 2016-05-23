package com.callba.phone.bean;

import java.io.Serializable;
import java.util.List;

public class CalllogDetailBean implements Serializable {

	private static final long serialVersionUID = 1L;
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
}
