package org.wreader.reader.reader.presenter;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;

import org.wreader.reader.core.helper.FileHelper;
import org.wreader.reader.reader.beans.Chapter;
import org.wreader.reader.reader.beans.Page;
import org.wreader.reader.reader.beans.Sentence;
import org.wreader.reader.reader.view.ReaderView;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.wreader.reader.core.helper.TextHelper.isOccurenceTimeOdd;
import static org.wreader.reader.core.helper.TextHelper.isSpace;

public class ReaderTtsHelper {
    private static final Set<Character> SENTENCE_BEGIN_CHARS = new HashSet<>();
    private static final Set<Character> SENTENCE_BEGIN_IF_ODD_CHARS = new HashSet<>();
    private static final Set<Character> SENTENCE_END_IF_FOLLOWED_BY_SPACE_CHARS = new HashSet<>();
    private static final Set<Character> SENTENCE_END_CHARS = new HashSet<>();
    private static final Set<Character> SENTENCE_END_IF_EVEN_CHARS = new HashSet<>();

    static {
        for (char c : "“(（[【{「『".toCharArray()) {
            SENTENCE_BEGIN_CHARS.add(c);
        }
        for (char c : "\"".toCharArray()) {
            SENTENCE_BEGIN_IF_ODD_CHARS.add(c);
        }
        for (char c : ".".toCharArray()) {
            SENTENCE_END_IF_FOLLOWED_BY_SPACE_CHARS.add(c);
        }
        for (char c : "．。?？!！…⋯”)）]】}」』".toCharArray()) {
            SENTENCE_END_CHARS.add(c);
        }
        for (char c : "\"".toCharArray()) {
            SENTENCE_END_IF_EVEN_CHARS.add(c);
        }
    }

    private final ReaderView readerView;
    private final SpeechSynthesizer synthesizer;
    private final SynthesizerListener listener;

    private Sentence currentSentence;

    public static void init(Context context) {
        try {
            SpeechUtility.createUtility(context, FileHelper.readTextFromAssets("tts_key.txt"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public ReaderTtsHelper(ReaderView readerView) {
        this.readerView = readerView;
        this.synthesizer = SpeechSynthesizer.createSynthesizer(readerView.getContext(), new InitListener() {
            @Override
            public void onInit(int status) {
                ReaderTtsHelper.this.onTtsInit(status == 0, "" + status);
            }
        });
        this.listener = new SynthesizerListener() {
            @Override
            public void onSpeakBegin() {
                ReaderTtsHelper.this.onTtsBegin();
            }

            @Override
            public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
            }

            @Override
            public void onSpeakPaused() {
                ReaderTtsHelper.this.onTtsPaused();
            }

            @Override
            public void onSpeakResumed() {
                ReaderTtsHelper.this.onTtsResumed();
            }

            @Override
            public void onSpeakProgress(int percent, int beginPos, int endPos) {
            }

            @Override
            public void onCompleted(SpeechError error) {
                if (error == null) {
                    ReaderTtsHelper.this.onTtsCompleted();
                } else {
                    ReaderTtsHelper.this.onTtsError("" + error.getErrorCode() + ": " + error.getErrorDescription());
                }
            }

            @Override
            public void onEvent(int eventType, int arg1, int arg2, Bundle bundle) {
            }
        };
    }

    public void start(Sentence sentence) {
        currentSentence = sentence;
        readerView.setSpeakingSentence(currentSentence);
        if (currentSentence != null) {
            synthesizer.startSpeaking(currentSentence.text, listener);
        }
    }

    public void pause() {
        synthesizer.pauseSpeaking();
    }

    public void resume() {
        synthesizer.resumeSpeaking();
    }

    public void stop() {
        synthesizer.stopSpeaking();
        currentSentence = null;
        readerView.setSpeakingSentence(null);
    }

    public Sentence getFirstSentenceInPage(Page page) {
        if (page == null || page.pageIndex < 0) {
            return null;
        }
        Chapter chapter = readerView.getCachedChapter(page.chapterId);
        if (chapter == null || chapter.status != Chapter.STATUS_LOADED) {
            return null;
        }
        //
        // When pageIndex == 0
        //
        if (page.pageIndex == 0) {
            return new Sentence(page.chapterId, true,
                    -1, -1, -1,
                    chapter.title);
        }
        //
        // When pageIndex > 0
        //
        Sentence sentence;
        if (page.beginParagraphIndex == 0) {
            sentence = getNextSentence(new Sentence(page.chapterId, true,
                    -1, -1, -1,
                    chapter.title));
        } else {
            int paragraphIndex = page.beginParagraphIndex - 1;
            String paragraph = chapter.paragraphs.get(paragraphIndex);
            sentence = getNextSentence(new Sentence(page.chapterId, false,
                    paragraphIndex, 0, paragraph.length(),
                    paragraph));
        }
        for (; ; sentence = getNextSentence(sentence)) {
            if (sentence == null || !sentence.chapterId.equals(page.chapterId)) {
                return null;
            }
            if (sentence.endCharacterIndex > page.beginCharacterIndex) {
                return sentence;
            }
        }
    }

    public Sentence getPreviousSentence(Sentence sentence) {
        if (sentence == null || sentence.isTitle) {
            return sentence;
        }
        Chapter chapter = readerView.getCachedChapter(sentence.chapterId);
        if (chapter == null || chapter.status != Chapter.STATUS_LOADED) {
            return null;
        }
        //
        // 1. Filter paragraph leading spaces.
        //
        int paragraphIndex = sentence.paragraphIndex;
        String paragraph = chapter.paragraphs.get(paragraphIndex);
        boolean isFirstSentenceInParagraph = true;
        for (int i = sentence.beginCharacterIndex - 1; i >= 0; i--) {
            if (!isSpace(paragraph.charAt(i))) {
                isFirstSentenceInParagraph = false;
                break;
            }
        }
        //
        // 2. Calculate end character index.
        //
        int endCharacterIndex;
        if (!isFirstSentenceInParagraph) {
            //
            // 2.1 The previous sentence is in the same paragraph.
            //
            endCharacterIndex = sentence.beginCharacterIndex;
        } else if (paragraphIndex > 0) {
            //
            // 2.2 The previous sentence is in the previous paragraph.
            //
            paragraphIndex = paragraphIndex - 1;
            paragraph = chapter.paragraphs.get(paragraphIndex);
            endCharacterIndex = paragraph.length();
        } else {
            //
            // 2.3 The previous sentence is the title.
            //
            return new Sentence(chapter.id, true,
                    -1, -1, -1,
                    chapter.title);
        }
        //
        // 3. Calculate begin character index.
        //
        int beginCharacterIndex;
        for (beginCharacterIndex = endCharacterIndex - 1; beginCharacterIndex > 0; beginCharacterIndex--) {
            if (!isSpace(paragraph.charAt(beginCharacterIndex))) {
                break;
            }
        }
        boolean nonDelimiterFound = false;
        boolean beginDelimiterFound = false;
        for (; beginCharacterIndex >= 0; beginCharacterIndex--) {
            char c = paragraph.charAt(beginCharacterIndex);
            boolean isEndDelimiter = false;
            if (SENTENCE_END_CHARS.contains(c)) {
                isEndDelimiter = true;
            }
            if (SENTENCE_END_IF_EVEN_CHARS.contains(c)
                    && !isOccurenceTimeOdd(paragraph, beginCharacterIndex)) {
                isEndDelimiter = true;
            }
            if (SENTENCE_END_IF_FOLLOWED_BY_SPACE_CHARS.contains(c)
                    && beginCharacterIndex < paragraph.length() - 1
                    && isSpace(paragraph.charAt(beginCharacterIndex + 1))) {
                isEndDelimiter = true;
            }
            if (isEndDelimiter && nonDelimiterFound) {
                beginCharacterIndex++;
                break;
            }
            if (!isEndDelimiter) {
                nonDelimiterFound = true;
            }
            boolean isBeginDelimiter = false;
            if (SENTENCE_BEGIN_CHARS.contains(c)) {
                isBeginDelimiter = true;
            }
            if (SENTENCE_BEGIN_IF_ODD_CHARS.contains(c)
                    && isOccurenceTimeOdd(paragraph, beginCharacterIndex)) {
                isBeginDelimiter = true;
            }
            if (isBeginDelimiter) {
                beginDelimiterFound = true;
            }
            if (beginDelimiterFound && !isBeginDelimiter) {
                beginCharacterIndex++;
                break;
            }
        }
        if (beginCharacterIndex < 0) {
            beginCharacterIndex = 0;
        }
        //
        // 4. Filter paragraph leading spaces.
        //
        if (beginCharacterIndex == 0) {
            for (; beginCharacterIndex < paragraph.length(); beginCharacterIndex++) {
                if (!isSpace(paragraph.charAt(beginCharacterIndex))) {
                    break;
                }
            }
        }
        return new Sentence(chapter.id, false,
                paragraphIndex, beginCharacterIndex, endCharacterIndex,
                paragraph.substring(beginCharacterIndex, endCharacterIndex));
    }

    public Sentence getNextSentence(Sentence sentence) {
        if (sentence == null) {
            return null;
        }
        Chapter chapter = readerView.getCachedChapter(sentence.chapterId);
        if (chapter == null || chapter.status != Chapter.STATUS_LOADED) {
            return null;
        }
        //
        // 1. Calculate begin character index.
        //
        int paragraphIndex;
        String paragraph;
        int beginCharacterIndex;
        if (sentence.isTitle) {
            //
            // 1.1 The next sentence is in the first paragraph.
            //
            paragraphIndex = 0;
            paragraph = chapter.paragraphs.get(paragraphIndex);
            beginCharacterIndex = 0;
        } else if (sentence.endCharacterIndex < chapter.paragraphs.get(sentence.paragraphIndex).length()) {
            //
            // 1.2 The next sentence is in the same paragraph.
            //
            paragraphIndex = sentence.paragraphIndex;
            paragraph = chapter.paragraphs.get(paragraphIndex);
            beginCharacterIndex = sentence.endCharacterIndex;
        } else if (sentence.paragraphIndex < chapter.paragraphs.size() - 1) {
            //
            // 1.3 The next sentence is in the next paragraph.
            //
            paragraphIndex = sentence.paragraphIndex + 1;
            paragraph = chapter.paragraphs.get(paragraphIndex);
            beginCharacterIndex = 0;
        } else {
            //
            // 1.4 The next sentence is in the next chapter.
            //
            if (TextUtils.isEmpty(chapter.nextId)) {
                return null;
            }
            Chapter nextChapter = readerView.getCachedChapter(chapter.nextId);
            if (nextChapter == null || nextChapter.status != Chapter.STATUS_LOADED) {
                return null;
            }
            readerView.getPaginator().recalculatePages(nextChapter, 0.0f);
            return new Sentence(nextChapter.id, true,
                    -1, -1, -1,
                    nextChapter.title);
        }
        //
        // 2. Filter paragraph leading spaces.
        //
        if (beginCharacterIndex == 0) {
            for (; beginCharacterIndex < paragraph.length(); beginCharacterIndex++) {
                if (!isSpace(paragraph.charAt(beginCharacterIndex))) {
                    break;
                }
            }
        }
        //
        // 3. Calculate end character index.
        //
        int endCharacterIndex = (paragraph.length() > 0) ? beginCharacterIndex + 1 : 0;
        boolean delimiterFound = false;
        for (; endCharacterIndex < paragraph.length(); endCharacterIndex++) {
            char c = paragraph.charAt(endCharacterIndex);
            if (SENTENCE_BEGIN_CHARS.contains(c)) {
                break;
            }
            if (SENTENCE_BEGIN_IF_ODD_CHARS.contains(c)
                    && isOccurenceTimeOdd(paragraph, endCharacterIndex)) {
                break;
            }
            if (SENTENCE_END_IF_FOLLOWED_BY_SPACE_CHARS.contains(c)
                    && endCharacterIndex + 1 < paragraph.length()
                    && isSpace(paragraph.charAt(endCharacterIndex + 1))) {
                endCharacterIndex += 2;
                break;
            }
            if (SENTENCE_END_CHARS.contains(c)) {
                delimiterFound = true;
                continue;
            }
            if (SENTENCE_END_IF_EVEN_CHARS.contains(c)
                    && !isOccurenceTimeOdd(paragraph, endCharacterIndex)) {
                delimiterFound = true;
                continue;
            }
            if (delimiterFound) {
                break;
            }
        }
        return new Sentence(sentence.chapterId, false,
                paragraphIndex, beginCharacterIndex, endCharacterIndex,
                paragraph.substring(beginCharacterIndex, endCharacterIndex));
    }

    private void onTtsInit(boolean succeeded, String message) {
        if (succeeded) {
            Log.d("WReader", "ReaderTtsHelper.onTtsInit() [Succeeded] - message=" + message);
        } else {
            Log.e("WReader", "ReaderTtsHelper.onTtsInit() [Failed] - message=" + message);
        }
    }

    private void onTtsBegin() {
    }

    private void onTtsPaused() {
    }

    private void onTtsResumed() {
    }

    private void onTtsCompleted() {
        start(getNextSentence(currentSentence));
    }

    private void onTtsError(String message) {
        currentSentence = null;
        readerView.onTtsError(message);
    }
}
