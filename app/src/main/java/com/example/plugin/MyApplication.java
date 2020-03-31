package com.example.plugin;

import android.app.Application;

public class MyApplication extends Application {

//    private Resources resources;

    @Override
    public void onCreate() {
        super.onCreate();

        LoadUtil.loadClass(this);

        HookUtil.hookAMS();
        HookUtil.hookHandler();
    }

}
