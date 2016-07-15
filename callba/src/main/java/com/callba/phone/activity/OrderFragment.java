package com.callba.phone.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseFragment;
import com.callba.phone.adapter.OrderAdapter;
import com.callba.phone.adapter.RecyclerArrayAdapter;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Order;
import com.callba.phone.widget.DividerItemDecoration;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PC-20160514 on 2016/7/15.
 */
@ActivityFragmentInject(contentViewId = R.layout.fragment_order)
public class OrderFragment extends BaseFragment {
    @InjectView(R.id.list)
    RecyclerView orderList;
    @InjectView(R.id.hint)
    TextView hint;
    private OrderAdapter orderAdapter;
    private ArrayList<Order> orders;
    @Override
    protected void initView(View fragmentRootView) {
        ButterKnife.inject(this, fragmentRootView);
        orders=(ArrayList<Order>)getArguments().getParcelableArrayList("list").get(0);
        orderAdapter=new OrderAdapter(getActivity());
        orderAdapter.addAll(orders);
        orderList.setAdapter(orderAdapter);
        orderList.addItemDecoration(new DividerItemDecoration(
                getActivity(), DividerItemDecoration.VERTICAL_LIST));
        if(orders.size()==0){
            hint.setVisibility(View.VISIBLE);
        }
        orderAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

            }
        });
    }

    @Override
    protected void lazyLoad() {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
