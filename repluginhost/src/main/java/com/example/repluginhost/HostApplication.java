package com.example.repluginhost;

import android.content.Context;

import com.qihoo360.replugin.RePluginApplication;

/**
 * @Description
 * @Author zhoufeng
 * @Date 2020-04-01 10:26
 */

public class HostApplication extends RePluginApplication {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext() {
        return mContext;
    }
}
