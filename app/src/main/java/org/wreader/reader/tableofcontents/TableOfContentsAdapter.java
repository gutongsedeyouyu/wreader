package org.wreader.reader.tableofcontents;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.wreader.reader.R;
import org.wreader.reader.reader.Chapter;

import java.util.List;

class TableOfContentsAdapter extends RecyclerView.Adapter<TableOfContentsAdapter.ViewHolder> implements View.OnClickListener {
    private final TableOfContentsActivity context;

    private List<Chapter> tableOfContents;

    private String selectedChapterId;

    TableOfContentsAdapter(TableOfContentsActivity context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.table_of_contents_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        holder.itemView.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chapter chapter = tableOfContents.get(position);
        if (selectedChapterId.equals(chapter.id)) {
            holder.primaryTextView.setTextColor(context.getResources().getColor(R.color.fg_checked));
        } else {
            holder.primaryTextView.setTextColor(context.getResources().getColor(R.color.fg_primary));
        }
        holder.primaryTextView.setText(chapter.title);
        holder.itemView.setTag(chapter);
    }

    @Override
    public int getItemCount() {
        return (tableOfContents != null) ? tableOfContents.size() : 0;
    }

    @Override
    public void onClick(View view) {
        final int viewId = view.getId();
        if (viewId == R.id.item_view) {
            if (view.getTag() instanceof Chapter) {
                Chapter chapter = (Chapter) view.getTag();
                Intent intent = new Intent();
                intent.putExtra(TableOfContentsActivity.PARAM_KEY_CHAPTER_ID, chapter.id);
                context.setResult(TableOfContentsActivity.RESULT_OK, intent);
                context.finish();
            }
        } else {
            // Do nothing.
        }
    }

    void setData(List<Chapter> tableOfContents, String selectedChapterId) {
        this.tableOfContents = tableOfContents;
        this.selectedChapterId = selectedChapterId;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView primaryTextView;

        ViewHolder(View itemView) {
            super(itemView);
            primaryTextView = itemView.findViewById(R.id.primary_text_view);
        }
    }
}
