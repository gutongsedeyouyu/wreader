package org.wreader.reader.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import org.wreader.reader.R;
import org.wreader.reader.core.App;
import org.wreader.reader.core.BaseActivity;
import org.wreader.reader.core.helper.FileHelper;

import java.io.IOException;

public class CrashLogActivity extends BaseActivity implements View.OnClickListener {
    private TextView crashLogTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crash_log_activity);
        findViewById(R.id.action_bar_left_button).setOnClickListener(this);
        ((TextView) findViewById(R.id.action_bar_text_view)).setText(getString(R.string.crash_log));
        crashLogTextView = findViewById(R.id.crash_log_text_view);
        if (FileHelper.isCacheFileExists(App.CRASH_LOG_PATH)) {
            try {
                String crashLog = FileHelper.readTextFromCache(App.CRASH_LOG_PATH);
                crashLogTextView.setText(crashLog);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.action_bar_left_button: {
                finish();
                break;
            }
            default: {
                break;
            }
        }
    }
}
