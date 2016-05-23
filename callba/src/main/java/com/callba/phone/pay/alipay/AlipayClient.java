package com.callba.phone.pay.alipay;

import java.net.URLEncoder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.callba.R;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;

/**
 * 调用支付宝客户端支付
 * 
 * @author zhanghw
 * @version 创建时间：2013-9-29 下午4:51:30
 */
public class AlipayClient {
	private Context context;
	private String tradeHao; // 订单号
	private String tradeJine; // 订单金额

	private ProgressDialog mProgress = null;

//	private String tradeSubject =context.getString(R.string.rechargeend);
//	private String tradeBody = "元闰通充值卡";
	private String serviceName = "mobile.securitypay.pay";
	private String paymentType = "1";
	/**
	 * 支付完成通知回调
	 */
//	private String notifyURL = "http://inter.callda.com/inter/pay/alipay_notify_url.jsp";

	public AlipayClient(Context context, String tradeNo, String tradeFee) {
		super();
		this.context = context;
		this.tradeHao = tradeNo;
		this.tradeJine = tradeFee;
	}

	/**
	 * 检查是否安装支付宝客户端以及客户端版本
	 */
	public void prepared2Pay() {
		// 检测安全支付服务是否被安装
		MobileSecurePayHelper mspHelper = new MobileSecurePayHelper(context);
		boolean isInstalled = mspHelper.detectMobile_sp();

		if (isInstalled) {
			start2Pay(tradeHao, tradeJine);
		} else {
			//未安装支付宝插件
			IntentFilter packageAddedFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
			packageAddedFilter.addDataScheme("package");
			context.registerReceiver(mPackageInstallationListener, packageAddedFilter);
		}
	}

	/**
	 * 
	 * @param tradeNo
	 *            订单号
	 * @param tradeFee
	 *            订单金额
	 */
	private void start2Pay(String tradeNo, String tradeFee) {
		// 检测安全支付服务是否安装
		MobileSecurePayHelper mspHelper = new MobileSecurePayHelper(context);
		boolean isMobile_spExist = mspHelper.detectMobile_sp();
		//未安装
		if (!isMobile_spExist) {
			return;
		}

		// 根据订单信息开始进行支付
		try {
			// 准备订单信息
			String orderInfo = getOrderInfo(tradeNo, tradeFee);
			// 这里根据签名方式对订单信息进行签名
			String signType = getSignType();
			String strsign = sign(signType, orderInfo);
			Log.v("sign:", strsign);
			// 对签名进行编码
			strsign = URLEncoder.encode(strsign, "UTF-8");
			// 组装好参数
			String info = orderInfo + "&sign=" + "\"" + strsign + "\"" + "&"
					+ getSignType();
			Log.v("orderInfo:", info);
			// start the pay.
			// 调用pay方法进行支付
			MobileSecurePayer msp = new MobileSecurePayer();
			boolean bRet = msp.pay(info, mHandler, AlixId.RQF_PAY,
					(Activity) context);

			if (bRet) {
				// 显示“正在支付”进度条
				closeProgress();
				mProgress = BaseHelper.showProgress(context, null, context.getString(R.string.recharging),
						false, true);
			} else
				;
		} catch (Exception ex) {
			Toast.makeText(context, R.string.remote_call_failed,
					Toast.LENGTH_SHORT).show();
		}
	}

	// 这里接收支付结果，支付宝手机端同步通知
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			try {
				String ret = (String) msg.obj;
				
				Logger.i("支付成功返回结果", ret);

				// Log.e(TAG, ret); //
				// strRet范例：resultStatus={9000};memo={};result={partner="2088201564809153"&seller="2088201564809153"&out_trade_no="050917083121576"&subject="123456"&body="2010新款NIKE 耐克902第三代板鞋 耐克男女鞋 386201 白红"&total_fee="0.01"&notify_url="http://notify.java.jpxx.org/index.jsp"&success="true"&sign_type="RSA"&sign="d9pdkfy75G997NiPS1yZoYNCmtRbdOP0usZIMmKCCMVqbSG1P44ohvqMYRztrB6ErgEecIiPj9UldV5nSy9CrBVjV54rBGoT6VSUF/ufjJeCSuL510JwaRpHtRPeURS1LXnSrbwtdkDOktXubQKnIMg2W0PreT1mRXDSaeEECzc="}
				switch (msg.what) {
				case AlixId.RQF_PAY: {
					//
					closeProgress();

					// BaseHelper.log(TAG, ret);

					// 处理交易结果
					try {
						// 获取交易状态码，具体状态代码请参看文档
						String tradeStatus = "resultStatus={";
						int imemoStart = ret.indexOf("resultStatus=");
						imemoStart += tradeStatus.length();
						int imemoEnd = ret.indexOf("};memo=");
						tradeStatus = ret.substring(imemoStart, imemoEnd);

						// 先验签通知
						ResultChecker resultChecker = new ResultChecker(ret);
						int retVal = resultChecker.checkSign();
						// 验签失败
						if (retVal == ResultChecker.RESULT_CHECK_SIGN_FAILED) {
							BaseHelper.showDialog(
									(Activity) context,
									context.getString(R.string.sacc_tip),
									context.getResources().getString(
											R.string.check_sign_failed),
									android.R.drawable.ic_dialog_alert);
						} else {// 验签成功。验签成功后再判断交易状态码
							if (tradeStatus.equals("9000"))// 判断交易状态码，只有9000表示交易成功
								BaseHelper.showDialog((Activity) context, context.getString(R.string.sacc_tip),
										context.getString(R.string.rechargesuccess) + tradeStatus,
										R.drawable.infoicon);
							else
								BaseHelper.showDialog((Activity) context, context.getString(R.string.sacc_tip),
										context.getString(R.string.rechargefailed) + tradeStatus,
										R.drawable.infoicon);
						}

					} catch (Exception e) {
						e.printStackTrace();
						BaseHelper.showDialog((Activity) context, context.getString(R.string.sacc_tip), ret,
								R.drawable.infoicon);
					}
				}
					break;
				}

				super.handleMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 * 获取订单信息
	 * 
	 * @param tradeNo
	 *            订单号
	 * @param tradeFee
	 *            订单金额
	 * @return
	 */
	private String getOrderInfo(String tradeNo, String tradeFee) {
		String strOrderInfo = "partner=" + "\"" + PartnerConfig.PARTNER + "\"";
		strOrderInfo += "&";
		strOrderInfo += "seller_id=" + "\"" + PartnerConfig.PARTNER + "\"";
		strOrderInfo += "&";
		strOrderInfo += "out_trade_no=" + "\"" + tradeNo + "\"";
		strOrderInfo += "&";
		strOrderInfo += "subject=" + "\"" + tradeFee+context.getString(R.string.rechargeend) + "\"";
		strOrderInfo += "&";
		strOrderInfo += "body=" + "\"" + tradeFee+context.getString(R.string.rechargeend) + "\"";
		strOrderInfo += "&";
		strOrderInfo += "total_fee=" + "\"" + tradeFee + "\"";
		strOrderInfo += "&";
		strOrderInfo += "notify_url=" + "\"" + Interfaces.PAY_NOTIFY_URL + "\"";
		strOrderInfo += "&";
		strOrderInfo += "service=" + "\"" + serviceName + "\"";
		strOrderInfo += "&";
		strOrderInfo += "payment_type=" + "\"" + paymentType + "\"";
		strOrderInfo += "&";
		strOrderInfo += "_input_charset=" + "\"" + "utf-8" + "\"";
		strOrderInfo += "&";
		strOrderInfo += "it_b_pay=" + "\"" + "1h" + "\"";
//		strOrderInfo += "&";
//		strOrderInfo += "show_url=" + "\"" + "" + "\"";
		return strOrderInfo;
	}

	private BroadcastReceiver mPackageInstallationListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String packageName = intent.getDataString();
			if (!TextUtils
					.equals(packageName, "package:com.alipay.android.app")) {
				return;
			}

			start2Pay(tradeHao, tradeJine);
			
			context.unregisterReceiver(this);
		}
	};

	/**
	 * sign the order info. 对订单信息进行签名
	 * 
	 * @param signType
	 *            签名方式
	 * @param content
	 *            待签名订单信息
	 * @return
	 */
	String sign(String signType, String content) {
		return Rsa.sign(content, PartnerConfig.RSA_PRIVATE);
	}

	/**
	 * get the sign type we use. 获取签名方式
	 * 
	 * @return
	 */
	String getSignType() {
		String getSignType = "sign_type=" + "\"" + "RSA" + "\"";
		return getSignType;
	}

	/**
	 * get the char set we use. 获取字符集
	 * 
	 * @return
	 */
	String getCharset() {
		String charset = "charset=" + "\"" + "utf-8" + "\"";
		return charset;
	}

	// 关闭进度框
	private void closeProgress() {
		try {
			if (mProgress != null) {
				mProgress.dismiss();
				mProgress = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 释放资源
	 */
	public void release() {
		context.unregisterReceiver(mPackageInstallationListener);
		try {
			mProgress.dismiss();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
