package com.callba.phone.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.Constant;
import com.callba.phone.adapter.ConversationAdapter;
import com.callba.phone.adapter.RecyclerArrayAdapter;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.EaseUser;
import com.callba.phone.db.InviteMessgeDao;
import com.callba.phone.logic.contact.ContactPersonEntity;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.ContactsAccessPublic;
import com.callba.phone.util.Logger;
import com.callba.phone.widget.DividerItemDecoration;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.util.EMLog;
import com.umeng.fb.model.Conversation;

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
        toolbarTitle = R.string.message,
        menuId = R.menu.menu_message
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
    private ConversationAdapter adapter;
    private ChatReceiver chatReceiver;
    private AsReadReceiver asReadReceiver;
    private int index = -1;
    private BroadcastReceiver broadcastReceiver;
    private LocalBroadcastManager broadcastManager;
   private  List<EMConversation> copyList;
    protected InputMethodManager inputMethodManager;
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

    @Override
    public void refresh(Object... params) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
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
                Intent intent = new Intent(MessageActivity.this, ChatActivity.class);
                intent.putExtra("username", conversationList.get(position).getUserName());
                startActivity(intent);
                adapter.notifyItemChanged(position);
            }
        });
        adapter.setOnItemLongClickListener(new RecyclerArrayAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemClick(int position) {
                showDeleteDialog(MessageActivity.this,adapter.getData().get(position));
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

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
               /* Intent intent = new Intent(MessageActivity.this, PostMessageActivity.class);
                startActivity(intent);*/
                Intent intent=new Intent(MessageActivity.this,SettingsActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
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
        copyList=list;
        return list;
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(chatReceiver);
        unregisterReceiver(asReadReceiver);
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

    class ChatReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            conversationList.clear();
            conversationList.addAll(loadConversationList());
            adapter.clear();
            adapter.addAll(conversationList);
        }
    }

    class AsReadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            conversationList.clear();
            conversationList.addAll(loadConversationList());
            adapter.clear();
            adapter.addAll(conversationList);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        conversationList.clear();
        conversationList.addAll(loadConversationList());
        adapter.clear();
        adapter.addAll(conversationList);
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



    public class MyFilter extends Filter{
        List<EMConversation> mOriginalList;
        public MyFilter(List<EMConversation> messages) {
        this.mOriginalList=messages;
        }

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();
            if(mOriginalList==null){
                mOriginalList = new ArrayList<EMConversation>();
            }


            if(prefix==null || prefix.length()==0){
                results.values = copyList;
                results.count = copyList.size();
            }else{
                String prefixString = prefix.toString();
                final int count = mOriginalList.size();
                final ArrayList<EMConversation> newValues = new ArrayList<EMConversation>();
                for(int i=0;i<count;i++){
                    final EMConversation conversation= mOriginalList.get(i);
//                    String username = user.getNick();
//                    if(username == null)
//                        username = user.getNick();
              String username=conversation.getUserName();
                    if(username.startsWith(prefixString)){
                        newValues.add(conversation);
                    }
                    else{
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
                results.values=newValues;
                results.count=newValues.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.clear();
            adapter.addAll((ArrayList<EMConversation>)results.values);
        }
    }
    protected void hideSoftKeyboard() {
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    private void showDeleteDialog(Context context,
                                  final EMConversation entity) {

        final DialogHelper helper = new DialogHelper(entity);
        Dialog dialog = new AlertDialog.Builder(this).setView(helper.getView()).create();
        helper.setDialog(dialog);
        dialog.show();
    }
    class DialogHelper implements DialogInterface.OnDismissListener,View.OnClickListener {
        private Dialog mDialog;
        private View view;
        TextView tv_name;
        Button delete_conversation;
        Button delete_conversation_message;
        EMConversation entity;
        public DialogHelper(EMConversation entity) {
            this.entity=entity;
            view=getLayoutInflater().inflate(R.layout.dialog_conversation,null);
            tv_name=(TextView)view.findViewById(R.id.name);
            delete_conversation=(Button)view.findViewById(R.id.delete_conversation);
            delete_conversation.setOnClickListener(this);
            delete_conversation_message=(Button)view.findViewById(R.id.delete_conversation_messages);
            delete_conversation_message.setOnClickListener(this);
            tv_name.setText(entity.getUserName());
        }

        @Override
        public void onClick(View v) {
            mDialog.dismiss();
            switch (v.getId()){
                case R.id.delete_conversation:
                    EMClient.getInstance().chatManager().deleteConversation(entity.getUserName(),false);
                    adapter.remove(entity);
                    break;
                case R.id.delete_conversation_messages:
                    EMClient.getInstance().chatManager().deleteConversation(entity.getUserName(),true);
                    adapter.remove(entity);
                    break;

            }
        }

        public View getView() {
            return view;
        }

        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            mDialog=null;
        }
    }

}
