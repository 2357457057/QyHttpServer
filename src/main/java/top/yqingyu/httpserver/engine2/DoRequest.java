package top.yqingyu.httpserver.engine2;

import com.alibaba.fastjson2.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.HttpMethod;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.common.qydata.ConcurrentDataMap;
import top.yqingyu.httpserver.common.*;

import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static top.yqingyu.httpserver.common.ServerConfig.SESSION_TIME_OUT;

public class DoRequest extends SimpleChannelInboundHandler<FullHttpRequest> {
    static Logger logger = LoggerFactory.getLogger(DoResponse.class);

    private static final ConcurrentDataMap<String, Session> SESSION_CONTAINER;

    static {
        ConcurrentDataMap<String, Session> temp;
        try {
            Field sessionContainer = Session.class.getDeclaredField("SESSION_CONTAINER");
            sessionContainer.setAccessible(true);

            @SuppressWarnings("unchecked")
            ConcurrentDataMap<String, Session> temp2 = (ConcurrentDataMap<String, Session>) sessionContainer.get(null);

            temp = temp2;
        } catch (Exception ignore) {
            temp = new ConcurrentDataMap<>();
        }
        SESSION_CONTAINER = temp;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        HttpMethod method = msg.method();
        String name = method.name();
        String uri = URLDecoder.decode(msg.uri(), StandardCharsets.UTF_8);
        HttpHeaders headers = msg.headers();
        ByteBuf content = msg.content();
        Request qyReq = new Request();

        while (content.readableBytes() > 0) {
            int readabled = content.readableBytes();
            byte[] bytes = new byte[readabled];
            content.readBytes(bytes);
            qyReq.setBody(bytes);
        }

        if (Dict.MULTIPART_FILE_LIST.equals(headers.get(Dict.MULTIPART_FILE_LIST))) {
            List<MultipartFile> multipartFileList = DoRequestPre.getMultipartFileList((DefaultFullHttpRequest) msg);
            qyReq.setMultipartFile(multipartFileList.get(0));
        }

        qyReq.setUrl(uri);
        qyReq.setMethod(name);
        int i = StringUtils.indexOf(uri, '?');
        if (i != -1) {
            String substring = uri.substring(i + 1);
            String[] split = substring.split("&");
            for (int j = 0; j < split.length; j++) {

                String[] urlParamKV = split[j].split("=");
                if (urlParamKV.length == 2) qyReq.putUrlParam(urlParamKV[0], urlParamKV[1]);
                else qyReq.putUrlParam("NoKey_" + j, split[j]);
            }
        }
        List<Map.Entry<String, String>> entries = headers.entries();
        for (Map.Entry<String, String> entry : entries) {
            qyReq.putHeader(entry.getKey(), entry.getValue());
        }

        Response qyResp = new Response();
        qyResp.setHttpVersion(top.yqingyu.httpserver.common.HttpVersion.V_1_1);
        LocationMapping.fileResourceMapping(qyReq, qyResp);

        if (!qyResp.isAssemble()) {
            //session相关逻辑
            Session session;
            String sessionID = qyReq.getCookie(Session.name);
            if (SESSION_CONTAINER.containsKey(sessionID))
                session = SESSION_CONTAINER.get(sessionID);
            else {
                session = new Session();
                SESSION_CONTAINER.put(session.getSessionVersionID(), session);
            }
            qyReq.setSession(session);

            //接口资源
            LocationMapping.beanResourceMapping(qyReq, qyResp, true);

            if (qyResp.isAssemble() && qyReq.getSession().isNewInstance()) {
                session.setNewInstance(false);
                top.yqingyu.httpserver.common.Cookie cookie = new top.yqingyu.httpserver.common.Cookie(top.yqingyu.httpserver.common.Session.name, session.getSessionVersionID());
                cookie.setMaxAge((int) SESSION_TIME_OUT);
                qyResp.addCookie(cookie);
            }

        }

        //NotFound
        if (!qyResp.isAssemble()) {
            qyResp = Response.$404_NOT_FOUND.putHeaderDate(ZonedDateTime.now());
        }
        Channel channel = ctx.channel();
        channel.write(new HttpEventEntity(qyReq, qyResp));
        channel.flush();
        logger.debug("reqs {}", JSON.toJSONString(qyReq));
    }
}
