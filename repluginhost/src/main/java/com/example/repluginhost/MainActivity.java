package com.example.repluginhost;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.qihoo360.replugin.RePlugin;

/**
 * 360插件-宿主
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btnLoadPlugin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadUtil.installPlugin();
            }
        });

        findViewById(R.id.btnUninstallPlugin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean uninstall = RePlugin.uninstall("replugin-debug");
                if (uninstall){
                    Toast.makeText(MainActivity.this, "卸载成功", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MainActivity.this, "卸载失败", Toast.LENGTH_SHORT).show();
                }

            }
        });
        findViewById(R.id.btnJumpActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"点击路启动按钮",Toast.LENGTH_SHORT).show();
                RePlugin.startActivity(MainActivity.this,
                        RePlugin.createIntent("replugin-debug", "com.example.replugin.MainActivity"));

            }
        });
    }
}
