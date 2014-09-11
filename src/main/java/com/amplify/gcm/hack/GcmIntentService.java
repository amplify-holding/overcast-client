package com.amplify.gcm.hack;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService {
    private static final String TAG = "GCM Demo";
    private static final int NOTIFICATION_ID = 1;

    private NotificationManager mNotificationManager;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        String action = intent.getAction();
        if(!extras.isEmpty() && !action.equals("com.google.android.c2dm.intent.REGISTRATION")) {
            if(messageType.equals(GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR)) {
                sendNotification("Send error: " + extras.toString());
            }
            else if(messageType.equals(GoogleCloudMessaging.MESSAGE_TYPE_DELETED)) {
                sendNotification("Deleted messages on server: " + extras.toString());
            }
            else if(messageType.equals(GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE)) {
//                for(int i = 0; i < 5; i++) {
//                    Log.i(TAG, "Working... " + (i+1) + "/5 @ " + SystemClock.elapsedRealtime());
//                    try {
//                        Thread.sleep(5000);
//                    }
//                    catch (InterruptedException e) {}
//                }
                Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());

                sendNotification("Received: " + extras.toString());
                Log.i(TAG, "Received: " + extras.toString());
            }
        }

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, DemoActivity.class), 0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                                                    .setSmallIcon(R.drawable.ic_stat_gcm)
                                                    .setContentTitle("GCM Notification")
                                                    .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                                                    .setContentText(msg);
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
