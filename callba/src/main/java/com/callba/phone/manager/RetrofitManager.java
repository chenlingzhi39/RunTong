package com.callba.phone.manager;

import com.callba.phone.bean.ApiService;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by PC-20160514 on 2016/8/9.
 */
public class RetrofitManager {
    ApiService mApiService;
    static RetrofitManager instance;
    public static RetrofitManager getInstance() {
        if (instance == null) {
            instance = new RetrofitManager();
        }
            return instance;

    }
    RetrofitManager() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://inter.boboit.cn/inter/")
                //增加返回值为String的支持
                .addConverterFactory(ScalarsConverterFactory.create())
                //增加返回值为Gson的支持(以实体类返回)
                .addConverterFactory(GsonConverterFactory.create())
                //增加返回值为Oservable<T>的支持
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        mApiService=retrofit.create(ApiService.class);
    }
      public Observable<String> getNearBy(String name, String password, String latitude, String longitude, String radius, String page){
          return  mApiService.getNearBy(name,password,latitude,longitude,radius,page).subscribeOn(Schedulers.io())
                  .observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.io());
      }
}
