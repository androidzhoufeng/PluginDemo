package com.example.zfplugin;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.lang.reflect.Method;

public class LoadUtil {

    private final static String apkPath = "/sdcard/zfplugin-debug.apk";

    private static Resources mResources;

    public static Resources getResources(Context context) {
        if (mResources == null) {
            mResources = loadResources(context);
        }
        return mResources;
    }

    public static Resources loadResources(Context context) {
        try {
            // 1.创建一个 AssetManager
            AssetManager assetManager = AssetManager.class.newInstance();

            // 2.反射获取 addAssetPath 方法
            Method addAssetPathMethod = assetManager.getClass()
                    .getDeclaredMethod("addAssetPath", String.class);
            addAssetPathMethod.setAccessible(true);

            // 3.执行 addAssetPath 方法，将插件路径作为参数传入
            addAssetPathMethod.invoke(assetManager, apkPath);

            // 4.获取宿主的 resouces 对象，因为 context 是宿主的上下文
            Resources resources = context.getResources();

            //5.new 一个 Resources 对象并返回，将 assetManager 对象作为参数传入
            return new Resources(assetManager, resources.getDisplayMetrics(),
                    resources.getConfiguration());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
