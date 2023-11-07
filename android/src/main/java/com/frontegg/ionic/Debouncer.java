package com.frontegg.ionic;

import java.util.Timer;
import java.util.TimerTask;

public class Debouncer {
    private final long delayMillis;
    private Timer timer = new Timer();
    private TimerTask task;

    public Debouncer(long delayMillis) {
        this.delayMillis = delayMillis;
    }

    public void debounce(final Runnable action) {
        if (task != null) {
            task.cancel();
        }
        task = new TimerTask() {
            @Override
            public void run() {
                action.run();
            }
        };
        timer.schedule(task, delayMillis);
    }
}
