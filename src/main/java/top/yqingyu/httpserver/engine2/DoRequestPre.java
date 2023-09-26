package top.yqingyu.httpserver.engine2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.*;
import org.jetbrains.annotations.NotNull;
import top.yqingyu.common.qydata.ConcurrentQyMap;
import top.yqingyu.common.utils.ArrayUtil;
import top.yqingyu.common.utils.StringUtil;
import top.yqingyu.httpserver.common.ContentType;
import top.yqingyu.httpserver.common.HttpUtil;
import top.yqingyu.httpserver.common.LocationDispatcher;
import top.yqingyu.httpserver.common.MultipartFile;
import top.yqingyu.httpserver.exception.HttpException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static top.yqingyu.common.utils.ArrayUtil.RN;
import static top.yqingyu.common.utils.ArrayUtil.splitByTarget;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.httpserver.engine2.DoRequestPre
 * @description
 * @createTime 2023年03月10日 00:08:00
 */
public class DoRequestPre extends MessageToMessageDecoder<HttpObject> {

    private volatile boolean isMultipartFile = false;
    private volatile MultipartFile multipartFile;
    private volatile HttpMessage httpMessage;

    private volatile DefaultFullHttpRequest fullHttpRequest;
    private byte[] boundary;
    private byte[] boundaryEnd;

    private final List<MultipartFile> multipartFileList = new ArrayList<>(1);

    private final AtomicInteger boundaryTimes = new AtomicInteger(0);

    private final static ConcurrentQyMap<DefaultFullHttpRequest, List<MultipartFile>> MULTIPART_FILE_CONTAINER = new ConcurrentQyMap<>();

    @Override
    protected void decode(ChannelHandlerContext ctx, HttpObject httpObject, List<Object> list) throws Exception {

        if (isStartMessage(httpObject)) {
            httpMessage = (HttpMessage) httpObject;
            HttpHeaders headers = httpMessage.headers();
            String s = headers.get(HttpHeaderNames.CONTENT_TYPE);
            ContentType contentType = ContentType.parse(s);
            if (ContentType.MULTIPART_FORM_DATA.isSameMimeType(contentType)) {
                if (httpMessage instanceof DefaultHttpRequest httpRequest && !LocationDispatcher.MULTIPART_BEAN_RESOURCE_MAPPING.containsKey(httpRequest.uri().split("[?]")[0])) {
                    throw new HttpException.NotAMultipartFileInterfaceException("非可上传接口");
                }
                isMultipartFile = true;
                boundary = ("--" + contentType.getParameter("boundary") + "\r\n").getBytes(StandardCharsets.UTF_8);
                boundaryEnd = ("\r\n--" + contentType.getParameter("boundary") + "--\r\n").getBytes(StandardCharsets.UTF_8);
            } else {
                if (httpMessage instanceof DefaultHttpRequest httpRequest) {
                    fullHttpRequest = new DefaultFullHttpRequest(httpRequest.protocolVersion(), httpRequest.method(), httpRequest.uri());
                    fullHttpRequest.headers().add(httpRequest.headers());
                }
            }

        } else if (isContentMessage(httpObject)) {
            HttpContent httpContent = (HttpContent) httpObject;
            if (isLastContentMessage(httpContent)) {
                LastHttpContent lastHttpContent = (LastHttpContent) httpContent;
                if (!isMultipartFile) {
                    ByteBuf content = fullHttpRequest.content();
                    content.writeBytes(lastHttpContent.content());
                    ctx.fireChannelRead(fullHttpRequest);
                    return;
                }
                ByteBuf content = httpContent.content();
                int readableBytes = content.readableBytes();
                byte[] bytes = new byte[readableBytes];
                content.readBytes(bytes);
                ArrayList<byte[]> bytes1 = splitByTarget(bytes, boundaryEnd);
                HttpUtil.write(multipartFile,bytes1.get(0));
                HttpUtil.endWrite(multipartFile);
                if (httpMessage instanceof DefaultHttpRequest httpRequest) {
                    fullHttpRequest = new DefaultFullHttpRequest(httpRequest.protocolVersion(), httpRequest.method(), httpRequest.uri());
                    HttpHeaders headers = fullHttpRequest.headers();
                    headers.add(httpRequest.headers());
                    headers.add(Dict.MULTIPART_FILE_LIST, Dict.MULTIPART_FILE_LIST);
                    MULTIPART_FILE_CONTAINER.put(fullHttpRequest, multipartFileList);
                    ctx.fireChannelRead(fullHttpRequest);
                }
            } else {
                if (!isMultipartFile) {
                    ByteBuf content = fullHttpRequest.content();
                    content.writeBytes(httpContent.content());
                    return;
                }
                ByteBuf content = httpContent.content();
                int readableBytes = content.readableBytes();
                byte[] bytes = new byte[readableBytes];
                content.readBytes(bytes);
                if (ArrayUtil.firstIndexOfTarget(bytes, boundary) != -1) {
                    ArrayList<byte[]> data1 = ArrayUtil.splitByTarget(bytes, boundary);
                    if (boundaryTimes.get() != 0) {
                        data1 = ArrayUtil.splitByTarget(bytes, boundaryEnd);
                        HttpUtil.write(multipartFile,data1.get(0));
                        HttpUtil.endWrite(multipartFile);
                    }
                    ArrayList<byte[]> data2 = ArrayUtil.splitByTarget(data1.get(1), ArrayUtil.RN_RN);
                    byte[] header = data2.get(0);
                    ArrayList<byte[]> multiHeader = ArrayUtil.splitByTarget(header, RN);
                    if (multiHeader.size() == 2) {
                        String Content_Disposition = new String(multiHeader.get(0), StandardCharsets.UTF_8);
                        String[] Content_Dispositions = Content_Disposition.split("filename=\"");
                        String fileName = StringUtil.removeEnd(Content_Dispositions[1], "\"");
                        multipartFile = new MultipartFile(fileName, "/tmp");
                        HttpUtil.write(multipartFile,data2.get(1));
                        multipartFileList.add(multipartFile);
                    }
                    boundaryTimes.getAndIncrement();
                    return;
                }
                HttpUtil.write(multipartFile,bytes);
            }
        } else {
            ctx.fireChannelRead(httpObject);
        }

    }

    public static List<MultipartFile> getMultipartFileList(@NotNull DefaultFullHttpRequest defaultHttpRequest) {
        HttpHeaders headers = defaultHttpRequest.headers();
        headers.remove(Dict.MULTIPART_FILE_LIST);
        return MULTIPART_FILE_CONTAINER.remove(defaultHttpRequest);
    }

    protected boolean isStartMessage(HttpObject msg) throws Exception {
        return msg instanceof HttpMessage;
    }

    protected boolean isContentMessage(HttpObject msg) throws Exception {
        return msg instanceof HttpContent;
    }

    protected boolean isLastContentMessage(HttpContent msg) throws Exception {
        return msg instanceof LastHttpContent;
    }
}
