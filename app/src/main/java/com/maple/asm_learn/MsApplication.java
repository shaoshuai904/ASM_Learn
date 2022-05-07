package com.maple.asm_learn;

import android.app.Application;
import android.content.Context;

public class MsApplication extends Application {
    public static MsApplication instance = null;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        instance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static Context getWBApplicationContext() {
        return instance;
    }
}
