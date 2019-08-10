package org.wreader.reader.reader;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Scroller;

import org.wreader.reader.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReaderView extends FrameLayout implements View.OnClickListener {
    private static final float PAGE_TURNING_THRESHOLD = 10 * Resources.getSystem().getDisplayMetrics().density;
    private static final long LOAD_CHAPTER_TIMEOUT = 60L * 1000;

    private String bookId;
    private String lastReadChapterId;
    private final List<Chapter> tableOfContents = new ArrayList<>();
    private final Map<String, Chapter> cachedChapters = new HashMap<>();
    private final Map<String, Long> loadingChapters = new HashMap<>();

    private final ReaderPaginator paginator;

    private Page currentPage;
    private Bitmap currentPageBitmap;
    private Canvas currentPageCanvas;

    private Page newPage;
    private Bitmap newPageBitmap;
    private Canvas newPageCanvas;

    private ReaderChildView loadFailedView;
    private ReaderChildView paymentRequiredView;

    private final ReaderPageTurningAnimator pageTurningAnimator;
    private final PointF actionDownPoint = new PointF();
    private final PointF actionMovePoint = new PointF();
    private float actionDeltaX;
    private int actionDirectionX;
    private final Scroller scroller;

    public ReaderView(Context context, AttributeSet attributes) {
        super(context, attributes);
        paginator = new ReaderPaginator(this);
        pageTurningAnimator = ReaderPageTurningAnimator.getInstance();
        scroller = new Scroller(context);
        setOnClickListener(this);
        setWillNotDraw(false);
        //
        // Child views
        //
        loadFailedView = new ReaderLoadFailedView(this);
        addView(loadFailedView.getContentView());
        paymentRequiredView = new ReaderPaymentRequiredView(this);
        addView(paymentRequiredView.getContentView());
    }

    void setTextSizeSetting(ReaderTextSizeSetting textSizeSetting) {
        Log.d("WReader", "ReaderView.setTextSizeSetting()");
        paginator.setTextSizeSetting(textSizeSetting);
        onSizeChangedRefreshCurrentPage();
    }

    void setColorSetting(ReaderColorSetting colorSetting) {
        Log.d("WReader", "ReaderView.setColorSetting()");
        setBackgroundColor(colorSetting.backgroundColor);
        paginator.setColorSetting(colorSetting);
        pageTurningAnimator.setColorSetting(colorSetting);
        loadFailedView.setColorSetting(colorSetting);
        paymentRequiredView.setColorSetting(colorSetting);
        if (currentPage != null) {
            setCurrentPage(currentPage);
        }
    }

    void setPageTurningStyle(int pageTurningStyle) {
        Log.d("WReader", "ReaderView.setPageTurningStyle()");
        pageTurningAnimator.setStyle(pageTurningStyle);
    }

    void init(final String bookId, final Page page) {
        Log.d("WReader", "ReaderView.init() - bookId=" + bookId
                + ", page.chapterId=" + page.chapterId
                + ", page.progress=" + page.progress
                + ", page.pageIndex=" + page.pageIndex);
        post(new Runnable() {
            @Override
            public void run() {
                ReaderView.this.bookId = bookId;
                setCurrentPage(page);
                loadTableOfContents();
            }
        });
    }

    void setCurrentChapterId(String chapterId) {
        Log.d("WReader", "ReaderView.setCurrentChapterId() - chapterId=" + chapterId);
        removeCachedChapterIfNotLoaded(chapterId);
        setCurrentPage(new Page(chapterId, 0.0f));
    }

    float calculateProgressInChapter(Page page) {
        Chapter chapter = getCachedChapter(page.chapterId);
        if (page.pageIndex < 0 || chapter == null || chapter.pages.size() <= 1) {
            return page.progress;
        } else {
            return 1.0f * page.pageIndex / (chapter.pages.size() - 1);
        }
    }

    float calculateProgressInBook() {
        Log.d("WReader", "ReaderView.calculateProgressInBook()");
        return paginator.calculateProgressInBook(currentPage.chapterId, currentPage.pageIndex);
    }

    void setProgressInBook(float progressInBook) {
        Log.d("WReader", "ReaderView.setProgressInBook() - progressInBook=" + progressInBook);
        Page page = paginator.getPageAtProgressInBook(progressInBook);
        removeCachedChapterIfNotLoaded(page.chapterId);
        setCurrentPage(page);
    }

    void reloadCurrentChapterIfNotLoaded() {
        if (removeCachedChapterIfNotLoaded(currentPage.chapterId)) {
            setCurrentPage(new Page(currentPage.chapterId, currentPage.progress));
        }
    }

    void onChildViewUpdated() {
        Log.d("WReader", "ReaderView.onChildViewUpdated()");
        paginator.drawBackground(currentPageCanvas);
        paginator.drawPage(currentPage, currentPageCanvas);
        paginator.drawBackground(newPageCanvas);
        paginator.drawPage(newPage, newPageCanvas);
        invalidate();
    }

    Page getCurrentPage() {
        return currentPage;
    }

    List<Chapter> getTableOfContents() {
        return tableOfContents;
    }

    Chapter getCachedChapter(String chapterId) {
        return cachedChapters.get(chapterId);
    }

    ReaderChildView getLoadFailedView() {
        return loadFailedView;
    }

    ReaderChildView getPaymentRequiredView() {
        return paymentRequiredView;
    }

    ReaderActivity getReaderActivity() {
        return (ReaderActivity) getContext();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        Log.d("WReader", "ReaderView.onSizeChanged()");
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        currentPageBitmap = null;
        currentPageCanvas = null;
        newPageBitmap = null;
        newPageCanvas = null;
        currentPageBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        currentPageCanvas = new Canvas(currentPageBitmap);
        newPageBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        newPageCanvas = new Canvas(newPageBitmap);
        onSizeChangedRefreshCurrentPage();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.v("WReader", "ReaderView.onDraw()");
        super.onDraw(canvas);
        if (actionDeltaX == 0.0f) {
            canvas.drawBitmap(currentPageBitmap, 0.0f, 0.0f, null);
        } else {
            pageTurningAnimator.draw(canvas,
                                     actionDeltaX < 0.0f ? newPageBitmap : currentPageBitmap,
                                     actionDeltaX < 0.0f ? currentPageBitmap : newPageBitmap,
                                     actionDownPoint, actionMovePoint, actionDeltaX);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                actionDownPoint.x = event.getX();
                actionDownPoint.y = event.getY();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                actionMovePoint.x = event.getX();
                actionMovePoint.y = event.getY();
                break;
            }
            default: {
                break;
            }
        }
        if (!scroller.isFinished()) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                actionDeltaX = 0.0f;
                actionDirectionX = 0;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (actionDirectionX == 0) {
                    float delta = actionMovePoint.x - actionDownPoint.x;
                    if (delta < - PAGE_TURNING_THRESHOLD) {
                        actionDirectionX = -1;
                        setNewPage(paginator.getNextPage(currentPage));
                    }
                    if (delta > PAGE_TURNING_THRESHOLD) {
                        actionDirectionX = 1;
                        setNewPage(paginator.getPreviousPage(currentPage));
                    }
                }
                if (actionDirectionX != 0 && newPage != null) {
                    actionDeltaX = actionMovePoint.x - actionDownPoint.x;
                    if ((actionDirectionX < 0 && actionDeltaX > 0.0f)
                            || (actionDirectionX > 0 && actionDeltaX < 0.0f)) {
                        actionDeltaX = 0.0f;
                    }
                    invalidate();
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (newPage == null) {
                    //
                    // This is already the first/last page, or the previous/next page is not known yet.
                    //
                    break;
                }
                if (Math.abs(actionDeltaX) > PAGE_TURNING_THRESHOLD) {
                    //
                    // Start page turning animation.
                    //
                    if (actionDeltaX < 0.0f) {
                        actionDownPoint.x = getWidth();
                    }
                    pageTurningAnimator.startScroll(scroller, getWidth(), getHeight(),
                                                    actionDownPoint, actionMovePoint, actionDirectionX);
                    invalidate();
                } else {
                    //
                    // Page turning canceled.
                    //
                    actionDeltaX = 0.0f;
                    setCurrentPage(currentPage);
                    setNewPage(null);
                }
                break;
            }
            default: {
                break;
            }
        }
        return (actionDirectionX != 0) || super.onTouchEvent(event);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.reader_view: {
                if (actionDownPoint.x < getWidth() * 0.2f) {
                    jumpToPreviousPageAnimated();
                } else if (actionDownPoint.x > getWidth() * 0.8f) {
                    jumpToNextPageAnimated();
                } else {
                    getReaderActivity().showMenuView(true);
                }
                break;
            }
            default: {
                break;
            }
        }
    }

    @Override
    public void computeScroll() {
        if (!scroller.computeScrollOffset()) {
            return;
        }
        actionMovePoint.x = scroller.getCurrX();
        actionMovePoint.y = scroller.getCurrY();
        actionDeltaX = actionMovePoint.x - actionDownPoint.x;
        invalidate();
        if (scroller.isFinished()) {
            actionDeltaX = 0.0f;
            actionDirectionX = 0;
            if (newPage != null) {
                setCurrentPage(newPage);
                setNewPage(null);
            }
        }
    }

    private void setCurrentPage(Page page) {
        if (page == null) {
            throw new IllegalArgumentException();
        }
        Log.d("WReader", "ReaderView.setCurrentPage() - page.chapterId=" + page.chapterId
                + ", page.progress=" + page.progress
                + ", page.pageIndex=" + page.pageIndex);
        currentPage = revisePage(page, true);
        paginator.drawBackground(currentPageCanvas);
        paginator.drawPage(currentPage, currentPageCanvas);
        if (actionDeltaX == 0.0f) {
            updateChildViews(true);
        }
        invalidate();
    }

    private void setNewPage(Page page) {
        if (page != null) {
            Log.d("WReader", "ReaderView.setNewPage() - page.chapterId=" + page.chapterId
                    + ", page.progress=" + page.progress
                    + ", page.pageIndex=" + page.pageIndex);
        } else {
            Log.d("WReader", "ReaderView.setNewPage() - page=null");
        }
        if (newPage == null && page != null) {
            removeCachedChapterIfNotLoaded(page.chapterId);
        }
        newPage = revisePage(page, false);
        paginator.drawBackground(newPageCanvas);
        paginator.drawPage(newPage, newPageCanvas);
        if (newPage != null) {
            updateChildViews(false);
        }
        invalidate();
    }

    private Page revisePage(Page page, boolean isCurrentPage) {
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
            paginator.recalculatePages(chapter, page.progress);
            return chapter.pages.get(Math.round((chapter.pages.size() - 1) * page.progress));
        } else {
            return page;
        }
    }

    private boolean removeCachedChapterIfNotLoaded(String chapterId) {
        Chapter chapter = getCachedChapter(chapterId);
        if (chapter == null || chapter.status == Chapter.STATUS_LOADED) {
            return false;
        }
        cachedChapters.remove(chapterId);
        Log.d("WReader", "ReaderView.removeCachedChapterIfNotLoaded() - removed " + chapterId);
        removeCachedChapterIfNotLoaded(chapter.nextId);
        return true;
    }

    private void onSizeChangedRefreshCurrentPage() {
        if (currentPage == null) {
            return;
        }
        if (currentPage.pageIndex < 0) {
            setCurrentPage(currentPage);
            return;
        }
        setCurrentPage(new Page(currentPage.chapterId, calculateProgressInChapter(currentPage)));
    }

    private void updateChildViews(boolean showChildView) {
        loadFailedView.hide();
        paymentRequiredView.hide();
        if (!showChildView || actionDeltaX != 0.0f || currentPage.pageIndex < 0) {
            return;
        }
        Chapter chapter = getCachedChapter(currentPage.chapterId);
        if (chapter == null || currentPage.pageIndex < chapter.pages.size() - 1) {
            return;
        }
        switch (chapter.status) {
            case Chapter.STATUS_LOAD_FAILED: {
                loadFailedView.show(chapter);
                break;
            }
            case Chapter.STATUS_LOADED: {
                break;
            }
            case Chapter.STATUS_PAYMENT_REQUIRED: {
                paymentRequiredView.show(chapter);
                break;
            }
            default: {
                break;
            }
        }
    }

    private void jumpToPreviousPageAnimated() {
        Page previousPage = paginator.getPreviousPage(currentPage);
        if (previousPage != null) {
            setNewPage(previousPage);
            actionDownPoint.x = 0.0f;
            actionDownPoint.y = getHeight() * 0.5f;
            actionMovePoint.x = getWidth() * 0.2f;
            actionMovePoint.y = getHeight() * 0.5f;
            actionDirectionX = 1;
            pageTurningAnimator.startScroll(scroller, getWidth(), getHeight(),
                                            actionDownPoint, actionMovePoint, actionDirectionX);
        }
    }

    private void jumpToNextPageAnimated() {
        Page nextPage = paginator.getNextPage(currentPage);
        if (nextPage != null) {
            setNewPage(nextPage);
            actionDownPoint.x = getWidth();
            actionDownPoint.y = getHeight() * 0.5f;
            actionMovePoint.x = getWidth() * 0.8f;
            actionMovePoint.y = getHeight() * 0.5f;
            actionDirectionX = -1;
            pageTurningAnimator.startScroll(scroller, getWidth(), getHeight(),
                                            actionDownPoint, actionMovePoint, actionDirectionX);
        }
    }

    private void loadTableOfContents() {
        Log.d("WReader", "ReaderView.loadTableOfContents() [start]");
        BookDataHelper.loadTableOfContents(bookId, new BookDataHelper.DataLoadedCallback<List<Chapter>>() {
            @Override
            public void onDataLoaded(List<Chapter> chapters) {
                Log.d("WReader", "ReaderView.loadTableOfContents() [done]");
                if (chapters == null) {
                    return;
                }
                tableOfContents.clear();
                tableOfContents.addAll(chapters);
                setCurrentPage(currentPage);
            }
        });
    }

    private void loadChapter(final String chapterId) {
        Long loadingStartTime = loadingChapters.get(chapterId);
        if (loadingStartTime != null && loadingStartTime > System.currentTimeMillis() - LOAD_CHAPTER_TIMEOUT) {
            return;
        }
        loadingChapters.put(chapterId, System.currentTimeMillis());
        Log.d("WReader", "ReaderView.loadChapter() [start] - chapterId=" + chapterId);
        BookDataHelper.loadChapter(bookId, chapterId, new BookDataHelper.DataLoadedCallback<Chapter>() {
            @Override
            public void onDataLoaded(final Chapter chapter) {
                loadingChapters.remove(chapterId);
                if (chapter == null) {
                    return;
                }
                Log.d("WReader", "ReaderView.loadChapter() [done] - chapterId=" + chapterId
                        + ", chapter.status=" + chapter.status);
                if (TextUtils.isEmpty(currentPage.chapterId)
                        || currentPage.chapterId.equals(chapter.id)) {
                    float progress = calculateProgressInChapter(currentPage);
                    cachedChapters.put(chapter.id, chapter);
                    setCurrentPage(new Page(chapter.id, progress));
                } else if (newPage != null
                        && newPage.chapterId != null
                        && newPage.chapterId.equals(chapter.id)) {
                    float progress = calculateProgressInChapter(newPage);
                    cachedChapters.put(chapter.id, chapter);
                    setNewPage(new Page(chapter.id, progress));
                } else {
                    cachedChapters.put(chapter.id, chapter);
                }
            }
        });
    }

    private void onReadChapter(Chapter chapter) {
        Log.d("WReader", "ReaderView.onReadChapter() - chapter.id=" + chapter.id);
        if (!TextUtils.isEmpty(chapter.nextId)
                && (getCachedChapter(chapter.nextId) == null || removeCachedChapterIfNotLoaded(chapter.nextId))) {
            loadChapter(chapter.nextId);
        }
    }
}
