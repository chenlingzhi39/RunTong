package com.callba.phone.ui.base;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.cfg.*;
import com.callba.phone.manager.UserManager;
import com.callba.phone.util.SPUtils;

/**
 * Created by PC-20160514 on 2016/5/18.
 */
public abstract class BaseFragment extends Fragment {
    protected View fragmentRootView;
    protected int mContentViewId;
    public ProgressDialog progressDialog;
    public boolean is_first=true;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        if (null == fragmentRootView) {
            if (getClass().isAnnotationPresent(ActivityFragmentInject.class)) {
                ActivityFragmentInject annotation = getClass()
                        .getAnnotation(ActivityFragmentInject.class);
                mContentViewId = annotation.contentViewId();
            } else {
                throw new RuntimeException(
                        "Class must add annotations of ActivityFragmentInitParams.class");
            }
            fragmentRootView = inflater.inflate(mContentViewId, container, false);
            initView(fragmentRootView);
        }
        return fragmentRootView;
    }
    protected abstract void initView(View fragmentRootView);
    public void toast(String msg){
        Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
    }
    /** Fragment当前状态是否可见 */
    protected boolean isVisible;
/*

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(getUserVisibleHint()) {
            isVisible = true;
            onVisible();
        } else {
            isVisible = false;
            onInvisible();
        }
    }


    *//**
     * 可见
     *//*
    protected void onVisible() {
        if(is_first){
        lazyLoad();
        is_first=false;
        }
    }


    *//**
     * 不可见
     *//*
    protected void onInvisible() {


    }


    *//**
     * 延迟加载
     * 子类必须重写此方法
     *//*
    protected abstract void lazyLoad();*/
    public String getUsername() {
        return UserManager.getUsername(getActivity());
    }

    public String getPassword() {
        return UserManager.getPassword(getActivity());
    }
}
