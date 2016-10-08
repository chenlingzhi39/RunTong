package com.callba.phone.ui.base;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.callba.R;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.cfg.*;
import com.callba.phone.manager.UserManager;
import com.callba.phone.util.SPUtils;
import com.yanzhenjie.permission.AndPermission;

import java.net.UnknownHostException;

import rx.Subscription;

/**
 * Created by PC-20160514 on 2016/5/18.
 */
public abstract class BaseFragment extends Fragment {
    protected View fragmentRootView;
    protected int mContentViewId;
    public ProgressDialog progressDialog;
    public boolean is_first=true;
    public Subscription subscription;
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
    public void toast(int id){
        Toast.makeText(getActivity(),getActivity().getString(id),Toast.LENGTH_SHORT).show();
    }
    /** Fragment当前状态是否可见 */
    protected boolean isVisible;


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



    protected void onVisible() {
        if(is_first){
        lazyLoad();
        is_first=false;
        }
    }



    protected void onInvisible() {


    }

    @Override
    public void onDestroy() {
        if(subscription!=null)
            subscription.unsubscribe();
        super.onDestroy();
    }

    protected void lazyLoad(){};
    public String getUsername() {
        return UserManager.getUsername(getActivity());
    }

    public String getPassword() {
        return UserManager.getPassword(getActivity());
    }
    public void showException(Exception e){
        if (e instanceof UnknownHostException) {
            toast(R.string.conn_failed);
        } else {
            toast(R.string.network_error);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // 这个Activity中没有Fragment，这句话可以注释。
        // super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        AndPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
}
