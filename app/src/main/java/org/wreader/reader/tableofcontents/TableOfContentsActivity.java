package org.wreader.reader.tableofcontents;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.wreader.reader.R;
import org.wreader.reader.core.BaseActivity;
import org.wreader.reader.reader.BookDataHelper;
import org.wreader.reader.reader.Chapter;

import java.util.List;

public class TableOfContentsActivity extends BaseActivity implements View.OnClickListener {
    public static final String PARAM_KEY_SELECTED_CHAPTER_ID = "SELECTED_CHAPTER_ID";

    private String bookId;

    private String selectedChapterId;

    private RecyclerView recyclerView;

    private TableOfContentsAdapter adapter;

    @Override
    protected boolean isFullScreen() {
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bookId = getIntent().getStringExtra(PARAM_KEY_BOOK_ID);
        selectedChapterId = getIntent().getStringExtra(PARAM_KEY_SELECTED_CHAPTER_ID);
        setContentView(R.layout.table_of_contents_activity);
        findViewById(R.id.cancel_button).setOnClickListener(this);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TableOfContentsAdapter(this);
        recyclerView.setAdapter(adapter);
        BookDataHelper.loadTableOfContents(bookId, new BookDataHelper.DataLoadedCallback<List<Chapter>>() {
            @Override
            public void onDataLoaded(List<Chapter> tableOfContents) {
                adapter.setData(tableOfContents, selectedChapterId);
                for (int i = 0; i < tableOfContents.size(); i++) {
                    Chapter chapter = tableOfContents.get(i);
                    if (selectedChapterId.equals(chapter.id)) {
                        recyclerView.scrollToPosition(i);
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancel_button: {
                setResult(RESULT_CANCELED);
                finish();
                break;
            }
            default: {
                break;
            }
        }
    }
}
