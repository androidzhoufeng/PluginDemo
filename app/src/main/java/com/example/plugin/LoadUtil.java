package com.example.plugin;

import android.content.Context;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;

/**
 * 处理插件中普通类
 */
public class LoadUtil {


    private final static String apkPath = "/sdcard/zfplugin-debug.apk";

    public static void loadClass(Context context) {

        try {
            // dalvik/system/DexPathList.java
            // 2.dexElements 的 Field 对象 --》只与类相关，和对象不相关
            Class<?> clazz = Class.forName("dalvik.system.DexPathList");
            Field dexElementsField = clazz.getDeclaredField("dexElements");
            dexElementsField.setAccessible(true);

            // 4.pathList 的 Field 对象
            Class<?> baseClassLoaderClass = Class.forName("dalvik.system.BaseDexClassLoader");
            Field pathListField = baseClassLoaderClass.getDeclaredField("pathList");
            pathListField.setAccessible(true);

            // 5.BaseDexClassLoader  的类的对象
            // 宿主 -- 类加载器  -- PathClassLoader
            ClassLoader hostClassLoader = context.getClassLoader();

            // 3.DexPathList 的类的对象
            Object hostPathList = pathListField.get(hostClassLoader);

            // 宿主 、 插件
            // 1.目的：dexElements 的对象
            Object[] hostDexElements = (Object[]) dexElementsField.get(hostPathList);


            // 5.BaseDexClassLoader  的类的对象
            // 插件 -- 类加载器  -- PathClassLoader
            ClassLoader pluginClassLoader = new DexClassLoader(apkPath,
                    context.getCacheDir().getAbsolutePath(), null, hostClassLoader);


            // 3.DexPathList 的类的对象
            Object pluginPathList = pathListField.get(pluginClassLoader);

            // 宿主 、 插件
            // 1.目的：dexElements 的对象
            Object[] pluginDexElements = (Object[]) dexElementsField.get(pluginPathList);


            // 创建一个新的数组  Element  --> new Element[]
            Object[] newElement = (Object[]) Array.newInstance(
                    hostDexElements.getClass().getComponentType(),
                    hostDexElements.length + pluginDexElements.length);

            // 赋值
            System.arraycopy(hostDexElements, 0, newElement,
                    0, hostDexElements.length);
            System.arraycopy(pluginDexElements, 0,
                    newElement, hostDexElements.length, pluginDexElements.length);

            // 宿主里面的 dexElements
            // hostDexElements = newElement;
            dexElementsField.set(hostPathList, newElement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
