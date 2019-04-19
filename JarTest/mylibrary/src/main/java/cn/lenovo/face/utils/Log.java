package cn.lenovo.face.utils;

import cn.lenovo.face.Contans;

/**
 * Created by baohm1 on 2018/3/15.
 */

public class Log {
    public static void d(String tag, String info) {
        if (Contans.LOG_DEBUG) {
            android.util.Log.d(tag, info);
        }
    }

    public static void e(String tag, String info) {
        android.util.Log.e(tag, info);
    }

    public static void i(String tag, String info) {
        android.util.Log.i(tag, info);
    }

    public static void v(String tag, String info) {
        android.util.Log.v(tag, info);
    }
}
