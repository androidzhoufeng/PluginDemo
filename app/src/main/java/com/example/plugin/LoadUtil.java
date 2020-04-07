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
            // 1.获取 BaseDexClassLoader 类的 DexPathList 属性（对象名为：pathList）
            Class<?> baseClassLoaderClass = Class.forName("dalvik.system.BaseDexClassLoader");
            Field pathListField = baseClassLoaderClass.getDeclaredField("pathList");
            pathListField.setAccessible(true);

            // dalvik/system/DexPathList.java
            // 2.获取 DexPathList 类的 Element[] 属性（对象名为：dexElements）
            Class<?> clazz = Class.forName("dalvik.system.DexPathList");
            Field dexElementsField = clazz.getDeclaredField("dexElements");
            dexElementsField.setAccessible(true);

            // 3.获取宿主 -- 类加载器  -- PathClassLoader
            ClassLoader hostClassLoader = context.getClassLoader();

            // 4.获取宿主的 DexPathList 属性的值：pathList
            Object hostPathList = pathListField.get(hostClassLoader);

            // 5.目的一：获取宿主的 dexElements 属性的值：dexElements
            Object[] hostDexElements = (Object[]) dexElementsField.get(hostPathList);


            // 6.BaseDexClassLoader  的类的对象
            // new 一个 DexClassLoader 作为插件的类加载器
            ClassLoader pluginClassLoader = new DexClassLoader(apkPath,
                    context.getCacheDir().getAbsolutePath(), null, hostClassLoader);

            // 7.获取插件的 DexPathList 属性的值：pathList
            Object pluginPathList = pathListField.get(pluginClassLoader);

            // 8.目的二：获取插件的 dexElements 属性的值：dexElements
            Object[] pluginDexElements = (Object[]) dexElementsField.get(pluginPathList);


            // 9.创建一个新的数组  Element  --> new Element[]
            Object[] newElement = (Object[]) Array.newInstance(
                    hostDexElements.getClass().getComponentType(),
                    hostDexElements.length + pluginDexElements.length);

            // 10.把插件和数组的 hostDexElements、pluginDexElements 的值都赋值到新数组 newElement中
            System.arraycopy(hostDexElements, 0, newElement,
                    0, hostDexElements.length);
            System.arraycopy(pluginDexElements, 0,
                    newElement, hostDexElements.length, pluginDexElements.length);

            // 11. 把新数组赋值到宿主的 DexPathList 的 dexElementsField 属性上
            dexElementsField.set(hostPathList, newElement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
