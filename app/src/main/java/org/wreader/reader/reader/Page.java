package org.wreader.reader.reader;

public class Page {
    public final String chapterId;

    public final float progress;

    public final int pageIndex;

    public final int beginParagraphIndex;

    public final int beginCharacterIndex;

    public final int endParagraphIndex;

    public final int endCharacterIndex;

    public Page(String chapterId, float progress) {
        this(chapterId, progress, -1,
             -1, -1, -1, -1);
    }

    public Page(String chapterId, float progress, int pageIndex,
                int beginParagraphIndex, int beginCharacterIndex, int endParagraphIndex, int endCharacterIndex) {
        this.chapterId = chapterId;
        this.progress = progress;
        this.pageIndex = pageIndex;
        this.beginParagraphIndex = beginParagraphIndex;
        this.beginCharacterIndex = beginCharacterIndex;
        this.endParagraphIndex = endParagraphIndex;
        this.endCharacterIndex = endCharacterIndex;
    }

    public boolean containsSentence(Sentence sentence) {
        if (pageIndex < 0 || !chapterId.equals(sentence.chapterId)) {
            return false;
        }
        if (sentence.isTitle) {
            return pageIndex == 0;
        }
        if (sentence.paragraphIndex < beginParagraphIndex
                || sentence.paragraphIndex > endParagraphIndex) {
            return false;
        }
        if (sentence.paragraphIndex > beginParagraphIndex
                && sentence.paragraphIndex < endParagraphIndex) {
            return true;
        }
        if (sentence.paragraphIndex == beginParagraphIndex
                && sentence.paragraphIndex == endParagraphIndex) {
            if (sentence.beginCharacterIndex < beginCharacterIndex) {
                return sentence.endCharacterIndex > beginCharacterIndex;
            } else if (sentence.beginCharacterIndex < endCharacterIndex) {
                return true;
            } else {
                return false;
            }
        } else if (sentence.paragraphIndex == beginParagraphIndex) {
            return sentence.endCharacterIndex > beginCharacterIndex;
        } else if (sentence.paragraphIndex == endParagraphIndex) {
            return sentence.beginCharacterIndex < endCharacterIndex;
        } else {
            throw new IllegalStateException();
        }
    }
}
