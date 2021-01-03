package org.wreader.reader.reader.view;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.wreader.reader.R;
import org.wreader.reader.reader.beans.Chapter;
import org.wreader.reader.reader.beans.ReaderColorSetting;

class ReaderPaymentRequiredView implements ReaderChildView, View.OnClickListener {
    private final ReaderViewImpl readerView;

    private View contentView;
    private View divider;
    private TextView paymentRequiredTextView;
    private View purchaseButton;

    ReaderPaymentRequiredView(ReaderViewImpl readerView) {
        this.readerView = readerView;
        contentView = LayoutInflater.from(readerView.getContext()).inflate(
                R.layout.reader_activity_payment_required, readerView, false);
        divider = contentView.findViewById(R.id.divider);
        paymentRequiredTextView = contentView.findViewById(R.id.payment_required_text_view);
        purchaseButton = contentView.findViewById(R.id.purchase_button);
        purchaseButton.setOnClickListener(this);
    }

    @Override
    public View getContentView() {
        return contentView;
    }

    @Override
    public void setColorSetting(ReaderColorSetting colorSetting) {
        divider.setBackgroundColor(colorSetting.dividerColor);
        paymentRequiredTextView.setTextColor(colorSetting.textColorPrimary);
    }

    @Override
    public void show(Chapter chapter) {
        contentView.setVisibility(View.VISIBLE);
        contentView.post(new Runnable() {
            @Override
            public void run() {
                readerView.onChildViewUpdated();
            }
        });
    }

    @Override
    public void hide() {
        contentView.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        final int viewId = view.getId();
        if (viewId == R.id.purchase_button) {
            readerView.reloadCurrentChapterIfNotLoaded();
        } else {
            // Do nothing.
        }
    }
}
