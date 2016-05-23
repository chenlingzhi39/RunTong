package com.callba.phone.bean;

/**
 * 获取充值套餐 实体类
 * {"suite_name":"A.国内直拨包月卡套餐选择","suite":[{"showSuiteName":"500分钟直拨包月","suiteName":"call-1m-1","money":"20","discount":"4分"}
 * @author zhanghw
 * @version 创建时间：2013-9-30 下午2:43:59
 */
public class RechargeMealSuiteBean {
	private String showSuiteName;
	private String suiteName;
	private String money;
	private String discount;
	
	public String getShowSuiteName() {
		return showSuiteName;
	}

	public void setShowSuiteName(String showSuiteName) {
		this.showSuiteName = showSuiteName;
	}

	public String getSuiteName() {
		return suiteName;
	}

	public void setSuiteName(String suiteName) {
		this.suiteName = suiteName;
	}

	public String getMoney() {
		return money;
	}

	public void setMoney(String money) {
		this.money = money;
	}

	public String getDiscount() {
		return discount;
	}

	public void setDiscount(String discount) {
		this.discount = discount;
	}

}
