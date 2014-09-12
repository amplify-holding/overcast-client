package com.amplify.gcm.hack;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    private static final String TAG = "GcmBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String registrationId = intent.getStringExtra("registration_id");
        Log.i(TAG, "registration id = " + registrationId);

        ComponentName comp = new ComponentName(context.getPackageName(), GcmIntentService.class.getName());
        intent.putExtra("received-time", NTPClient.getSyncedTime().toString());
        startWakefulService(context, intent.setComponent(comp));
        setResultCode(Activity.RESULT_OK);
    }
}
