package com.callba.phone.ui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.callba.R;
import com.callba.phone.Constant;
import com.callba.phone.MyApplication;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.EaseUser;
import com.callba.phone.listener.InputWindowListener;
import com.callba.phone.ui.adapter.ConversationAdapter;
import com.callba.phone.ui.adapter.RecyclerArrayAdapter;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.EaseUserUtils;
import com.callba.phone.util.InitiateSearch;
import com.callba.phone.util.Logger;
import com.callba.phone.util.RxBus;
import com.callba.phone.util.SimpleHandler;
import com.callba.phone.widget.DividerItemDecoration;
import com.callba.phone.widget.IMMListenerRelativeLayout;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/5/15.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.tab_message,
        toolbarTitle = R.string.message,
        menuId = R.menu.message
)

public class MessageActivity extends BaseActivity {
    @InjectView(R.id.conversation_list)
    RecyclerView conversationListview;
    //EaseConversationList conversationListView;
    @InjectView(R.id.refresh)
    SwipeRefreshLayout refresh;
    @InjectView(R.id.view_search)
    IMMListenerRelativeLayout viewSearch;
    @InjectView(R.id.image_search_back)
    ImageView imageSearchBack;
    @InjectView(R.id.edit_text_search)
    EditText editTextSearch;
    @InjectView(R.id.clearSearch)
    ImageView clearSearch;
    @InjectView(R.id.line_divider)
    View lineDivider;
    @InjectView(R.id.card_search)
    CardView cardSearch;
    private ConversationAdapter adapter;
    private ChatReceiver chatReceiver;
    private int index = -1;
    private BroadcastReceiver broadcastReceiver;
    private LocalBroadcastManager broadcastManager;
    private List<EMConversation> copyList;
    protected InputMethodManager inputMethodManager;
    protected FrameLayout errorItemContainer;
    protected TextView errorText;
    private MyFilter filter;
    public interface EaseConversationListItemClickListener {
        /**
         * 会话listview item点击事件
         *
         * @param conversation 被点击item所对应的会话
         */
        void onListItemClicked(EMConversation conversation);
    }

    private EaseConversationListItemClickListener listItemClickListener;
    protected Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    onConnectionDisconnected();
                    break;
                case 1:
                    onConnectionConnected();
                    break;
                default:
                    break;
            }
        }
    };
    protected EMConnectionListener connectionListener = new EMConnectionListener() {

        @Override
        public void onDisconnected(int error) {
            if (error == EMError.USER_REMOVED || error == EMError.USER_LOGIN_ANOTHER_DEVICE) {
                //isConflict = true;
            } else {
                handler.sendEmptyMessage(0);
            }
        }

        @Override
        public void onConnected() {
            handler.sendEmptyMessage(1);
        }
    };

    /**
     * 连接到服务器
     */
    protected void onConnectionConnected() {
        errorItemContainer.setVisibility(View.GONE);
        refresh();
    }

    /**
     * 连接断开
     */
    protected void onConnectionDisconnected() {
        errorItemContainer.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        View errorView = View.inflate(this, R.layout.em_chat_neterror_item, null);
        errorItemContainer = (FrameLayout) findViewById(R.id.fl_error_item);
        errorText = (TextView) errorView.findViewById(R.id.tv_connect_errormsg);
        errorItemContainer.setVisibility(MyApplication.getInstance().detect() ? View.GONE : View.VISIBLE);
        errorItemContainer.addView(errorView);
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        String language = Locale.getDefault().getLanguage();
        Logger.i("language", language);
        Locale.setDefault(new Locale("zh"));
        Logger.i("language", Locale.getDefault().getLanguage());
        conversationListview.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL_LIST));
        conversationListview.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ConversationAdapter(this);
        subscription = Observable.create(new Observable.OnSubscribe<List<EMConversation>>() {
            @Override
            public void call(Subscriber<? super List<EMConversation>> subscriber) {
                List<EMConversation> emConversations = loadConversationList();
                subscriber.onNext(emConversations);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<List<EMConversation>>() {
            @Override
            public void call(List<EMConversation> emConversations) {
                adapter.addAll(emConversations);
                filter=new MyFilter(emConversations);
            }
        });
        adapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                adapter.getItem(position).markAllMessagesAsRead();
                adapter.notifyItemChanged(position);
                EMConversation conversation = adapter.getItem(position);
                String username = conversation.getUserName();
                if (username.equals(EMClient.getInstance().getCurrentUser()))
                    Toast.makeText(MessageActivity.this, R.string.Cant_chat_with_yourself, 0).show();
                else {
                    // 进入聊天页面
                    Intent intent = new Intent(MessageActivity.this, ChatActivity.class);
                    if (conversation.isGroup()) {
                        if (conversation.getType() == EMConversation.EMConversationType.ChatRoom) {
                            // it's group chat
                            intent.putExtra(Constant.EXTRA_CHAT_TYPE, Constant.CHATTYPE_CHATROOM);
                        } else {
                            intent.putExtra(Constant.EXTRA_CHAT_TYPE, Constant.CHATTYPE_GROUP);
                        }

                    }
                    // it's single chat
                    intent.putExtra(Constant.EXTRA_USER_ID, username);
                    startActivity(intent);
                }
            }
        });
        adapter.setOnItemLongClickListener(new RecyclerArrayAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemClick(int position) {
                showDeleteDialog(adapter.getItem(position));
                return false;
            }
        });
        SimpleHandler.getInstance().postDelayed(new Runnable() {
            @Override
            public void run() {
                conversationListview.setAdapter(adapter);
            }
        }, 0);

        IntentFilter filter = new IntentFilter(
                "com.callba.chat");
        chatReceiver = new ChatReceiver();
        registerReceiver(chatReceiver, filter);


        conversationListview.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 隐藏软键盘
                hideSoftKeyboard();
                return false;
            }
        });
        refresh.setColorSchemeResources(R.color.orange);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                subscription = Observable.create(new Observable.OnSubscribe<List<EMConversation>>() {
                    @Override
                    public void call(Subscriber<? super List<EMConversation>> subscriber) {
                        List<EMConversation> emConversations = loadConversationList();
                        subscriber.onNext(emConversations);
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<List<EMConversation>>() {
                    @Override
                    public void call(List<EMConversation> emConversations) {
                        adapter.clear();
                        adapter.addAll(emConversations);
                        refresh.setRefreshing(false);
                    }
                });

            }
        });
        EMClient.getInstance().addConnectionListener(connectionListener);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refresh();
            }
        };
        broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(Constant.ACTION_GROUP_CHANAGED));
        InitiateSearch();
        HandleSearch();
    }
    private void InitiateSearch() {
        viewSearch.setListener(new InputWindowListener() {
            @Override
            public void show() {

            }

            @Override
            public void hide() {
                Log.i("input", "hide");
                if (cardSearch.getVisibility() == View.VISIBLE)
                    InitiateSearch.handleToolBar1(MessageActivity.this, cardSearch, viewSearch, editTextSearch, lineDivider);
            }
        });
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter.filter(s);
                if (editTextSearch.getText().toString().length() == 0) {
                    clearSearch.setVisibility(View.GONE);
                } else {
                    clearSearch.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextSearch.setText("");
                ((InputMethodManager) MessageActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });

    }
    private void HandleSearch() {
        imageSearchBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("search", "back");
                InitiateSearch.handleToolBar(MessageActivity.this, cardSearch, viewSearch, editTextSearch, lineDivider);
            }
        });
        editTextSearch.requestFocus();
    }
    @Override
    public void onNetworkChanged(boolean isAvailable) {
        errorItemContainer.setVisibility(isAvailable ? View.GONE : View.VISIBLE);
    }

    /**
     * 获取会话列表
     *
     * @param context
     * @return +
     */
    protected List<EMConversation> loadConversationList() {

        // 获取所有会话，包括陌生人
        Map<String, EMConversation> conversations = EMClient.getInstance().chatManager().getAllConversations();
        // 过滤掉messages size为0的conversation
        /**
         * 如果在排序过程中有新消息收到，lastMsgTime会发生变化
         * 影响排序过程，Collection.sort会产生异常
         * 保证Conversation在Sort过程中最后一条消息的时间不变
         * 避免并发问题
         */
        List<Pair<Long, EMConversation>> sortList = new ArrayList<Pair<Long, EMConversation>>();
        synchronized (conversations) {
            for (EMConversation conversation : conversations.values()) {
                if (conversation.getAllMessages().size() != 0) {
                    //if(conversation.getType() != EMConversationType.ChatRoom){
                    sortList.add(new Pair<Long, EMConversation>(conversation.getLastMessage().getMsgTime(), conversation));
                    //}
                }
            }
        }
        try {
            // Internal is TimSort algorithm, has bug
            sortConversationByLastChatTime(sortList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<EMConversation> list = new ArrayList<EMConversation>();
        for (Pair<Long, EMConversation> sortItem : sortList) {
            list.add(sortItem.second);
        }
        copyList = list;
        return list;
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(chatReceiver);
        broadcastManager.unregisterReceiver(broadcastReceiver);
        EMClient.getInstance().removeConnectionListener(connectionListener);
        super.onDestroy();
    }

    /**
     * 根据最后一条消息的时间排序
     *
     * @param usernames
     */
    private void sortConversationByLastChatTime(List<Pair<Long, EMConversation>> conversationList) {
        Collections.sort(conversationList, new Comparator<Pair<Long, EMConversation>>() {
            @Override
            public int compare(final Pair<Long, EMConversation> con1, final Pair<Long, EMConversation> con2) {

                if (con1.first == con2.first) {
                    return 0;
                } else if (con2.first > con1.first) {
                    return 1;
                } else {
                    return -1;
                }
            }

        });
    }

    public void refresh() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SimpleHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        subscription = Observable.create(new Observable.OnSubscribe<List<EMConversation>>() {
                            @Override
                            public void call(Subscriber<? super List<EMConversation>> subscriber) {
                                List<EMConversation> emConversations = loadConversationList();
                                subscriber.onNext(emConversations);
                            }
                        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<List<EMConversation>>() {
                            @Override
                            public void call(List<EMConversation> emConversations) {
                                adapter.clear();
                                adapter.addAll(emConversations);
                                filter=new MyFilter(emConversations);
                                if(!TextUtils.isEmpty(editTextSearch.getText().toString())){
                                    filter.filter(editTextSearch.getText().toString());
                                }
                            }
                        });
                    }
                });

            }
        }).start();

    }

    class ChatReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            refresh();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Locale.setDefault(new Locale("zh"));
        refresh();
    }

    /**
     * 重写onkeyDown 捕捉返回键
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 转到后台运行
            ActivityUtil.moveAllActivityToBack();
            return true;
        }
        return false;
    }


    public class MyFilter extends Filter {
        List<EMConversation> mOriginalList;

        public MyFilter(List<EMConversation> messages) {
            this.mOriginalList = messages;
        }

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();
            if (mOriginalList == null) {
                mOriginalList = new ArrayList<>();
            }


            if (prefix == null || prefix.length() == 0) {
                results.values = mOriginalList;
                results.count = mOriginalList.size();
            } else {
                String prefixString = prefix.toString();
                final int count = mOriginalList.size();
                final ArrayList<EMConversation> newValues = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    final EMConversation conversation = mOriginalList.get(i);
//                    String username = user.getNick();
//                    if(username == null)
//                        username = user.getNick();
                    String username = conversation.getUserName();
                    if (conversation.getType() == EMConversation.EMConversationType.GroupChat)
                        username = EMClient.getInstance().groupManager().getGroup(conversation.getUserName()).getGroupName();
                    else if (conversation.getType() == EMConversation.EMConversationType.Chat) {
                        EaseUser user = EaseUserUtils.getUserInfo(conversation.getUserName());
                        if (user != null) {
                            if (!TextUtils.isEmpty(user.getNick()))
                                username = user.getNick();
                            if (!TextUtils.isEmpty(user.getRemark()))
                                username = user.getRemark();
                        }
                        if (conversation.getUserName().equals("admin"))
                            username = "系统消息";
                    }
                    if (username.startsWith(prefixString)) {
                        newValues.add(conversation);
                    } else {
                        final String[] words = username.split(" ");
                        final int wordCount = words.length;

                        // Start at index 0, in case valueText starts with space(s)
                        for (int k = 0; k < wordCount; k++) {
                            if (words[k].startsWith(prefixString)) {
                                newValues.add(conversation);
                                break;
                            }
                        }
                    }
                }
                results.values = newValues;
                results.count = newValues.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.clear();
            adapter.addAll((ArrayList<EMConversation>) results.values);
        }
    }

    protected void hideSoftKeyboard() {
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void showDeleteDialog(final EMConversation entity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(new String[]{getString(R.string.delete_conversation), getString(R.string.delete_conversation_messages)},
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                EMClient.getInstance().chatManager().deleteConversation(entity.getUserName(), false);
                                sendBroadcast(new Intent("message_num"));
                                adapter.remove(entity);
                                break;
                            case 1:
                                EMClient.getInstance().chatManager().deleteConversation(entity.getUserName(), true);
                                sendBroadcast(new Intent("message_num"));
                                adapter.remove(entity);
                                break;
                            default:
                                break;
                        }
                    }
                });
        builder.create().show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                InitiateSearch.handleToolBar(MessageActivity.this, cardSearch, viewSearch, editTextSearch, lineDivider);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
