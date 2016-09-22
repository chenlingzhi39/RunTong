package com.example;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import sun.rmi.runtime.Log;

public class MyClass {
    public static void main(String[] args) {
       ArrayList<StringBuilder> aa=new ArrayList<>();
        StringBuilder builder=new StringBuilder("ss");
        aa.add(builder);
        StringBuilder builder1=aa.get(0);
        builder1=null;
        System.out.println(aa.get(0));
    }

}
