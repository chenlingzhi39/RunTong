package com.callba.phone.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps2d.model.Text;
import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.Constant;
import com.callba.phone.adapter.ConversationAdapter;
import com.callba.phone.adapter.RecyclerArrayAdapter;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.Logger;
import com.callba.phone.util.SharedPreferenceUtil;
import com.callba.phone.util.SimpleHandler;
import com.callba.phone.widget.DividerItemDecoration;
import com.hyphenate.EMCallBack;
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
import butterknife.OnClick;

/**
 * Created by Administrator on 2016/5/15.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.tab_message,
        toolbarTitle = R.string.message
)

public class MessageActivity extends BaseActivity {
    @InjectView(R.id.conversation_list)
    RecyclerView conversationListview;
    //EaseConversationList conversationListView;
    protected List<EMConversation> conversationList = new ArrayList<>();
    @InjectView(R.id.query)
    EditText query;
    @InjectView(R.id.search_clear)
    ImageButton clearSearch;
    @InjectView(R.id.refresh)
    SwipeRefreshLayout refresh;
    private ConversationAdapter adapter;
    private ChatReceiver chatReceiver;
    private AsReadReceiver asReadReceiver;
    private int index = -1;
    private BroadcastReceiver broadcastReceiver;
    private LocalBroadcastManager broadcastManager;
    private List<EMConversation> copyList;
    protected InputMethodManager inputMethodManager;
    protected FrameLayout errorItemContainer;
    protected TextView errorText;
    @OnClick(R.id.search_clear)
    public void onClick() {
        query.getText().clear();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(query.getWindowToken(), 0);
    }

    public interface EaseConversationListItemClickListener {
        /**
         * 会话listview item点击事件
         *
         * @param conversation 被点击item所对应的会话
         */
        void onListItemClicked(EMConversation conversation);
    }

    private EaseConversationListItemClickListener listItemClickListener;
    protected Handler handler = new Handler(){
        public void handleMessage(android.os.Message msg) {
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
    protected void onConnectionConnected(){
        errorItemContainer.setVisibility(View.GONE);
        refresh();
    }

    /**
     * 连接断开
     */
    protected void onConnectionDisconnected(){
        errorItemContainer.setVisibility(View.VISIBLE);
       /* if(CalldaGlobalConfig.getInstance()!=null)
            if(!CalldaGlobalConfig.getInstance().getUsername().equals("")){
                EMClient.getInstance().login(SharedPreferenceUtil.getInstance(MessageActivity.this).getString(com.callba.phone.cfg.Constant.LOGIN_USERNAME)+"-callba",SharedPreferenceUtil.getInstance(MessageActivity.this).getString(com.callba.phone.cfg.Constant.LOGIN_PASSWORD),new EMCallBack() {//回调
                    @Override
                    public void onSuccess() {

                        EMClient.getInstance().groupManager().loadAllGroups();
                        EMClient.getInstance().chatManager().loadAllConversations();
                        Log.d("main", "登录聊天服务器成功！");
                        refresh();
                        //DemoHelper.getInstance().getUserProfileManager().asyncGetCurrentUserInfo();
                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }

                    @Override
                    public void onError(int code, String message) {
                        Log.d("main", "登录聊天服务器失败！");
                    }
                });
            }*/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        View errorView = (LinearLayout) View.inflate(this,R.layout.em_chat_neterror_item, null);
        errorItemContainer = (FrameLayout) findViewById(R.id.fl_error_item);
        errorText = (TextView) errorView.findViewById(R.id.tv_connect_errormsg);
        errorItemContainer.addView(errorView);
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        String language = Locale.getDefault().getLanguage();
        Logger.i("language", language);
        Locale.setDefault(new Locale("zh"));
        Logger.i("language", Locale.getDefault().getLanguage());
        conversationList.addAll(loadConversationList());
        conversationListview.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL_LIST));
        conversationListview.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ConversationAdapter(this);
        adapter.addAll(conversationList);
        adapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                adapter.getData().get(position).markAllMessagesAsRead();
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
                showDeleteDialog(adapter.getData().get(position));
                return false;
            }
        });
        conversationListview.setAdapter(adapter);
        IntentFilter filter = new IntentFilter(
                "com.callba.chat");
        IntentFilter filter1 = new IntentFilter(
                "com.callba.asread");
        chatReceiver = new ChatReceiver();
        registerReceiver(chatReceiver, filter);
        asReadReceiver = new AsReadReceiver();
        registerReceiver(asReadReceiver, filter1);
        query.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                new MyFilter(copyList).filter(s);
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
                conversationList.clear();
                conversationList.addAll(loadConversationList());
                adapter.clear();
                adapter.addAll(conversationList);
                refresh.setRefreshing(false);
            }
        });
        EMClient.getInstance().addConnectionListener(connectionListener);
        broadcastReceiver= new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
              refresh();
            }
        };
        broadcastManager=LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(broadcastReceiver,new IntentFilter(Constant.ACTION_GROUP_CHANAGED));
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
        unregisterReceiver(asReadReceiver);
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
  public void refresh(){
      new Thread(new Runnable() {
          @Override
          public void run() {
              conversationList.clear();
              conversationList.addAll(loadConversationList());
              SimpleHandler.getInstance().post(new Runnable() {
                  @Override
                  public void run() {
                      adapter.clear();
                      adapter.addAll(conversationList);
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

    class AsReadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            refresh();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                mOriginalList = new ArrayList<EMConversation>();
            }


            if (prefix == null || prefix.length() == 0) {
                results.values = copyList;
                results.count = copyList.size();
            } else {
                String prefixString = prefix.toString();
                final int count = mOriginalList.size();
                final ArrayList<EMConversation> newValues = new ArrayList<EMConversation>();
                for (int i = 0; i < count; i++) {
                    final EMConversation conversation = mOriginalList.get(i);
//                    String username = user.getNick();
//                    if(username == null)
//                        username = user.getNick();
                    String username = conversation.getUserName();
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
            builder.setItems(new String[] { getString(R.string.delete_conversation), getString(R.string.delete_conversation_messages) },
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            switch (which) {
                                case 0:
                                    EMClient.getInstance().chatManager().deleteConversation(entity.getUserName(), false);
                                    adapter.remove(entity);
                                    break;
                                case 1:
                                    EMClient.getInstance().chatManager().deleteConversation(entity.getUserName(), true);
                                    adapter.remove(entity);
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
            builder.create().show();
    }


}
