package com.callba.phone.adapter;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.callba.phone.util.SimpleHandler;
import com.hyphenate.chat.EMMessage;

/**
 * Created by PC-20160514 on 2016/5/30.
 */
public class BaseChatViewHolder extends BaseViewHolder<EMMessage> {
    public BaseChatViewHolder(ViewGroup parent, @LayoutRes int res) {
        super(parent, res);
    }


    public void updateView(final EMMessage message,final ProgressBar progressBar,final  TextView percentageView,final  ImageView statusView){
        SimpleHandler.getInstance().post(new Runnable() {
            @Override
            public void run() {
                switch (message.status()) {
                    case SUCCESS:
                        progressBar.setVisibility(View.INVISIBLE);
                        if(percentageView != null)
                            percentageView.setVisibility(View.INVISIBLE);
                        if(statusView!=null)
                            statusView.setVisibility(View.INVISIBLE);
                        break;
                    case FAIL:
                        progressBar.setVisibility(View.INVISIBLE);
                        if(percentageView != null)
                            percentageView.setVisibility(View.INVISIBLE);
                        if(statusView!=null)
                            statusView.setVisibility(View.VISIBLE);
                        break;
                    case INPROGRESS:
                        progressBar.setVisibility(View.VISIBLE);
                        if(percentageView != null){
                            percentageView.setVisibility(View.VISIBLE);
                            percentageView.setText(message.progress() + "%");
                        }
                        if(statusView!=null)
                            statusView.setVisibility(View.INVISIBLE);
                        break;
                    default:
                        progressBar.setVisibility(View.INVISIBLE);
                        if(percentageView != null)
                            percentageView.setVisibility(View.INVISIBLE);
                        if(statusView!=null)
                            statusView.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

    }

}
