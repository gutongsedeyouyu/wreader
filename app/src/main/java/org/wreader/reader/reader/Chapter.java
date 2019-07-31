package org.wreader.reader.reader;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.ArrayList;

public class Chapter {
    public static final int STATUS_LOAD_FAILED = 0;
    public static final int STATUS_LOADED = 1;
    public static final int STATUS_PAYMENT_REQUIRED = 2;

    @JSONField(name = "id")
    public String id;

    @JSONField(name = "previousId")
    public String previousId;

    @JSONField(name = "nextId")
    public String nextId;

    @JSONField(name = "title")
    public String title;

    @JSONField(name = "paragraphs")
    public ArrayList<String> paragraphs;

    @JSONField(name = "status")
    public int status;

    public transient ArrayList<Page> pages;
}
