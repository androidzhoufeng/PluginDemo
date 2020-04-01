package com.example.plugin;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                printClassLoader();

                try {
                    Class<?> clazz = Class.forName("com.example.zfplugin.Bean");
                    Method print = clazz.getMethod("print");
                    print.invoke(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.example.zfplugin",
                        "com.example.zfplugin.MainActivity"));
                startActivity(intent);

            }
        });
    }

    private void printClassLoader() {
        ClassLoader classLoader = getClassLoader();
        while (classLoader != null) {
            Log.e("========", "printClassLoader: " + classLoader);
            classLoader = classLoader.getParent();
        }
        Log.e("========", "printClassLoader111: " + Activity.class.getClassLoader());
        Log.e("========", "printClassLoader111: " + AppCompatActivity.class.getClassLoader());
    }

}
