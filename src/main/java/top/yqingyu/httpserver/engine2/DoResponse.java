package top.yqingyu.httpserver.engine2;

import com.alibaba.fastjson2.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedNioFile;
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
import top.yqingyu.httpserver.common.WebFile;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicLong;

import static top.yqingyu.httpserver.common.ServerConfig.*;


public class DoResponse extends MessageToByteEncoder<HttpEventEntity> {
    private static final ConcurrentDataMap<String, byte[]> FILE_BYTE_CACHE = new ConcurrentDataMap<>();
    private final AtomicLong CurrentFileCacheSize = new AtomicLong();

    private static final Logger logger = LoggerFactory.getLogger(DoResponse.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, HttpEventEntity msg, ByteBuf out) throws Exception {
        Response response = msg.getResponse();
        logger.debug("Response {}", JSON.toJSONString(response));
        if ("304".equals(response.getStatue_code())) {
            DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_MODIFIED);
            addHeader(httpResponse, response);
            ctx.writeAndFlush(httpResponse);
            return;
        }

        if (FILE_COMPRESS_ON && !UN_DO_COMPRESS_FILE.contains(response.gainHeaderContentType()))
            compress(msg);

        if ((response.getStrBody() == null && response.gainFileBody() == null)) {
            DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
            addHeader(httpResponse, response);
            ctx.writeAndFlush(httpResponse);
            return;
        }

        File file_body = response.getFile_body();
        if (file_body != null && !file_body.exists()) {
            DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
            addHeader(httpResponse, response);
            ctx.writeAndFlush(httpResponse);
        }
        if (file_body == null || response.isCompress()) {
            ByteBufAllocator alloc = out.alloc();
            ByteBuf content = alloc.directBuffer(0);
            DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(Integer.parseInt(response.getStatue_code())), content);
            addHeader(httpResponse, response);
            content.writeBytes(response.gainBodyBytes2());
            ctx.writeAndFlush(httpResponse);
            return;
        }
        FileChannel fileChannel = FileChannel.open(file_body.toPath(), StandardOpenOption.READ);
        HttpResponse nettyResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        addHeader(nettyResponse, response);
        HttpUtil.setContentLength(nettyResponse, fileChannel.size());
        ctx.write(nettyResponse);
        ctx.write(new ChunkedNioFile(fileChannel, 0, fileChannel.size(), 3096), ctx.newProgressivePromise());
        ChannelFuture channelFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        channelFuture.addListener(e -> fileChannel.close());
    }

    private void compress(HttpEventEntity eventEntity) throws IOException {
        Request request = eventEntity.getRequest();
        Response response = eventEntity.getResponse();
        if (!top.yqingyu.httpserver.common.HttpUtil.canCompress(request))
            return;

        String url = request.getUrl();
        ContentType requestCtTyp = ContentType.parse(request.getHeader("Content-Type"));
        Charset charset;
        if (requestCtTyp != null)
            charset = requestCtTyp.getCharset() == null ? StandardCharsets.UTF_8 : requestCtTyp.getCharset();
        else charset = StandardCharsets.UTF_8;
        if ("304|100".contains(response.getStatue_code()))
            return;

        if (response.getStrBody() == null && response.gainFileBody() == null)
            return;

        String strBody = response.getStrBody();
        if (StringUtils.isNotBlank(strBody)) {
            byte[] bytes = GzipUtil.$2CompressBytes(strBody, charset);
            response.setCompressByteBody(bytes);
            response.putHeaderContentLength(bytes.length).putHeaderCompress();
            return;
        }
        WebFile file = response.getFile_body();
        if (file == null) {
            return;
        }
        if (file.isModify() && FILE_BYTE_CACHE.containsKey(url))
            CurrentFileCacheSize.lazySet(CurrentFileCacheSize.get() - FILE_BYTE_CACHE.remove(url).length);

        if (FILE_BYTE_CACHE.containsKey(url)) {
            byte[] bytes = FILE_BYTE_CACHE.get(url);
            response.setCompressByteBody(bytes);
            response.putHeaderContentLength(bytes.length).putHeaderCompress();
            return;
        }

        long length = file.length();
        if (length < MAX_SINGLE_FILE_COMPRESS_SIZE && CurrentFileCacheSize.get() < MAX_FILE_CACHE_SIZE) {
            byte[] bytes = GzipUtil.$2CompressBytes(response.getFile_body());
            //开启压缩池
            if (CACHE_POOL_ON) {
                FILE_BYTE_CACHE.put(url, bytes);
                CurrentFileCacheSize.addAndGet(bytes.length);
            }
            response.setCompressByteBody(bytes);
            response.putHeaderContentLength(bytes.length).putHeaderCompress();
        }
    }

    private static void addHeader(HttpResponse response, Response orin) {
        DataMap header = orin.getHeader();
        HttpHeaders headers = response.headers();
        ConcurrentDataSet<top.yqingyu.httpserver.common.Cookie> cookies = orin.getCookies();
        cookies.forEach((e) -> headers.add("set-cookie", e.toCookieValStr()));
        header.forEach(headers::add);
    }

}
