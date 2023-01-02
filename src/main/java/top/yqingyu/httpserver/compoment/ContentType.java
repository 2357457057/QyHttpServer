package top.yqingyu.httpserver.compoment;

import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicHeaderValueFormatter;
import org.apache.hc.core5.http.message.BasicHeaderValueParser;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.message.ParserCursor;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.CharArrayBuffer;
import org.apache.hc.core5.util.TextUtils;
import top.yqingyu.common.utils.YamlUtil;

import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.*;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.httpserver.entity.ContentType
 * @description
 * @createTime 2022年09月11日 00:03:00
 */
public class ContentType implements Serializable {


    @Serial
    private static final long serialVersionUID = -7768694718232371896L;

    private String adviceStatusCode;

    public String getAdviceStatusCode() {
        return adviceStatusCode;
    }

    public ContentType setAdviceStatusCode(String adviceStatusCode) {
        this.adviceStatusCode = adviceStatusCode;
        return this;
    }

    /**
     * Param that represent {@code charset} constant.
     */
    private static final String CHARSET = "charset";

    // constants
    public static final ContentType APPLICATION_ATOM_XML = create(
            "application/atom+xml", StandardCharsets.UTF_8);
    public static final ContentType APPLICATION_FORM_URLENCODED = create(
            "application/x-www-form-urlencoded", StandardCharsets.ISO_8859_1);
    public static final ContentType APPLICATION_JSON = create(
            "application/json", StandardCharsets.UTF_8);


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

    /**
     * Public constant media type for {@code multipart/mixed}.
     *
     * @since 5.1
     */
    public static final ContentType MULTIPART_MIXED = create(
            "multipart/mixed", StandardCharsets.ISO_8859_1);

    /**
     * Public constant media type for {@code multipart/related}.
     *
     * @since 5.1
     */
    public static final ContentType MULTIPART_RELATED = create(
            "multipart/related", StandardCharsets.ISO_8859_1);


    public static final ContentType TEXT_PLAIN = create(
            "text/plain", StandardCharsets.UTF_8);
    /**
     * Public constant media type for {@code text/event-stream}.
     *
     * @see <a href="https://www.w3.org/TR/eventsource/">Server-Sent Events W3C recommendation</a>
     * @since 5.1
     */
    public static final ContentType TEXT_EVENT_STREAM = create(
            "text/event-stream", StandardCharsets.UTF_8);

    public static final ContentType WILDCARD = create(
            "*/*", (Charset) null);

    /**
     * An empty immutable {@code NameValuePair} array.
     */
    private static final NameValuePair[] EMPTY_NAME_VALUE_PAIR_ARRAY = new NameValuePair[0];


    // defaults
    public static final ContentType DEFAULT_TEXT = TEXT_PLAIN;
    public static final ContentType DEFAULT_BINARY = APPLICATION_OCTET_STREAM;

    private final String mimeType;
    private final Charset charset;
    private final NameValuePair[] params;

    ContentType(
            final String mimeType,
            final Charset charset) {
        this.mimeType = mimeType;
        this.charset = charset;
        this.params = null;
    }

    ContentType(
            final String mimeType,
            final Charset charset,
            final NameValuePair[] params) {
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
        Args.notEmpty(name, "Parameter name");
        if (this.params == null) {
            return null;
        }
        for (final NameValuePair param : this.params) {
            if (param.getName().equalsIgnoreCase(name)) {
                return param.getValue();
            }
        }
        return null;
    }

    /**
     * Generates textual representation of this content type which can be used as the value
     * of a {@code Content-Type} header.
     */
    @Override
    public String toString() {
        final CharArrayBuffer buf = new CharArrayBuffer(64);
        buf.append(this.mimeType);
        if (this.params != null) {
            buf.append("; ");
            BasicHeaderValueFormatter.INSTANCE.formatParameters(buf, this.params, false);
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

    /**
     * Creates a new instance of {@link ContentType}.
     *
     * @param mimeType MIME type. It may not be {@code null} or empty. It may not contain
     *                 characters {@code <">, <;>, <,>} reserved by the HTTP specification.
     * @param charset  charset.
     * @return content type
     */
    public static ContentType create(final String mimeType, final Charset charset) {
        final String normalizedMimeType = Args.notBlank(mimeType, "MIME type").toLowerCase(Locale.ROOT);
        Args.check(valid(normalizedMimeType), "MIME type may not contain reserved characters");
        return new ContentType(normalizedMimeType, charset);
    }

    /**
     * Creates a new instance of {@link ContentType} without a charset.
     *
     * @param mimeType MIME type. It may not be {@code null} or empty. It may not contain
     *                 characters {@code <">, <;>, <,>} reserved by the HTTP specification.
     * @return content type
     */
    public static ContentType create(final String mimeType) {
        return create(mimeType, (Charset) null);
    }

    /**
     * Creates a new instance of {@link ContentType}.
     *
     * @param mimeType MIME type. It may not be {@code null} or empty. It may not contain
     *                 characters {@code <">, <;>, <,>} reserved by the HTTP specification.
     * @param charset  charset. It may not contain characters {@code <">, <;>, <,>} reserved by the HTTP
     *                 specification. This parameter is optional.
     * @return content type
     * @throws UnsupportedCharsetException Thrown when the named charset is not available in
     *                                     this instance of the Java virtual machine
     */
    public static ContentType create(
            final String mimeType, final String charset) throws UnsupportedCharsetException {
        return create(mimeType, !TextUtils.isBlank(charset) ? Charset.forName(charset) : null);
    }

    private static ContentType create(final HeaderElement helem, final boolean strict) {
        final String mimeType = helem.getName();
        if (TextUtils.isBlank(mimeType)) {
            return null;
        }
        return create(helem.getName(), helem.getParameters(), strict);
    }

    private static ContentType create(final String mimeType, final NameValuePair[] params, final boolean strict) {
        Charset charset = null;
        if (params != null) {
            for (final NameValuePair param : params) {
                if (param.getName().equalsIgnoreCase(CHARSET)) {
                    final String s = param.getValue();
                    if (!TextUtils.isBlank(s)) {
                        try {
                            charset = Charset.forName(s);
                        } catch (final UnsupportedCharsetException ex) {
                            if (strict) {
                                throw ex;
                            }
                        }
                    }
                    break;
                }
            }
        }
        return new ContentType(mimeType, charset, params != null && params.length > 0 ? params : null);
    }

    public static ContentType parseContentType(String resourceUrl) {
        String[] s;

        if (YamlUtil.isWindows())
            s = resourceUrl.split("\\\\");
        else
            s = resourceUrl.split("/");

        String fileName = s[s.length - 1];
        String[] extendName = fileName.split("[.]");
        if (extendName.length == 1) {
            return ContentType.TEXT_HTML;
        } else {
            String code = "304";
            return switch (extendName[extendName.length - 1]) {
                case "js" -> APPLICATION_JS.setAdviceStatusCode(code);
                case "css" -> TEXT_CSS.setAdviceStatusCode(code);
                case "png" -> IMAGE_PNG.setAdviceStatusCode(code);
                case "jpg", "jpeg" -> IMAGE_JPEG.setAdviceStatusCode(code);
                case "gif" -> IMAGE_GIF.setAdviceStatusCode(code);
                case "ico" -> IMAGE_X_ICON.setAdviceStatusCode(code);
                case "html","htm" -> TEXT_HTML.setAdviceStatusCode(code);
                case "pdf" -> APPLICATION_PDF;
                case "md" -> TEXT_MARKDOWN;
                case "webp" -> IMAGE_WEBP;
                case "txt", "java", "yml", "yaml", "xml", "properties" -> TEXT_PLAIN;
                case "m4a", "m4v", "mp4" -> VIDEO_MP4;
                case "doc", "docx", "xlsx", "xls", "ppt", "pptx",
                        "exe", "apk", "msi", "rpm",
                        "zip", "rar", "7z", "gz",
                        "mkv", "iso", "srt", "ass", "torrent" -> APPLICATION_OCTET_STREAM;
                case "mp3" -> AUDIO_MP3;
                case "avi" -> VIDEO_AVI;
                case "webm" -> VIDEO_WEBM;
                case "wmv" -> VIDEO_WMV;
                default -> ContentType.TEXT_HTML;
            };
        }
    }

    /**
     * Creates a new instance of {@link ContentType} with the given parameters.
     *
     * @param mimeType MIME type. It may not be {@code null} or empty. It may not contain
     *                 characters {@code <">, <;>, <,>} reserved by the HTTP specification.
     * @param params   parameters.
     * @return content type
     * @since 4.4
     */
    public static ContentType create(
            final String mimeType, final NameValuePair... params) throws UnsupportedCharsetException {
        final String type = Args.notBlank(mimeType, "MIME type").toLowerCase(Locale.ROOT);
        Args.check(valid(type), "MIME type may not contain reserved characters");
        return create(mimeType, params, true);
    }


    public static ContentType parse(final CharSequence s) throws UnsupportedCharsetException {
        return parse(s, true);
    }


    public static ContentType parseLenient(final CharSequence s) throws UnsupportedCharsetException {
        return parse(s, false);
    }

    private static ContentType parse(final CharSequence s, final boolean strict) throws UnsupportedCharsetException {
        if (TextUtils.isBlank(s)) {
            return null;
        }
        final ParserCursor cursor = new ParserCursor(0, s.length());
        final HeaderElement[] elements = BasicHeaderValueParser.INSTANCE.parseElements(s, cursor);
        if (elements.length > 0) {
            return create(elements[0], strict);
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

    /**
     * Creates a new instance with this MIME type and the given parameters.
     *
     * @param params
     * @return a new instance with this MIME type and the given parameters.
     * @since 4.4
     */
    public ContentType withParameters(
            final NameValuePair... params) throws UnsupportedCharsetException {
        if (params.length == 0) {
            return this;
        }
        final Map<String, String> paramMap = new LinkedHashMap<>();
        if (this.params != null) {
            for (final NameValuePair param : this.params) {
                paramMap.put(param.getName(), param.getValue());
            }
        }
        for (final NameValuePair param : params) {
            paramMap.put(param.getName(), param.getValue());
        }
        final List<NameValuePair> newParams = new ArrayList<>(paramMap.size() + 1);
        if (this.charset != null && !paramMap.containsKey(CHARSET)) {
            newParams.add(new BasicNameValuePair(CHARSET, this.charset.name()));
        }
        for (final Map.Entry<String, String> entry : paramMap.entrySet()) {
            newParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        return create(this.getMimeType(), newParams.toArray(EMPTY_NAME_VALUE_PAIR_ARRAY), true);
    }

    public boolean isSameMimeType(final ContentType contentType) {
        return contentType != null && mimeType.equalsIgnoreCase(contentType.getMimeType());
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p>
     * The {@code equals} method implements an equivalence relation
     * on non-null object references:
     * <ul>
     * <li>It is <i>reflexive</i>: for any non-null reference value
     *     {@code x}, {@code x.equals(x)} should return
     *     {@code true}.
     * <li>It is <i>symmetric</i>: for any non-null reference values
     *     {@code x} and {@code y}, {@code x.equals(y)}
     *     should return {@code true} if and only if
     *     {@code y.equals(x)} returns {@code true}.
     * <li>It is <i>transitive</i>: for any non-null reference values
     *     {@code x}, {@code y}, and {@code z}, if
     *     {@code x.equals(y)} returns {@code true} and
     *     {@code y.equals(z)} returns {@code true}, then
     *     {@code x.equals(z)} should return {@code true}.
     * <li>It is <i>consistent</i>: for any non-null reference values
     *     {@code x} and {@code y}, multiple invocations of
     *     {@code x.equals(y)} consistently return {@code true}
     *     or consistently return {@code false}, provided no
     *     information used in {@code equals} comparisons on the
     *     objects is modified.
     * <li>For any non-null reference value {@code x},
     *     {@code x.equals(null)} should return {@code false}.
     * </ul>
     *
     * <p>
     * An equivalence relation partitions the elements it operates on
     * into <i>equivalence classes</i>; all the members of an
     * equivalence class are equal to each other. Members of an
     * equivalence class are substitutable for each other, at least
     * for some purposes.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj
     * argument; {@code false} otherwise.
     * @implSpec The {@code equals} method for class {@code Object} implements
     * the most discriminating possible equivalence relation on objects;
     * that is, for any non-null reference values {@code x} and
     * {@code y}, this method returns {@code true} if and only
     * if {@code x} and {@code y} refer to the same object
     * ({@code x == y} has the value {@code true}).
     * <p>
     * In other words, under the reference equality equivalence
     * relation, each equivalence class only has a single element.
     * method whenever this method is overridden, so as to maintain the
     * general contract for the {@code hashCode} method, which states
     * that equal objects must have equal hash codes.
     * @see #hashCode()
     * @see HashMap
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof ContentType c)) return false;
        return this.toString().equals(c.toString());
    }
}
