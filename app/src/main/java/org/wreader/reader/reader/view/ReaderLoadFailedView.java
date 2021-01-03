package org.wreader.reader.reader.view;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.wreader.reader.R;
import org.wreader.reader.reader.beans.Chapter;
import org.wreader.reader.reader.beans.ReaderColorSetting;

class ReaderLoadFailedView implements ReaderChildView, View.OnClickListener {
    private final ReaderViewImpl readerView;

    private View contentView;
    private TextView loadFailedTextView;
    private View reloadButton;

    ReaderLoadFailedView(ReaderViewImpl readerView) {
        this.readerView = readerView;
        contentView = LayoutInflater.from(readerView.getContext()).inflate(
                R.layout.reader_activity_load_failed, readerView, false);
        loadFailedTextView = contentView.findViewById(R.id.load_failed_text_view);
        reloadButton = contentView.findViewById(R.id.reload_button);
        reloadButton.setOnClickListener(this);
    }

    @Override
    public View getContentView() {
        return contentView;
    }

    @Override
    public void setColorSetting(ReaderColorSetting colorSetting) {
        loadFailedTextView.setTextColor(colorSetting.textColorSecondary);
    }

    @Override
    public void show(Chapter chapter) {
        contentView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hide() {
        contentView.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        final int viewId = view.getId();
        if (viewId == R.id.reload_button) {
            readerView.reloadCurrentChapterIfNotLoaded();
        } else {
            // Do nothing.
        }
    }
}
