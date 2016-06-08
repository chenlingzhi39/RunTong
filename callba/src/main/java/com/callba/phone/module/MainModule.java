package com.callba.phone.module;

import android.content.Context;

import com.callba.phone.bean.UserDao;

import dagger.Module;
import dagger.Provides;

/**
 * Created by PC-20160514 on 2016/6/8.
 */
@Module
public class MainModule {
    private final Context mContext;
    private final UserDao.PostListener mPostListener;

    public MainModule(Context context,UserDao.PostListener postListener) {
        mContext = context;
        mPostListener = postListener ;
    }
    @Provides
    Context provideContext(){
        return mContext;
    }

    @Provides
    UserDao.PostListener providePostListener() {
        return mPostListener;
    }


}
