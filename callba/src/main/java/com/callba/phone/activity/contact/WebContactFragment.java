package com.callba.phone.activity.contact;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;

import com.callba.R;
import com.callba.phone.BaseFragment;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.EaseUser;
import com.callba.phone.util.EaseCommonUtils;
import com.callba.phone.widget.EaseContactList;
import com.hyphenate.chat.EMClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PC-20160514 on 2016/6/21.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.web_contact
)
public class WebContactFragment extends BaseFragment {
    @InjectView(R.id.contact_list)
    EaseContactList contactListLayout;
    @InjectView(R.id.query)
    EditText query;
    @InjectView(R.id.search_clear)
    ImageButton searchClear;
    @InjectView(R.id.content_container)
    FrameLayout contentContainer;
    protected ListView listView;
    private List<String> usernames;
    protected List<EaseUser> contactList;
    Map<String, EaseUser> contactsMap;
    public static WebContactFragment newInstance(){
        WebContactFragment webContactFragment=new WebContactFragment();
        return webContactFragment;
    }
    @Override
    protected void initView(View fragmentRootView) {
        ButterKnife.inject(this, fragmentRootView);
        listView=contactListLayout.getListView();

        try{
        usernames = EMClient.getInstance().contactManager().getAllContactsFromServer();
            for (String username : usernames) {
                EaseUser user = new EaseUser(username);
                EaseCommonUtils.setUserInitialLetter(user);
                contactsMap.put(username, user);
            }}

        catch(Exception e){

        }
        contactList = new ArrayList<EaseUser>();
        getContactList();
        contactListLayout.init(contactList);
    }
    /**
     * 获取联系人列表，并过滤掉黑名单和排序
     */
    protected void getContactList() {
        contactList.clear();
        //获取联系人列表
        if(contactsMap == null){
            return;
        }
        synchronized (this.contactsMap) {
            Iterator<Map.Entry<String, EaseUser>> iterator = contactsMap.entrySet().iterator();
            List<String> blackList = EMClient.getInstance().contactManager().getBlackListUsernames();
            while (iterator.hasNext()) {
                Map.Entry<String, EaseUser> entry = iterator.next();
                //兼容以前的通讯录里的已有的数据显示，加上此判断，如果是新集成的可以去掉此判断
                if (!entry.getKey().equals("item_new_friends")
                        && !entry.getKey().equals("item_groups")
                        && !entry.getKey().equals("item_chatroom")
                        && !entry.getKey().equals("item_robots")){
                    if(!blackList.contains(entry.getKey())){
                        //不显示黑名单中的用户
                        EaseUser user = entry.getValue();
                        EaseCommonUtils.setUserInitialLetter(user);
                        contactList.add(user);
                    }
                }
            }
        }

        // 排序
        Collections.sort(contactList, new Comparator<EaseUser>() {

            @Override
            public int compare(EaseUser lhs, EaseUser rhs) {
                if(lhs.getInitialLetter().equals(rhs.getInitialLetter())){
                    return lhs.getNick().compareTo(rhs.getNick());
                }else{
                    if("#".equals(lhs.getInitialLetter())){
                        return 1;
                    }else if("#".equals(rhs.getInitialLetter())){
                        return -1;
                    }
                    return lhs.getInitialLetter().compareTo(rhs.getInitialLetter());
                }

            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }


}
