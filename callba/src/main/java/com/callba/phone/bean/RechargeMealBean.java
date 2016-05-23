package com.callba.phone.bean;

import java.util.List;

/**
 * 获取充值套餐 实体类
 * {"suite_name":"A.国内直拨包月卡套餐选择","suite":[{"showSuiteName":"500分钟直拨包月","suiteName":"call-1m-1","money":"20","discount":"4分"}
 * @author zhanghw
 * @version 创建时间：2013-9-30 下午2:43:59
 */
public class RechargeMealBean {
	private String suite_name;
	private List<RechargeMealSuiteBean> suiteBeans;
	
	public String getSuite_name() {
		return suite_name;
	}

	public void setSuite_name(String suite_name) {
		this.suite_name = suite_name;
	}

	public List<RechargeMealSuiteBean> getSuiteBeans() {
		return suiteBeans;
	}

	public void setSuiteBeans(List<RechargeMealSuiteBean> suiteBeans) {
		this.suiteBeans = suiteBeans;
	}
	
}
