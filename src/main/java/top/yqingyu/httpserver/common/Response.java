package top.yqingyu.httpserver.common;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.apache.commons.lang3.StringUtils;
import top.yqingyu.common.qydata.ConcurrentDataSet;
import top.yqingyu.common.qydata.DataMap;
import top.yqingyu.common.utils.ArrayUtil;
import top.yqingyu.httpserver.Version;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;

import static top.yqingyu.common.utils.LocalDateTimeUtil.HTTP_FORMATTER;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.httpserver.entity.Response
 * @description
 * @createTime 2022年09月13日 22:10:00
 */
public class Response implements HttpAction, Serializable {
    private HttpVersion httpVersion;
    private String statue_code;
    private final DataMap header = new DataMap();

    private final ConcurrentDataSet<Cookie> cookie = new ConcurrentDataSet<>();

    private String string_body;
    @JSONField(serialize = false)
    private WebFile file_body;
    @JSONField(serialize = false)
    private ByteBuffer compress_body;
    @JSONField(serialize = false)
    private byte[] compressByteBody;

    public Response() {
        this.putHeaderServer();
    }

    public byte[] getCompressByteBody() {
        return compressByteBody;
    }

    public void setCompressByteBody(byte[] compressByteBody) {
        this.compressByteBody = compressByteBody;
    }

    //是否组装完毕
    private boolean assemble = false;

    //是否已压缩
    private boolean compress = false;

    public WebFile gainFileBody() {
        return file_body;
    }

    public boolean isAssemble() {
        return assemble;
    }

    public Response setAssemble(boolean assemble) {
        this.assemble = assemble;
        return this;
    }

    public void setFile_body(WebFile file_body) {
        this.file_body = file_body;
    }

    public Response putHeader(String key, String value) {
        header.put(key, value);
        return this;
    }

    public Response putHeaderToken(String value) {
        header.put("Token", value);
        return this;
    }

    public Response putHeaderServer() {
        header.put("Server", Version.SERVER_VERSION);
        return this;
    }

    public Response putHeaderContentType(ContentType value) {
        header.put("Content-Type", value.toString());
        return this;
    }

    public ContentType gainHeaderContentType() {
        return ContentType.parse(header.getString("Content-Type"));
    }

    public Response putHeaderContentLength(long value) {
        header.put("Content-Length", String.valueOf(value));
        return this;
    }

    public String gainHeaderContentLength() {
        return header.getString("Content-Length");
    }

    public Response putHeaderAcceptRanges() {
        header.put("Accept-Ranges", "bytes");
        return this;
    }

    public Response putHeaderContentRanges(long start, long end) {
        header.put("Content-Ranges", "bytes=" + start + "-" + end);
        return this;
    }

    public Response putHeaderCROS() {
        header.put("Access-Control-Allow-Origin", "*");
        return this;
    }

    public String getHeaderCROS() {
        return this.header.getString("Access-Control-Allow-Origin");
    }

    public Response putHeaderRedirect(String url) {
        header.put("Location", url);
        return this;
    }

    public String getHeaderRedirect() {
        return this.header.getString("Location");
    }

    public void putHeaderCompress() {
        compress = true;
        header.put("Content-Encoding", "gzip");
    }

    public boolean isCompress() {
        return compress;
    }


    public Response putHeaderDate(ZonedDateTime ldt) {
        String s = HTTP_FORMATTER.format(ldt);
        header.put("Date", formatHttpTime(s));
        return this;
    }

    public Response setHttpVersion(HttpVersion httpVersion) {
        this.httpVersion = httpVersion;
        return this;
    }

    public Response setStatue_code(String statue_code) {
        this.statue_code = statue_code;
        return this;
    }


    public HttpVersion getHttpVersion() {
        return httpVersion;
    }

    public String getStatue_code() {
        return statue_code;
    }

    public DataMap getHeader() {
        return header;
    }

    @Deprecated
    public String getStrBody() {
        return string_body;
    }


    public Response setString_body(String string_body) {
        this.string_body = string_body;
        return this;
    }

    @Deprecated
    public void setCompress_body(ByteBuffer compress_body) {
        this.compress_body = compress_body;
    }

    @Deprecated
    public ByteBuffer gainBodyBytes() throws FileNotFoundException {
        if (compress)
            return compress_body;
        byte[] bytes = string_body == null ? ArrayUtil.EMPTY_BYTE_ARRAY : string_body.getBytes(StandardCharsets.UTF_8);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bytes.length);
        byteBuffer.put(bytes);
        return byteBuffer;
    }

    @Deprecated
    public byte[] gainBodyBytes2() throws FileNotFoundException {
        if (compress)
            return compressByteBody;
        return string_body == null ? ArrayUtil.EMPTY_BYTE_ARRAY : string_body.getBytes(StandardCharsets.UTF_8);
    }

    public WebFile getFile_body() {
        return file_body;
    }

    public void addCookie(Cookie c) {
        this.cookie.add(c);
    }

    public ConcurrentDataSet<Cookie> getCookies() {
        return this.cookie;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(httpVersion.getV()).append(" ").append(statue_code).append("\r\n");

        if (StringUtils.isBlank(gainHeaderContentLength())) {
            if (StringUtils.isNotBlank(string_body))
                putHeaderContentLength(string_body.getBytes(StandardCharsets.UTF_8).length);
            else if (file_body != null) {
                if (!file_body.exists()) {
                    return HttpStatue.$404.Response().toString();
                }
                putHeaderContentLength(file_body.length());
            }
        }

        header.forEach((k, v) -> sb.append(k).append(":").append(" ").append(v).append("\r\n"));

        cookie.forEach((e) -> sb.append(e.toSetString()));

        sb.append("\r\n");
        return sb.toString();
    }

    public static String formatHttpTime(String str) {

        return str
                .replace("周一", "Mon")
                .replace("周二", "Tue")
                .replace("周三", "Wed")
                .replace("周四", "Thu")
                .replace("周五", "Fri")
                .replace("周六", "Sat")
                .replace("周日", "Sun")
                .replace("1月", "Jan")
                .replace("2月", "Feb")
                .replace("3月", "Mar")
                .replace("4月", "Apr")
                .replace("5月", "May")
                .replace("6月", "Jun")
                .replace("7月", "Jul")
                .replace("8月", "Aug")
                .replace("9月", "Sep")
                .replace("10月", "Oct")
                .replace("11月", "Nov")
                .replace("12月", "Dec");

    }

    public String toJsonString() {
        return JSON.toJSONString(this);
    }
}
