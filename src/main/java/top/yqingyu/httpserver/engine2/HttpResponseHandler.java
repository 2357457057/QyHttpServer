package top.yqingyu.httpserver.engine2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.common.qydata.ConcurrentDataMap;
import top.yqingyu.common.qydata.ConcurrentDataSet;
import top.yqingyu.common.qydata.DataMap;
import top.yqingyu.common.utils.GzipUtil;
import top.yqingyu.httpserver.common.ContentType;
import top.yqingyu.httpserver.common.Request;
import top.yqingyu.httpserver.common.Response;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;

import static io.netty.handler.codec.http.HttpUtil.setContentLength;
import static top.yqingyu.httpserver.common.ServerConfig.*;


public class HttpResponseHandler extends MessageToByteEncoder<HttpEventEntity> {
    private static final ConcurrentDataMap<String, byte[]> FILE_BYTE_CACHE = new ConcurrentDataMap<>();
    private final AtomicLong CurrentFileCacheSize = new AtomicLong();

    static Logger logger = LoggerFactory.getLogger(HttpResponseHandler.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, HttpEventEntity msg, ByteBuf out) throws Exception {
        Response response = msg.getResponse();
        Request msgRequest = msg.getRequest();
//        if (FILE_COMPRESS_ON && !UN_DO_COMPRESS_FILE.contains(response.gainHeaderContentType()))
            compress(msg);
        if (!"304|100".contains(response.getStatue_code()) || (response.getStrBody() != null ^ response.gainFileBody() == null)) {
            File file_body = response.getFile_body();
            if (file_body != null && !response.isCompress()) {
                RandomAccessFile randomAccessFile;
                randomAccessFile = new RandomAccessFile(file_body, "r");  //只读模式
                long fileLength = randomAccessFile.length();    //文件大小
                HttpResponse nettyResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                addHeader(nettyResponse, response);
                setContentLength(nettyResponse, fileLength);
                nettyResponse.headers().set("Content-Type", ContentType.parseContentType(msgRequest.getUrl()));
                ctx.pipeline().addFirst("respEncode", new HttpResponseEncoder());
                ctx.write(nettyResponse);
                ctx.write(new ChunkedFile(randomAccessFile, 0, fileLength, 1300), ctx.newProgressivePromise());
                ChannelFuture channelFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                channelFuture.addListener(e -> {
                    ctx.pipeline().remove("respEncode");
                });
                randomAccessFile.close();
                logger.info("from chunk {}", msgRequest.getUrl());
            } else {
                logger.info("from cache {}", msgRequest.getUrl());
                String s = response.toString();
                out.writeBytes(s.getBytes(StandardCharsets.UTF_8));
                out.writeBytes(response.gainBodyBytes2());
            }
        }
    }

    private void compress(HttpEventEntity eventEntity) throws IOException {
        Request request = eventEntity.getRequest();
        Response response = eventEntity.getResponse();
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
                    response.setCompressByteBody(bytes);
                    response.putHeaderContentLength(bytes.length).putHeaderCompress();
                } else {
                    if (FILE_BYTE_CACHE.containsKey(url)) {
                        byte[] bytes = FILE_BYTE_CACHE.get(url);
                        response.setCompressByteBody(bytes);
                        response.putHeaderContentLength(bytes.length).putHeaderCompress();
                        logger.info("from cache {}", request.getUrl());
                    } else {
                        File file = response.getFile_body();
                        if (file != null) {
                            long length = file.length();
                            if (length < MAX_SINGLE_FILE_COMPRESS_SIZE && CurrentFileCacheSize.get() < MAX_FILE_CACHE_SIZE) {
                                byte[] bytes = GzipUtil.$2CompressBytes(response.getFile_body());
                                logger.info("compress {}", request.getUrl());
                                //开启压缩池
                                if (CACHE_POOL_ON) {
                                    FILE_BYTE_CACHE.put(url, bytes);
                                    CurrentFileCacheSize.addAndGet(bytes.length);
                                }

                                response.setCompressByteBody(bytes);
                                response.putHeaderContentLength(bytes.length).putHeaderCompress();
                            }
                        }
                    }
                }
            }
        }
    }

    private static void addHeader(HttpResponse response, Response orin) {
        DataMap header = orin.getHeader();
        ConcurrentDataSet<top.yqingyu.httpserver.common.Cookie> cookies = orin.getCookies();
        cookies.forEach((e) -> {
            response.headers().add("set-cookie", e.toCookieValStr());
        });
        header.forEach((k, v) -> response.headers().add(k, v));
    }

}
