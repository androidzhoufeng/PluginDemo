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

    public static void hookAMS() {
        try {
            // 1. ActivityManager 的Class 对象
            Field singletonField = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Class<?> clazz = Class.forName("android.app.ActivityManager");
                singletonField = clazz.getDeclaredField("IActivityManagerSingleton");

            } else {
                Class<?> clazz = Class.forName("android.app.ActivityManagerNative");
                singletonField = clazz.getDeclaredField("gDefault");
            }
            singletonField.setAccessible(true);
            Object singleton = singletonField.get(null);

            // 获取mInstance对象
//            Class<?> singletonClass = singleton.getClass();
            Class<?> singletonClass = Class.forName("android.util.Singleton");
            Field mInstanceField = singletonClass.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);
            final Object mInstance = mInstanceField.get(singleton);

            Class<?> iActivityManagerClass = Class.forName("android.app.IActivityManager");

            // new IActivityManagerProxy();
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
                                //拿到了 intent --》 插件：1
                                Intent intent = (Intent) args[index];

                                // 替换
                                Intent proxyIntent = new Intent();
                                proxyIntent.setClassName("com.enjoy.leo_plugin",
                                        "com.enjoy.leo_plugin.ProxyActivity");

                                proxyIntent.putExtra(TARGET_INTENT, intent);

                                //代理替换了插件的
                                args[index] = proxyIntent;
                            }

                            // IActivityManager 对象
                            return method.invoke(mInstance, args);
                        }
                    });
            mInstanceField.set(singleton, proxyInstance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void hookHandler() {

        try {
            Class<?> clazz = Class.forName("android.app.ActivityThread");

            Field sCurrentActivityThreadField = clazz.getDeclaredField("sCurrentActivityThread");
            sCurrentActivityThreadField.setAccessible(true);
            Object activityThread = sCurrentActivityThreadField.get(null);

            Field mHField = clazz.getDeclaredField("mH");
            mHField.setAccessible(true);
            Object mH = mHField.get(activityThread);

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
                                // 替换的：Intent intent; --》 ActivityClientRecord的对象 == msg.obj
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
                            // List<ClientTransactionItem> mActivityCallbacks;
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
                                        // chajian == 1 daili== 2
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
