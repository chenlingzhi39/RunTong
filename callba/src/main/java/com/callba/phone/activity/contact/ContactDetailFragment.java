package com.callba.phone.activity.contact;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.callba.R;
import com.callba.phone.BaseFragment;
import com.callba.phone.activity.SelectDialPopupWindow;
import com.callba.phone.adapter.ContactNumberAdapter;
import com.callba.phone.adapter.RecyclerArrayAdapter;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.CalldaCalllogBean;
import com.callba.phone.bean.CalllogDetailBean;
import com.callba.phone.util.CallUtils;
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
    CallUtils callUtils;
    @Override
    protected void initView(View fragmentRootView) {
        ButterKnife.inject(this, fragmentRootView);
        bean=(ContactMutliNumBean) getArguments().get("contact");
        lvPhoneNums.setLayoutManager(new LinearLayoutManager(getActivity()));
        setDatatoAdapter();
    }

    @Override
    protected void lazyLoad() {

    }

    private void setDatatoAdapter() {
            List<String> phoneNums = bean.getContactPhones();
            Logger.i("number",phoneNums.size()+"");
            contactNumberAdapter=new ContactNumberAdapter(getActivity());
            contactNumberAdapter.addAll(phoneNums);
            lvPhoneNums.setAdapter(contactNumberAdapter);
            callUtils=new CallUtils();
            contactNumberAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    String phoneNum = contactNumberAdapter.getData().get(position);
                    Intent intent=new Intent(getActivity(), SelectDialPopupWindow.class);
                    intent.putExtra("name",bean.getDisplayName());
                    intent.putExtra("number",phoneNum);
                    startActivity(intent);
                    //callUtils.judgeCallMode(getActivity(), phoneNum,bean.getDisplayName());
                }
            });
            // bean为空时，设置联系人编辑不可用

        }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
