package org.wreader.reader.reader.view;

import android.content.Context;

import org.wreader.reader.reader.beans.Chapter;
import org.wreader.reader.reader.beans.Page;
import org.wreader.reader.reader.presenter.ReaderTtsHelper;
import org.wreader.reader.reader.beans.Sentence;

import java.util.List;

public interface ReaderView {
    Context getContext();

    void setCurrentChapterId(String chapterId);

    void refreshCurrentPage();

    float calculateProgressInChapter(Page page);

    float calculateProgressInBook();

    void setProgressInBook(float progressInBook);

    void reloadCurrentChapterIfNotLoaded();

    Page getCurrentPage();

    void setCurrentPage(Page page);

    Page getNewPage();

    void setNewPage(Page page);

    List<Chapter> getTableOfContents();

    Chapter getCachedChapter(String chapterId);

    ReaderPaginator getPaginator();

    ReaderTtsHelper getTtsHelper();

    Sentence getSpeakingSentence();

    void setSpeakingSentence(Sentence speakingSentence);

    void onTtsError(String message);
}
