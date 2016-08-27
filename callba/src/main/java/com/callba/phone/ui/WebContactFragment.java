package com.callba.phone.ui;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.callba.R;
import com.callba.phone.ui.base.BaseFragment;
import com.callba.phone.Constant;
import com.callba.phone.DemoHelper;
import com.callba.phone.DemoHelper.DataSyncListener;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.BaseUser;
import com.callba.phone.bean.EaseUser;
import com.callba.phone.db.InviteMessgeDao;
import com.callba.phone.db.UserDao;
import com.callba.phone.util.EaseCommonUtils;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.util.SimpleHandler;
import com.callba.phone.widget.ContactItemView;
import com.callba.phone.widget.EaseContactList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.EMLog;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import okhttp3.Call;

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
    ImageButton clearSearch;
    @InjectView(R.id.content_container)
    FrameLayout contentContainer;
    protected ListView listView;
    protected List<EaseUser> contactList;
    Map<String, EaseUser> contactsMap;
    private ContactItemView applicationItem;
    private ContactItemView blackListItem;
    private ContactItemView nearByItem;
    private ContactItemView communityItem;
    protected InputMethodManager inputMethodManager;
    private InviteMessgeDao inviteMessgeDao;
    private BroadcastReceiver broadcastReceiver;
    private LocalBroadcastManager broadcastManager;
    private View loadingView;
    private ContactSyncListener contactSyncListener;
    private BlackListSyncListener blackListSyncListener;
    private ContactInfoSyncListener contactInfoSyncListener;
    protected EaseUser toBeProcessUser;
    protected String toBeProcessUsername;
    private Gson gson;
    private static final String TAG = WebContactFragment.class.getSimpleName();
    private boolean hidden;

    public static WebContactFragment newInstance() {
        WebContactFragment webContactFragment = new WebContactFragment();
        return webContactFragment;
    }


    @Override
    protected void initView(View fragmentRootView) {
        ButterKnife.inject(this, fragmentRootView);
        inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.em_contacts_header, null);
        HeaderItemClickListener clickListener = new HeaderItemClickListener();
        listView = contactListLayout.getListView();
        listView.addHeaderView(headerView);
        applicationItem = (ContactItemView) headerView.findViewById(R.id.application_item);
        applicationItem.setOnClickListener(clickListener);
        blackListItem = (ContactItemView) headerView.findViewById(R.id.black_item);
        blackListItem.setOnClickListener(clickListener);
        nearByItem = (ContactItemView) headerView.findViewById(R.id.nearby_item);
        nearByItem.setOnClickListener(clickListener);
        communityItem = (ContactItemView) headerView.findViewById(R.id.community_item);
        communityItem.setOnClickListener(clickListener);
        headerView.findViewById(R.id.group_item).setOnClickListener(clickListener);
        loadingView = LayoutInflater.from(getActivity()).inflate(R.layout.em_layout_loading_data, null);
        contentContainer.addView(loadingView);
        contactsMap = DemoHelper.getInstance().getContactList();
        contactList = new ArrayList<EaseUser>();
        query.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                contactListLayout.filter(s);
                if (s.length() > 0) {
                    clearSearch.setVisibility(View.VISIBLE);
                } else {
                    clearSearch.setVisibility(View.INVISIBLE);

                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                query.getText().clear();
                hideSoftKeyboard();
            }
        });

        listView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 隐藏软键盘
                hideSoftKeyboard();
                return false;
            }
        });
        contactSyncListener = new ContactSyncListener();
        DemoHelper.getInstance().addSyncContactListener(contactSyncListener);

        blackListSyncListener = new BlackListSyncListener();
        DemoHelper.getInstance().addSyncBlackListListener(blackListSyncListener);

        contactInfoSyncListener = new ContactInfoSyncListener();
        DemoHelper.getInstance().getUserProfileManager().addSyncContactInfoListener(contactInfoSyncListener);

        if (DemoHelper.getInstance().isContactsSyncedWithServer()) {
            loadingView.setVisibility(View.GONE);
        } else if (DemoHelper.getInstance().isSyncingContactsWithServer()) {
            loadingView.setVisibility(View.VISIBLE);
        }
        registerForContextMenu(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                EaseUser user = (EaseUser) listView.getItemAtPosition(position);
                intent.putExtra(Constant.EXTRA_USER_ID, user.getUsername());
                startActivity(intent);
            }
        });
        //refresh();
        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                query.getText().clear();
                hideSoftKeyboard();
            }
        });

        listView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 隐藏软键盘
                hideSoftKeyboard();
                return false;
            }
        });
        gson = new Gson();
        contactListLayout.setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                OkHttpUtils
                        .post()
                        .url(Interfaces.GET_FRIENDS)
                        .addParams("loginName", getUsername())
                        .addParams("loginPwd", getPassword())
                        .build().execute(new StringCallback() {
                    @Override
                    public void onAfter(int id) {
                        contactListLayout.setRefreshing(false);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        toast(R.string.network_error);
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        try { Logger.i("get_result", response);
                            String[] result = response.split("\\|");
                            if (result[0].equals("0")) {
                                ArrayList<BaseUser> list;
                                list = gson.fromJson(result[1], new TypeToken<ArrayList<BaseUser>>() {
                                }.getType());
                                List<EaseUser> mList = new ArrayList<EaseUser>();
                                for (BaseUser baseUser : list) {
                                    EaseUser user = new EaseUser(baseUser.getPhoneNumber() + "-callba");
                                    user.setAvatar(baseUser.getUrl_head());
                                    user.setNick(baseUser.getNickname());
                                    user.setSign(baseUser.getSign());
                                    user.setRemark(baseUser.getRemark());
                                    EaseCommonUtils.setUserInitialLetter(user);
                                    mList.add(user);
                                }
                                DemoHelper.getInstance().updateContactList(mList);
                                refresh();
                            } else {
                                toast(result[1]);
                            }
                        }catch(Exception e){
                            toast(R.string.getserverdata_exception);
                        }
                    }
                });
            }
        });

    }

    @Override
    protected void lazyLoad() {
        contactListLayout.init(contactList);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        toBeProcessUser = (EaseUser) listView.getItemAtPosition(((AdapterView.AdapterContextMenuInfo) menuInfo).position);
        toBeProcessUsername = toBeProcessUser.getUsername();
        getActivity().getMenuInflater().inflate(R.menu.em_context_contact_list, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete_contact) {
            OkHttpUtils
                    .post()
                    .url(Interfaces.DELETE_FRIENDS)
                    .addParams("loginName", getUsername())
                    .addParams("loginPwd", getPassword())
                    .addParams("phoneNumber", toBeProcessUser.getUsername().substring(0, 11))
                    .build().execute(new StringCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {
                    toast("删除失败");
                }

                @Override
                public void onResponse(String response, int id) {
                    try {
                        String[] result = response.split("\\|");
                        Logger.i("delete_result", response);
                        if (result[0].equals("0")) {
                            // 删除此联系人
                            deleteContact(toBeProcessUser);
                            // 删除相关的邀请消息
                            InviteMessgeDao dao = new InviteMessgeDao(getActivity());
                            dao.deleteMessage(toBeProcessUser.getUsername());
                        } else {
                            toast("删除失败");
                        }
                    }catch(Exception e){
                        toast(R.string.getserverdata_exception);
                    }
                }
            });


            return true;
        } else if (item.getItemId() == R.id.add_to_blacklist) {
            moveToBlacklist(toBeProcessUsername);
            return true;
        }else if(item.getItemId()==R.id.change_remark){
            Intent intent=new Intent(getActivity(),RemarkActivity.class);
            intent.putExtra("username",toBeProcessUsername);
            startActivity(intent);
        }
        return super.onContextItemSelected(item);
    }

    public void refresh() {
        Map<String, EaseUser> m = DemoHelper.getInstance().getContactList();
        if (m instanceof Hashtable<?, ?>) {
            m = (Map<String, EaseUser>) ((Hashtable<String, EaseUser>) m).clone();
        }
        setContactsMap(m);
        if (inviteMessgeDao == null) {
            inviteMessgeDao = new InviteMessgeDao(getActivity());
        }
        if (inviteMessgeDao.getUnreadMessagesCount() > 0) {
            applicationItem.showUnreadMsgView();
        } else {
            applicationItem.hideUnreadMsgView();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                getContactList();
                SimpleHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        if (contactListLayout != null)
                            contactListLayout.refresh();
                    }
                });
            }
        }).start();

    }

    /**
     * 获取联系人列表，并过滤掉黑名单和排序
     */
    protected void getContactList() {
        contactList.clear();
        //获取联系人列表
        if (contactsMap == null) {
            return;
        }
        synchronized (this.contactsMap) {
            final Iterator<Map.Entry<String, EaseUser>> iterator = contactsMap.entrySet().iterator();
            final List<String> blackList = EMClient.getInstance().contactManager().getBlackListUsernames();

            while (iterator.hasNext()) {
                Map.Entry<String, EaseUser> entry = iterator.next();
                //兼容以前的通讯录里的已有的数据显示，加上此判断，如果是新集成的可以去掉此判断
                if (!entry.getKey().equals("item_new_friends")
                        && !entry.getKey().equals("item_groups")
                        && !entry.getKey().equals("item_chatroom")
                        && !entry.getKey().equals("item_robots")) {
                    if (!blackList.contains(entry.getKey())) {
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
                if (lhs.getInitialLetter().equals(rhs.getInitialLetter())) {
                    return lhs.getNick().compareTo(rhs.getNick());
                } else {
                    if ("#".equals(lhs.getInitialLetter())) {
                        return 1;
                    } else if ("#".equals(rhs.getInitialLetter())) {
                        return -1;
                    }
                    return lhs.getInitialLetter().compareTo(rhs.getInitialLetter());
                }

            }
        });


    }

    protected class HeaderItemClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.application_item:
                    // 进入申请与通知页面
                    startActivity(new Intent(getActivity(), NewFriendsMsgActivity.class));
                    break;
                case R.id.black_item:
                    startActivity(new Intent(getActivity(), BlacklistActivity.class));
                    break;
                case R.id.nearby_item:
                    startActivity(new Intent(getActivity(), FriendActivity.class));
                    break;
                case R.id.group_item:
                    // 进入群聊列表页面
                    startActivity(new Intent(getActivity(), GroupsActivity.class));
                    break;
                case R.id.community_item:
                    toast("暂未开放");
                    //startActivity(new Intent(getActivity(), CommunityActivity.class));
                    break;
               /* case R.id.chat_room_item:
                    //进入聊天室列表页面
                    //startActivity(new Intent(getActivity(), PublicChatRoomsActivity.class));
                    break;
                case R.id.robot_item:
                    //进入Robot列表页面
                    //startActivity(new Intent(getActivity(), RobotsActivity.class));
                    break;*/

                default:
                    break;
            }
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //unregisterBroadcastReceiver();
        unregisterForContextMenu(listView);
        ButterKnife.reset(this);
    }

    /**
     * 设置需要显示的数据map，key为环信用户id
     *
     * @param contactsMap
     */
    public void setContactsMap(Map<String, EaseUser> contactsMap) {
        this.contactsMap = contactsMap;
    }

    protected void hideSoftKeyboard() {
        if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getActivity().getCurrentFocus() != null)
                inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void registerBroadcastReceiver() {
        broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter(Constant.ACTION_CONTACT_CHANAGED);
        broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                Logger.i("webContact", Constant.ACTION_CONTACT_CHANAGED);
                refresh();

            }
        };
        broadcastManager.registerReceiver(broadcastReceiver, intentFilter);
    }

    private void unregisterBroadcastReceiver() {
        broadcastManager.unregisterReceiver(broadcastReceiver);
    }

    class ContactSyncListener implements DataSyncListener {
        @Override
        public void onSyncComplete(final boolean success) {
            EMLog.d(TAG, "on contact list sync success:" + success);

            if (success) {
                SimpleHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        loadingView.setVisibility(View.GONE);
                    }
                });

                refresh();
            } else {
                String s1 = getActivity().getResources().getString(R.string.get_failed_please_check);
                Toast.makeText(getActivity(), s1, 1).show();
                SimpleHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        loadingView.setVisibility(View.GONE);
                    }
                });
            }

        }
    }

    class BlackListSyncListener implements DataSyncListener {

        @Override
        public void onSyncComplete(boolean success) {
            EMLog.d(TAG, "on black list sync success:" + success);
            refresh();
        }

    }

    class ContactInfoSyncListener implements DataSyncListener {

        @Override
        public void onSyncComplete(final boolean success) {
            EMLog.d(TAG, "on contactinfo list sync success:" + success);
            refresh();

        }
    }

    /**
     * 把user移入到黑名单
     */
    protected void moveToBlacklist(final String username) {
        final ProgressDialog pd = new ProgressDialog(getActivity());
        String st1 = getResources().getString(R.string.Is_moved_into_blacklist);
        final String st2 = getResources().getString(R.string.Move_into_blacklist_success);
        final String st3 = getResources().getString(R.string.Move_into_blacklist_failure);
        pd.setMessage(st1);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        new Thread(new Runnable() {
            public void run() {
                try {
                    //加入到黑名单
                    EMClient.getInstance().contactManager().addUserToBlackList(username, false);
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                            Toast.makeText(getActivity(), st2, 0).show();
                            refresh();
                        }
                    });
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                            Toast.makeText(getActivity(), st3, 0).show();
                        }
                    });
                }
            }
        }).start();

    }

    /**
     * 删除联系人
     *
     * @param toDeleteUser
     */
    public void deleteContact(final EaseUser tobeDeleteUser) {
        String st1 = getResources().getString(R.string.deleting);
        final String st2 = getResources().getString(R.string.Delete_failed);
        final ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setMessage(st1);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        new Thread(new Runnable() {
            public void run() {
                try {
                    EMClient.getInstance().contactManager().deleteContact(tobeDeleteUser.getUsername());
                    // 删除db和内存中此用户的数据
                    UserDao dao = new UserDao(getActivity());
                    dao.deleteContact(tobeDeleteUser.getUsername());
                    DemoHelper.getInstance().getContactList().remove(tobeDeleteUser.getUsername());
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                            contactList.remove(tobeDeleteUser);
                            contactListLayout.refresh();

                        }
                    });
                } catch (final Exception e) {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                            Toast.makeText(getActivity(), st2 + e.getMessage(), 1).show();
                        }
                    });

                }

            }
        }).start();

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        this.hidden = hidden;
        if (!hidden) {
            refresh();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!hidden) {
            refresh();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (contactSyncListener != null) {
            DemoHelper.getInstance().removeSyncContactListener(contactSyncListener);
            contactSyncListener = null;
        }

        if (blackListSyncListener != null) {
            DemoHelper.getInstance().removeSyncBlackListListener(blackListSyncListener);
        }

        if (contactInfoSyncListener != null) {
            DemoHelper.getInstance().getUserProfileManager().removeSyncContactInfoListener(contactInfoSyncListener);
        }
    }
}
