package org.wreader.reader.reader.presenter;

import android.text.TextUtils;
import android.util.Log;

import org.wreader.reader.reader.beans.Chapter;
import org.wreader.reader.reader.beans.Page;
import org.wreader.reader.reader.model.BookDataHelper;
import org.wreader.reader.reader.view.ReaderView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReaderPresenter {
    private static final long LOAD_CHAPTER_TIMEOUT = 60L * 1000;

    private final ReaderView readerView;
    private String bookId;
    private String lastReadChapterId;

    private final List<Chapter> tableOfContents = new ArrayList<>();
    private final Map<String, Chapter> cachedChapters = new HashMap<>();
    private final Map<String, Long> loadingChapters = new HashMap<>();

    public ReaderPresenter(ReaderView readerView) {
        this.readerView = readerView;
    }

    public List<Chapter> getTableOfContents() {
        return tableOfContents;
    }

    public Chapter getCachedChapter(String chapterId) {
        return cachedChapters.get(chapterId);
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public void loadTableOfContents() {
        Log.d("WReader", "ReaderPresenter.loadTableOfContents() [start]");
        BookDataHelper.loadTableOfContents(bookId, new BookDataHelper.DataLoadedCallback<List<Chapter>>() {
            @Override
            public void onDataLoaded(List<Chapter> chapters) {
                Log.d("WReader", "ReaderView.loadTableOfContents() [done]");
                if (chapters == null) {
                    return;
                }
                tableOfContents.clear();
                tableOfContents.addAll(chapters);
                readerView.refreshCurrentPage();
            }
        });
    }

    private void loadChapter(final String chapterId) {
        Long loadingStartTime = loadingChapters.get(chapterId);
        if (loadingStartTime != null && loadingStartTime > System.currentTimeMillis() - LOAD_CHAPTER_TIMEOUT) {
            return;
        }
        loadingChapters.put(chapterId, System.currentTimeMillis());
        Log.d("WReader", "ReaderPresenter.loadChapter() [start] - chapterId=" + chapterId);
        BookDataHelper.loadChapter(bookId, chapterId, new BookDataHelper.DataLoadedCallback<Chapter>() {
            @Override
            public void onDataLoaded(final Chapter chapter) {
                loadingChapters.remove(chapterId);
                if (chapter == null) {
                    return;
                }
                Log.d("WReader", "ReaderPresenter.loadChapter() [done] - chapterId=" + chapterId
                        + ", chapter.status=" + chapter.status);
                Page currentPage = readerView.getCurrentPage();
                Page newPage = readerView.getNewPage();
                if (TextUtils.isEmpty(currentPage.chapterId)
                        || currentPage.chapterId.equals(chapter.id)) {
                    float progress = readerView.calculateProgressInChapter(currentPage);
                    cachedChapters.put(chapter.id, chapter);
                    readerView.setCurrentPage(new Page(chapter.id, progress));
                } else if (newPage != null
                        && newPage.chapterId != null
                        && newPage.chapterId.equals(chapter.id)) {
                    float progress = readerView.calculateProgressInChapter(newPage);
                    cachedChapters.put(chapter.id, chapter);
                    readerView.setNewPage(new Page(chapter.id, progress));
                } else {
                    cachedChapters.put(chapter.id, chapter);
                }
            }
        });
    }

    public Page revisePage(Page page, boolean isCurrentPage) {
        if (page == null) {
            return null;
        }
        //
        // This chapter is not loaded yet.
        //
        Chapter chapter = getCachedChapter(page.chapterId);
        if (chapter == null) {
            loadChapter(page.chapterId);
            return page;
        }
        //
        // This chapter is already loaded.
        //
        if (isCurrentPage
                && chapter.status == Chapter.STATUS_LOADED
                && !chapter.id.equals(lastReadChapterId)) {
            lastReadChapterId = chapter.id;
            onReadChapter(chapter);
        }
        if (page.pageIndex < 0) {
            readerView.getPaginator().recalculatePages(chapter, page.progress);
            return chapter.pages.get(Math.round((chapter.pages.size() - 1) * page.progress));
        } else {
            return page;
        }
    }

    private void onReadChapter(Chapter chapter) {
        Log.d("WReader", "ReaderPresenter.onReadChapter() - chapter.id=" + chapter.id);
        if (!TextUtils.isEmpty(chapter.nextId)
                && (getCachedChapter(chapter.nextId) == null || removeCachedChapterIfNotLoaded(chapter.nextId))) {
            loadChapter(chapter.nextId);
        }
    }

    public boolean removeCachedChapterIfNotLoaded(String chapterId) {
        Chapter chapter = getCachedChapter(chapterId);
        if (chapter == null || chapter.status == Chapter.STATUS_LOADED) {
            return false;
        }
        cachedChapters.remove(chapterId);
        Log.d("WReader", "ReaderPresenter.removeCachedChapterIfNotLoaded() - removed " + chapterId);
        removeCachedChapterIfNotLoaded(chapter.nextId);
        return true;
    }

    public Page getInitPage(String bookId, String chapterId) {
        if (!TextUtils.isEmpty(chapterId)) {
            return new Page(chapterId, 0.0f);
        }
        Page page = getLastReadProgress(bookId);
        if (page != null) {
            return page;
        }
        return new Page("", 0.0f);
    }

    public Page getLastReadProgress(String bookId) {
        return BookDataHelper.getReadProgress(bookId);
    }

    public void saveLastReadProgress() {
        Page currentPage = readerView.getCurrentPage();
        if (currentPage != null) {
            BookDataHelper.setReadProgress(bookId,
                    currentPage.chapterId,
                    readerView.calculateProgressInChapter(currentPage));
        }
    }
}
