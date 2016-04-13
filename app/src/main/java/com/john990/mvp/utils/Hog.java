package com.john990.mvp.utils;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by wangkai19 on 2014/12/19.
 */
public class Hog {
    public static final String TAG = "Hog";

    public static boolean DEBUG = false;

    public static void showable(boolean debug) {
        DEBUG = debug;
    }

    public static void v(String subTag, String msg) {
        if (!DEBUG) return;
        Log.v(TAG, getLogMsg(subTag, msg));
    }

    public static void d(String subTag, String msg) {
        if (!DEBUG) return;
        Log.d(TAG, getLogMsg(subTag, msg));
    }

    public static void i(String subTag, String msg) {
        if (!DEBUG) return;
        Log.i(TAG, getLogMsg(subTag, msg));
    }

    public static void w(String subTag, String msg) {
        if (!DEBUG) return;
        Log.w(TAG, getLogMsg(subTag, msg));
    }

    public static void w(String subTag, String msg, Throwable e) {
        if (!DEBUG) return;
        Log.w(TAG, getLogMsg(subTag, msg + " Exception: " + getExceptionMsg(e)));
    }

    public static void e(String subTag, String msg) {
        if (!DEBUG) return;
        Log.e(TAG, getLogMsg(subTag, msg));
    }

    public static void e(String subTag, String msg, Throwable e) {
        if (!DEBUG) return;
        Log.e(TAG, getLogMsg(subTag, msg + " Exception: " + getExceptionMsg(e)));
    }

    public static void d(Exception e) {
        if (!DEBUG) return;
        Log.d(TAG, e.getMessage());
    }

    public static void e(Throwable e) {
        if (!DEBUG) return;
        e.printStackTrace();
    }

    private static String getLogMsg(String subTag, String msg) {
        return "[" + subTag + "] " + msg;
    }

    private static String getExceptionMsg(Throwable e) {
        StringWriter sw = new StringWriter(1024);
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }
}
