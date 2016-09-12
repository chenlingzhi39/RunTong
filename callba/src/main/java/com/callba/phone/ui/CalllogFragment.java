package com.callba.phone.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.CalldaCalllogBean;
import com.callba.phone.bean.ContactMutliNumBean;
import com.callba.phone.logic.contact.ContactEntity;
import com.callba.phone.service.CalllogService;
import com.callba.phone.ui.adapter.CalllogAdapter;
import com.callba.phone.ui.adapter.RecyclerArrayAdapter;
import com.callba.phone.ui.base.BaseFragment;
import com.callba.phone.util.ScrimUtil;
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
    @InjectView(R.id.text)
    TextView text;
    @InjectView(R.id.shadow)
    View shadow;

    @Override
    protected void initView(View fragmentRootView) {
        ButterKnife.inject(this, fragmentRootView);
        shadow.setBackground(
                ScrimUtil.makeCubicGradientScrimDrawable(
                        Color.parseColor("#55000000"), //颜色
                        8, //渐变层数
                        Gravity.TOP));
        calllogService = new CalllogService(getActivity(), new CalllogService.CalldaCalllogListener() {
            @Override
            public void onQueryCompleted(final List<CalldaCalllogBean> calldaCalllogBeans) {
                if (calldaCalllogBeans.size() > 0) {
                    List<String> numbers = ((ContactMutliNumBean) getArguments().get("contact")).getContactPhones();
                    ArrayList<CalldaCalllogBean> beans = new ArrayList<>();
                    for (CalldaCalllogBean bean : calldaCalllogBeans) {
                        if (numbers.contains(bean.getCallLogNumber()))
                            beans.add(bean);
                    }
                    calllogAdapter = new CalllogAdapter(getActivity());
                    calllogAdapter.addAll(sortByDate(beans));
                    calllogList.setAdapter(calllogAdapter);
                    calllogList.addItemDecoration(new DividerItemDecoration(
                            getActivity(), DividerItemDecoration.VERTICAL_LIST));
                    calllogAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position) {
                            Intent intent = new Intent(getActivity(), SelectDialPopupWindow.class);
                            intent.putExtra("name", calllogAdapter.getData().get(position).getDisplayName());
                            intent.putExtra("number", calllogAdapter.getData().get(position).getCallLogNumber());
                            startActivity(intent);
                        }
                    });
                    if (beans.size() == 0) text.setVisibility(View.VISIBLE);
                } else {

                }
            }

            @Override
            public void onDeleteCompleted() {

            }
        });
        calllogService.startQueryCallLog(false);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    public ArrayList<CalldaCalllogBean> sortByDate(ArrayList<CalldaCalllogBean> beans) {
        ArrayList<CalldaCalllogBean> calllogBeens = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String date = "";
        for (CalldaCalllogBean bean : beans) {
            if (date.equals("") || !dateFormat.format(new Date(bean.getCallLogTime())).equals(date)) {
                CalldaCalllogBean calllogBean = new CalldaCalllogBean();
                calllogBean.setIndex(ContactEntity.CONTACT_TYPE_INDEX);
                calllogBean.setCallLogTime(bean.getCallLogTime());
                calllogBeens.add(calllogBean);
                calllogBeens.add(bean);
                date = dateFormat.format(new Date(bean.getCallLogTime()));
            } else
                calllogBeens.add(bean);


        }
        return calllogBeens;
    }

}
