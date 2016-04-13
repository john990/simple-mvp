package com.john990.mvp;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.support.multidex.MultiDex;

import com.activeandroid.ActiveAndroid;
import com.john990.mvp.utils.Hog;
import com.john990.mvp.utils.WifiHelper;

/**
 * Created by John on 16/4/10.
 */
public class AutoApplication extends Application {
    private static Application context;

    private static final Handler handler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        Hog.showable(BuildConfig.DEBUG);
        ActiveAndroid.initialize(this);
        WifiHelper.init(this);
    }

    public static Application getContext() {
        return context;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static void runOnMainThread(Runnable runnable) {
        handler.post(runnable);
    }
}
