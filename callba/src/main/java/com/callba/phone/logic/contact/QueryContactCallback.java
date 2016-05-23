package com.callba.phone.logic.contact;

import java.util.List;

/**
 * 查询回调接口
 * @author Administrator
 */
public interface QueryContactCallback {
	void queryCompleted(List<ContactPersonEntity> contacts);
}
