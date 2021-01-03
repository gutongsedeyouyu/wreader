package org.wreader.reader.reader.view;

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
import org.wreader.reader.core.helper.Router;
import org.wreader.reader.reader.beans.Chapter;
import org.wreader.reader.reader.beans.Page;
import org.wreader.reader.reader.beans.ReaderTextSizeSetting;
import org.wreader.reader.reader.beans.ReaderColorSetting;
import org.wreader.reader.reader.presenter.ReaderPresenter;
import org.wreader.reader.reader.presenter.ReaderTtsHelper;
import org.wreader.reader.reader.beans.Sentence;

import java.util.List;

public class ReaderViewImpl extends FrameLayout implements ReaderView, View.OnClickListener {
    private static final float PAGE_TURNING_THRESHOLD = 10 * Resources.getSystem().getDisplayMetrics().density;
    private static final float SPEAKING_ACTION_THRESHOLD = 20 * Resources.getSystem().getDisplayMetrics().density;

    private ReaderPresenter presenter;

    private final ReaderPaginator paginator;

    private Page currentPage;
    private Bitmap currentPageBitmap;
    private Canvas currentPageCanvas;

    private Page newPage;
    private Bitmap newPageBitmap;
    private Canvas newPageCanvas;

    private ReaderLoadFailedView loadFailedView;
    private ReaderPaymentRequiredView paymentRequiredView;

    private final ReaderPageTurningAnimator pageTurningAnimator;
    private final PointF actionDownPoint = new PointF();
    private final PointF actionMovePoint = new PointF();
    private float actionDeltaX;
    private int actionDirectionX;
    private final Scroller scroller;

    private final ReaderTtsHelper ttsHelper;
    private Sentence speakingSentence;
    private final PointF speakingActionPoint = new PointF();
    private boolean speakingActionMoving;

    public ReaderViewImpl(Context context, AttributeSet attributes) {
        super(context, attributes);
        paginator = new ReaderPaginator(this, true, true);
        pageTurningAnimator = ReaderPageTurningAnimator.getInstance();
        scroller = new Scroller(context);
        presenter = new ReaderPresenter(this);
        ttsHelper = new ReaderTtsHelper(this);
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

    public void setTextSizeSetting(ReaderTextSizeSetting textSizeSetting) {
        Log.d("WReader", "ReaderView.setTextSizeSetting()");
        paginator.setTextSizeSetting(textSizeSetting);
        onSizeChangedRefreshCurrentPage();
    }

    public void setColorSetting(ReaderColorSetting colorSetting) {
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

    public void setPageTurningStyle(int pageTurningStyle) {
        Log.d("WReader", "ReaderView.setPageTurningStyle()");
        pageTurningAnimator.setStyle(pageTurningStyle);
    }

    public void init(final String bookId, final String chapterId) {
        Log.d("WReader", "ReaderView.init() - bookId=" + bookId
                + ", chapterId=" + chapterId);
        final Page page = presenter.getInitPage(bookId, chapterId);
        post(new Runnable() {
            @Override
            public void run() {
                setCurrentPage(page);
                presenter.setBookId(bookId);
                presenter.loadTableOfContents();
            }
        });
    }

    public ReaderPresenter getPresenter() {
        return presenter;
    }

    @Override
    public void setCurrentChapterId(String chapterId) {
        Log.d("WReader", "ReaderView.setCurrentChapterId() - chapterId=" + chapterId);
        presenter.removeCachedChapterIfNotLoaded(chapterId);
        setCurrentPage(new Page(chapterId, 0.0f));
    }

    @Override
    public void refreshCurrentPage() {
        Log.d("WReader", "ReaderView.refreshCurrentPage()");
        setCurrentPage(currentPage);
    }

    @Override
    public float calculateProgressInChapter(Page page) {
        Log.d("WReader", "ReaderView.calculateProgressInChapter()");
        Chapter chapter = getCachedChapter(page.chapterId);
        if (page.pageIndex < 0 || chapter == null || chapter.pages.size() <= 1) {
            return page.progress;
        } else {
            return 1.0f * page.pageIndex / (chapter.pages.size() - 1);
        }
    }

    @Override
    public float calculateProgressInBook() {
        Log.d("WReader", "ReaderView.calculateProgressInBook()");
        return paginator.calculateProgressInBook(currentPage.chapterId, currentPage.pageIndex);
    }

    @Override
    public void setProgressInBook(float progressInBook) {
        Log.d("WReader", "ReaderView.setProgressInBook() - progressInBook=" + progressInBook);
        Page page = paginator.getPageAtProgressInBook(progressInBook);
        presenter.removeCachedChapterIfNotLoaded(page.chapterId);
        setCurrentPage(page);
    }

    @Override
    public void reloadCurrentChapterIfNotLoaded() {
        Log.d("WReader", "ReaderView.reloadCurrentChapterIfNotLoaded()");
        if (presenter.removeCachedChapterIfNotLoaded(currentPage.chapterId)) {
            setCurrentPage(new Page(currentPage.chapterId, currentPage.progress));
        }
    }

    public void onChildViewUpdated() {
        Log.d("WReader", "ReaderView.onChildViewUpdated()");
        paginator.drawBackground(currentPageCanvas);
        paginator.drawPage(currentPage, currentPageCanvas);
        paginator.drawBackground(newPageCanvas);
        paginator.drawPage(newPage, newPageCanvas);
        invalidate();
    }

    @Override
    public Page getCurrentPage() {
        return currentPage;
    }

    @Override
    public List<Chapter> getTableOfContents() {
        return presenter.getTableOfContents();
    }

    @Override
    public Chapter getCachedChapter(String chapterId) {
        return presenter.getCachedChapter(chapterId);
    }

    @Override
    public ReaderPaginator getPaginator() {
        return paginator;
    }

    @Override
    public ReaderTtsHelper getTtsHelper() {
        return ttsHelper;
    }

    public ReaderChildView getLoadFailedView() {
        return loadFailedView;
    }

    public ReaderChildView getPaymentRequiredView() {
        return paymentRequiredView;
    }

    @Override
    public Sentence getSpeakingSentence() {
        return speakingSentence;
    }

    @Override
    public void setSpeakingSentence(Sentence speakingSentence) {
        if (speakingSentence == null) {
            this.speakingSentence = null;
            setCurrentPage(currentPage);
            return;
        }
        Chapter chapter = getCachedChapter(speakingSentence.chapterId);
        if (chapter == null || chapter.status != Chapter.STATUS_LOADED) {
            this.speakingSentence = null;
            setCurrentPage(currentPage);
            return;
        }
        if (currentPage.containsSentence(speakingSentence)) {
            this.speakingSentence = speakingSentence;
            setCurrentPage(currentPage);
            return;
        }
        for (int i = 0; i < chapter.pages.size(); i++) {
            Page page = chapter.pages.get(i);
            if (page.containsSentence(speakingSentence)) {
                this.speakingSentence = speakingSentence;
                setCurrentPage(page);
                break;
            }
        }
    }

    @Override
    public void onTtsError(String message) {
        setSpeakingSentence(null);
        if (!TextUtils.isEmpty(message)) {
            Router.toast(getContext(), message);
        }
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
        if (speakingSentence != null) {
            return onTouchEventSpeaking(event);
        } else {
            return onTouchEventReading(event);
        }
    }

    private boolean onTouchEventSpeaking(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                speakingActionPoint.x = event.getX();
                speakingActionPoint.y = event.getY();
                speakingActionMoving = false;
                ttsHelper.pause();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (event.getY() - speakingActionPoint.y < -SPEAKING_ACTION_THRESHOLD) {
                    setSpeakingSentence(ttsHelper.getPreviousSentence(speakingSentence));
                    speakingActionPoint.x = event.getX();
                    speakingActionPoint.y = event.getY();
                    speakingActionMoving = true;
                }
                if (event.getY() - speakingActionPoint.y > SPEAKING_ACTION_THRESHOLD) {
                    setSpeakingSentence(ttsHelper.getNextSentence(speakingSentence));
                    speakingActionPoint.x = event.getX();
                    speakingActionPoint.y = event.getY();
                    speakingActionMoving = true;
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (speakingActionMoving) {
                    ttsHelper.start(speakingSentence);
                }
                break;
            }
            default: {
                break;
            }
        }
        return speakingActionMoving || super.onTouchEvent(event);
    }

    private boolean onTouchEventReading(MotionEvent event) {
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
        final int viewId = view.getId();
        if (viewId == R.id.reader_view) {
            if (speakingSentence != null) {
                getReaderActivity().showTtsMenuView(true);
            } else {
                if (actionDownPoint.x < getWidth() * 0.2f) {
                    jumpToPreviousPageAnimated();
                } else if (actionDownPoint.x > getWidth() * 0.8f) {
                    jumpToNextPageAnimated();
                } else {
                    getReaderActivity().showMenuView(true);
                }
            }
        } else {
            // Do nothing.
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

    public ReaderActivity getReaderActivity() {
        return (ReaderActivity) getContext();
    }

    public void setCurrentPage(Page page) {
        if (page == null) {
            throw new IllegalArgumentException();
        }
        Log.d("WReader", "ReaderView.setCurrentPage() - page.chapterId=" + page.chapterId
                + ", page.progress=" + page.progress
                + ", page.pageIndex=" + page.pageIndex);
        currentPage = presenter.revisePage(page, true);
        paginator.drawBackground(currentPageCanvas);
        paginator.drawPage(currentPage, currentPageCanvas);
        if (actionDeltaX == 0.0f) {
            updateChildViews(true);
        }
        invalidate();
    }

    @Override
    public Page getNewPage() {
        return newPage;
    }

    @Override
    public void setNewPage(Page page) {
        if (page != null) {
            Log.d("WReader", "ReaderView.setNewPage() - page.chapterId=" + page.chapterId
                    + ", page.progress=" + page.progress
                    + ", page.pageIndex=" + page.pageIndex);
        } else {
            Log.d("WReader", "ReaderView.setNewPage() - page=null");
        }
        if (newPage == null && page != null) {
            presenter.removeCachedChapterIfNotLoaded(page.chapterId);
        }
        newPage = presenter.revisePage(page, false);
        paginator.drawBackground(newPageCanvas);
        paginator.drawPage(newPage, newPageCanvas);
        if (newPage != null) {
            updateChildViews(false);
        }
        invalidate();
    }

    private void updateChildViews(boolean isForCurrentPage) {
        loadFailedView.hide();
        paymentRequiredView.hide();
        if (!isForCurrentPage || actionDeltaX != 0.0f || currentPage.pageIndex < 0) {
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
}
