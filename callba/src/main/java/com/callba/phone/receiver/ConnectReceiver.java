package com.callba.phone.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.callba.phone.util.RxBus;

/**
 * Created by PC-20160514 on 2016/10/11.
 */

public class ConnectReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = manager.getActiveNetworkInfo();
        RxBus.get().post("has_network",activeInfo != null&&activeInfo.isAvailable());
        if(activeInfo == null||!activeInfo.isAvailable())
            Toast.makeText(context,"请检查您的网络",Toast.LENGTH_SHORT).show();
    }
}
