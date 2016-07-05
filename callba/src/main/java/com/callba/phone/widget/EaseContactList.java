package com.callba.phone.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;


import com.callba.R;
import com.callba.phone.adapter.EaseContactAdapter;
import com.callba.phone.bean.EaseUser;

import java.util.ArrayList;
import java.util.List;

public class EaseContactList extends RelativeLayout {
    protected static final String TAG = EaseContactList.class.getSimpleName();
    
    protected Context context;
    protected ListView listView;
    protected EaseContactAdapter adapter;
    protected List<EaseUser> contactList;
    protected EaseSidebar sidebar;
    
    protected int primaryColor;
    protected int primarySize;
    protected boolean showSiderBar;
    protected Drawable initialLetterBg;
    protected SwipeRefreshLayout swipeRefreshLayout;
    static final int MSG_UPDATE_LIST = 0;
    protected SwipeRefreshLayout.OnRefreshListener refreshListener;
    Handler handler = new Handler() {
        
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_UPDATE_LIST:
                if(adapter != null){
                	adapter.clear();
                	adapter.addAll(new ArrayList<EaseUser>(contactList));
                	adapter.notifyDataSetChanged();	
                }
                break;
            default:
                break;
            }
            super.handleMessage(msg);
        }
    };

    protected int initialLetterColor;

    
    public EaseContactList(Context context) {
        super(context);
        init(context, null);
    }

    public EaseContactList(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    
    public EaseContactList(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    
    private void init(Context context, AttributeSet attrs) {
        this.context = context;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EaseContactList);
        primaryColor = ta.getColor(R.styleable.EaseContactList_ctsListPrimaryTextColor, 0);
        primarySize = ta.getDimensionPixelSize(R.styleable.EaseContactList_ctsListPrimaryTextSize, 0);
        showSiderBar = ta.getBoolean(R.styleable.EaseContactList_ctsListShowSiderBar, true);
        initialLetterBg = ta.getDrawable(R.styleable.EaseContactList_ctsListInitialLetterBg);
        initialLetterColor = ta.getColor(R.styleable.EaseContactList_ctsListInitialLetterColor, 0);
        ta.recycle();
        
        
        LayoutInflater.from(context).inflate(R.layout.ease_widget_contact_list, this);
        listView = (ListView)findViewById(R.id.list);
        sidebar = (EaseSidebar) findViewById(R.id.sidebar);
        swipeRefreshLayout=(SwipeRefreshLayout) findViewById(R.id.refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.orange);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshListener.onRefresh();
            }
        });

        if(!showSiderBar)
            sidebar.setVisibility(View.GONE);
    }

    public void setRefreshListener(SwipeRefreshLayout.OnRefreshListener refreshListener) {
        this.refreshListener = refreshListener;
    }
    public void setRefreshing(boolean isRefreshing){
        swipeRefreshLayout.setRefreshing(isRefreshing);
    }
    /*
         * init view
         */
    public void init(List<EaseUser> contactList){
    	this.contactList = contactList;
        adapter = new EaseContactAdapter(context, 0, new ArrayList<EaseUser>(contactList));
        adapter.setPrimaryColor(primaryColor).setPrimarySize(primarySize).setInitialLetterBg(initialLetterBg)
            .setInitialLetterColor(initialLetterColor);
        listView.setAdapter(adapter);
        
        if(showSiderBar){
            sidebar.setListView(listView);
        }
    }
    
    
    public void refresh(){
        Message msg = handler.obtainMessage(MSG_UPDATE_LIST);
        handler.sendMessage(msg);
    }

    public void filter(CharSequence str) {
        if(adapter!=null)
        adapter.getFilter().filter(str);
    }
    
    public ListView getListView(){
        return listView;
    }
    
    public void setShowSiderBar(boolean showSiderBar){
        if(showSiderBar){
            sidebar.setVisibility(View.VISIBLE);
        }else{
            sidebar.setVisibility(View.GONE);
        }
    }


}
