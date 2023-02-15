package top.yqingyu.httpserver.compomentv2;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.common.bean.NetChannel;
import top.yqingyu.common.qydata.ConcurrentDataMap;
import top.yqingyu.common.utils.GzipUtil;
import top.yqingyu.common.utils.IoUtil;
import top.yqingyu.httpserver.common.ContentType;
import top.yqingyu.httpserver.common.Cookie;
import top.yqingyu.httpserver.common.HttpVersion;
import top.yqingyu.common.server$aio.Session;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.httpserver.component.DoResponse
 * @description
 * @createTime 2022年09月19日 15:19:00
 */
class DoResponse implements Callable<Object> {

    private final Session session;


    static long DEFAULT_SEND_BUF_LENGTH;

    static boolean FILE_COMPRESS_ON;

    static ArrayList<ContentType> UN_DO_COMPRESS_FILE = new ArrayList<>(Arrays.asList(
            ContentType.APPLICATION_OCTET_STREAM,
            ContentType.IMAGE_JPEG,
            ContentType.IMAGE_PNG,
            ContentType.IMAGE_GIF,
            ContentType.IMAGE_WEBP,
            ContentType.IMAGE_BMP,
            ContentType.IMAGE_SVG,
            ContentType.IMAGE_X_ICON,
            ContentType.IMAGE_TIFF,
            ContentType.VIDEO_AVI,
            ContentType.VIDEO_MP4,
            ContentType.VIDEO_MPEG4,
            ContentType.VIDEO_WMV,
            ContentType.VIDEO_WEBM,
            ContentType.AUDIO_MP3
    ));

    //最大压缩源文件大小 128MB
    static long MAX_SINGLE_FILE_COMPRESS_SIZE;

    //是否开启缓存池
    static boolean CACHE_POOL_ON;

    //最大缓存池大小 1.5GB
    static long MAX_FILE_CACHE_SIZE;

    static long SESSION_TIME_OUT;

    private final AtomicLong CurrentFileCacheSize = new AtomicLong();

    private static final ConcurrentDataMap<String, byte[]> FILE_BYTE_CACHE = new ConcurrentDataMap<>();

    private static final Logger log = LoggerFactory.getLogger(DoResponse.class);
    HttpEventEntity httpEventEntity;


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
            netChannel.close();
        }
        return null;
    }


    /**
     * 寻找相应的资源。
     *
     * @author YYJ
     * @description
     */
    void initResponse(Request request, AtomicReference<Response> resp) {

        Response response = resp.get();
        //优先文件资源
        if (!response.isAssemble()) {
            LocationMapping.fileResourceMapping(request, response);
        }

        //接口
        if (!response.isAssemble()) {
            //session相关逻辑
            top.yqingyu.httpserver.compomentv2.Session session;
            String sessionID = request.getCookie(top.yqingyu.httpserver.compomentv2.Session.name);
            if (top.yqingyu.httpserver.compomentv2.Session.SESSION_CONTAINER.containsKey(sessionID))
                session = top.yqingyu.httpserver.compomentv2.Session.SESSION_CONTAINER.get(sessionID);
            else {
                session = new top.yqingyu.httpserver.compomentv2.Session();
                top.yqingyu.httpserver.compomentv2.Session.SESSION_CONTAINER.put(session.getSessionVersionID(), session);
            }
            request.setSession(session);

            //接口资源
            LocationMapping.beanResourceMapping(request, response);


            if (response.isAssemble() && request.getSession().isNewInstance()) {
                session.setNewInstance(false);
                Cookie cookie = new Cookie(top.yqingyu.httpserver.compomentv2.Session.name, session.getSessionVersionID());
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
                long l = 0;
                long size = fileChannel.size();
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect((int) DEFAULT_SEND_BUF_LENGTH);
                do {
                    byteBuffer.clear();
                    byteBuffer.flip();
                    fileChannel.read(byteBuffer, l);
                    l += session.write(byteBuffer, 1000, TimeUnit.MILLISECONDS);
                } while (l != size);
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
