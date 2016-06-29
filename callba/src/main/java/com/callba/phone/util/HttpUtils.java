package com.callba.phone.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import java.net.HttpURLConnection;
public class HttpUtils {
	private static final int REQUEST_TIMEOUT = 5 * 1000;// 设置请求超时5秒钟
//	private static final int SO_TIMEOUT = 10 * 1000; // 设置等待数据超时时间10秒钟

	/**
	 * 
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public static String getDatefromNet(String path) throws Exception {

		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5 * 1000);
		conn.setRequestMethod("GET");
		if (conn.getResponseCode() == 200) {
			InputStream inStream = conn.getInputStream();
			byte[] data = readInputStream(inStream);
			conn.disconnect();
			return new String(data, "utf-8");
		} else {
			conn.disconnect();
			return null;
		}
	}

	/**
	 * 通过HttpGet 获取数据
	 * 
	 * @param urlstring
	 * @return
	 * @throws Exception
	 */
	public static String getDataFromHttpGet(String urlstring) throws Exception {
		BasicHttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, REQUEST_TIMEOUT);
//		HttpConnectionParams.setSoTimeout(httpParams, SO_TIMEOUT);

		HttpClient httpClient = new DefaultHttpClient(httpParams);
		/* 创建一个HttpGet的对象 */
		HttpGet httpGet = new HttpGet(urlstring);
		/* 创建HttpResponse对象，处理请求 */
		HttpResponse response = httpClient.execute(httpGet);
		/* 获取这次回应的消息实体，获取返回的实体消息 */
		HttpEntity entity = response.getEntity();
		InputStream is = entity.getContent();
		byte[] data = readInputStream(is);
		return new String(data, "utf-8");
	}

	/**
	 * 
	 * @param inStream
	 * @return
	 * @throws IOException
	 * @throws Exception
	 */
	public static byte[] readInputStream(InputStream inStream)
			throws IOException {
		ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
			outSteam.write(buffer, 0, len);
		}
		outSteam.close();
		inStream.close();
		return outSteam.toByteArray();
	}

	/**
	 * 通过httpPost获取数据
	 * @author zhw
	 *
	 * @param path 请求的url
	 * @param params 请求的参数
	 * @param encode 编码类型
	 * @param keepAlieve 是否保持连接
	 * @return 请求返回的结果
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public static String getDataFromHttpPost(String path,
			Map<String, String> params, String encode, boolean keepAlieve) throws ClientProtocolException, IOException {
		BasicHttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, REQUEST_TIMEOUT);
//		HttpConnectionParams.setSoTimeout(httpParams, SO_TIMEOUT);
		HttpClient httpClient = new DefaultHttpClient(httpParams);
		
		List<NameValuePair> paramspairs = new ArrayList<NameValuePair>();
		if (params != null && !params.isEmpty()) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				paramspairs.add(new BasicNameValuePair(entry.getKey(), entry
						.getValue()));
			}
		}
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramspairs,
				encode);// 对请求参数进行编码,得到实体数据
		HttpPost post = new HttpPost(path);
		if(!keepAlieve) {
			//请求完毕后，关闭该连接
			post.setHeader("Connection", "close");
		}
		post.setEntity(entity);
		HttpResponse response = null;
		String result = null;
		try {
			
			 response = httpClient.execute(post);
			 result = new String(readInputStream(response.getEntity().getContent()),
					 "utf-8");
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
		
		if(!keepAlieve) {
			//请求完毕后，关闭该连接
			httpClient.getConnectionManager().shutdown();
		}
		
		return result;
	}

	
	
	/**
	 * 通过httpPost获取数据（默认utf-8编码）
	 * 
	 * @param path 请求的url
	 * @param params 请求的参数
	 * @return 请求返回的结果
	 * @throws Exception
	 */
	public static String getDataFromHttpPost(String path,
			Map<String, String> params) throws Exception {
		return getDataFromHttpPost(path, params, "utf-8", true);
	}
	
	/**
	 * 通过httpPost获取数据（默认utf-8编码） 请求完毕后关闭连接
	 * @author zhw
	 *
	 * @param path 请求的url
	 * @param params 请求的参数
	 * @return 请求返回的结果
	 * @throws Exception
	 */
	public static String getDatafFromPostConnClose(String path,
			Map<String, String> params) throws Exception {
		//return getDataFromHttpPost(path, params, "utf-8", false);
		return post(path,params);
	}

	public static String post(String urlStr,Map<String, String> params) {
		urlStr=urlStr+"?";
		for (String key :params.keySet()) {
			System.out.println("key= "+ key + " and value= " + params.get(key));
			urlStr=urlStr+key+"="+params.get(key)+"&";
		}
		Logger.i("url",urlStr);
		String content = "";
		String result = "";
		URL url = null;
		URLConnection conn = null;
		OutputStreamWriter writer = null;
		try {
			url = new URL(urlStr);
			conn = url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestProperty("Referer", "inter.boboit.cn");
			conn.setConnectTimeout(10000);
			writer = new OutputStreamWriter(conn.getOutputStream());
			writer.flush();
			writer.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			//Tools.logOfTxt("/root/error_proxy_log.txt", "url:" + urlStr + "?"+ args);
		} catch (IOException e) {
			e.printStackTrace();
			//Tools.logOfTxt("/root/error_proxy_log.txt", "url:" + urlStr + "?"+ args);
		}

		try {
			InputStreamReader reder = new InputStreamReader(conn.getInputStream(), "utf-8");
			BufferedReader breader = new BufferedReader(reder);
			while ((content = breader.readLine()) != null) {
				result += content;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println("result-> "+result);
		if (result == null || result.equals("")) {
			result = "|";
		}
		return result;
	}
	
	public static byte[] getImage(String path) throws Exception {
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5 * 1000);
		conn.setRequestMethod("GET");
		if (conn.getResponseCode() == 200) {
			InputStream inStream = conn.getInputStream();
			return readInputStream(inStream);
		} else {
			throw new Exception("请求路径失败");
		}
	}

	public static byte[] postDigestFromHttpClient(String path,
			Map<String, String> params) throws ClientProtocolException,
			IOException {

		HttpGet hg = new HttpGet(path);
		String strHost = hg.getURI().getHost();
		int iPort = hg.getURI().getPort();

		AuthScope as = new AuthScope(strHost, iPort, null);
		UsernamePasswordCredentials upc = new UsernamePasswordCredentials("7",
				"7");

		BasicCredentialsProvider bcp = new BasicCredentialsProvider();
		bcp.setCredentials(as, upc);

		DefaultHttpClient client = new DefaultHttpClient();
		client.setCredentialsProvider(bcp);

		if (params != null && !params.isEmpty()) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				hg.addHeader(entry.getKey(), entry.getValue());
			}
		}
		HttpResponse sponse = client.execute(hg);

		return readInputStream(sponse.getEntity().getContent());

	}
	
}
