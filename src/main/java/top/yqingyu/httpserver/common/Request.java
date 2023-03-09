package top.yqingyu.httpserver.common;

import com.alibaba.fastjson2.JSON;
import top.yqingyu.common.qydata.DataMap;

import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.httpserver.entity.Request
 * @description
 * @createTime 2022年09月09日 22:02:00
 */
public class Request implements HttpAction {


    private HttpMethod method;
    private HttpVersion httpVersion;
    private String url;

    private String host;

    private InetSocketAddress inetSocketAddress;

    private final DataMap urlParam = new DataMap();
    private final DataMap header = new DataMap();
    private final DataMap cookie = new DataMap();

    private Session session;

    private MultipartFile multipartFile;

    private byte[] body;

    private boolean parseEnd = false;

    @Deprecated
    public void setParseEnd() {
        this.parseEnd = true;
    }

    @Deprecated
    public void setMethod(byte[] method) {
        this.method = HttpMethod.getMethod(new String(method, StandardCharsets.UTF_8));
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public void setMethod(String method) {
        this.method = HttpMethod.getMethod(method);
    }


    void setHttpVersion(HttpVersion httpVersion) {
        this.httpVersion = httpVersion;
    }

    @Deprecated
    public void setHttpVersion(byte[] httpVersion) {
        this.httpVersion = HttpVersion.getVersion(new String(httpVersion, StandardCharsets.UTF_8));
    }


    public void setUrl(String url) {
        this.url = url;
    }

    @Deprecated
    public void setUrl(byte[] url) {
        //中文解码
        this.url = URLDecoder.decode(new String(url, StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }

    void putHeader(String key, Object obj) {
        this.header.put(key, obj);
    }

    @Deprecated
    public void putUrlParam(String key, Object obj) {
        this.urlParam.put(key, obj);
    }

    @Deprecated
    public void setBody(byte[] body) {
        this.body = body;
    }

    @Deprecated
    public void setSession(Session session) {
        this.session = session;
    }

    @Deprecated
    public void putHeader(byte[] key, byte[] obj) {
        String keyStr = new String(key, StandardCharsets.UTF_8);
        String vStr = obj == null ? "" : new String(obj, StandardCharsets.UTF_8);
        if ("Cookie".equals(keyStr)) {
            String[] cookies = vStr.split("; ");
            for (String coo : cookies) {
                String[] split = coo.split("=");
                //session逻辑
                if (Session.name.equals(split[0]) && Session.SESSION_CONTAINER.containsKey(split[1])) {
                    this.session = Session.SESSION_CONTAINER.get(split[0]);
                }
                this.cookie.put(split[0], split[1]);
            }
        } else
            this.header.put(keyStr, vStr);
    }

    public void putHeader(String key, String val) {
        if ("Cookie".equals(key)) {
            String[] cookies = val.split("; ");
            for (String coo : cookies) {
                String[] split = coo.split("=");
                //session逻辑
                if (Session.name.equals(split[0]) && Session.SESSION_CONTAINER.containsKey(split[1])) {
                    this.session = Session.SESSION_CONTAINER.get(split[0]);
                }
                this.cookie.put(split[0], split[1]);
            }
        } else
            this.header.put(key, val);
    }

    //防止body过大导致toString异常
    byte[] getBody() {
        return null;
    }

    @Deprecated
    public byte[] gainBody() {
        return body;
    }

    DataMap getCookie() {
        return this.cookie;
    }

    @Deprecated
    public boolean canCompress() {
        String string = header.getString("Accept-Encoding", "");
        return string.toUpperCase().contains("GZIP");
    }

    @Deprecated
    public MultipartFile getMultipartFile() {
        return multipartFile;
    }

    @Deprecated
    public void setMultipartFile(MultipartFile multipartFile) {
        this.multipartFile = multipartFile;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public HttpVersion getHttpVersion() {
        return httpVersion;
    }

    public String getUrl() {
        return url;
    }

    public DataMap getHeader() {
        return header;
    }

    public String getHeader(String head) {
        return header.getString(head);
    }

    public String getCookie(String cook) {
        return this.cookie.getString(cook);
    }

    public Session getSession() {
        return session;
    }

    public DataMap getUrlParam() {
        return urlParam;
    }

    public String getUrlParam(String key) {
        return urlParam.getString(key);
    }

    public boolean isParseEnd() {
        return parseEnd;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public InetSocketAddress getInetSocketAddress() {
        return inetSocketAddress;
    }

    public void setInetSocketAddress(InetSocketAddress inetSocketAddress) {
        this.inetSocketAddress = inetSocketAddress;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
