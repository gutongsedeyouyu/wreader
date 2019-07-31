package org.wreader.reader.reader;

public class Page {
    public final String chapterId;

    public final float progress;

    public final int pageIndex;

    public final int startParagraphIndex;

    public final int startCharacterIndex;

    public final int endParagraphIndex;

    public final int endCharacterIndex;

    public Page(String chapterId, float progress) {
        this(chapterId, progress, -1,
             -1, -1, -1, -1);
    }

    public Page(String chapterId, float progress, int pageIndex,
                int startParagraphIndex, int startCharacterIndex, int endParagraphIndex, int endCharacterIndex) {
        this.chapterId = chapterId;
        this.progress = progress;
        this.pageIndex = pageIndex;
        this.startParagraphIndex = startParagraphIndex;
        this.startCharacterIndex = startCharacterIndex;
        this.endParagraphIndex = endParagraphIndex;
        this.endCharacterIndex = endCharacterIndex;
    }
}
