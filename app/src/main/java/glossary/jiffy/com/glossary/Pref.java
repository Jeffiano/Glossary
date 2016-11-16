package glossary.jiffy.com.glossary;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2016/11/1.
 */
public class Pref {
    private final static String PREF_NAME = "yqtec.pref";

    private static SharedPreferences getPref(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static long getPrevSyncTime(Context c) {
        return getPref(c).getLong("sync_time", 0);
    }

    public static void savePrevSyncTime(Context c, long time) {
        getPref(c).edit().putLong("sync_time", time).commit();
    }

    public static String getCookie(Context context) {
        return getPref(context).getString("cookie", null);
    }

    public static void saveCookie(Context context, String cookie) {
        getPref(context).edit().putString("cookie", cookie).commit();
    }

}
