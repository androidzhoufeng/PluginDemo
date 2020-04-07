package com.example.plugin;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public class HookUtil {

    private final static String TARGET_INTENT = "target_intent";

    /**
     * 系统检测之前替换
     */
    public static void hookAMS() {
        try {
            // 1. ActivityTaskManager 的Class 对象
            Field singletonField = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Class<?> clazz = Class.forName("android.app.ActivityTaskManager");
                singletonField = clazz.getDeclaredField("IActivityTaskManagerSingleton");
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Class<?> clazz = Class.forName("android.app.ActivityManager");
                singletonField = clazz.getDeclaredField("IActivityManagerSingleton");
            } else {
                Class<?> clazz = Class.forName("android.app.ActivityManagerNative");
                singletonField = clazz.getDeclaredField("gDefault");
            }
            singletonField.setAccessible(true);
            //2. 获取 ActivityTaskManager 的 IActivityTaskManagerSingleton 属性的值（其他版本同理），参数是静态的所以对象可以传 null
            Object singleton = singletonField.get(null);

            //3. 获取 Singleton 类的 mInstance 属性值
            Class<?> singletonClass = Class.forName("android.util.Singleton");
            Field mInstanceField = singletonClass.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);
            final Object mInstance = mInstanceField.get(singleton);

            //4.获取 IActivityTaskManager 类的 class 对象
            Class<?> iActivityManagerClass = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                iActivityManagerClass = Class.forName("android.app.IActivityTaskManager");
            } else {
                iActivityManagerClass = Class.forName("android.app.IActivityManager");

            }
            // 5.通过动态代理实现 Activity 类的替换， new IActivityTaskManagerProxy()替换原先的 IActivityTaskManager
            Object proxyInstance = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class[]{iActivityManagerClass}, new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                            /**
                             * startActivity(whoThread, who.getBasePackageName(), intent,
                             *                         intent.resolveTypeIfNeeded(who.getContentResolver()),
                             *                         token, target != null ? target.mEmbeddedID : null,
                             *                         requestCode, 0, null, options);
                             */
                            // 过滤
                            if ("startActivity".equals(method.getName())) {
                                int index = 0;
                                for (int i = 0; i < args.length; i++) {
                                    if (args[i] instanceof Intent) {
                                        index = i;
                                        break;
                                    }
                                }
                                //拿到了原 intent
                                Intent intent = (Intent) args[index];
                                // new 一个代理 Intent 用来替换
                                Intent proxyIntent = new Intent();
                                proxyIntent.setClassName("com.example.plugin",
                                        "com.example.plugin.ProxyActivity");
                                //保留原本的intent
                                proxyIntent.putExtra(TARGET_INTENT, intent);
                                //代理替换了原来的
                                args[index] = proxyIntent;
                            }
                            // IActivityTaskManager 对象
                            return method.invoke(mInstance, args);
                        }
                    });
            mInstanceField.set(singleton, proxyInstance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 系统检测之后还原
     * 目的：通过重写 Handler 的 Callback 的 handleMessage() 方法， 然后赋值给 ActivityThread 类中 mH （handler 类对象）的 mCallback。
     *      在 Callback 的 handleMessage() 方法中换回原 intent
     */
    public static void hookHandler() {

        try {
            // 1、获取 ActivityThread 类的 sCurrentActivityThread （静态的）
            Class<?> clazz = Class.forName("android.app.ActivityThread");
            Field sCurrentActivityThreadField = clazz.getDeclaredField("sCurrentActivityThread");
            sCurrentActivityThreadField.setAccessible(true);

            // 2、通过 sCurrentActivityThread 获取 ActivityThread 对象
            Object activityThread = sCurrentActivityThreadField.get(null);

            // 3、获取 ActivityThread 类的 mH 属性
            Field mHField = clazz.getDeclaredField("mH");
            mHField.setAccessible(true);

            // 4、获取属性值
            Object mH = mHField.get(activityThread);

            // 5、获取 Handler 类的 mCallback 属性
            Class<?> handlerClass = Class.forName("android.os.Handler");
            Field mCallbackField = handlerClass.getDeclaredField("mCallback");
            mCallbackField.setAccessible(true);

            // 覆盖这个 final Callback mCallback;
            mCallbackField.set(mH, new Handler.Callback() {
                @Override
                public boolean handleMessage(@NonNull Message msg) {
                    // 修改 msg 的值

                    switch (msg.what) {
                        case 100:
                            try {
                                // 替换的：Intent intent; --》 ActivityClientRecord r = msg.obj
                                Field intentField = msg.obj.getClass().getDeclaredField("intent");
                                intentField.setAccessible(true);
                                // 代理的
                                Intent proxyIntent = (Intent) intentField.get(msg.obj);

                                // 获取插件的
                                Intent intent = proxyIntent.getParcelableExtra(TARGET_INTENT);

                                //替换
                                if (intent != null) {
                                    intentField.set(msg.obj, intent);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case 159:
                            // final ClientTransaction transaction = (ClientTransaction) msg.obj;
                            // List<ClientTransactionItem> mActivityCallbacks;（ClientTransactionItem --》子类 LaunchActivityItem）
                            // private Intent mIntent;
                            try {
                                Field mActivityCallbacksField = msg.obj.getClass().getDeclaredField("mActivityCallbacks");
                                mActivityCallbacksField.setAccessible(true);
                                List mActivityCallbacks = (List) mActivityCallbacksField.get(msg.obj);

                                for (int i = 0; i < mActivityCallbacks.size(); i++) {
                                    if (mActivityCallbacks.get(i).getClass().getName()
                                            .equals("android.app.servertransaction.LaunchActivityItem")) {
                                        Object launchActivityItem = mActivityCallbacks.get(i);
                                        Field mIntentField = launchActivityItem.getClass().getDeclaredField("mIntent");
                                        mIntentField.setAccessible(true);

                                        Intent proxyIntent = (Intent) mIntentField.get(launchActivityItem);
                                        // 获取插件的
                                        Intent intent = proxyIntent.getParcelableExtra(TARGET_INTENT);

                                        //替换
                                        if (intent != null) {
                                            mIntentField.set(launchActivityItem, intent);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
