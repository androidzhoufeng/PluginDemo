package com.example.zfplugin;

import android.app.Activity;
import android.content.res.Resources;

public abstract class BaseActivity extends Activity {

    @Override
    public Resources getResources() {

        Resources resources = LoadUtil.getResources(getApplication());

        return resources == null ? super.getResources() : resources;
    }
}
