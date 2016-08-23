package com.callba.phone.util;
/**
 * 网络访问接口
 * 
 * @author Zhang
 */
public interface Interfaces {
	String BASIC_URL = "http://inter.boboit.cn/inter/basic";

    String URL="http://inter.boboit.cn/inter";
	/**
	 * 获取版本信息
	 */
	String Version = BASIC_URL + "/version.jsp";
	/**
	 * 用户登录接口
	 */
	String Login = BASIC_URL + "/login.jsp";
	/**
	 * 余额查询
	 */
	String Query_Balance = BASIC_URL + "/user_balance.jsp";
	/**
	 * 获取验证码
	 */
	String Verification_Code = BASIC_URL + "/user_rgist_code.jsp?";
	/**
	 * 用户注册
	 */
	String Register = BASIC_URL + "/user_rgist.jsp";
	/**
	 * 发送短信
	 */
	String Send_SMS = BASIC_URL+"/keyt/sms_key.jsp";
	/**
	 * 修改密码
	 */
	String Change_Pass = BASIC_URL + "/modify_pwd.jsp";
	/**
	 * 找回密码
	 */
	String Retrieve_Pass = BASIC_URL + "/forgot_pwd.jsp?";
	/**
	 * 每日签到
	 */
	String Sign = BASIC_URL + "/user_signin.jsp";
	/**
	 * 查询用户套餐
	 */
	String QUERY_MEAL =URL+ "/suite/user_suites.jsp";
	/**
	 * 查询用户通话记录
	 */
	String QUERY_CALLLOG = URL+"/callrecord/callrecord.jsp";
	/**
	 * 回拨
	 */
	String DIAL_CALLBACK = URL+ "/call/callbacking.jsp";
	/**
	 * 闰通卡支付
	 */
	String CALLDA_PAY = URL+ "/pay/pay_card.jsp";
	/**
	 * 获取广告1
	 */
	String GET_ADVERTICEMENT1 =URL+ "/ad/ad1.jsp";
	/**
	 * 获取广告2
	 */
	String GET_ADVERTICEMENT2 =URL+ "/ad/ad2.jsp";
	/**
	 * 获取广告3
	 */
	String GET_ADVERTICEMENT3 =URL+ "/ad/ad3.jsp";
	/**
	 * 获取广告4
	 */
	String GET_ADVERTICEMENT4 =URL+ "/ad/ad4.jsp";
	/**
	 * 附近的人
	 */
	String GET_NEARBY=URL+ "/nearby/nearby.jsp";
	/**
	 * 保存位置
	 */
	String SAVE_LOCATION=URL+ "/savelocation/savelocation.jsp";
	/**
	 * 获取签到日期
	 */
	String GET_MARKS=URL+ "/getmarks/getmarks.jsp";
	/**
	 * 上传头像
	 */
	String CHANGE_HEAD=BASIC_URL+"/user_head.jsp";
	/**
	 * 修改信息
	 */
	String CHANGE_INFO=BASIC_URL+"/user_info_update.jsp";
	/**
	 * 获取动态
	 */
	String GET_MOODS=URL+ "/circle/getmood.jsp";
	/**
	 * 发心情
	 */
    String SEND_MOODS=URL+ "/circle/sendmood.jsp";
	/**
	 * 获取Call吧号码
	 */
	String GET_SYSTEM_PHONE_NUMBER=URL+ "/basic/systemphonebook.jsp";
	/**
	 * 下订单
	 */
	String SET_ORDER=URL+ "/pay/pay_online_order.jsp";
	/**
	 * 支付完成
	 */
	String PAY_SUCCESS=URL+ "/pay/app_notify_url.jsp";
	/**
	 * 帮助中心
	 */
	String HELP_CENTER = URL+ "/help/help_android.html";
	/**
	 * 添加好友
	 */
	String ADD_FRIEND=BASIC_URL+"/addfriend.jsp";
	/**
	 * 获取好友列表
	 */
	String GET_FRIENDS=URL+ "/friend/friendlist.jsp";
	/**
	 * 删除好友
	 */
	String DELETE_FRIENDS=URL+ "/friend/deletefriend.jsp";
	/**
	 * 添加多个好友
	 */
	String ADD_FRIENDS=URL+ "/friend/addfriends.jsp";
	/**
	 * 订单
	 */
	String ORDER=URL+ "/myorder/myorder.jsp";
	/**
	 * 团队
	 */
	String TEAM=URL+ "/myteam/myteam.jsp";
	/**
	 * A类客户
	 */
	String A_TYPE=URL+ "/mypotentialcustomer/mypotentialcustomer.jsp";
	/**
	 * 累计佣金明细
	 */
	String PROFIT=URL+ "/myprofit/myprofit.jsp";
	/**
	 * 提现记录
	 */
	String TXRECORD=URL+ "/mytxrecord/mytxrecord.jsp";
	/**
	 * 获取二维码
	 */
	String IMAGE_QR=URL+ "/myqr/myqr.jsp";
	/**
	 * 提现
	 */
	String TX=URL+ "/tx/tx.jsp";
	/**
	 * 流量卡充值
	 */
	String FLOW_CARD=URL+ "/pay/pay_card_flow.jsp";
	/**
	 * 流量包订单
	 */
	String FLOW_ORDER=URL+ "/pay/pay_flow_online_order.jsp";
	/**
	 * 金币兑换
	 */
	String EXCHANGE_BALANCE=URL+ "/exchangebalance/exchangebalance.jsp";
	/**
	 * 优惠券
	 */
	String COUPON=URL+ "/mycoupon/mycoupon.jsp";
	/**
	 *流量包
	 */
	String FLOW_ITEM=URL+ "/items/items.jsp";
	/**
	 * 赠送优惠券
	 */
	String GIVE_COUPON=URL+ "/givecoupontofriend/givecoupontofriend.jsp";
	/**
	 * 添加备注
	 */
	String UPDATE_REMARK=URL+ "/friend/updateremark.jsp";
	int GET_CODE_START=0;
	int GET_CODE_FAILURE=1;
	int GET_KEY_SUCCESS=2;
	int GET_KEY_START=3;
	int GET_KEY_FAILURE=4;
	int GET_CODE_SUCCESS=5;

}
