package com.callba.phone.activity.more;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.Interfaces;


/**
 * 最新优惠
 * @author zhanghw
 * @version 创建时间：2013-10-8 上午11:30:44
 */
public class PreferentialActivity extends BaseActivity {
	private Button bn_back;
	private WebView mWebView;
	private ProgressBar mProgressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.more_preferential);

		bn_back = (Button) findViewById(R.id.bn_preferential_back);
		bn_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		mProgressBar = (ProgressBar) findViewById(R.id.pb_loading_precent);

		mWebView = (WebView) findViewById(R.id.webview_help_info);
//		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setLoadsImagesAutomatically(true);
		mWebView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return super.shouldOverrideUrlLoading(view, url);
			}
			
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				mProgressBar.setVisibility(View.VISIBLE);
				super.onPageStarted(view, url, favicon);
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				mProgressBar.setVisibility(View.GONE);
				super.onPageFinished(view, url);
			}
		});
		
		mWebView.setWebChromeClient(new WebChromeClient(){
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				mProgressBar.setProgress(newProgress);
				super.onProgressChanged(view, newProgress);
			}
		});
		ActivityUtil activityUtil=new ActivityUtil();
		String lan=activityUtil.language(PreferentialActivity.this);
		mWebView.loadUrl(Interfaces.Preferential_Push + "?showType=web&lan="+lan);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
			mWebView.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}


	@Override
	public void refresh(Object... params) {
	}
}
