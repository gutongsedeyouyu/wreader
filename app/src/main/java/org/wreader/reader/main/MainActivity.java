package org.wreader.reader.main;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.wreader.reader.R;
import org.wreader.reader.core.BaseActivity;
import org.wreader.reader.reader.Book;
import org.wreader.reader.reader.BookDataHelper;

import java.util.List;

public class MainActivity extends BaseActivity {
    private RecyclerView recyclerView;

    private BooksAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
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
}
