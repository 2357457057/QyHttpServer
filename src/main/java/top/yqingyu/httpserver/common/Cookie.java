package top.yqingyu.httpserver.common;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.httpserver.entity.Cookie
 * @description
 * @createTime 2022年09月17日 14:47:00
 */
public class Cookie {

    private final String name;
    private String value;
    private int maxAge = -1;
    private int version = 0;
    private String comment;
    private String domain;
    private String path;
    private boolean secure = false;
    private boolean httpOnly = false;


    public Cookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String toSetString() {
        StringBuilder sb = new StringBuilder();
        sb.append("set-cookie: ");

        sb.append(name).append("=").append(value == null ? "" : value);

        if (version != 0)
            sb.append("; ").append("Version").append("=").append(version);

        if (comment != null)
            sb.append("; ").append("Comment").append("=").append(comment);

        if (path != null)
            sb.append("; ").append("Path").append("=").append(path);

        if (domain != null)
            sb.append("; ").append("Domain").append("=").append(domain);

        sb.append("; ").append("Max-Age").append("=").append(maxAge);

        if (secure)
            sb.append("; ").append("Secure");

        if (httpOnly)
            sb.append(";").append("HttpOnly");

        return sb.append("\r\n").toString();
    }

    public String toCookieValStr() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("=").append(value == null ? "" : value);

        if (version != 0)
            sb.append("; ").append("Version").append("=").append(version);

        if (comment != null)
            sb.append("; ").append("Comment").append("=").append(comment);

        if (path != null)
            sb.append("; ").append("Path").append("=").append(path);

        if (domain != null)
            sb.append("; ").append("Domain").append("=").append(domain);

        sb.append("; ").append("Max-Age").append("=").append(maxAge);

        if (secure)
            sb.append("; ").append("Secure");

        if (httpOnly)
            sb.append(";").append("HttpOnly");

        return sb.toString();
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }
}
