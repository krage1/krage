package ru.krage.clock;

import static ru.krage.clock.Clock.workRequest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.work.WorkManager;


public class ClockConfigureActivity extends Activity implements View.OnClickListener {

    int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    Intent resultValue;

    public final static String WIDGET_PREF = "widget_pref";
    public final static String WIDGET_BACKGROUND_IMG = "widget_background_img";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //   Извлекаем ID конфигурируемого виджета
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null){
            widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        //   И проверяем его корректность
        if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID){
            finish();
        }
        //   Формируем INTENT  ответа
        resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);

        //   Отрицательный ответ
        setResult(RESULT_CANCELED, resultValue);

        setContentView(R.layout.clock_configure);

        Button btnOK = findViewById(R.id.btnOK);
        btnOK.setOnClickListener(this);
    }

    //   Вычисляем значения с экрана
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int selRadioBtn = ((RadioGroup) findViewById(R.id.rgBackground)).getCheckedRadioButtonId();
        Context context = getApplicationContext();
        int imgBackground = 0;
        switch (selRadioBtn){
            case R.id.radioBrown: imgBackground = R.drawable.img_wbgr_brown; break;
            case R.id.radioViolet: imgBackground = R.drawable.img_wbgr_violet; break;
            case R.id.radioSalad: imgBackground = R.drawable.img_wbgr_salad; break;
            case R.id.radioRed: imgBackground = R.drawable.img_wbgr_red; break;
            case R.id.radioGreen: imgBackground = R.drawable.img_wbgr_green; break;
            case R.id.radioBlue: imgBackground = R.drawable.img_wbgr_blue; break;
            case R.id.radioLightGrey: imgBackground = R.drawable.img_wbgr_light_grey; break;
            case R.id.radioDarkGrey: imgBackground = R.drawable.img_wbgr_dark_grey; break;
            case R.id.radioEmpty: break;
        }
        //   Записываем значения с экрана в Preferences
        SharedPreferences sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(WIDGET_BACKGROUND_IMG + widgetID, imgBackground);
        editor.apply();

        WorkManager.getInstance(context).enqueue(workRequest);   //   Запуск  Worker-a

        //     Обновляем виджет
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
        Clock.updateAppWidget(this, widgetManager, widgetID);
        //   Положительный ответ
        setResult(RESULT_OK, resultValue);
        finish();
    }
}