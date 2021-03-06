package com.callba.phone.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;

import com.callba.R;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.util.Interfaces;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by PC-20160514 on 2016/6/11.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.help,
        navigationId = R.drawable.press_back,
        toolbarTitle = R.string.help
)
public class HelpActivity extends BaseActivity {
    @BindView(R.id.list)
    ExpandableListView list;
    @BindView(R.id.webView)
    WebView mWebView;
    @BindView(R.id.pb_loading_precent)
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
      /*  helpListAdapter=new HelpListAdapter(this);
        list.setGroupIndicator(null);
        list.setAdapter(helpListAdapter);
        list.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                for (int i = 0; i < helpListAdapter.getGroupCount(); i++) {
                    if (groupPosition != i) {
                        list.collapseGroup(i);
                    }
                }
            }
        });*/
//		mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setLoadsImagesAutomatically(true);
        mWebView.setWebViewClient(new WebViewClient() {
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

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                mProgressBar.setProgress(newProgress);
                super.onProgressChanged(view, newProgress);
            }
        });
        mWebView.loadUrl(Interfaces.HELP_CENTER);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
