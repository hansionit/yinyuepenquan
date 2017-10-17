package com.hansion;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;

/**
 * Description：
 * Author: Hansion
 * Time: 2017/10/17 11:36
 */
public class App  extends Application {

    //Context
    private static App mInstance;
    //TODO 上线前更改
    public static boolean isDebug = true;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        //判断是否是Debug模式
        isDebug = isApkDebugable();
    }

    public boolean isApkDebugable() {
        ApplicationInfo info = getApplicationInfo();
        return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }


    //For get Global Context
    public static Context getAppContext() {
        if (mInstance != null) {
            return mInstance;
        } else {
            mInstance = new App();
            mInstance.onCreate();
            return mInstance;
        }
    }
}