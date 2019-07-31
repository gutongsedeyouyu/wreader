package org.wreader.reader.reader;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.ArrayList;

public class Book {
    @JSONField(name = "id")
    public String id;

    @JSONField(name = "name")
    public String name;

    @JSONField(name = "tags")
    public ArrayList<String> tags;

    @JSONField(name = "introduction")
    public String introduction;

    @JSONField(name = "lastReadTime")
    public long lastReadTime;
}
