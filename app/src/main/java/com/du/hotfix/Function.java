package com.du.hotfix;


import android.util.Log;

public class Function {
    public void test(){
        throw new NullPointerException("My error!");
//        Log.e("Tag" , "hello world");
    }
}
