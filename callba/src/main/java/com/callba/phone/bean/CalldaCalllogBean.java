package com.callba.phone.bean;

import java.io.Serializable;

/**
 * 封装闰通通话记录
 * 
 * @author zhw
 */
public class CalldaCalllogBean implements Serializable{
	
	
	private static final long serialVersionUID = 1L;
	
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
	private int callLogType;
	private int callLogMIME;
	private SearchSortKeyBean searchSortKeyBean;
	private String formatedCallLogDuration = "00:00:00";
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

	public String getFormatedCallLogDuration() {
		return formatedCallLogDuration;
	}

	public void setFormatedCallLogDuration(String formatedCallLogDuration) {
		this.formatedCallLogDuration = formatedCallLogDuration;
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

	@Override
	public String toString() {
		return "CalldaCalllogBean [id=" + id + ", callLogNumber="
				+ callLogNumber + ", displayName=" + displayName
				+ ", callLogTime=" + callLogTime + ", callLogType="
				+ callLogType + ", callLogMIME=" + callLogMIME
				+ ", searchSortKeyBean=" + searchSortKeyBean
				+ ", location=" + location
				+ ", formatedCallLogDuration=" + formatedCallLogDuration
				+ ",occurrenceNumber=" + occurrenceNumber + "]";
	}

}
