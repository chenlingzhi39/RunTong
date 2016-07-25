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
	 * ��ȡ�ֻ��ŵĹ�����
	 * @param mobile
	 * @return
	 * @throws Exception
	 */
	public static String getAddress(Context context, String mobile) throws Exception {
		// ��ȡSOAPЭ��XML��������
		String soap = readSoap(context);
		// �滻ռλ��
		soap = soap.replaceAll("\\$mobile", mobile);
		// �õ����ֽ�����ʵ����Ҳ��������Ҫ���͸��Է���ʵ������
		byte[] entity = soap.getBytes();
		// ����Ҫȷ������Ҫ�����webservice api���ڵ�·��
		String path = "http://ws.webxml.com.cn/WebServices/MobileCodeWS.asmx";
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5000);
		conn.setRequestMethod("POST");
		// ��������������
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type", "application/soap+xml; charset=utf-8");
		conn.setRequestProperty("Content-Length", String.valueOf(entity.length));
		// ��ʵ�����ݷ��͸��Է�
		conn.getOutputStream().write(entity);

		// ������webservice api֮�󣬻᷵�ظ���������һ��xml���ݣ�������Ҫ�Է��ص����ݽ��н���
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
			//�������ص�xml����
			//ȡ������������һ�߽���һ�߶�ȡ
			return parseSoap(conn.getInputStream());
			//System.out.println("���ͳɹ���");
		} else {
			System.out.println("����ʧ�ܣ�");
		}

		return null;
	}

	private static String parseSoap(InputStream xml) throws Exception {
		// TODO Auto-generated method stub
		//��ȡXmlPullParser����������
		XmlPullParser pullParser=Xml.newPullParser();
		//����Ҫ����������
		pullParser.setInput(xml,"utf-8");
		//��ʼ����
		//����һ����������¼�
		int event=pullParser.getEventType();
		while(event!=XmlPullParser.END_DOCUMENT)
		{
			switch(event)
			{
			case XmlPullParser.START_TAG:
				if("getMobileCodeInfoResult".equals(pullParser.getName()))
				{
					//���ص�ǰ�ڵ������
					return pullParser.nextText();
				}
				break;
			}
			event=pullParser.next();
		}
		return null;
	}

	// �õ�xml�ļ��е�����
	private static String readSoap(Context context) throws Exception {
		// TODO Auto-generated method stub
		// ��ȡxml�ļ�����
		// ��·���ĸ�Ŀ¼����
		InputStream inStream = context.getAssets().open("soap12.xml");
		// �õ������������Ҫ����������ȡ�ļ�����
		byte[] data = StreamTool.read(inStream);
		return new String(data);
	}
}
