package com.callba.phone.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class StreamTool {

	public static byte[] read(InputStream inStream) throws Exception {
		// TODO Auto-generated method stub
		//一边读一边往内存写数据
		ByteArrayOutputStream outStream=new ByteArrayOutputStream();
		byte[] buffer=new byte[1024];
		int len=0;
		while((len=inStream.read(buffer)) !=-1)
		{
			outStream.write(buffer,0,len);
		}
		inStream.close();
		//返回内存中的数据
		return outStream.toByteArray();
	}

}
