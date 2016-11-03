package com.callba.phone.ui.base;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.callba.R;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.manager.UserManager;
import com.yanzhenjie.permission.AndPermission;
import com.zhy.http.okhttp.request.RequestCall;

import java.net.UnknownHostException;
import java.util.ArrayList;

import rx.Subscription;

/**
 * Created by PC-20160514 on 2016/5/18.
 */
public abstract class BaseFragment extends Fragment {
    protected View fragmentRootView;
    protected int mContentViewId;
    private int mToolbarTitle;
    public ProgressDialog progressDialog;
    public boolean is_first=true;
    public Subscription subscription;
    public Toolbar toolbar;
    private int mMenuId;
    public ArrayList<RequestCall> requestCalls;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        requestCalls=new ArrayList<>();
        if (null == fragmentRootView) {
            if (getClass().isAnnotationPresent(ActivityFragmentInject.class)) {
                ActivityFragmentInject annotation = getClass()
                        .getAnnotation(ActivityFragmentInject.class);
                mContentViewId = annotation.contentViewId();
                mMenuId = annotation.menuId();
                mToolbarTitle = annotation.toolbarTitle();
            } else {
                throw new RuntimeException(
                        "Class must add annotations of ActivityFragmentInitParams.class");
            }
            fragmentRootView = inflater.inflate(mContentViewId, container, false);
            initToolbar();

            initView(fragmentRootView);
        }
        return fragmentRootView;
    }
    protected abstract void initView(View fragmentRootView);
    protected void initToolbar(){
        toolbar = (Toolbar)fragmentRootView.findViewById(R.id.toolbar);
        if (toolbar != null) {
            setHasOptionsMenu(true);
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("");
            if (mToolbarTitle != -1)
            { TextView title = (TextView) fragmentRootView.findViewById(R.id.title);
            title.setText(getResources().getString(mToolbarTitle));}
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mMenuId != -1)
        {menu.clear();
        inflater.inflate(mMenuId, menu);}
    }

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
        for(RequestCall requestCall:requestCalls){
            requestCall.cancel();
        }
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
    public RequestCall addRequestCall(RequestCall requestCall){
        requestCalls.add(requestCall);
        return requestCall;
    }
}
