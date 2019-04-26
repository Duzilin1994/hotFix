package com.du.hotfix;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.IOException;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Log.e("tag" , this.getClassLoader().toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE , Manifest.permission.READ_EXTERNAL_STORAGE} , 1024);
        }
        File dexDir = new File(Environment.getExternalStorageDirectory() , "hotFix");
        if(!dexDir.exists()){
            dexDir.mkdirs();
        }
    }

    public void next(View view) {
        startActivity(new Intent(this , MainActivity.class));
        this.finish();
    }

    public void fix(View view) {
        FixUtils.fix(this);
    }
}
