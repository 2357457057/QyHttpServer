package top.yqingyu.httpserver.compomentv2;

import com.alibaba.fastjson2.JSON;
import org.apache.commons.lang3.StringUtils;
import top.yqingyu.common.qydata.ConcurrentDataSet;
import top.yqingyu.common.qydata.DataMap;
import top.yqingyu.common.utils.ArrayUtil;
import top.yqingyu.httpserver.Version;
import top.yqingyu.httpserver.common.ContentType;
import top.yqingyu.httpserver.common.Cookie;
import top.yqingyu.httpserver.common.HttpAction;
import top.yqingyu.httpserver.common.HttpVersion;

import java.io.File;
import java.io.FileNotFoundException;
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
public class Response  implements HttpAction {

    public static final Response $404_NOT_FOUND = new Response().setStatue_code("404").setHttpVersion(HttpVersion.V_1_1).setString_body("木有资源啦 ^ Ω ^").putHeaderContentType(ContentType.TEXT_PLAIN).setAssemble(true);
    public static final Response $413_ENTITY_LARGE = new Response().setStatue_code("413").setHttpVersion(HttpVersion.V_1_1).setString_body("413 Request Entity Too Large").putHeaderContentType(ContentType.TEXT_PLAIN).setAssemble(true);
    public static final Response $100_CONTINUE = new Response().setStatue_code("100").setHttpVersion(HttpVersion.V_1_1).setAssemble(true);
    public static final Response $400_BAD_REQUEST = new Response().setStatue_code("400").setHttpVersion(HttpVersion.V_1_1).setString_body("400 Bad Request").putHeaderContentType(ContentType.TEXT_PLAIN).setAssemble(true);
    public static final Response $401_BAD_REQUEST = new Response().setStatue_code("400").setHttpVersion(HttpVersion.V_1_1).setString_body("传你妈呢？").putHeaderContentType(ContentType.TEXT_PLAIN).setAssemble(true);


    private HttpVersion httpVersion;
    private String statue_code;
    private final DataMap header = new DataMap();

    private final ConcurrentDataSet<Cookie> cookie = new ConcurrentDataSet<>();

    private String string_body;
    private File file_body;

    private ByteBuffer compress_body;

    //是否组装完毕
    private boolean assemble = false;

    //是否已压缩
    private boolean compress = false;

    public File gainFileBody() {
        return file_body;
    }

    boolean isAssemble() {
        return assemble;
    }

    Response setAssemble(boolean assemble) {
        this.assemble = assemble;
        return this;
    }

    public void setFile_body(File file_body) {
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

    String getStrBody() {
        return string_body;
    }


    public Response setString_body(String string_body) {
        this.string_body = string_body;
        return this;
    }

    void setCompress_body(ByteBuffer compress_body) {
        this.compress_body = compress_body;
    }

    ByteBuffer gainBodyBytes() throws FileNotFoundException {
        if (compress)
            return compress_body;
        byte[] bytes = string_body == null ? ArrayUtil.EMPTY_BYTE_ARRAY : string_body.getBytes(StandardCharsets.UTF_8);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bytes.length);
        byteBuffer.put(bytes);
        return byteBuffer;
    }

    File getFile_body() {
        return file_body;
    }

    public void addCookie(Cookie c) {
        this.cookie.add(c);
    }


    @Override
    public String toString() {
        this.putHeaderServer();
        StringBuilder sb = new StringBuilder();
        sb.append(httpVersion.getV()).append(" ").append(statue_code).append("\r\n");

        if (StringUtils.isBlank(gainHeaderContentLength())) {
            if (StringUtils.isNotBlank(string_body))
                putHeaderContentLength(string_body.getBytes(StandardCharsets.UTF_8).length);
            else if (file_body != null)
                putHeaderContentLength(file_body.length());
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
