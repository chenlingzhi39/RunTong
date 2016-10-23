package com.callba.phone;

import android.app.Application;

import com.callba.phone.ui.base.BaseActivity;

import javax.inject.Singleton;

import dagger.Component;

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
