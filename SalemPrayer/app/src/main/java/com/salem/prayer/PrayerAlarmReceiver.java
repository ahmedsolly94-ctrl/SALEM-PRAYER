package com.salem.prayer;

import android.app.*;
import android.content.*;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

public class PrayerAlarmReceiver extends BroadcastReceiver {
    public static final String CHANNEL_ID = "prayer_times";
    @Override public void onReceive(Context context, Intent intent) {
        String prayer = intent.getStringExtra("prayer");
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            Uri sound = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI;
            AudioAttributes aa = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build();
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "مواقيت الصلاة", NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("تنبيهات دخول وقت الصلاة");
            ch.setSound(sound, aa);
            nm.createNotificationChannel(ch);
        }
        Intent open = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 100, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(context, CHANNEL_ID) : new Notification.Builder(context);
        b.setSmallIcon(com.salem.prayer.R.drawable.ic_launcher)
         .setContentTitle("Salem Prayer")
         .setContentText("حان الآن وقت صلاة " + prayer)
         .setAutoCancel(true).setContentIntent(pi).setPriority(Notification.PRIORITY_HIGH)
         .setDefaults(Notification.DEFAULT_VIBRATE);
        nm.notify(prayer.hashCode(), b.build());
        PrayerScheduler.scheduleToday(context);
    }
}
