package top.yqingyu.httpserver.compomentv2;

import cn.hutool.core.lang.UUID;
import top.yqingyu.common.qydata.ConcurrentDataMap;

import java.time.ZonedDateTime;
import java.util.Map;


/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.httpserver.entity.Session
 * @description
 * @createTime 2022年09月17日 10:44:00
 */
public class Session {

    static final String name = "sessionVersionID";

    static final ConcurrentDataMap<String, Session> SESSION_CONTAINER = new ConcurrentDataMap<>();


    private final String sessionVersionID;
    private final ConcurrentDataMap<Object, Object> sessionData;
    private ZonedDateTime zoneTime;
    private boolean newInstance;

    public Session() {
        sessionVersionID = UUID.randomUUID().toString();
        zoneTime = ZonedDateTime.now();
        sessionData = new ConcurrentDataMap<>();
        newInstance = true;
    }

    String getSessionVersionID() {
        return sessionVersionID;
    }

    ZonedDateTime getZoneTime() {
        return zoneTime;
    }

    void setZoneTime(ZonedDateTime zoneTime) {
        this.zoneTime = zoneTime;
    }

    public Object get(Object k) {
        return this.sessionData.get(k);
    }

    public void set(Object k, Object v) {
        this.sessionData.put(k, v);
    }

    public boolean containsKey(Object k) {
        return this.sessionData.containsKey(k);
    }

    public boolean containsValue(Object k) {
        return this.sessionData.containsValue(k);
    }

    public void putAll(Map<?, ?> map) {
        this.sessionData.putAll(map);
    }

    public void clear() {
        this.sessionData.clear();
    }

    boolean isNewInstance() {
        return newInstance;
    }

    void setNewInstance(boolean newInstance) {
        this.newInstance = newInstance;
    }
}
