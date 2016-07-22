package com.callba.phone.activity.contact;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.callba.R;
import com.callba.phone.BaseFragment;
import com.callba.phone.activity.SelectDialPopupWindow;
import com.callba.phone.adapter.CalllogAdapter;
import com.callba.phone.adapter.RecyclerArrayAdapter;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.CalldaCalllogBean;
import com.callba.phone.bean.ContactData;
import com.callba.phone.logic.contact.ContactEntity;
import com.callba.phone.service.CalllogService;
import com.callba.phone.util.Logger;
import com.callba.phone.widget.DividerItemDecoration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Administrator on 2016/7/6.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.fragment_calllog
)
public class CalllogFragment extends BaseFragment {
    @InjectView(R.id.calllog_list)
    RecyclerView calllogList;
    ArrayList<CalldaCalllogBean> beans;
    CalllogService calllogService;
    CalllogAdapter calllogAdapter;

    @Override
    protected void initView(View fragmentRootView) {
        ButterKnife.inject(this, fragmentRootView);
        calllogService=new CalllogService(getActivity(), new CalllogService.CalldaCalllogListener() {
            @Override
            public void onQueryCompleted(final List<CalldaCalllogBean> calldaCalllogBeans) {
                if(calldaCalllogBeans.size()>0)
                {  List<String> numbers=((ContactMutliNumBean)getArguments().get("contact")).getContactPhones();
                    ArrayList<CalldaCalllogBean> beans=new ArrayList<>();
                    for(CalldaCalllogBean bean:calldaCalllogBeans){
                         if(numbers.contains(bean.getCallLogNumber()))
                             beans.add(bean);
                    }
                calllogAdapter=new CalllogAdapter(getActivity());
                calllogAdapter.addAll(sortByDate(beans));
                calllogList.setAdapter(calllogAdapter);
                calllogList.addItemDecoration(new DividerItemDecoration(
                        getActivity(), DividerItemDecoration.VERTICAL_LIST));
                calllogAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        Intent intent=new Intent(getActivity(), SelectDialPopupWindow.class);
                        intent.putExtra("name",calllogAdapter.getData().get(position).getDisplayName());
                        intent.putExtra("number",calllogAdapter.getData().get(position).getCallLogNumber());
                        startActivity(intent);
                    }
                });
                }
            }

            @Override
            public void onDeleteCompleted() {

            }
        });
     calllogService.startQueryCallLog();
    }

    @Override
    protected void lazyLoad() {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
    public ArrayList<CalldaCalllogBean> sortByDate(ArrayList<CalldaCalllogBean> beans){
        ArrayList<CalldaCalllogBean> calllogBeens=new ArrayList<>();
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyyMMdd");
        String date="";
        for(CalldaCalllogBean bean:beans){
            if(date.equals("")||!dateFormat.format(new Date(bean.getCallLogTime())).equals(date)){
                CalldaCalllogBean calllogBean=new CalldaCalllogBean();
                calllogBean.setIndex(ContactEntity.CONTACT_TYPE_INDEX);
                calllogBean.setCallLogTime(bean.getCallLogTime());
                calllogBeens.add(calllogBean);
                calllogBeens.add(bean);
                date=dateFormat.format(new Date(bean.getCallLogTime()));
            }else
               calllogBeens.add(bean);


        }
        return calllogBeens;
    }
}
