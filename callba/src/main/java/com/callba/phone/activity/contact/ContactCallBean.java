package com.callba.phone.activity.contact;
/**
 * 通话记录实体类
 * @author Zhang
 */
public class ContactCallBean {
	public static final int CALL_IN = 1;
	public static final int CALL_OUT = 2;
	public static final int CALL_MISSED = 3;
	
	private String _id; // 数据库中唯一标示
	private String number; // 号码
	private String name; // 姓名
	private int callType;//通话类别
	private long startTime;// 开始时间（毫秒）
	private long endTime;// 结束时间

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public int getCallType() {
		return callType;
	}

	public void setCallType(int callType) {
		this.callType = callType;
	}

}
