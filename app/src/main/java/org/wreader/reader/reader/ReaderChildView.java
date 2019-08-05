package org.wreader.reader.reader;

import android.view.View;

interface ReaderChildView {
    View getContentView();

    void setColorSetting(ReaderColorSetting colorSetting);

    void show(Chapter chapter);

    void hide();
}
