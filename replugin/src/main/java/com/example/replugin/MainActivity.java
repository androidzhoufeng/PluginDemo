package com.example.replugin;

import android.os.Bundle;
import android.view.View;

import com.qihoo360.replugin.loader.a.PluginActivity;

/**
 * 360插件
 */
public class MainActivity extends PluginActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnPrint).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TestUtils.print();
            }
        });
    }
}
