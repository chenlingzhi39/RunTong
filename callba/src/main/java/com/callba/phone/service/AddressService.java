package com.callba.phone.service;

import android.content.Context;
import android.util.Xml;


import com.callba.R;
import com.callba.phone.MyApplication;
import com.callba.phone.util.StreamTool;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AddressService {

	/**
	 * 获取手机号的归属地
	 * @param mobile
	 * @return
	 * @throws Exception
	 */
	public static String getAddress(Context context, String mobile) throws Exception {
		// 读取SOAP协议XML内容数据
		String soap = readSoap(context);
		// 替换占位符
		soap = soap.replaceAll("\\$mobile", mobile);
		// 得到的字节数据实际上也就是我们要发送给对方的实体数据
		byte[] entity = soap.getBytes();
		// 首先要确定我们要请求的webservice api所在的路径
		String path = "http://ws.webxml.com.cn/WebServices/MobileCodeWS.asmx";
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5000);
		conn.setRequestMethod("POST");
		// 允许对外输出数据
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type", "application/soap+xml; charset=utf-8");
		conn.setRequestProperty("Content-Length", String.valueOf(entity.length));
		// 把实体数据发送给对方
		conn.getOutputStream().write(entity);

		// 调用玩webservice api之后，会返回给我们如下一段xml数据，我们需要对返回的数据进行解析
		/**
		 * <?xml version="1.0" encoding="utf-8"?> 
		 * <soap12:Envelope xmlns:xsi=
		 * "http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd=
		 * "http://www.w3.org/2001/XMLSchema" xmlns:soap12=
		 * "http://www.w3.org/2003/05/soap-envelope"> 
		 * <soap12:Body>
		 * 		<getMobileCodeInfoResponse xmlns="http://WebXml.com.cn/">
		 * 			<	>string</getMobileCodeInfoResult>
		 * 		</getMobileCodeInfoResponse> 
		 * </soap12:Body> 
		 * </soap12:Envelope>
		 */
		if (conn.getResponseCode() == 200) {
			//解析返回的xml数据
			//取得输入流对象，一边解析一边读取
			return parseSoap(conn.getInputStream());
			//System.out.println("发送成功！");
		} else {
			System.out.println("发送失败！");
		}

		return null;
	}

	private static String parseSoap(InputStream xml) throws Exception {
		// TODO Auto-generated method stub
		//获取XmlPullParser解析器对象
		XmlPullParser pullParser=Xml.newPullParser();
		//设置要解析的内容
		pullParser.setInput(xml,"utf-8");
		//开始解析
		//定义一个变量存放事件
		int event=pullParser.getEventType();
		while(event!=XmlPullParser.END_DOCUMENT)
		{
			switch(event)
			{
			case XmlPullParser.START_TAG:
				if("getMobileCodeInfoResult".equals(pullParser.getName()))
				{
					//返回当前节点的名称
					return pullParser.nextText();
				}
				break;
			}
			event=pullParser.next();
		}
		return null;
	}

	// 得到xml文件中的内容
	private static String readSoap(Context context) throws Exception {
		// TODO Auto-generated method stub
		// 读取xml文件内容
		// 类路径的跟目录底下
		InputStream inStream = context.getAssets().open("soap12.xml");
		// 得到输入流对象后要从输入流读取文件数据
		byte[] data = StreamTool.read(inStream);
		return new String(data);
	}
}
