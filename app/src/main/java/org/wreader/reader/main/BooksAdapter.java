package org.wreader.reader.main;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.wreader.reader.R;
import org.wreader.reader.core.helper.Router;
import org.wreader.reader.reader.beans.Book;
import org.wreader.reader.reader.model.BookDataHelper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.ViewHolder> implements View.OnClickListener {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    private final Context context;

    private List<Book> books;

    BooksAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.main_activity_item, parent, false);
        BooksAdapter.ViewHolder holder = new BooksAdapter.ViewHolder(view);
        holder.itemView.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book book = books.get(position);
        holder.bookNameTextView.setText(book.name);
        holder.introductionTextView.setText(book.introduction);
        if (book.lastReadTime == 0) {
            holder.lastReadTimeLabel.setVisibility(View.GONE);
            holder.lastReadTimeTextView.setVisibility(View.GONE);
        } else {
            holder.lastReadTimeTextView.setText(DATE_FORMAT.format(new Date(book.lastReadTime)));
            holder.lastReadTimeLabel.setVisibility(View.VISIBLE);
            holder.lastReadTimeTextView.setVisibility(View.VISIBLE);
        }
        holder.itemView.setTag(book);
    }

    @Override
    public int getItemCount() {
        return (books != null) ? books.size() : 0;
    }

    @Override
    public void onClick(View view) {
        final int viewId = view.getId();
        if (viewId == R.id.item_view) {
            if (view.getTag() instanceof Book) {
                Book book = (Book) view.getTag();
                book.lastReadTime = System.currentTimeMillis();
                BookDataHelper.saveBooks(books);
                try {
                    Router.route(context, String.format("wreader://readBook?id=%s", URLEncoder.encode(book.id, StandardCharsets.UTF_8.name())));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        } else {
            // Do nothing.
        }
    }

    void setData(List<Book> books) {
        this.books = books;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView bookNameTextView;

        private final TextView introductionTextView;

        private final TextView lastReadTimeLabel;

        private final TextView lastReadTimeTextView;

        ViewHolder(View itemView) {
            super(itemView);
            bookNameTextView = itemView.findViewById(R.id.book_name_text_view);
            introductionTextView = itemView.findViewById(R.id.introduction_text_view);
            lastReadTimeLabel = itemView.findViewById(R.id.last_read_time_label);
            lastReadTimeTextView = itemView.findViewById(R.id.last_read_time_text_view);
        }
    }
}
