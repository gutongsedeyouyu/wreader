package org.wreader.reader.core;

import android.app.Application;

import org.wreader.reader.core.helper.FileHelper;
import org.wreader.reader.reader.ReaderTtsHelper;

import java.io.IOException;

public class App extends Application {
    public static final String CRASH_LOG_PATH = "crash.log";

    private static App INSTANCE;

    public static App getInstance() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        setUncaughtExceptionHandler();
        ReaderTtsHelper.init(this);
    }

    private void setUncaughtExceptionHandler() {
        final Thread.UncaughtExceptionHandler exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable exception) {
                StringBuilder stringBuilder = new StringBuilder();
                Throwable throwable = exception;
                for (int i = 0; i < 5 && throwable != null; i++) {
                    if (i > 0) {
                        stringBuilder.append("\nCaused by: ");
                    }
                    stringBuilder.append(throwable.getClass().getName());
                    stringBuilder.append(": ");
                    stringBuilder.append(throwable.getMessage());
                    for (StackTraceElement element : throwable.getStackTrace()) {
                        stringBuilder.append("\n  at ");
                        stringBuilder.append(element.toString());
                    }
                    throwable = throwable.getCause();
                }
                try {
                    FileHelper.writeTextToCache(CRASH_LOG_PATH, stringBuilder.toString());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                if (exceptionHandler != null) {
                    exceptionHandler.uncaughtException(thread, exception);
                }
            }
        });
    }
}
