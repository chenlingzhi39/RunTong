package com.callba.phone.bean;

import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by PC-20160514 on 2016/8/9.
 */
public interface ApiService {
@POST("nearby/nearby.jsp")
Observable<String> getNearBy(@Query("loginName")String name, @Query("loginPwd")String password, @Query("latitude")String latitude, @Query("longitude")String longitude, @Query("radius")String radius, @Query("page")String page);
}
