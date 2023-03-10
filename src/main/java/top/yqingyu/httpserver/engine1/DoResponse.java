package top.yqingyu.httpserver.engine1;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.common.bean.NetChannel;
import top.yqingyu.common.qydata.ConcurrentDataMap;
import top.yqingyu.common.utils.GzipUtil;
import top.yqingyu.httpserver.common.*;
import top.yqingyu.common.server$aio.Session;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import static top.yqingyu.httpserver.common.ServerConfig.*;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.httpserver.component.DoRequest
 * @description
 * @createTime 2022年09月19日 15:19:00
 */
class DoResponse implements Callable<Object> {

    private final Session session;

    private final AtomicLong CurrentFileCacheSize = new AtomicLong();

    private static final ConcurrentDataMap<String, byte[]> FILE_BYTE_CACHE = new ConcurrentDataMap<>();

    private static final Logger log = LoggerFactory.getLogger(DoResponse.class);
    HttpEventEntity httpEventEntity;

    private static final ConcurrentDataMap<String, top.yqingyu.httpserver.common.Session> SESSION_CONTAINER;

    static {
        ConcurrentDataMap<String, top.yqingyu.httpserver.common.Session> temp;
        try {
            Field sessionContainer = top.yqingyu.httpserver.common.Session.class.getDeclaredField("SESSION_CONTAINER");
            sessionContainer.setAccessible(true);

            @SuppressWarnings("unchecked")
            ConcurrentDataMap<String, top.yqingyu.httpserver.common.Session> temp2 = (ConcurrentDataMap<String, top.yqingyu.httpserver.common.Session>) sessionContainer.get(null);

            temp = temp2;
        } catch (Exception ignore) {
            temp = new ConcurrentDataMap<>();
        }
        SESSION_CONTAINER = temp;
    }

    public DoResponse(HttpEventEntity httpEventEntity, Session session) { //, OperatingRecorder<Integer> SOCKET_CHANNEL_ACK) {
        this.session = session;
        this.httpEventEntity = httpEventEntity;
    }

    /**
     * @return
     * @throws Exception
     */
    @Override
    @SuppressWarnings("all")
    public Object call() throws Exception {

        NetChannel netChannel = null;
        LocalDateTime now = LocalDateTime.now();
        try {
            Request request = httpEventEntity.getRequest();
            Response response = httpEventEntity.getResponse();

            if (request == null && response == null) {
                return null;
            }

            if (response == null)
                response = new Response();
            if (request == null) {
                request = new Request();
            }


            AtomicReference<Response> resp = new AtomicReference<>();
            resp.set(response);
            response.setHttpVersion(HttpVersion.V_1_1);
            response.putHeaderDate(ZonedDateTime.now());


            //响应初始化，寻找本地资源 已组装完成的消息会跳过
            initResponse(request, resp);

            //压缩
            if (FILE_COMPRESS_ON && !UN_DO_COMPRESS_FILE.contains(resp.get().gainHeaderContentType()))
                compress(request, resp);

            doResponse(resp, netChannel);
        } catch (NullPointerException e) {
//            netChannel.close();
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }


    /**
     * 寻找相应的资源。
     *
     * @author YYJ
     * @description
     */
    void initResponse(Request request, AtomicReference<Response> resp) throws InvocationTargetException, IllegalAccessException {

        Response response = resp.get();
        //优先文件资源
        if (!response.isAssemble()) {
            LocationMapping.fileResourceMapping(request, response);
        }

        //接口
        if (!response.isAssemble()) {
            //session相关逻辑
            top.yqingyu.httpserver.common.Session session;
            String sessionID = request.getCookie(top.yqingyu.httpserver.common.Session.name);
            if (SESSION_CONTAINER.containsKey(sessionID))
                session = SESSION_CONTAINER.get(sessionID);
            else {
                session = new top.yqingyu.httpserver.common.Session();
                SESSION_CONTAINER.put(session.getSessionVersionID(), session);
            }
            request.setSession(session);

            //接口资源
            LocationMapping.beanResourceMapping(request, response,false);


            if (response.isAssemble() && request.getSession().isNewInstance()) {
                session.setNewInstance(false);
                Cookie cookie = new Cookie(top.yqingyu.httpserver.common.Session.name, session.getSessionVersionID());
                cookie.setMaxAge((int) SESSION_TIME_OUT);
                response.addCookie(cookie);
            }

        }

        //NotFound
        if (!response.isAssemble()) {
            resp.setRelease(Response.$404_NOT_FOUND.putHeaderDate(ZonedDateTime.now()));
        }

    }


    /**
     * 对response 压缩、缓存。
     *
     * @param resp    响应
     * @param request 请求
     * @author YYJ
     * @description
     */
    private void compress(Request request, AtomicReference<Response> resp) throws IOException {
        Response response = resp.get();
        String url = request.getUrl();
        ContentType requestCtTyp = ContentType.parse(request.getHeader("Content-Type"));

        Charset charset;
        if (requestCtTyp != null)
            charset = requestCtTyp.getCharset() == null ? StandardCharsets.UTF_8 : requestCtTyp.getCharset();
        else charset = StandardCharsets.UTF_8;
        if (!"304|100".contains(response.getStatue_code()) || (response.getStrBody() != null ^ response.gainFileBody() == null)) {
            if (request.canCompress()) {
                String strBody = response.getStrBody();
                if (StringUtils.isNotBlank(strBody)) {
                    byte[] bytes = GzipUtil.$2CompressBytes(strBody, charset);
                    ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
                    buffer.put(bytes);
                    response.setCompress_body(buffer);
                    response.putHeaderContentLength(bytes.length).putHeaderCompress();
                } else {
                    if (FILE_BYTE_CACHE.containsKey(url)) {
                        byte[] bytes = FILE_BYTE_CACHE.get(url);
                        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bytes.length);
                        byteBuffer.put(bytes);
                        response.setCompress_body(byteBuffer);
                        response.putHeaderContentLength(byteBuffer.limit()).putHeaderCompress();
                    } else {
                        File file = response.getFile_body();
                        if (file != null) {
                            long length = file.length();
                            if (length < MAX_SINGLE_FILE_COMPRESS_SIZE && CurrentFileCacheSize.get() < MAX_FILE_CACHE_SIZE) {
                                byte[] bytes = GzipUtil.$2CompressBytes(response.getFile_body());
                                ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
                                buffer.put(bytes);
                                //开启压缩池
                                if (CACHE_POOL_ON) {
                                    FILE_BYTE_CACHE.put(url, bytes);
                                    CurrentFileCacheSize.addAndGet(bytes.length);
                                }

                                response.setCompress_body(buffer);
                                response.putHeaderContentLength(bytes.length).putHeaderCompress();
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("all")
    private void doResponse(AtomicReference<Response> resp, NetChannel netChannel) throws Exception {


        Response response = resp.get();


        ContentType type = response.gainHeaderContentType();
        byte[] bytes;
        if (type != null && type.getCharset() != null)
            bytes = response.toString().getBytes(type.getCharset());
        else
            bytes = response.toString().getBytes();

        //Header
        try {
            session.writeBytes(bytes, 2000);
        } catch (Exception e) {
            log.error("", e);
            netChannel.close();
        }
        //body
        if (!"304|100".contains(response.getStatue_code()) || (response.getStrBody() != null ^ response.gainFileBody() == null)) {
            byte[] buf = new byte[(int) DEFAULT_SEND_BUF_LENGTH];
            int length;
            File file_body = response.getFile_body();
            if (file_body != null && !response.isCompress()) {
                FileChannel fileChannel = new FileInputStream(response.getFile_body()).getChannel();
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect((int) DEFAULT_SEND_BUF_LENGTH);
                session.writeFile(fileChannel, byteBuffer);
                fileChannel.close();
            } else {
                try {
                    session.writeBytes(response.gainBodyBytes(), 2000);
                } catch (Exception e) {
                    log.error("", e);
                    netChannel.close();
                }
            }
        }

        log.info("Response: {}", response.toJsonString());
    }

}
