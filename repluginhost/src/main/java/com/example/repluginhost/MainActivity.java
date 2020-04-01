package com.example.repluginhost;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.qihoo360.replugin.RePlugin;

/**
 * 360插件-宿主
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         *
         */
        findViewById(R.id.btnLoadBean).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!LoadUtil.installPlugin())return;
                RePlugin.startActivity(MainActivity.this,
                        RePlugin.createIntent("replugin","com.example.replugin.MainActivity"));

            }
        });
    }
}
