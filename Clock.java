package ru.krage.clock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Clock extends AppWidgetProvider {

    public final static String FORCE_WIDGETS_UPDATES = "force_widgets_updates";

    static int[] currentDates = new int[8];
    static int[] dateIDs = {R.id.im_day1, R.id.im_day2, R.id.im_month1, R.id.im_month2,
                                     R.id.im_year1, R.id.im_year2, R.id.im_year3, R.id.im_year4};
    static int[] dateDrawables = {R.drawable.date_0, R.drawable.date_1, R.drawable.date_2, R.drawable.date_3, R.drawable.date_4
                                  , R.drawable.date_5, R.drawable.date_6, R.drawable.date_7, R.drawable.date_8, R.drawable.date_9};
    static int[] dataWeeks = {R.drawable.week_1, R.drawable.week_2, R.drawable.week_3, R.drawable.week_4,
                                    R.drawable.week_5, R.drawable.week_6, R.drawable.week_7};

    static OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ClockWorker.class).build();

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        //   Читаем параметры Preferences
        SharedPreferences sp = context.getSharedPreferences(ClockConfigureActivity.WIDGET_PREF, Context.MODE_PRIVATE);
        int widgetBackground = sp.getInt(ClockConfigureActivity.WIDGET_BACKGROUND_IMG + appWidgetId, 0);

        // Создайте объект RemoteViews.
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.clock);
        views.setInt(R.id.rlBackground, "setBackgroundResource", widgetBackground);

        //    Вычисляем текущее время и дату
        Date currentTime = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentTime);
        int year = calendar.get(Calendar.YEAR);
        int month  = calendar.get(Calendar.MONTH);
        int dayMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int dayWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        int hour1 = hour / 10;
        int hour2 = hour % 10;
        int minute1 = minute / 10;
        int minute2 = minute % 10;
        int second1 = second / 10;
        int second2 = second % 10;

        //     Наполняем виджет данными
        views.setImageViewResource(R.id.im_hour1, getPicTime(hour1));
        views.setImageViewResource(R.id.im_hour2, getPicTime(hour2));
        views.setImageViewResource(R.id.im_minute1, getPicTime(minute1));
        views.setImageViewResource(R.id.im_minute2, getPicTime(minute2));
        views.setImageViewResource(R.id.im_second1, getPicSecond(second1));
        views.setImageViewResource(R.id.im_second2, getPicSecond(second2));
        views.setImageViewResource(R.id.im_dayWeek, dataWeeks[dayWeek - 1]);
        views.setImageViewResource(R.id.im_doubleDot, R.drawable.double_dot);
        views.setImageViewResource(R.id.im_dot, R.drawable.dot);
        views.setImageViewResource(R.id.im_dot1, R.drawable.dot);
        views.setImageViewResource(R.id.im_dot2, R.drawable.dot);

        getPicDate(dayMonth, month, year, currentDates);
        for (int i = 0; i < 8; i ++){
            int d = currentDates[i];
            views.setImageViewResource(dateIDs[i], dateDrawables[d]);
        }

        //     Конфигурационный экран
        Intent configIntent = new Intent(context, ClockConfigureActivity.class);
        configIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pIntent = PendingIntent.getActivity(context, appWidgetId, configIntent, PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.rlBackground, pIntent);

        // Поручите менеджеру виджетов обновить виджет
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction().equalsIgnoreCase(FORCE_WIDGETS_UPDATES)){
            ComponentName thisAppWidget = new ComponentName(context.getPackageName(),
                    getClass().getName());
            AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
            int [] ids = widgetManager.getAppWidgetIds(thisAppWidget);
            for (int appWidgetID : ids){
                updateAppWidget(context, widgetManager, appWidgetID);
            }
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Активных виджетов может быть несколько, поэтому обновите их все.
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        //      Удаляем Preferences
        SharedPreferences.Editor editor = context.getSharedPreferences(ClockConfigureActivity.WIDGET_PREF,
                Context.MODE_PRIVATE).edit();
        for (int appWidgetId : appWidgetIds) {
                editor.remove(ClockConfigureActivity.WIDGET_BACKGROUND_IMG + appWidgetId);
        }
        editor.apply();
    }

    @Override
    public void onEnabled(Context context) {
        // Введите соответствующую функциональность при создании первого виджета.
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, Clock.class);
        alarmIntent.setAction(FORCE_WIDGETS_UPDATES);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), null);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                alarmManager.setAlarmClock(alarmClockInfo, pIntent);
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Intent intent = new Intent(context, Clock.class);
        intent.setAction(FORCE_WIDGETS_UPDATES);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pIntent);

        WorkManager.getInstance(context).cancelWorkById(workRequest.getId());
    }

    private static void getPicDate(int dataD, int dataM, int dataY, int[] dataA){
        String sDate = dataD + Integer.toString(dataM+1) + dataY;
        for (int i = 0; i < sDate.length(); i ++){
            if (Character.isDigit(sDate.charAt(i))){
                dataA[i] =  Character.getNumericValue(sDate.charAt(i));
            }
        }
    }
    private static int getPicTime(int data){
       switch (data){
           case 0: return R.drawable.hour_0;
           case 1: return R.drawable.hour_1;
           case 2: return R.drawable.hour_2;
           case 3: return R.drawable.hour_3;
           case 4: return R.drawable.hour_4;
           case 5: return R.drawable.hour_5;
           case 6: return R.drawable.hour_6;
           case 7: return R.drawable.hour_7;
           case 8: return R.drawable.hour_8;
           case 9: return R.drawable.hour_9;
       }
       return data;
    }
    private static int getPicSecond(int data){
        switch (data){
            case 0: return R.drawable.sec_0;
            case 1: return R.drawable.sec_1;
            case 2: return R.drawable.sec_2;
            case 3: return R.drawable.sec_3;
            case 4: return R.drawable.sec_4;
            case 5: return R.drawable.sec_5;
            case 6: return R.drawable.sec_6;
            case 7: return R.drawable.sec_7;
            case 8: return R.drawable.sec_8;
            case 9: return R.drawable.sec_9;
        }
        return data;
    }
}