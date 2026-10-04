package com.salem.prayer;

import java.util.Calendar;
import java.util.TimeZone;

public class PrayerTimesCalculator {
    public static class Times {
        public String fajr, sunrise, dhuhr, asr, sunset, maghrib, isha;
        public Times(String fajr, String sunrise, String dhuhr, String asr, String sunset, String maghrib, String isha) {
            this.fajr=fajr; this.sunrise=sunrise; this.dhuhr=dhuhr; this.asr=asr; this.sunset=sunset; this.maghrib=maghrib; this.isha=isha;
        }
    }

    private static final double LAT = 30.4667; // Benha
    private static final double LNG = 31.1833;
    private static final double FAJR_ANGLE = 19.5; // Egyptian General Authority of Survey
    private static final double ISHA_ANGLE = 17.5;

    public static Times calculate(Calendar date) {
        double jd = julian(date.get(Calendar.YEAR), date.get(Calendar.MONTH)+1, date.get(Calendar.DAY_OF_MONTH));
        double d = jd - 2451545.0;
        double g = fixAngle(357.529 + 0.98560028*d);
        double q = fixAngle(280.459 + 0.98564736*d);
        double L = fixAngle(q + 1.915*sin(g) + 0.020*sin(2*g));
        double e = 23.439 - 0.00000036*d;
        double ra = atan2(cos(e)*sin(L), cos(L))/15.0;
        double eqt = q/15.0 - fixHour(ra);
        double decl = asin(sin(e)*sin(L));
        double noon = fixHour(12 - eqt + (0 - LNG)/15.0 + (timezoneOffsetHours(date))/1.0);
        // The formula above uses local standard/DST offset. Solar noon is 12 - equation of time - longitude/15 + timezone.
        double fajr = noon - hourAngle(FAJR_ANGLE, decl)/15.0;
        double sunrise = noon - hourAngle(0.833, decl)/15.0;
        double sunset = noon + hourAngle(0.833, decl)/15.0;
        double maghrib = sunset;
        double asr = noon + asrTime(1, decl)/15.0;
        double isha = noon + hourAngle(ISHA_ANGLE, decl)/15.0;
        return new Times(fmt(fajr),fmt(sunrise),fmt(noon),fmt(asr),fmt(sunset),fmt(maghrib),fmt(isha));
    }

    private static double timezoneOffsetHours(Calendar c) { return c.getTimeZone().getOffset(c.getTimeInMillis()) / 3600000.0; }
    private static double asrTime(int factor,double decl) { return hourAngle(-arccot(factor + cot(Math.abs(LAT-decl))), decl); }
    private static double hourAngle(double angle,double decl) { double x=(-sin(angle)-sin(LAT)*sin(decl))/(cos(LAT)*cos(decl)); x=Math.max(-1,Math.min(1,x)); return acos(x); }
    private static String fmt(double h) { h=fixHour(h); int hr=(int)Math.floor(h); int min=(int)Math.round((h-hr)*60); if(min>=60){hr=(hr+1)%24;min=0;} return String.format(java.util.Locale.US,"%02d:%02d",hr,min); }
    private static double julian(int y,int m,int day){ if(m<=2){y--;m+=12;} int A=y/100; int B=2-A+A/4; return Math.floor(365.25*(y+4716))+Math.floor(30.6001*(m+1))+day+B-1524.5; }
    private static double sin(double x){return Math.sin(Math.toRadians(x));} private static double cos(double x){return Math.cos(Math.toRadians(x));} private static double tan(double x){return Math.tan(Math.toRadians(x));}
    private static double asin(double x){return Math.toDegrees(Math.asin(x));} private static double acos(double x){return Math.toDegrees(Math.acos(x));} private static double atan2(double y,double x){return Math.toDegrees(Math.atan2(y,x));}
    private static double arccot(double x){return 90-Math.toDegrees(Math.atan(x));} private static double cot(double x){return 1.0/tan(x);}
    private static double fixAngle(double a){return a-360*Math.floor(a/360.0);} private static double fixHour(double h){return h-24*Math.floor(h/24.0);}
}
