package org.wreader.reader.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.wreader.reader.R;
import org.wreader.reader.core.BaseActivity;
import org.wreader.reader.reader.Book;
import org.wreader.reader.reader.BookDataHelper;

import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private TextView actionBarTextView;
    private long actionBarTextViewClickTime;
    private int actionBarTextViewClickTimes;
    private RecyclerView recyclerView;

    private BooksAdapter adapter;

    @Override
    protected boolean isStatusBarLight() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        actionBarTextView = findViewById(R.id.action_bar_text_view);
        actionBarTextView.setText(getResources().getString(R.string.app_name));
        actionBarTextView.setOnClickListener(this);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BooksAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BookDataHelper.loadBooks(new BookDataHelper.DataLoadedCallback<List<Book>>() {
            @Override
            public void onDataLoaded(List<Book> books) {
                adapter.setData(books);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.action_bar_text_view: {
                long now = System.currentTimeMillis();
                if (now < actionBarTextViewClickTime || now - actionBarTextViewClickTime > 3 * 1000) {
                    actionBarTextViewClickTime = now;
                    actionBarTextViewClickTimes = 0;
                    break;
                }
                if (++actionBarTextViewClickTimes == 7) {
                    Intent intent = new Intent(this, CrashLogActivity.class);
                    startActivity(intent);
                }
                break;
            }
            default: {
                break;
            }
        }
    }
}
