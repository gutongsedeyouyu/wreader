package org.wreader.reader.core.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class BatteryBroadcastReceiver extends BroadcastReceiver {
    private static final BatteryBroadcastReceiver instance = new BatteryBroadcastReceiver();

    private float value;

    private BatteryBroadcastReceiver() {
    }

    public static BatteryBroadcastReceiver getInstance() {
        return instance;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
            int level = intent.getIntExtra("level", 0);
            int scale = intent.getIntExtra("scale", 100);
            value = 1.0f * level / scale;
        }
    }

    public void register(Context context) {
        context.registerReceiver(this, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    public void unregister(Context context) {
        context.unregisterReceiver(this);
    }

    public float getValue() {
        return value;
    }
}
