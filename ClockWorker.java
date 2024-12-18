package ru.krage.clock;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ClockWorker extends Worker {
    public ClockWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        Clock clock = new Clock();
        clock.onEnabled(context);
        return Result.success();
    }
}
