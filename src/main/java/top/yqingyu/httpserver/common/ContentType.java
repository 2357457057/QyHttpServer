package top.yqingyu.httpserver.common;

import top.yqingyu.common.qydata.DataMap;
import top.yqingyu.common.utils.StringUtil;
import top.yqingyu.common.utils.UnameUtil;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Locale;

/**
 * @author YYJ
 * @version 1.0.0
 */
public class ContentType implements Serializable {


    private String adviceStatusCode;

    public String getAdviceStatusCode() {
        return adviceStatusCode;
    }

    public ContentType setAdviceStatusCode(String adviceStatusCode) {
        this.adviceStatusCode = adviceStatusCode;
        return this;
    }

    private static final String CHARSET = "charset";


    public static final ContentType APPLICATION_ATOM_XML = create("application/atom+xml", StandardCharsets.UTF_8);
    public static final ContentType APPLICATION_FORM_URLENCODED = create("application/x-www-form-urlencoded", StandardCharsets.ISO_8859_1);
    public static final ContentType APPLICATION_JSON = create("application/json", StandardCharsets.UTF_8);

    public static final ContentType TEXT_MARKDOWN = create("text/markdown", StandardCharsets.UTF_8);
    public static final ContentType TEXT_XML = create("text/xml", StandardCharsets.UTF_8);
    public static final ContentType TEXT_HTML = create("text/html", StandardCharsets.UTF_8);
    public static final ContentType TEXT_CSS = create("text/css", StandardCharsets.UTF_8);
    public static final ContentType APPLICATION_JS = create("application/javascript", StandardCharsets.UTF_8);
    public static final ContentType APPLICATION_PDF = create("application/pdf", StandardCharsets.UTF_8);
    public static final ContentType APPLICATION_XML = create("application/xml", StandardCharsets.UTF_8);
    public static final ContentType APPLICATION_OCTET_STREAM = create("application/octet-stream", (Charset) null);

    public static final ContentType IMAGE_BMP = create("image/bmp");
    public static final ContentType IMAGE_GIF = create("image/gif");
    public static final ContentType IMAGE_JPEG = create("image/jpeg");
    public static final ContentType IMAGE_PNG = create("image/png");
    public static final ContentType IMAGE_X_ICON = create("image/x-icon");
    public static final ContentType IMAGE_SVG = create("image/svg+xml");
    public static final ContentType IMAGE_TIFF = create("image/tiff");
    public static final ContentType IMAGE_WEBP = create("image/webp");
    public static final ContentType VIDEO_MP4 = create("video/mp4");
    public static final ContentType VIDEO_MPEG4 = create("video/mpeg4");
    public static final ContentType VIDEO_WMV = create("video/x-ms-wmv");
    public static final ContentType VIDEO_AVI = create("video/avi");
    public static final ContentType VIDEO_WEBM = create("video/webm");
    public static final ContentType AUDIO_MP3 = create("audio/mp3");
    public static final ContentType MULTIPART_FORM_DATA = create("multipart/form-data", StandardCharsets.ISO_8859_1);

    public static final ContentType MULTIPART_MIXED = create("multipart/mixed", StandardCharsets.ISO_8859_1);

    public static final ContentType MULTIPART_RELATED = create("multipart/related", StandardCharsets.ISO_8859_1);


    public static final ContentType TEXT_PLAIN = create("text/plain", StandardCharsets.UTF_8);
    public static final ContentType TEXT_EVENT_STREAM = create("text/event-stream", StandardCharsets.UTF_8);

    public static final ContentType WILDCARD = create("*/*", (Charset) null);

    public static final ContentType DEFAULT_TEXT = TEXT_PLAIN;
    // defaults
    public static final ContentType DEFAULT_BINARY = APPLICATION_OCTET_STREAM;

    public static final HashMap<String, ContentType> FILE_CONTENT_TYPE = new HashMap<>() {{
        put("js", APPLICATION_JS.setAdviceStatusCode("304"));
        put("css", TEXT_CSS.setAdviceStatusCode("304"));
        put("png", IMAGE_PNG.setAdviceStatusCode("304"));
        put("jpg", IMAGE_JPEG.setAdviceStatusCode("304"));
        put("jpeg", IMAGE_JPEG.setAdviceStatusCode("304"));
        put("gif", IMAGE_GIF.setAdviceStatusCode("304"));
        put("ico", IMAGE_X_ICON.setAdviceStatusCode("304"));
        put("html", TEXT_HTML.setAdviceStatusCode("304"));
        put("htm", TEXT_HTML.setAdviceStatusCode("304"));
        put("xml", APPLICATION_XML);
        put("pdf", APPLICATION_PDF);
        put("md", TEXT_MARKDOWN);
        put("webp", IMAGE_WEBP);
        put("java", TEXT_PLAIN);
        put("yml", TEXT_PLAIN);
        put("yaml", TEXT_PLAIN);
        put("properties", TEXT_PLAIN);
        put("cfg", TEXT_PLAIN);
        put("txt", TEXT_PLAIN);
        put("wmv", VIDEO_WMV);
        put("webm", VIDEO_WEBM);
        put("avi", VIDEO_AVI);
        put("mp3", AUDIO_MP3);
        put("mp4", VIDEO_MP4);
        put("m4v", VIDEO_MP4);
        put("m4a", VIDEO_MP4);
    }};

    private final String mimeType;
    private final Charset charset;
    private final DataMap params;

    ContentType(final String mimeType, final Charset charset) {
        this.mimeType = mimeType;
        this.charset = charset;
        this.params = null;
    }

    ContentType(final String mimeType, final Charset charset, DataMap params) {
        this.mimeType = mimeType;
        this.charset = charset;
        this.params = params;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public Charset getCharset() {
        return this.charset;
    }

    /**
     * @since 4.3
     */
    public String getParameter(final String name) {
        if (this.params == null) {
            return null;
        }
        return this.params.getString(name);
    }

    /**
     * Generates textual representation of this content type which can be used as the value
     * of a {@code Content-Type} header.
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.mimeType);
        if (this.params != null) {
            this.params.forEach((k, v) -> {
                buf.append("; ").append(k).append("=").append(v);
            });
        } else if (this.charset != null) {
            buf.append("; charset=");
            buf.append(this.charset.name());
        }
        return buf.toString();
    }

    private static boolean valid(final String s) {
        for (int i = 0; i < s.length(); i++) {
            final char ch = s.charAt(i);
            if (ch == '"' || ch == ',' || ch == ';') {
                return false;
            }
        }
        return true;
    }

    public static ContentType create(final String mimeType, final Charset charset) {
        String normalizedMimeType = mimeType.toLowerCase(Locale.ROOT);
        return new ContentType(normalizedMimeType, charset);
    }

    public static ContentType create(final String mimeType) {
        return create(mimeType, (Charset) null);
    }

    public static ContentType create(final String mimeType, final String charset) throws UnsupportedCharsetException {
        return create(mimeType, !StringUtil.isBlank(charset) ? Charset.forName(charset) : null);
    }

    private static ContentType create(DataMap helem) {
        final String mimeType = helem.getString("mimeType");
        if (StringUtil.isBlank(mimeType)) {
            return null;
        }
        return create(mimeType, helem.getData("Parameters"));
    }

    private static ContentType create(final String mimeType, DataMap params) {
        Charset charset = null;
        if (params != null) {
            charset = params.getCharSet(CHARSET, StandardCharsets.UTF_8);
        }
        return new ContentType(mimeType, charset, params != null && !params.isEmpty() ? params : null);
    }

    public static ContentType parseContentType(String resourceUrl) {
        String[] s;

        if (UnameUtil.isWindows())
            s = resourceUrl.split("\\\\");
        else
            s = resourceUrl.split("/");

        String fileName = s[s.length - 1];
        String[] extendName = fileName.split("[.]");
        if (extendName.length == 1) {
            return DEFAULT_TEXT;
        } else {
            ContentType rtn = FILE_CONTENT_TYPE.get(extendName[extendName.length - 1]);
            return rtn == null ? DEFAULT_BINARY : rtn;
        }
    }


    public static ContentType parseLenient(final CharSequence s) throws UnsupportedCharsetException {
        return parse(s);
    }

    public static ContentType parse(final CharSequence s) throws UnsupportedCharsetException {
        if (StringUtil.isBlank(s)) {
            return null;
        }
        String string = s.toString();
        String[] split = StringUtil.split(string, "; ");
        if (split.length < 1) {
            return null;
        }
        DataMap dataMap = new DataMap();
        DataMap Parameters = new DataMap();
        dataMap.put("mimeType", split[0]);
        dataMap.put("Parameters", Parameters);
        for (String value : split) {
            String[] split1 = StringUtil.split(value, "=");
            if (split1.length == 2)
                Parameters.put(split1[0], split1[1]);
        }
        if (!dataMap.isEmpty()) {
            return create(dataMap);
        }
        return null;
    }


    /**
     * Creates a new instance with this MIME type and the given Charset.
     *
     * @param charset charset
     * @return a new instance with this MIME type and the given Charset.
     * @since 4.3
     */
    public ContentType withCharset(final Charset charset) {
        return create(this.getMimeType(), charset);
    }

    /**
     * Creates a new instance with this MIME type and the given Charset name.
     *
     * @param charset name
     * @return a new instance with this MIME type and the given Charset name.
     * @throws UnsupportedCharsetException Thrown when the named charset is not available in
     *                                     this instance of the Java virtual machine
     * @since 4.3
     */
    public ContentType withCharset(final String charset) {
        return create(this.getMimeType(), charset);
    }

    public boolean isSameMimeType(final ContentType contentType) {
        return contentType != null && mimeType.equalsIgnoreCase(contentType.getMimeType());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof ContentType c)) return false;
        return this.toString().equals(c.toString());
    }
}
