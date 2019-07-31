package org.wreader.reader.core.helper;

import org.wreader.reader.core.App;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class FileHelper {
    public static String readAssetsText(String path) throws IOException {
        return readText(App.getInstance().getAssets().open(path));
    }

    public static String readCacheText(String path) throws IOException {
        return readText(new FileInputStream(new File(App.getInstance().getCacheDir(), path)));
    }

    public static boolean isCacheFileExists(String path) {
        return new File(App.getInstance().getCacheDir(), path).exists();
    }

    public static void writeCacheText(String path, String text) throws IOException {
        OutputStream outputStream = null;
        OutputStreamWriter writer = null;
        BufferedWriter bufferedWriter = null;
        try {
            File file = new File(App.getInstance().getCacheDir(), path);
            file.getParentFile().mkdirs();
            outputStream = new FileOutputStream(file);
            writer = new OutputStreamWriter(outputStream);
            bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write(text);
            bufferedWriter.flush();
        } finally {
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
            } finally {
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } finally {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                }
            }
        }
    }

    private static String readText(InputStream inputStream) throws IOException {
        InputStreamReader reader = null;
        BufferedReader bufferedReader = null;
        try {
            reader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(reader);
            String line = null;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            }
        }
    }
}
