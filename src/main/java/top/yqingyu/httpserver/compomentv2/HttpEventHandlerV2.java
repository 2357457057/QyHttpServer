package top.yqingyu.httpserver.compomentv2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.common.server$aio.Session;

import java.io.IOException;
import java.time.LocalDateTime;

import static top.yqingyu.common.server$aio.SessionBridge.NET_CHANNELS;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.httpserver.compomentv2.HttpEventHandlerV2
 * @description
 * @createTime 2023年02月09日 23:24:00
 */
public class HttpEventHandlerV2 extends Session {
    private final static Logger logger = LoggerFactory.getLogger(HttpEventHandlerV2.class);


    public HttpEventHandlerV2() {
        super();
    }

    @Override
    public void ready() {
        int i = this.getChannel().hashCode();
        try {
            NET_CHANNELS.get(i).put("LocalDateTime", LocalDateTime.now());
            NET_CHANNELS.get(i).put("isResponseEnd", Boolean.FALSE);
            DoRequest doRequest = new DoRequest(this);
            HttpEventEntity httpEventEntity = doRequest.call();
            DoResponse doResponse = new DoResponse(httpEventEntity, this);
            doResponse.call();
            NET_CHANNELS.get(i).put("isResponseEnd", Boolean.TRUE);
            this.close();
        } catch (Exception e) {
            NET_CHANNELS.get(i).put("isResponseEnd", Boolean.TRUE);
            logger.error("", e);
        }
    }

    @Override
    public void doLogicError(Throwable exec) {
        try {
            this.getChannel().close();
        } catch (IOException e) {
            logger.error("", e);
        }
    }
}

