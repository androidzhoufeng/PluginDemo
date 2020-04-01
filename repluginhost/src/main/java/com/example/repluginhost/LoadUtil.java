package com.example.repluginhost;

import android.widget.Toast;

import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.model.PluginInfo;

import java.io.File;

/**
 * @Description TODO
 * @Author zhoufeng
 * @Date 2020-04-01 11:34
 */

public class LoadUtil {
    private final static String apkPath = "/sdcard/replugin-debug.apk";

    public static Boolean installPlugin() {
        File pluginFile = new File(apkPath);
        //文件不存在就返回
        if (!pluginFile.exists()){
            Toast.makeText(HostApplication.getContext(),"插件不存在",Toast.LENGTH_SHORT).show();
            return false;
        }

        PluginInfo pluginInfo = RePlugin.install(apkPath);

        if (pluginInfo == null){
            Toast.makeText(HostApplication.getContext(),"插件加载失败",Toast.LENGTH_SHORT).show();
            return false;
        }
        RePlugin.preload(pluginInfo);
        Toast.makeText(HostApplication.getContext(),"插件加载成功",Toast.LENGTH_SHORT).show();
        return true;
    }
}
