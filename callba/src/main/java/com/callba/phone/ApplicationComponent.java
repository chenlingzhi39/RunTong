package com.callba.phone;

import android.app.Activity;
import android.app.Application;

import com.callba.phone.bean.ApiService;
import com.callba.phone.ui.FriendActivity;
import com.callba.phone.ui.base.BaseActivity;
import com.zhy.http.okhttp.OkHttpUtils;

import javax.inject.Singleton;

import dagger.Component;
import dagger.Module;
import okhttp3.OkHttpClient;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by PC-20160514 on 2016/8/10.
 */
@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {
    void inject(BaseActivity Activity);
     final class AppInitialize{
        public static  ApplicationComponent  init(Application application){
          return DaggerApplicationComponent.builder().applicationModule(new ApplicationModule(application)).build();
        }
    }
}
