package com.callba.phone.util;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by PC-20160514 on 2016/7/19.
 */
public class SmsTools {
    static String JTPHONE="10690844200664";
    static int YZMLENGTH=4;
    public static String getsmsyzm(Activity c) {
        Uri uri = Uri.parse("content://sms/inbox");
        String[] projection = new String[] { "address", "person", "body" };
        String selection = " address='" + JTPHONE + "' ";
        String[] selectionArgs = new String[] {};
        String sortOrder = "date desc";
        @SuppressWarnings("deprecation")
        Cursor cur = c.managedQuery(uri, projection, null, selectionArgs,
                sortOrder);
        if(cur!=null&&cur.getCount()>0){
            cur.moveToFirst();
            String body = cur.getString(cur.getColumnIndex("body")).replaceAll(
                    "\n", " ");
            cur.close();
            return getyzm(body, YZMLENGTH);
        }
        return null;
    }


    /**
     * 从短信字符窜提取验证码
     * @param body 短信内容
     * @param YZMLENGTH  验证码的长度 一般6位或者4位
     * @return 接取出来的验证码
     */
    public static String getyzm(String body, int YZMLENGTH) {
        // 首先([a-zA-Z0-9]{YZMLENGTH})是得到一个连续的六位数字字母组合
        // (?<![a-zA-Z0-9])负向断言([0-9]{YZMLENGTH})前面不能有数字
        // (?![a-zA-Z0-9])断言([0-9]{YZMLENGTH})后面不能有数字出现
        Pattern p = Pattern
                .compile("(?<![a-zA-Z0-9])([a-zA-Z0-9]{" + YZMLENGTH + "})(?![a-zA-Z0-9])");
        Matcher m = p.matcher(body);
        if (m.find()) {
            System.out.println(m.group());
            return m.group(0);
        }
        return null;
    }
}
