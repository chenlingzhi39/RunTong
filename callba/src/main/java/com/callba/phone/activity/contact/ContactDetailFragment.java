package com.callba.phone.activity.contact;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.callba.R;
import com.callba.phone.BaseFragment;
import com.callba.phone.adapter.ContactNumberAdapter;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.CalldaCalllogBean;
import com.callba.phone.bean.CalllogDetailBean;
import com.callba.phone.util.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PC-20160514 on 2016/7/6.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.fragment_contact_detail
)
public class ContactDetailFragment extends BaseFragment {
    @InjectView(R.id.lv_phone_nums)
    RecyclerView lvPhoneNums;
    private ContactMutliNumBean bean;
   private ContactNumberAdapter contactNumberAdapter;
    @Override
    protected void initView(View fragmentRootView) {
        ButterKnife.inject(this, fragmentRootView);
        bean=(ContactMutliNumBean) getArguments().get("contact");
        lvPhoneNums.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    protected void lazyLoad() {
        setDatatoAdapter();
    }

    private void setDatatoAdapter() {
            List<String> phoneNums =new ArrayList<>();
            phoneNums =(ArrayList<String>) bean.getContactPhones();
            Logger.i("number",phoneNums.size()+"");
            contactNumberAdapter=new ContactNumberAdapter(getActivity());
            contactNumberAdapter.addAll(phoneNums);
            lvPhoneNums.setAdapter(contactNumberAdapter);

            // bean为空时，设置联系人编辑不可用

        }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
