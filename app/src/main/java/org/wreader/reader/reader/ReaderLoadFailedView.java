package org.wreader.reader.reader;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.wreader.reader.R;

class ReaderLoadFailedView implements View.OnClickListener {
    private final ReaderView readerView;

    private View contentView;
    private TextView loadFailedTextView;
    private View reloadButton;

    ReaderLoadFailedView(ReaderView readerView) {
        this.readerView = readerView;
        contentView = LayoutInflater.from(readerView.getContext()).inflate(
                R.layout.reader_activity_load_failed, readerView, false);
        loadFailedTextView = contentView.findViewById(R.id.load_failed_text_view);
        reloadButton = contentView.findViewById(R.id.reload_button);
        reloadButton.setOnClickListener(this);
    }

    void setColorSetting(ReaderColorSetting colorSetting) {
        loadFailedTextView.setTextColor(colorSetting.textColorSecondary);
    }

    void show(Chapter chapter) {
        contentView.setVisibility(View.VISIBLE);
    }

    void hide() {
        contentView.setVisibility(View.INVISIBLE);
    }

    View getContentView() {
        return contentView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.reload_button: {
                readerView.reloadCurrentChapterIfNotLoaded();
                break;
            }
            default: {
                break;
            }
        }
    }
}
