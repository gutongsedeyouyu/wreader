package org.wreader.reader.core.helper;

import android.content.Context;
import android.content.SharedPreferences;

import org.wreader.reader.core.App;

public class SharedPreferencesHelper {
    private static final SharedPreferences sharedPreferences = App.getInstance().getSharedPreferences("WReader", Context.MODE_PRIVATE);

    private SharedPreferencesHelper() {
    }

    public static int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    public static void setInt(String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public static void setString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }
}
