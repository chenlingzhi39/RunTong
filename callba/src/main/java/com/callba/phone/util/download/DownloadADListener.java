package com.callba.phone.util.download;

public interface DownloadADListener {
	void onSuccess(String result);
	void onFailed(Object... params);
}
