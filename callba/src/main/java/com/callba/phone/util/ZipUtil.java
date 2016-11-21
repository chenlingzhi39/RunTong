package com.callba.phone.util;

import android.content.Context;

import com.callba.phone.cfg.Constant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ZipUtil {
	private static final int BUFF_SIZE = 1024 * 1024; // 1M Byte
	private static final int ASSETS_SUFFIX_BEGIN = 101;
	private static final int ASSETS_SUFFIX_END = 102;
	/**
	 * 复制(大于1M 需要用文件分割器分割)
	 * 
	 * @throws IOException
	 */
	public static void copyBigDataBase(Context context) throws IOException {
		InputStream myInput;
			Constant.ZIP_PATH = StorageUtils.getFilesDirectory(context).getAbsolutePath();
			Logger.v("SD卡DB_PATH", Constant.ZIP_PATH);
		File dir = new File(Constant.ZIP_PATH);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		String outFileName = Constant.ZIP_PATH +"/"+ Constant.DB_NAME;
		
		try {
			OutputStream myOutput = new FileOutputStream(outFileName);
			for (int i = ASSETS_SUFFIX_BEGIN; i < ASSETS_SUFFIX_END + 1; i++) {
				myInput = context.getAssets().open(Constant.ASSETS_NAME + "." + i);
				byte[] buffer = new byte[1024];
				int length;
				while ((length = myInput.read(buffer)) > 0) {
					myOutput.write(buffer, 0, length);
				}
				myOutput.flush();
				myInput.close();
			}
			myOutput.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
//		unzipFile(DB_PATH, outFileName);
		File zipFile = new File(outFileName);
		String zippath=upZipFile(zipFile, Constant.ZIP_PATH,context);
		Constant.DB_PATH=zippath;
		Logger.v("Constant.DB_PATH", "Constant.DB_PATH=zippath:"+zippath);
	}
	/**
     * 解压缩一个文件
     *
     * @param zipFile 压缩文件
     * @param folderPath 解压缩的目标目录
     * @throws IOException 当解压缩过程出错时抛出
     */
    public static String upZipFile(File zipFile, String folderPath,Context context) throws ZipException, IOException {
        File desDir = new File(folderPath);
        if (!desDir.exists()) {
            desDir.mkdirs();
        }
        //压缩文件
        @SuppressWarnings("resource")
		ZipFile zf = new ZipFile(zipFile);
        String str="";
        for (Enumeration<?> entries = zf.entries(); entries.hasMoreElements();) {
            ZipEntry entry = ((ZipEntry)entries.nextElement());
            String name = entry.getName(); 
            if(name.endsWith(File.separator)) 
            continue; 
            InputStream in = zf.getInputStream(entry);
             str = folderPath + File.separator + entry.getName();
            str = new String(str.getBytes("GB2312"), "UTF-8");
            File desFile = new File(str);
            if (!desFile.exists()) {
                File fileParentDir = desFile.getParentFile();
                if (!fileParentDir.exists()) {
                    fileParentDir.mkdirs();
                }
                desFile.createNewFile();
            }
            OutputStream out = new FileOutputStream(desFile);
            byte buffer[] = new byte[BUFF_SIZE];
            int realLength;
            while ((realLength = in.read(buffer)) > 0) {
                out.write(buffer, 0, realLength);
            }
            in.close();
            out.close();
        }
        Logger.v("db位置", str);
        SPUtils.put(context,Constant.PACKAGE_NAME,Constant.DB_PATH_, str);
		return str;
    }
}
