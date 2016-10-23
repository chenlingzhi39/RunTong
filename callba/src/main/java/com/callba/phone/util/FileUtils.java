package com.callba.phone.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by PC-20160514 on 2016/8/10.
 */
public class FileUtils {
    public static void writeObjectToFile(String path,Object obj)
    {
        try { File file =new File(path);
        if(file.exists())
            file.delete();
        file.createNewFile();
        FileOutputStream out;

            out = new FileOutputStream(file);
            ObjectOutputStream objOut=new ObjectOutputStream(out);
            objOut.writeObject(obj);
            objOut.flush();
            objOut.close();
            System.out.println("write object success!");
        } catch (IOException e) {
            System.out.println("write object failed");
            e.printStackTrace();
        }
    }
    public static Object readObjectFromFile(String path)
    {
        Object temp=null;
        File file =new File(path);
        FileInputStream in;
        if(file.exists())
        try {
            in = new FileInputStream(file);
            ObjectInputStream objIn=new ObjectInputStream(in);
            temp=objIn.readObject();
            objIn.close();
            System.out.println("read object success!");
        } catch (IOException e) {
            System.out.println("read object failed");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return temp;
    }
}
