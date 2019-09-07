package org.wreader.reader.reader;

public class Sentence {
    public final String chapterId;
    public final boolean isTitle;
    public final int paragraphIndex;
    public final int beginCharacterIndex;
    public final int endCharacterIndex;
    public final String text;

    public Sentence(String chapterId, boolean isTitle,
                    int paragraphIndex, int beginCharacterIndex, int endCharacterIndex,
                    String text) {
        this.chapterId = chapterId;
        this.isTitle = isTitle;
        this.paragraphIndex = paragraphIndex;
        this.beginCharacterIndex = beginCharacterIndex;
        this.endCharacterIndex = endCharacterIndex;
        this.text = text;
    }
}
