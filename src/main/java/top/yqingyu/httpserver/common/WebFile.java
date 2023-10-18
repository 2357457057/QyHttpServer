package top.yqingyu.httpserver.common;

import java.io.File;

public class WebFile extends File {
    private volatile long lastModify;
    private final ContentType contentType;

    public WebFile(String fileStr) {
        super(fileStr);
        contentType = ContentType.parseContentType(fileStr);
        lastModify = this.lastModified();
    }


    public boolean isModify() {
        if (!exists()) return true;
        long lastModified = this.lastModified();
        try {
            return lastModified == lastModify;
        } finally {
            lastModify = lastModified;
        }

    }

    public ContentType getContentType() {
        return contentType;
    }
}
