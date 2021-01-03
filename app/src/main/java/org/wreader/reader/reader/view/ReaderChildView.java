package org.wreader.reader.reader.view;

import android.view.View;

import org.wreader.reader.reader.beans.Chapter;
import org.wreader.reader.reader.beans.ReaderColorSetting;

public interface ReaderChildView {
    View getContentView();

    void setColorSetting(ReaderColorSetting colorSetting);

    void show(Chapter chapter);

    void hide();
}
