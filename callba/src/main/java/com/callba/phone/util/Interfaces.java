package com.callba.phone.util;
/**
 * 网络访问接口
 * 
 * @author Zhang
 */
public interface Interfaces {
	/**
	 * 根路径"http://inter.runtong77.com/inter/basic"
	 */
	String BASIC_URL = "http://inter.boboit.cn/inter/basic";


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
	String QUERY_MEAL = "http://inter.boboit.cn/inter/suite/user_suites.jsp";
	/**
	 * 查询用户通话记录
	 */
	String QUERY_CALLLOG = "http://inter.boboit.cn/inter/callrecord/callrecord.jsp";
	/**
	 * 回拨
	 */
	String DIAL_CALLBACK = "http://inter.boboit.cn/inter/call/callbacking.jsp";
	/**
	 * 闰通卡支付
	 */
	String CALLDA_PAY = "http://inter.boboit.cn/inter/pay/pay_card.jsp";
	/**
	 * 获取广告1
	 */
	String GET_ADVERTICEMENT1 ="http://inter.boboit.cn/inter/ad/ad1.jsp";
	/**
	 * 获取广告2
	 */
	String GET_ADVERTICEMENT2 ="http://inter.boboit.cn/inter/ad/ad2.jsp";
	/**
	 * 获取广告3
	 */
	String GET_ADVERTICEMENT3 ="http://inter.boboit.cn/inter/ad/ad3.jsp";
	/**
	 * 获取广告4
	 */
	String GET_ADVERTICEMENT4 ="http://inter.boboit.cn/inter/ad/ad4.jsp";
	/**
	 * 附近的人
	 */
	String GET_NEARBY="http://inter.boboit.cn/inter/nearby/nearby.jsp";
	/**
	 * 保存位置
	 */
	String SAVE_LOCATION="http://inter.boboit.cn/inter/savelocation/savelocation.jsp";
	/**
	 * 获取签到日期
	 */
	String GET_MARKS="http://inter.boboit.cn/inter/getmarks/getmarks.jsp";
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
	String GET_MOODS="http://inter.boboit.cn/inter/circle/getmood.jsp";
	/**
	 * 发心情
	 */
    String SEND_MOODS="http://inter.boboit.cn/inter/circle/sendmood.jsp";
	/**
	 * 获取Call吧号码
	 */
	String GET_SYSTEM_PHONE_NUMBER="http://inter.boboit.cn/inter/basic/systemphonebook.jsp";
	/**
	 * 下订单
	 */
	String SET_ORDER="http://inter.boboit.cn/inter/pay/pay_online_order.jsp";
	/**
	 * 支付完成
	 */
	String PAY_SUCCESS="http://inter.boboit.cn/inter/pay/app_notify_url.jsp";
	/**
	 * 帮助中心
	 */
	String HELP_CENTER = "http://inter.boboit.cn/inter/help/help_android.html";
	/**
	 * 添加好友
	 */
	String ADD_FRIEND=BASIC_URL+"/addfriend.jsp";
	/**
	 * 获取好友列表
	 */
	String GET_FRIENDS="http://inter.boboit.cn/inter/friend/friendlist.jsp";
	/**
	 * 删除好友
	 */
	String DELETE_FRIENDS="http://inter.boboit.cn/inter/friend/deletefriend.jsp";
	/**
	 * 添加多个好友
	 */
	String ADD_FRIENDS="http://inter.boboit.cn/inter/friend/addfriends.jsp";
	/**
	 * 订单
	 */
	String ORDER="http://inter.boboit.cn/inter/myorder/myorder.jsp";
	/**
	 * 团队
	 */
	String TEAM="http://inter.boboit.cn/inter/myteam/myteam.jsp";
	/**
	 * A类客户
	 */
	String A_TYPE="http://inter.boboit.cn/inter/mypotentialcustomer/mypotentialcustomer.jsp";
	/**
	 * 累计佣金明细
	 */
	String PROFIT="http://inter.boboit.cn/inter/myprofit/myprofit.jsp";
	/**
	 * 提现记录
	 */
	String TXRECORD="http://inter.boboit.cn/inter/mytxrecord/mytxrecord.jsp";
	/**
	 * 获取二维码
	 */
	String IMAGE_QR="http://inter.boboit.cn/inter/myqr/myqr.jsp";
	/**
	 * 提现
	 */
	String TX="http://inter.boboit.cn/inter/tx/tx.jsp";
	/**
	 * 流量卡充值
	 */
	String FLOW_CARD="http://inter.boboit.cn/inter/pay/pay_card_flow.jsp";
	/**
	 * 流量包订单
	 */
	String FLOW_ORDER="http://inter.boboit.cn/inter/pay/pay_flow_online_order.jsp";
	/**
	 * 金币兑换
	 */
	String EXCHANGE_BALANCE="http://inter.boboit.cn/inter/exchangebalance/exchangebalance.jsp";
	/**
	 * 优惠券
	 */
	String COUPON="http://inter.boboit.cn/inter/mycoupon/mycoupon.jsp";
	/**
	 *流量包
	 */
	String FLOW_ITEM="http://inter.boboit.cn/inter//items/items.jsp";
	/**
	 * 赠送优惠券
	 */
	String GIVE_COUPON="http://inter.boboit.cn/inter/givecoupontofriend/givecoupontofriend.jsp";
	/**
	 * 添加备注
	 */
	String UPDATE_REMARK="http://inter.boboit.cn/inter/friend/updateremark.jsp";
	int GET_CODE_START=0;
	int GET_CODE_FAILURE=1;
	int GET_KEY_SUCCESS=2;
	int GET_KEY_START=3;
	int GET_KEY_FAILURE=4;
	int GET_CODE_SUCCESS=5;

}
