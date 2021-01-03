package org.wreader.reader.core.helper;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import org.wreader.reader.R;
import org.wreader.reader.reader.view.ReaderActivity;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class Router {
    private Router() {
    }

    public static void route(Context context, String uriString) {
        try {
            URI uri = new URI(uriString);
            if (!"wreader".equals(uri.getScheme())) {
                return;
            }
            Method method = Router.class.getMethod(uri.getAuthority(), Context.class, Map.class);
            method.invoke(Router.class, context, getQuery(uri));
        } catch (Exception ex) {
            unsupported(context);
        }
    }

    private static Map<String, String> getQuery(URI uri) {
        Map<String, String> query = new HashMap<>();
        String rawQuery = uri.getRawQuery();
        if (rawQuery != null) {
            for (String q : rawQuery.split("&")) {
                String s[] = q.split("=");
                try {
                    query.put(s[0], (s.length > 1) ? URLDecoder.decode(s[1], "utf-8") : "");
                } catch (UnsupportedEncodingException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return query;
    }

    @Since(version = "1.0.0")
    private static void unsupported(Context context) {
        toast(context, context.getString(R.string.unsupported_action));
    }

    @Since(version = "1.0.0")
    public static void toast(Context context, String text) {
        if (!TextUtils.isEmpty(text)) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        }
    }

    @Since(version = "1.0.0")
    public static void toast(Context context, Map<String, String> query) {
        toast(context, query.get("text"));
    }

    @Since(version = "1.0.0")
    public static void readBook(Context context, Map<String, String> query) {
        Intent intent = new Intent(context, ReaderActivity.class);
        intent.putExtra(ReaderActivity.PARAM_KEY_BOOK_ID, query.get("id"));
        intent.putExtra(ReaderActivity.PARAM_KEY_CHAPTER_ID, query.get("chapterId"));
        context.startActivity(intent);
    }
}
