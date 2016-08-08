package com.callba.phone.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import com.callba.phone.bean.Task;

import java.io.Serializable;

/**
 * 版本更新检测器
 * 
 * @author zhw
 * @version V1.0
 * @createtime：2014年6月12日 下午5:31:09
 */
public class AppVersionChecker {

	/**
	 * 检查是否存在新版本
	 * @author zhw
	 * 
	 * @param context
	 * @param serverVersion
	 * @return
	 */
	public static boolean detectNewVersion(AppVersionBean appVersionBean) {
		String localVersion = appVersionBean.getLocalVersionCode();
		String serverVersion = appVersionBean.getServerVersionCode();
		
		if(TextUtils.isEmpty(localVersion)
				|| TextUtils.isEmpty(serverVersion)) {
			return false;
		}
		
		try {
			String[] localVersionCodes = localVersion.split("\\.");
			String[] serverVersionCodes = serverVersion.split("\\.");
			
			for(int i=0; i<serverVersionCodes.length; i++) {
				int serverCode = Integer.parseInt(serverVersionCodes[i]);
				int localCode = Integer.parseInt(localVersionCodes[i]);
				
				if(serverCode > localCode) {
					return true;
				} else if(serverCode == localCode){
					continue;
				} else {
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return false;
	}
	
	/**
	 * 解析返回的版本信息
	 * @author zhw
	 *
	 * @param verionMessage
	 * @return
	 */
	public static AppVersionBean parseVersionInfo(Context context, Message verionMessage) {
		AppVersionBean appVersionBean = new AppVersionBean();
		
		if (verionMessage.arg1 == Task.TASK_SUCCESS) {
			Bundle bundle = (Bundle) verionMessage.obj;
			String result = bundle.getString("result");
            Logger.i("result",result);
			if (TextUtils.isEmpty(result)) {
				appVersionBean.setHasNewVersion(false);
			}

			try {
				result = result.replaceAll("\r", "").replaceAll("\t", "").replaceAll("\n", "");
				String[] str = result.split("\\|");
				if ("0".equals(str[0])) {
					
					if (str.length < 5) {
						appVersionBean.setHasNewVersion(false);
					} else {
						String upgradeCode = str[4];
						if("0".equals(upgradeCode)) {
							//强制升级
							appVersionBean.setForceUpgrade(true);
						} else {
							//提示升级
							appVersionBean.setForceUpgrade(false);
						}
						
						appVersionBean.setServerVersionCode(str[1]);
						appVersionBean.setDownloadUrl(str[2]);
						appVersionBean.setSecretKey(str[3]);
						
						PackageManager pm = context.getPackageManager();
						String localVersion = "";
						try {
							PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
							localVersion = packageInfo.versionName;
						} catch (NameNotFoundException e) {
							e.printStackTrace();
						}
						appVersionBean.setLocalVersionCode(localVersion);
						
						//解析新版本
						appVersionBean.setHasNewVersion(detectNewVersion(appVersionBean));
					}
				} else {
					appVersionBean.setHasNewVersion(false);
				}
			} catch (Exception e) {
				appVersionBean.setHasNewVersion(false);
			}
		} else {
			appVersionBean.setHasNewVersion(false);
		}
		
		return appVersionBean;
	}
	public static AppVersionBean parseVersionInfo(Context context, String result) {
		AppVersionBean appVersionBean = new AppVersionBean();
			Logger.i("result",result);
			if (TextUtils.isEmpty(result)) {
				appVersionBean.setHasNewVersion(false);
			}

			try {
				result = result.replaceAll("\r", "").replaceAll("\t", "").replaceAll("\n", "");
				String[] str = result.split("\\|");
				if ("0".equals(str[0])) {

					if (str.length < 5) {
						appVersionBean.setHasNewVersion(false);
					} else {
						String upgradeCode = str[4];
						if("0".equals(upgradeCode)) {
							//强制升级
							appVersionBean.setForceUpgrade(true);
						} else {
							//提示升级
							appVersionBean.setForceUpgrade(false);
						}

						appVersionBean.setServerVersionCode(str[1]);
						appVersionBean.setDownloadUrl(str[2]);
						appVersionBean.setSecretKey(str[3]);

						PackageManager pm = context.getPackageManager();
						String localVersion = "";
						try {
							PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
							localVersion = packageInfo.versionName;
						} catch (NameNotFoundException e) {
							e.printStackTrace();
						}
						appVersionBean.setLocalVersionCode(localVersion);

						//解析新版本
						appVersionBean.setHasNewVersion(detectNewVersion(appVersionBean));
					}
				} else {
					appVersionBean.setHasNewVersion(false);
				}
			} catch (Exception e) {
				appVersionBean.setHasNewVersion(false);
			}
		return appVersionBean;
	}
	/**
	 * 封装解析的版本信息
	 * @author zhw
	 */
	public static class AppVersionBean implements Serializable{
		private String serverVersionCode;//新版本版本号
		private String localVersionCode;//本地版本号
		private String downloadUrl;		//新版本下载地址
		private String secretKey;		//App key
		private boolean forceUpgrade;	//强制升级
		private boolean hasNewVersion;	//是否有新版本

		public boolean isHasNewVersion() {
			return hasNewVersion;
		}

		public void setHasNewVersion(boolean hasNewVersion) {
			this.hasNewVersion = hasNewVersion;
		}

		public String getServerVersionCode() {
			return serverVersionCode;
		}

		public void setServerVersionCode(String serverVersionCode) {
			this.serverVersionCode = serverVersionCode;
		}

		public String getLocalVersionCode() {
			return localVersionCode;
		}

		public void setLocalVersionCode(String localVerionCode) {
			this.localVersionCode = localVerionCode;
		}

		public String getDownloadUrl() {
			return downloadUrl;
		}

		public void setDownloadUrl(String downloadUrl) {
			this.downloadUrl = downloadUrl;
		}

		public String getSecretKey() {
			return secretKey;
		}

		public void setSecretKey(String secretKey) {
			this.secretKey = secretKey;
		}

		public boolean isForceUpgrade() {
			return forceUpgrade;
		}

		public void setForceUpgrade(boolean forceUpgrade) {
			this.forceUpgrade = forceUpgrade;
		}

		@Override
		public String toString() {
			return "AppVersionBean [serverVersionCode=" + serverVersionCode
					+ ", localVerionCode=" + localVersionCode + ", downloadUrl="
					+ downloadUrl + ", secretKey=" + secretKey
					+ ", forceUpgrade=" + forceUpgrade + ", hasNewVersion="
					+ hasNewVersion + "]";
		}
	}
}
