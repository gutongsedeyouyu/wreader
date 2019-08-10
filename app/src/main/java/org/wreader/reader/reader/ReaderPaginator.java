package org.wreader.reader.reader;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import org.wreader.reader.R;
import org.wreader.reader.core.helper.BatteryBroadcastReceiver;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

class ReaderPaginator {
    private static final float DENSITY = Resources.getSystem().getDisplayMetrics().density;

    private static final Format TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private static final Set<Character> AVOID_LEADING_IF_EVEN_CHARS = new HashSet<>();
    private static final Set<Character> AVOID_LEADING_CHARS = new HashSet<>();
    private static final Set<Character> AVOID_BREAKING_CHARS_1 = new HashSet<>();
    private static final Set<Character> AVOID_BREAKING_CHARS_2 = new HashSet<>();
    private static final Set<Character> AVOID_TRAILING_CHARS = new HashSet<>();
    private static final Set<Character> AVOID_TRAILING_IF_ODD_CHARS = new HashSet<>();

    static {
        for (char c : "'\"`".toCharArray()) {
            AVOID_LEADING_IF_EVEN_CHARS.add(c);
        }
        for (char c : ",，:：;；、.。?？!！’”>》)）]】}」".toCharArray()) {
            AVOID_LEADING_CHARS.add(c);
        }
        for (char c : "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz'’&1234567890".toCharArray()) {
            AVOID_BREAKING_CHARS_1.add(c);
        }
        for (char c : "1234567890+-.,$¥".toCharArray()) {
            AVOID_BREAKING_CHARS_2.add(c);
        }
        for (char c : "‘“<《(（[【{「".toCharArray()) {
            AVOID_TRAILING_CHARS.add(c);
        }
        for (char c : "'\"`".toCharArray()) {
            AVOID_TRAILING_IF_ODD_CHARS.add(c);
        }
    }

    private final ReaderView readerView;
    private final Paint paint;

    private float textMarginTop = 0 * DENSITY;
    private float textMarginLeft = 16 * DENSITY;
    private float textMarginRight = 16 * DENSITY;
    private float textMarginBottom = 36 * DENSITY;
    private float titleSmallMarginTop = 10 * DENSITY;
    private float titleSmallMarginBottom = 10 * DENSITY;
    private float titleLargeMarginTop = 20 * DENSITY;
    private float titleLargeMarginBottom = 40 * DENSITY;
    private float paragraphSpacing = 18 * DENSITY;
    private float paragraphLineSpacing = 4 * DENSITY;

    private float textSizeSmall;
    private float textSizeDefault;
    private float textSizeLarge;

    private int backgroundColor;
    private int textColorPrimary;
    private int textColorSecondary;

    ReaderPaginator(ReaderView readerView) {
        this.readerView = readerView;
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    void setTextSizeSetting(ReaderTextSizeSetting textSizeSetting) {
        textSizeSmall = textSizeSetting.textSizeSmall * DENSITY;
        textSizeDefault = textSizeSetting.textSizeDefault * DENSITY;
        textSizeLarge = textSizeSetting.textSizeLarge * DENSITY;
    }

    void setColorSetting(ReaderColorSetting colorSetting) {
        backgroundColor = colorSetting.backgroundColor;
        textColorPrimary = colorSetting.textColorPrimary;
        textColorSecondary = colorSetting.textColorSecondary;
    }

    void recalculatePages(Chapter chapter, float progress) {
        Log.d("WReader", "ReaderPaginator.recalculatePages() - chapter.id=" + chapter.id
                + ", progress=" + progress);
        ArrayList<Page> pages = new ArrayList<>();
        Page page = drawOrCalculate(chapter, progress, 0,
                                    0, 0,
                                    null, paint);
        if (chapter.status == Chapter.STATUS_LOADED) {
            while (page != null) {
                pages.add(page);
                page = drawOrCalculate(chapter, progress, page.pageIndex + 1,
                                       page.endParagraphIndex, page.endCharacterIndex,
                                       null, paint);
            }
        } else {
            pages.add(page);
        }
        chapter.pages = pages;
    }

    Page getPreviousPage(Page page) {
        if (page == null) {
            return null;
        }
        Chapter chapter = readerView.getCachedChapter(page.chapterId);
        if (chapter == null || chapter.status == Chapter.STATUS_LOAD_FAILED) {
            //
            // This chapter is loading or load failed, try to find the previous chapter from table
            // of contents. Return last page of the previous chapter if found, null otherwise.
            //
            for (int i = 1; i < readerView.getTableOfContents().size(); i++) {
                if (page.chapterId.equals(readerView.getTableOfContents().get(i).id)) {
                    return new Page(readerView.getTableOfContents().get(i - 1).id, 1.0f);
                }
            }
            return null;
        } else if (page.pageIndex > 0) {
            //
            // Return previous page in this chapter.
            //
            return chapter.pages.get(page.pageIndex - 1);
        } else if (!TextUtils.isEmpty(chapter.previousId)) {
            //
            // Return last page of the previous chapter.
            //
            return new Page(chapter.previousId, 1.0f);
        } else {
            //
            // This is the first page of the first chapter, and there is no previous page.
            //
            return null;
        }
    }

    Page getNextPage(Page page) {
        if (page == null) {
            return null;
        }
        Chapter chapter = readerView.getCachedChapter(page.chapterId);
        if (chapter == null || chapter.status == Chapter.STATUS_LOAD_FAILED) {
            //
            // This chapter is loading or load failed, try to find the next chapter from table of
            // contents. Return first page of the next chapter if found, null otherwise.
            //
            for (int i = 0; i < readerView.getTableOfContents().size() - 1; i++) {
                if (page.chapterId.equals(readerView.getTableOfContents().get(i).id)) {
                    return new Page(readerView.getTableOfContents().get(i + 1).id, 0.0f);
                }
            }
            return null;
        } else if (page.pageIndex < chapter.pages.size() - 1) {
            //
            // Return next page in this chapter.
            //
            return chapter.pages.get(page.pageIndex + 1);
        } else if (!TextUtils.isEmpty(chapter.nextId)) {
            //
            // Return first page of the next chapter.
            //
            return new Page(chapter.nextId, 0.0f);
        } else {
            //
            // This is the last page in the last chapter, and there is no next page.
            //
            return null;
        }
    }

    Page getPageAtProgressInBook(float progressInBook) {
        int chaptersCount = readerView.getTableOfContents().size();
        if (chaptersCount == 0) {
            return readerView.getCurrentPage();
        }
        int chapterIndex = Math.round((readerView.getTableOfContents().size() - 1) * progressInBook);
        Chapter chapter = readerView.getTableOfContents().get(chapterIndex);
        return new Page(chapter.id, 0.0f);
    }

    float calculateProgressInBook(String chapterId, int pageIndex) {
        int chaptersCount = readerView.getTableOfContents().size();
        if (chaptersCount == 0) {
            return 0.0f;
        }
        for (int i = 0; i < readerView.getTableOfContents().size(); i++) {
            if (chapterId.equals(readerView.getTableOfContents().get(i).id)) {
                Chapter chapter = readerView.getCachedChapter(chapterId);
                if (chapter != null && pageIndex >= 0) {
                    int pagesCountPerChapter = chapter.pages.size();
                    int totalPagesCount = pagesCountPerChapter * chaptersCount;
                    int currentPage = i * pagesCountPerChapter + pageIndex;
                    if (currentPage == totalPagesCount - 1) {
                        currentPage = totalPagesCount;
                    }
                    return 1.0f * currentPage / totalPagesCount;
                } else {
                    return 1.0f * i / chaptersCount;
                }
            }
        }
        return 0.0f;
    }

    void drawBackground(Canvas canvas) {
        canvas.drawColor(backgroundColor);
    }

    void drawPage(Page page, Canvas canvas) {
        if (page != null) {
            drawOrCalculate(readerView.getCachedChapter(page.chapterId), page.progress, page.pageIndex,
                            page.startParagraphIndex, page.startCharacterIndex,
                            canvas, paint);
        }
    }

    private Page drawOrCalculate(Chapter chapter, float progress, int pageIndex,
                                 int startParagraphIndex, int startCharacterIndex,
                                 Canvas canvas, Paint paint) {
        if (chapter == null) {
            return drawOrCalculateLoading(canvas, paint);
        }
        if (canvas != null) {
            Log.v("WReader", "ReaderPaginator.drawOrCalculate() [draw] - chapter.id=" + chapter.id
                    + ", progress=" + progress
                    + ", pageIndex=" + pageIndex);
        } else {
            Log.v("WReader", "ReaderPaginator.drawOrCalculate() [calculate] - chapter.id=" + chapter.id
                    + ", progress=" + progress
                    + ", pageIndex=" + pageIndex);
        }
        if (pageIndex < 0) {
            return drawOrCalculateLoading(canvas, paint);
        }
        if (chapter.status == Chapter.STATUS_LOAD_FAILED) {
            return drawOrCalculateLoadFailed(chapter.id, progress, canvas, paint);
        }
        if (startParagraphIndex > chapter.paragraphs.size() - 1) {
            return null;
        }
        return drawOrCalculateOthers(chapter, progress, pageIndex,
                                     startParagraphIndex, startCharacterIndex,
                                     canvas, paint);
    }

    private Page drawOrCalculateLoading(Canvas canvas, Paint paint) {
        if (canvas != null) {
            String loadingText = readerView.getContext().getString(R.string.loading);
            paint.setTextSize(textSizeLarge);
            paint.setColor(textColorPrimary);
            float lineHeight = calculateLineHeight(paint);
            float y = (readerView.getHeight() - lineHeight) / 2;
            float x = (readerView.getWidth() - paint.measureText(loadingText)) / 2;
            canvas.drawText(loadingText, x, y, paint);
            return null;
        } else {
            throw new IllegalStateException();
        }
    }

    private Page drawOrCalculateLoadFailed(String chapterId, float progress,
                                           Canvas canvas, Paint paint) {
        if (canvas != null) {
            drawChildView(canvas, readerView.getLoadFailedView().getContentView());
            return null;
        } else {
            return new Page(chapterId, progress, 0,
                            0, 0, 0, 0);
        }
    }

    private Page drawOrCalculateOthers(Chapter chapter, float progress, int pageIndex,
                                       int startParagraphIndex, int startCharacterIndex,
                                       Canvas canvas, Paint paint) {
        final float maxTextWidth = readerView.getWidth() - textMarginLeft - textMarginRight;
        final float maxY;
        if (chapter.status == Chapter.STATUS_LOADED) {
            maxY = readerView.getHeight() - textMarginBottom;
        } else {
            maxY = readerView.getHeight()
                    - readerView.getResources().getDimension(R.dimen.reader_not_loaded_panel_height)
                    - paragraphSpacing;
        }
        float y = textMarginTop, x = textMarginLeft;
        //
        // Small title is showed on each page
        //
        paint.setTextSize(textSizeSmall);
        paint.setColor(textColorSecondary);
        float lineHeight = calculateLineHeight(paint);
        y += titleSmallMarginTop + lineHeight;
        int measuredChars = paint.breakText(chapter.title,
                                            true, maxTextWidth / 2, null);
        if (canvas != null) {
            canvas.drawText(chapter.title, 0, measuredChars, x, y, paint);
        }
        y = Math.max(y + titleSmallMarginBottom, readerView.getReaderActivity().getStatusBarHeight());
        //
        // Large title is only showed on the first page
        //
        if (startParagraphIndex == 0 && startCharacterIndex == 0) {
            y += titleLargeMarginTop;
            paint.setTextSize(textSizeLarge);
            paint.setColor(textColorPrimary);
            lineHeight = calculateLineHeight(paint);
            int characterIndex = 0;
            while (characterIndex < chapter.title.length()) {
                y += lineHeight;
                measuredChars = paint.breakText(chapter.title, characterIndex, chapter.title.length(),
                                                true, maxTextWidth, null);
                if (characterIndex + measuredChars < chapter.title.length()) {
                    measuredChars = remeasure(chapter.title, characterIndex, measuredChars);
                }
                float measuredWidth = paint.measureText(chapter.title,
                                                        characterIndex, characterIndex + measuredChars);
                x = textMarginLeft + (maxTextWidth - measuredWidth) / 2;
                if (canvas != null) {
                    canvas.drawText(chapter.title, characterIndex, characterIndex + measuredChars,
                                    x, y, paint);
                }
                characterIndex += measuredChars;
            }
            y += titleLargeMarginBottom;
            x = textMarginLeft;
        }
        //
        // Paragraphs
        //
        paint.setTextSize(textSizeDefault);
        paint.setColor(textColorPrimary);
        lineHeight = calculateLineHeight(paint);
        for (int i = startParagraphIndex; i < chapter.paragraphs.size(); i++) {
            String paragraph = chapter.paragraphs.get(i);
            int characterIndex = (i == startParagraphIndex) ? startCharacterIndex : 0;
            y += lineHeight;
            while (characterIndex < paragraph.length()) {
                if (y >= maxY) {
                    if (canvas != null) {
                        drawBottomView(chapter, pageIndex, canvas, paint);
                        return null;
                    } else {
                        return new Page(chapter.id, progress, pageIndex,
                                        startParagraphIndex, startCharacterIndex, i, characterIndex);
                    }
                }
                measuredChars = paint.breakText(paragraph, characterIndex, paragraph.length(),
                                                true, maxTextWidth, null);
                if (characterIndex + measuredChars < paragraph.length()) {
                    measuredChars = remeasure(paragraph, characterIndex, measuredChars);
                }
                if (canvas != null) {
                    String line = paragraph.substring(characterIndex, characterIndex + measuredChars);
                    drawLine(canvas, line, x, y,
                             paint, characterIndex + measuredChars >= paragraph.length(), maxTextWidth);
                }
                characterIndex += measuredChars;
                y += paragraphLineSpacing + lineHeight;
            }
            y += paragraphSpacing - paragraphLineSpacing - lineHeight;
        }
        if (canvas != null) {
            drawBottomView(chapter, pageIndex, canvas, paint);
            return null;
        } else {
            return new Page(chapter.id, progress, pageIndex,
                            startParagraphIndex, startCharacterIndex, chapter.paragraphs.size(), 0);
        }
    }

    private float calculateLineHeight(Paint paint) {
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        return fontMetrics.bottom - fontMetrics.top;
    }

    private int remeasure(String text, int characterIndex, int measuredChars) {
        if (AVOID_LEADING_IF_EVEN_CHARS.contains(text.charAt(characterIndex + measuredChars))
                && !isOccurenceTimeOdd(text, characterIndex + measuredChars)) {
            if (isUtf16TrailSurrogate(text.charAt(characterIndex + measuredChars - 1))) {
                measuredChars -= 2;
            } else {
                measuredChars -= 1;
            }
        }
        for (int i = 0; i < 3; i++) {
            if (AVOID_LEADING_CHARS.contains(text.charAt(characterIndex + measuredChars))) {
                if (isUtf16TrailSurrogate(text.charAt(characterIndex + measuredChars - 1))) {
                    measuredChars -= 2;
                } else {
                    measuredChars -= 1;
                }
            }
        }
        if (AVOID_BREAKING_CHARS_1.contains(text.charAt(characterIndex + measuredChars))) {
            for (int j = measuredChars; j > Math.max(measuredChars - 15, 0); j--) {
                if (!AVOID_BREAKING_CHARS_1.contains(text.charAt(characterIndex + j))) {
                    measuredChars = j + 1;
                    break;
                }
            }
        }
        if (AVOID_BREAKING_CHARS_2.contains(text.charAt(characterIndex + measuredChars))) {
            for (int j = measuredChars; j > Math.max(measuredChars - 15, 0); j--) {
                if (!AVOID_BREAKING_CHARS_2.contains(text.charAt(characterIndex + j))) {
                    measuredChars = j + 1;
                    break;
                }
            }
        }
        if (AVOID_TRAILING_CHARS.contains(text.charAt(characterIndex + measuredChars - 1))) {
            measuredChars--;
        }
        if (AVOID_TRAILING_IF_ODD_CHARS.contains(text.charAt(characterIndex + measuredChars - 1))
                && isOccurenceTimeOdd(text, characterIndex + measuredChars - 1)) {
            measuredChars--;
        }
        for (int j = 0; j < 2; j++) {
            if (characterIndex + measuredChars < text.length()) {
                char c = text.charAt(characterIndex + measuredChars);
                if (c == ' ' || c == '\u3000') {
                    measuredChars++;
                }
            }
        }
        return measuredChars;
    }

    private boolean isOccurenceTimeOdd(String text, int index) {
        char c = text.charAt(index);
        int occurrenceTime = 0;
        for (int i = 0; i <= index; i++) {
            if (text.charAt(i) == c) {
                occurrenceTime++;
            }
        }
        return occurrenceTime % 2 == 1;
    }

    private boolean isUtf16LeadSurrogate(char c) {
        return c >= 0xd800 && c <= 0xdbff;
    }

    private boolean isUtf16TrailSurrogate(char c) {
        return c >= 0xdc00 && c <= 0xdfff;
    }

    private void drawLine(Canvas canvas, String line, float x, float y,
                          Paint paint, boolean isLastLine, float maxTextWidth) {
        float lineMeasuredWidth = paint.measureText(line);
        if (!isLastLine && lineMeasuredWidth < maxTextWidth * 1.0f) {
            float extraSpacing = (maxTextWidth - lineMeasuredWidth) / line.length();
            for (int left = 0; left < line.length(); ) {
                char c = line.charAt(left);
                int right = Math.min((isUtf16LeadSurrogate(c) ? left + 2 : left + 1), line.length());
                String character = line.substring(left, right);
                canvas.drawText(character, x, y, paint);
                x += paint.measureText(character) + extraSpacing;
                left = right;
            }
        } else {
            canvas.drawText(line, x, y, paint);
        }
    }

    private void drawBottomView(Chapter chapter, int pageIndex, Canvas canvas, Paint paint) {
        if (pageIndex < chapter.pages.size() - 1) {
            //
            // Draw battery level
            //
            paint.setColor(textColorSecondary);
            canvas.drawLines(new float[]{
                             19 * DENSITY, canvas.getHeight() - 21 * DENSITY,
                             44 * DENSITY, canvas.getHeight() - 21 * DENSITY,
                             44 * DENSITY, canvas.getHeight() - 21 * DENSITY,
                             44 * DENSITY, canvas.getHeight() - 11 * DENSITY,
                             44 * DENSITY, canvas.getHeight() - 11 * DENSITY,
                             19 * DENSITY, canvas.getHeight() - 11 * DENSITY,
                             19 * DENSITY, canvas.getHeight() - 11 * DENSITY,
                             19 * DENSITY, canvas.getHeight() - 21 * DENSITY
                    },
                    paint);
            canvas.drawRect(20 * DENSITY,
                            canvas.getHeight() - 20 * DENSITY,
                            (20 + 23 * BatteryBroadcastReceiver.getInstance().getValue()) * DENSITY,
                            canvas.getHeight() - 12 * DENSITY,
                            paint);
            canvas.drawRect(45 * DENSITY,
                            canvas.getHeight() - 18 * DENSITY,
                            46 * DENSITY,
                            canvas.getHeight() - 14 * DENSITY,
                            paint);
            //
            // Draw time
            //
            paint.setTextSize(textSizeSmall);
            canvas.drawText(TIME_FORMAT.format(new Date(System.currentTimeMillis())),
                            53 * DENSITY,
                            canvas.getHeight() - 12 * DENSITY,
                            paint);
            //
            // Draw read progress
            //
            if (readerView.getTableOfContents().size() > 0) {
                float progressValue = calculateProgressInBook(chapter.id, pageIndex) * 100;
                String progressText = String.format(Locale.getDefault(), "%.2f%%", progressValue);
                canvas.drawText(progressText,
                                canvas.getWidth() - 19 * DENSITY - paint.measureText(progressText),
                                canvas.getHeight() - 12 * DENSITY,
                                paint);
            }
        } else {
            switch (chapter.status) {
                case Chapter.STATUS_LOADED: {
                    break;
                }
                case Chapter.STATUS_PAYMENT_REQUIRED: {
                    drawChildView(canvas, readerView.getPaymentRequiredView().getContentView());
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    private void drawChildView(Canvas canvas, View view) {
        view.setDrawingCacheEnabled(true);
        view.measure(
                View.MeasureSpec.makeMeasureSpec(readerView.getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(readerView.getHeight(), View.MeasureSpec.EXACTLY));
        view.layout(0, 0, readerView.getWidth(), readerView.getHeight());
        Bitmap drawingCache = view.getDrawingCache();
        if (drawingCache != null) {
            canvas.drawBitmap(drawingCache, 0.0f, 0.0f, null);
        }
        view.setDrawingCacheEnabled(false);
    }
}
