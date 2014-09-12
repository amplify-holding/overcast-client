package com.amplify.gcm.hack;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

public class GcmIntentService extends IntentService {
    private static final String TAG = "GCM Demo";
    private static final int NOTIFICATION_ID = 1;
    private static final String SERVICES_IP = "192.168.1.9";

    private NotificationManager mNotificationManager;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            NTPClient.syncServerTime("time-d.nist.gov");
        } catch (IOException e) {
            e.printStackTrace();
        }

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
                Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());

                //sendNotification("Received: " + extras.toString());
                Log.i(TAG, "Received: " + extras.toString());
                String protocol = extras.getString("protocol-used");
                String receivedTime = extras.getString("received-time");
                String sentTime = extras.getString("sent-time");
                String regId = getRegistrationId(this);

                Log.i(TAG, "Perf:" + sentTime +":" + receivedTime);

                try {
                    String params = "";
                    if(extras.containsKey("perf-type")) {
                        params = "&perf-type=" + extras.getString("perf-type");
                    }

                    sendPerformanceMetric(protocol, receivedTime, sentTime, regId, params);
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

    private void sendPerformanceMetric(String protocol, String receivedTime, String sentTime, String regId, String additionalParams) throws IOException {
        //http://localhost:8080/gcm-demo/register
        //POST
        //regId=[registration id]
        String params = "?receivedTime=" + receivedTime + "&sentTime="+sentTime+additionalParams;

        URL url = new URL("http://" + SERVICES_IP + ":8080/" + protocol + "/performance/metrics" + params);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("POST");
        conn.setDoInput(true);

        Writer writer = new OutputStreamWriter(conn.getOutputStream());
        writer.write(regId);
        writer.flush();
        writer.close();

        int response = conn.getResponseCode();
        Log.d(TAG, "The response is: " + response);
        if(response == 200) {
            Log.d(TAG, "metrics => server: success");
        }
        else {
            Log.d(TAG, "metrics => server: failure");
        }
        conn.disconnect();
    }

    private String getRegistrationId(Context context) {
        SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(DemoActivity.PROPERTY_REG_ID, "");
        if(registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found");
            return "";
        }

        return registrationId;
    }

    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(DemoActivity.class.getSimpleName(), Context.MODE_PRIVATE);
    }
}
