package com.callba.phone;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by PC-20160514 on 2016/8/25.
 */
public class CrashHandler implements UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";
    private Thread.UncaughtExceptionHandler mDefaultHandler;// 系统默认的UncaughtException处理类
    private static CrashHandler INSTANCE;// CrashHandler实例
    private Context mContext;// 程序的Context对象
    private Map<String, String> info = new HashMap<String, String>();// 用来存储设备信息和异常信息
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");// 用于格式化日期,作为日志文件名的一部分

    /**
     * 保证只有一个CrashHandler实例
     */
    private CrashHandler() {

    }

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static CrashHandler getInstance() {
        if(INSTANCE==null)
            INSTANCE = new CrashHandler();
        return INSTANCE;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context) {
        mContext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();// 获取系统默认的UncaughtException处理器
        Thread.setDefaultUncaughtExceptionHandler(this);// 设置该CrashHandler为程序的默认处理器
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        ex.printStackTrace();
        if (!handleException(ex) && mDefaultHandler != null) {
            // 如果自定义的没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(Integer.MAX_VALUE);// 如果处理了，让程序继续运行3秒再退出，保证文件保存并上传到服务器
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 退出程序
           /* android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);*/
            //mDefaultHandler.uncaughtException(thread, ex);
        }
    }

    /**
     * 当UncaughtException发生时会转入该重写的方法来处理
     */


    private void sendAppCrashReport(final Context context,
                                    final String crashReport, final File file) {
        // TODO Auto-generated method stub
        try {
            AlertDialog mDialog = null;
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setIcon(android.R.drawable.ic_dialog_info);
            builder.setTitle("程序出错啦");
            builder.setMessage("请把错误报告以邮件的形式提交给我们，谢谢(错误报告保存路径为/sdcard/crash)");
            builder.setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            // 发送异常报告
                            try {
                                //注释部分是已文字内容形式发送错误信息
                                // Intent intent = new Intent(Intent.ACTION_SENDTO);
                                // intent.setType("text/plain");
                                // intent.putExtra(Intent.EXTRA_SUBJECT,
                                // "推聊Android客户端 - 错误报告");
                                // intent.putExtra(Intent.EXTRA_TEXT, crashReport);
                                // intent.setData(Uri
                                // .parse("mailto:way.ping.li@gmail.com"));
                                // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                // context.startActivity(intent);

                                //下面是以附件形式发送邮件
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                String[] tos = {"931348039@qq.com"};
                                intent.putExtra(Intent.EXTRA_EMAIL, tos);
                                intent.putExtra(Intent.EXTRA_SUBJECT,
                                        "Call吧Android客户端 - 错误报告");
                                if (file != null) {
                                    intent.putExtra(Intent.EXTRA_STREAM,
                                            Uri.fromFile(file));
                                    intent.putExtra(Intent.EXTRA_TEXT,
                                            "请将此错误报告发送给我，以便我尽快修复此问题，谢谢合作！\n");
                                } else {
                                    intent.putExtra(Intent.EXTRA_TEXT,
                                            "请将此错误报告发送给我，以便我尽快修复此问题，谢谢合作！\n"
                                                    + crashReport);
                                }

                                intent.setType("text/plain");
                                intent.setType("message/rfc882");
                                Intent.createChooser(intent, "Choose Email Client");
                                context.startActivity(intent);

                            } catch (Exception e) {
                                Toast.makeText(context,
                                        "请安装电子邮件客户端",
                                        Toast.LENGTH_SHORT).show();
                            } finally {
                                dialog.dismiss();
                                // 退出
                              /*  android.os.Process.killProcess(android.os.Process
                                        .myPid());
                                System.exit(1);*/
                            }
                        }
                    });
            builder.setNegativeButton(android.R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            // 退出
                        /*    android.os.Process.killProcess(android.os.Process
                                    .myPid());
                            System.exit(1);*/
                        }
                    });
            builder.setCancelable(false);
            mDialog = builder.create();
            mDialog.getWindow().setType(
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            mDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex 异常信息
     * @return true 如果处理了该异常信息;否则返回false.
     */
    public boolean handleException(final Throwable ex) {
        if (ex == null || mContext == null)
            return false;
        final String crashReport = getCrashReport(mContext, ex);
        new Thread() {
            public void run() {
                Looper.prepare();
               // Toast.makeText(mContext, "很抱歉,程序出现异常,即将退出", 0).show();
                // 收集设备参数信息
                collectDeviceInfo(mContext);
                // 保存日志文件
                File file=saveCrashInfo2File(ex);
                sendAppCrashReport(mContext, crashReport, file);
                Looper.loop();
            }
        }.start();
        return true;
    }

    private String getCrashReport(Context context, Throwable ex) {
        PackageInfo pinfo = getPackageInfo(context);
        StringBuffer exceptionStr = new StringBuffer();
        exceptionStr.append("Version: " + pinfo.versionName + "("
                + pinfo.versionCode + ")\n");
        exceptionStr.append("Android: " + android.os.Build.VERSION.RELEASE
                + "(" + android.os.Build.MODEL + ")\n");
        exceptionStr.append("Exception: " + ex.getMessage() + "\n");
        StackTraceElement[] elements = ex.getStackTrace();
        for (int i = 0; i < elements.length; i++) {
            exceptionStr.append(elements[i].toString() + "\n");
        }
        return exceptionStr.toString();
    }

    /**
     * 获取App安装包信息
     *
     * @return
     */
    private PackageInfo getPackageInfo(Context context) {
        PackageInfo info = null;
        try {
            info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            // e.printStackTrace(System.err);
            // L.i("getPackageInfo err = " + e.getMessage());
        }
        if (info == null)
            info = new PackageInfo();
        return info;
    }

    /**
     * 收集设备参数信息
     *
     * @param context
     */
    public void collectDeviceInfo(Context context) {
        try {
            PackageManager pm = context.getPackageManager();// 获得包管理器
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_ACTIVITIES);// 得到该应用的信息，即主Activity
            if (pi != null) {
                String versionName = pi.versionName == null ? "null"
                        : pi.versionName;
                String versionCode = pi.versionCode + "";
                info.put("versionName", versionName);
                info.put("versionCode", versionCode);
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        Field[] fields = Build.class.getDeclaredFields();// 反射机制
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                info.put(field.getName(), field.get("").toString());
                Log.d(TAG, field.getName() + ":" + field.get(""));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private File saveCrashInfo2File(Throwable ex) {
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : info.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\r\n");
        }
        Writer writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        ex.printStackTrace(pw);
        Throwable cause = ex.getCause();
        // 循环着把所有的异常信息写入writer中
        while (cause != null) {
            cause.printStackTrace(pw);
            cause = cause.getCause();
        }
        pw.close();// 记得关闭
        String result = writer.toString();
        sb.append(result);
        // 保存文件
        long timetamp = System.currentTimeMillis();
        String time = format.format(new Date());
        String fileName = "crash-" + time + "-" + timetamp + ".log";
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            try {
                final File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "crash");
                Log.i("CrashHandler", dir.toString());
                if (!dir.exists())
                    dir.mkdir();
                FileOutputStream fos = new FileOutputStream(new File(dir,
                        fileName));
                fos.write(sb.toString().getBytes());
                fos.close();
                return (new File(dir,
                        fileName));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}



