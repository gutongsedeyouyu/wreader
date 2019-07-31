package org.wreader.reader.reader;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;

import org.wreader.reader.core.helper.FileHelper;
import org.wreader.reader.core.helper.SharedPreferencesHelper;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class BookDataHelper {
    private static final String PREFERENCE_KEY_READ_PROGRESS_PREFIX = "READ_PROGRESS_";

    private static final String BOOKS_JSON_PATH = "books/books.json";

    private static final int CHAPTER_STATUS_CHOICES[] = new int[] {
            Chapter.STATUS_LOADED,
            Chapter.STATUS_LOADED,
            Chapter.STATUS_LOADED,
            Chapter.STATUS_LOAD_FAILED,
            Chapter.STATUS_PAYMENT_REQUIRED
    };
    private static final Random RANDOM = new Random();

    public static void loadBooks(DataLoadedCallback<List<Book>> callback) {
        new LoadBooksTask(callback).execute();
    }

    public static void saveBooks(List<Book> books) {
        Collections.sort(books, new Comparator<Book>() {
            @Override
            public int compare(Book book1, Book book2) {
                return Long.compare(book2.lastReadTime, book1.lastReadTime);
            }
        });
        try {
            FileHelper.writeCacheText(BOOKS_JSON_PATH, JSON.toJSONString(books));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static Page getReadProgress(String bookId) {
        String progressKey = String.format("%s%s", PREFERENCE_KEY_READ_PROGRESS_PREFIX, bookId);
        String progressValue = SharedPreferencesHelper.getString(progressKey, null);
        if (progressValue == null) {
            return null;
        }
        String[] splits = progressValue.split(",");
        if (splits.length < 2) {
            return null;
        }
        String chapterId = splits[0];
        float progress;
        try {
            progress = Float.valueOf(splits[1]);
        } catch (NumberFormatException ex) {
            Log.e("WReader", "BookDataHelper.getReadProgress() -> progress=" + splits[1]);
            progress = 0.0f;
        }
        if (progress < 0.0f || progress > 1.0f) {
            Log.e("WReader", "BookDataHelper.getReadProgress() -> progress=" + progress);
            progress = 0.0f;
        }
        return new Page(chapterId, progress);
    }

    public static void setReadProgress(String bookId, String chapterId, float progress) {
        if (TextUtils.isEmpty(bookId) || TextUtils.isEmpty(chapterId)) {
            return;
        }
        if (progress < 0.0f || progress > 1.0f) {
            Log.e("WReader", "BookDataHelper.setReadProgress() - progress=" + progress);
            progress = 0.0f;
        }
        String progressKey = String.format("%s%s", PREFERENCE_KEY_READ_PROGRESS_PREFIX, bookId);
        String progressValue = String.format("%s,%f", chapterId, progress);
        SharedPreferencesHelper.setString(progressKey, progressValue);
    }

    public static void loadTableOfContents(String bookId, DataLoadedCallback<List<Chapter>> callback) {
        new LoadTableOfContentsTask(callback).execute(bookId);
    }

    public static void loadChapter(String bookId, String chapterId, DataLoadedCallback<Chapter> callback) {
        new LoadChapterTask(callback).execute(bookId, chapterId);
    }

    public interface DataLoadedCallback<T> {
        void onDataLoaded(T t);
    }

    private static class LoadBooksTask extends AsyncTask<Void, Integer, List<Book>> {
        private final DataLoadedCallback<List<Book>> callback;

        private LoadBooksTask(DataLoadedCallback<List<Book>> callback) {
            this.callback = callback;
        }

        @Override
        protected List<Book> doInBackground(Void... params) {
            simulateNetworkDelay();
            try {
                String jsonText;
                if (!FileHelper.isCacheFileExists(BOOKS_JSON_PATH)) {
                    jsonText = FileHelper.readAssetsText(BOOKS_JSON_PATH);
                    FileHelper.writeCacheText(BOOKS_JSON_PATH, jsonText);
                } else {
                    jsonText = FileHelper.readCacheText(BOOKS_JSON_PATH);
                }
                return JSON.parseArray(jsonText, Book.class);
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Book> books) {
            callback.onDataLoaded(books);
        }
    }

    private static class LoadTableOfContentsTask extends AsyncTask<String, Integer, List<Chapter>> {
        private final DataLoadedCallback<List<Chapter>> callback;

        private LoadTableOfContentsTask(DataLoadedCallback<List<Chapter>> callback) {
            this.callback = callback;
        }

        @Override
        protected List<Chapter> doInBackground(String... params) {
            simulateNetworkDelay();
            String bookId = params[0];
            try {
                String jsonText = FileHelper.readAssetsText(String.format("books/%s/tableOfContents.json", bookId));
                return JSON.parseArray(jsonText, Chapter.class);
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Chapter> tableOfContents) {
            callback.onDataLoaded(tableOfContents);
        }
    }

    private static class LoadChapterTask extends AsyncTask<String, Integer, Chapter> {
        private final DataLoadedCallback<Chapter> callback;

        private LoadChapterTask(DataLoadedCallback<Chapter> callback) {
            this.callback = callback;
        }

        @Override
        protected Chapter doInBackground(String... params) {
            simulateNetworkDelay();
            String bookId = params[0];
            String chapterId = !TextUtils.isEmpty(params[1]) ? params[1] : "1";
            try {
                String jsonText = FileHelper.readAssetsText(String.format("books/%s/%s.json", bookId, chapterId));
                Chapter chapter = JSON.parseObject(jsonText, Chapter.class);
                chapter.status = randomChapterStatus();
                return chapter;
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Chapter chapter) {
            callback.onDataLoaded(chapter);
        }
    }

    private static void simulateNetworkDelay() {
        try {
            Thread.sleep((long) (3 * 1000 * RANDOM.nextDouble()));
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private static int randomChapterStatus() {
        return CHAPTER_STATUS_CHOICES[RANDOM.nextInt(CHAPTER_STATUS_CHOICES.length)];
    }
}
