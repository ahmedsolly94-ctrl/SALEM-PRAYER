package com.salem.prayer;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends Activity implements SensorEventListener {

    LinearLayout root, content;

    int green = Color.rgb(6, 63, 54);
    int gold = Color.rgb(216, 173, 85);
    int cream = Color.rgb(247, 241, 229);
    int dark = Color.rgb(23, 51, 46);

    String[] names = {
            "الفجر", "الشروق", "الظهر",
            "العصر", "المغرب", "العشاء"
    };

    String[] times = {
            "--:--", "--:--", "--:--",
            "--:--", "--:--", "--:--"
    };

    CountDownTimer timer;
    SensorManager sm;
    Sensor rotation;

    TextView qiblaDegree;
    TextView qiblaArrow;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        TimeZone.setDefault(
                TimeZone.getTimeZone("Africa/Cairo")
        );

        requestNotificationPermission();
        showHome();
        PrayerScheduler.scheduleToday(this);
    }

    void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(
                        Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.POST_NOTIFICATIONS
                    },
                    40
            );
        }
    }

    void loadTimes() {
        Calendar c = Calendar.getInstance();

        PrayerTimesCalculator.Times t =
                PrayerTimesCalculator.calculate(c);

        times = new String[]{
                t.fajr,
                t.sunrise,
                t.dhuhr,
                t.asr,
                t.maghrib,
                t.isha
        };
    }

    int minutes(String s) {
        String[] p = s.split(":");

        return Integer.parseInt(p[0]) * 60
                + Integer.parseInt(p[1]);
    }

    int nextPrayerIndex() {

        Calendar now =
                Calendar.getInstance();

        int current =
                now.get(Calendar.HOUR_OF_DAY) * 60
                        + now.get(Calendar.MINUTE);

        for (int i = 0; i < times.length; i++) {

            if (i != 1 &&
                    minutes(times[i]) > current) {
                return i;
            }
        }

        return 0;
    }

    String nextPrayerName() {
        return names[nextPrayerIndex()];
    }

    void startCountdown(final TextView out) {

        if (timer != null) {
            timer.cancel();
        }

        timer = new CountDownTimer(
                24 * 60 * 60 * 1000L,
                1000
        ) {

            @Override
            public void onTick(long x) {

                Calendar now =
                        Calendar.getInstance();

                int index =
                        nextPrayerIndex();

                String[] a =
                        times[index].split(":");

                Calendar target =
                        (Calendar) now.clone();

                target.set(
                        Calendar.HOUR_OF_DAY,
                        Integer.parseInt(a[0])
                );

                target.set(
                        Calendar.MINUTE,
                        Integer.parseInt(a[1])
                );

                target.set(Calendar.SECOND, 0);
                target.set(Calendar.MILLISECOND, 0);

                if (target.before(now)) {
                    target.add(
                            Calendar.DAY_OF_MONTH,
                            1
                    );
                }

                long d =
                        target.getTimeInMillis()
                                - now.getTimeInMillis();

                out.setText(
                        String.format(
                                Locale.US,
                                "%02d:%02d:%02d",
                                d / 3600000,
                                (d / 60000) % 60,
                                (d / 1000) % 60
                        )
                );
            }

            @Override
            public void onFinish() {
                startCountdown(out);
            }

        }.start();
    }

    /*
     * TextView عربي بسيط
     * بدون خلط Emoji مع النص العربي.
     */
    TextView tv(String text, float size, int color) {

        TextView t =
                new TextView(this);

        t.setText(text);
        t.setTextSize(size);
        t.setTextColor(color);

        if (text.matches(".*[\\u0600-\\u06FF].*")) {

            t.setTextDirection(
                    View.TEXT_DIRECTION_RTL
            );

            t.setLayoutDirection(
                    View.LAYOUT_DIRECTION_RTL
            );

            t.setGravity(
                    Gravity.RIGHT |
                            Gravity.CENTER_VERTICAL
            );

            t.setTypeface(
                    Typeface.DEFAULT
            );

        } else {

            t.setGravity(
                    Gravity.CENTER_VERTICAL
            );
        }

        t.setPadding(
                18, 8, 18, 8
        );

        return t;
    }

    GradientDrawable bg(int color, float radius) {

        GradientDrawable g =
                new GradientDrawable();

        g.setColor(color);
        g.setCornerRadius(radius);

        return g;
    }

    LinearLayout row() {

        LinearLayout l =
                new LinearLayout(this);

        l.setOrientation(
                LinearLayout.HORIZONTAL
        );

        l.setGravity(
                Gravity.CENTER_VERTICAL
        );

        l.setPadding(
                8, 3, 8, 3
        );

        return l;
    }

    void base() {

        root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(
                cream
        );

        content =
                new LinearLayout(this);

        content.setOrientation(
                LinearLayout.VERTICAL
        );

        content.setPadding(
                14, 10, 14, 8
        );

        ScrollView scroll =
                new ScrollView(this);

        scroll.addView(content);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        LinearLayout nav =
                row();

        nav.setBackgroundColor(
                Color.WHITE
        );

        String[] navNames = {
                "الرئيسية",
                "الأذكار",
                "الأحاديث",
                "القبلة",
                "التسبيح"
        };

        for (String name : navNames) {

            TextView q =
                    tv(
                            name,
                            12,
                            green
                    );

            q.setGravity(
                    Gravity.CENTER
            );

            nav.addView(
                    q,
                    new LinearLayout.LayoutParams(
                            0,
                            64,
                            1
                    )
            );

            if (name.equals("الرئيسية")) {
                q.setOnClickListener(
                        v -> showHome()
                );
            }

            if (name.equals("الأذكار")) {
                q.setOnClickListener(
                        v -> showAzkar()
                );
            }

            if (name.equals("الأحاديث")) {
                q.setOnClickListener(
                        v -> showHadith()
                );
            }

            if (name.equals("القبلة")) {
                q.setOnClickListener(
                        v -> showQibla()
                );
            }

            if (name.equals("التسبيح")) {
                q.setOnClickListener(
                        v -> showTasbeeh()
                );
            }
        }

        root.addView(nav);

        setContentView(root);
    }

    void header(String title) {

        LinearLayout h =
                row();

        TextView logo =
                tv(
                        title,
                        22,
                        gold
                );

        logo.setTypeface(
                Typeface.DEFAULT_BOLD
        );

        logo.setGravity(
                Gravity.CENTER
        );

        h.addView(
                logo,
                new LinearLayout.LayoutParams(
                        0,
                        58,
                        1
                )
        );

        content.addView(h);
    }

    void showHome() {

        base();
        loadTimes();

        header("Salem Prayer");

        TextView loc =
                tv(
                        "بنها، القليوبية، مصر",
                        15,
                        dark
                );

        loc.setGravity(
               
