package com.alpha.museum.museum;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import static com.alpha.museum.museum.MainActivity.CHANNEL_ID;

public class ListeningService extends Service {

    String messageBody = null;
    String address = null;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
                SmsMessage[] smsMessage = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                messageBody = smsMessage[0].getMessageBody();
                address = smsMessage[0].getOriginatingAddress();
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("onListening")
                .setContentText("...")
                .setSmallIcon(R.drawable.head_image)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.setPriority(999);

        registerReceiver(receiver,intentFilter);
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
