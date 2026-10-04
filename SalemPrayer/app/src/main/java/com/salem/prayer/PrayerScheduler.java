package com.salem.prayer;

import android.app.*;
import android.content.*;
import android.os.Build;
import java.util.*;

public class PrayerScheduler {
    private static final String[] NAMES = {"الفجر","الظهر","العصر","المغرب","العشاء"};
    private static final int[] IDS = {11,12,13,14,15};

    public static void scheduleToday(Context context) {
        TimeZone.setDefault(TimeZone.getTimeZone("Africa/Cairo"));
        Calendar now = Calendar.getInstance();
        PrayerTimesCalculator.Times t = PrayerTimesCalculator.calculate(now);
        String[] ts = {t.fajr,t.dhuhr,t.asr,t.maghrib,t.isha};
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        for (int i=0;i<ts.length;i++) {
            String[] hm=ts[i].split(":");
            Calendar at=(Calendar)now.clone();
            at.set(Calendar.HOUR_OF_DAY,Integer.parseInt(hm[0])); at.set(Calendar.MINUTE,Integer.parseInt(hm[1]));
            at.set(Calendar.SECOND,0); at.set(Calendar.MILLISECOND,0);
            if(at.before(now)) continue;
            schedule(context, am, NAMES[i], IDS[i], at);
        }
    }

    private static void schedule(Context context, AlarmManager am, String prayer, int id, Calendar at) {
        Intent in=new Intent(context,PrayerAlarmReceiver.class).putExtra("prayer",prayer);
        PendingIntent pi=PendingIntent.getBroadcast(context,id,in,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        if(Build.VERSION.SDK_INT>=31 && !am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at.getTimeInMillis(),pi);
        } else if(Build.VERSION.SDK_INT>=23) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at.getTimeInMillis(),pi);
        } else { am.setExact(AlarmManager.RTC_WAKEUP,at.getTimeInMillis(),pi); }
    }
}
