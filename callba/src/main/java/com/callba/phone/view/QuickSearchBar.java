package com.callba.phone.view;

import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.util.Logger;


/**
 * 类说明	自定义控件，listview快速索引
 * 
 * @author zhwei
 * @version V1.0 创建时间：2013-12-28 下午2:00:19
 */
public class QuickSearchBar extends RelativeLayout implements OnTouchListener {
	private static final String TAG = QuickSearchBar.class.getCanonicalName();
	
	private TextView tvCurrentIndex;
	private ImageView ivIndexBar;
	private ListView mListView;	//用于定位的listview
	
	private Handler mHandler;
	
	private Map<String, Integer> mListSearchMap;
	private int mHeight;	//当前QuickSearchBar高度
	private int mSelectIndex;	//字母索引
	private int mListHeadCount;	

	private String[] letters = new String[] { "搜", "A", "B", "C", "D", "E",
			"F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
			"S", "T", "U", "V", "W", "X", "Y", "Z", "#" };
	
	public QuickSearchBar(Context context) {
		super(context);
		initView(context);
	}

	public QuickSearchBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public QuickSearchBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}
	
	private void initView(Context mContext) {
		mHandler = new Handler();
		
		View view = LayoutInflater.from(mContext).inflate(
				R.layout.quick_searchbar_layout, this);
		tvCurrentIndex = (TextView) view.findViewById(R.id.tv_position);
		ivIndexBar = (ImageView) view.findViewById(R.id.iv_slidebar);

		ivIndexBar.setOnTouchListener(this);
		
		ViewTreeObserver vto2 = this.getViewTreeObserver();
	    vto2.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
	        @SuppressWarnings("deprecation")
			@Override   
	        public void onGlobalLayout() {  
	        	QuickSearchBar.this.getViewTreeObserver().removeGlobalOnLayoutListener(this);
	        	mHeight = QuickSearchBar.this.getHeight();
	        }   
	    });  
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(mListView == null) {
			Logger.w(TAG, "QuickSearchBar onTouch listview is null.");
			return false;
		}
		
		float currentY = event.getY();
		int index = (int) (currentY / (mHeight / letters.length));
		 
		
		//防止越界
		if(index < 0 || index >= letters.length) {
			tvCurrentIndex.setVisibility(View.INVISIBLE);
			return false;
		}
		mSelectIndex=index;
		//might caught IndexOutOfBoundsException
		try {
			if(mListSearchMap.containsKey(letters[mSelectIndex])) {
				int pos = mListSearchMap.get(letters[mSelectIndex]);
//				if(Build.VERSION.SDK_INT < 11) {
					mListView.setSelectionFromTop(mListHeadCount + pos, 0);
//				} else {
//					mListView.smoothScrollToPositionFromTop(mListHeadCount + pos, 0);
//				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						//防止越界
						if(mSelectIndex >= 0 && mSelectIndex < letters.length) {
							tvCurrentIndex.setVisibility(View.VISIBLE);
							tvCurrentIndex.setText(letters[mSelectIndex]);
						}
					}
				});
				
				break;
				
			case MotionEvent.ACTION_MOVE:
				
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						//防止越界
						if(mSelectIndex >= 0 && mSelectIndex < letters.length) {
							tvCurrentIndex.setText(letters[mSelectIndex]);
						}
					}
				});
				
				break;
				
			case MotionEvent.ACTION_UP:
				
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						tvCurrentIndex.setVisibility(View.INVISIBLE);
					}
				});
				
				break;
				
			default:
				break;
		}
		return false;
	}

	/**
	 * 设置定位的listview
	 * @param listView
	 */
	public void setListView(ListView listView) {
		this.mListView = listView;
		mListHeadCount = listView.getHeaderViewsCount();
	}
	
	/**
	 * 目标listview匹配的数据
	 * @param listSearchMap
	 */
	public void setListSearchMap(Map<String, Integer> listSearchMap) {
		this.mListSearchMap = listSearchMap;
	}
}
