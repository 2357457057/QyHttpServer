package top.yqingyu.httpserver.common;

import top.yqingyu.common.bean.NetChannel;
import top.yqingyu.common.server$aio.Session;
import top.yqingyu.common.server$nio.core.RebuildSelectorException;
import top.yqingyu.common.utils.ArrayUtil;
import top.yqingyu.common.utils.StringUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Stack;


public class HttpUtil {
    public static void getUrlParam(Request qyReq, String substring) {
        String[] split = substring.split("&");
        for (String s : split) {
            String[] urlParamKV = s.split("=");
            if (urlParamKV.length == 2) qyReq.putUrlParam(urlParamKV[0], urlParamKV[1]);
            else qyReq.putUrlParam(urlParamKV[0], "");
        }
    }

    public static void parseMultipartFile(byte[] boundaryBytes, byte[] temp, Stack<MultipartFile> multipartFileStack) throws IOException {
        ArrayList<byte[]> boundary = ArrayUtil.splitByTarget(temp, boundaryBytes);

        if (boundary.isEmpty() & multipartFileStack.isEmpty()) {
            return;
        }
        for (int i = 0; i < boundary.size(); i++) {
            if (i == 0 && !multipartFileStack.isEmpty()) {
                multipartFileStack.peek().write(boundary.get(0));
            } else {
                ArrayList<byte[]> bytes = ArrayUtil.splitByTarget(boundary.get(i), ArrayUtil.RN_RN);
                if (bytes.isEmpty())
                    continue;
                byte[] multiHeaderBytes = bytes.get(0);
                ArrayList<byte[]> multiHeader = ArrayUtil.splitByTarget(multiHeaderBytes, ArrayUtil.RN);
                if (multiHeader.size() != 2)
                    continue;
                String Content_Disposition = new String(multiHeader.get(0), StandardCharsets.UTF_8);
                String[] Content_Dispositions = Content_Disposition.split("filename=\"");
                String fileName = StringUtil.removeEnd(Content_Dispositions[1], "\"");
                MultipartFile multipartFile = new MultipartFile(fileName, "/tmp");
                multipartFileStack.push(multipartFile);
                if (bytes.size() == 2) multipartFileStack.peek().write(bytes.get(1));
            }
        }

    }

    public static void assembleHeader(Request request, byte[] header, Object channel) throws Exception {
        //只剩body
        ArrayList<byte[]> info$header = ArrayUtil.splitByTarget(header, ArrayUtil.RN);
        ArrayList<byte[]> info = ArrayUtil.splitByTarget(info$header.remove(0), ArrayUtil.SPACE);

        if (info.size() < 3) {
            if (channel instanceof NetChannel netChannel) {
                netChannel.close();
            } else if (channel instanceof Session session) {
                session.close();
            }
            throw new RebuildSelectorException("消息解析异常");
        }

        request.setMethod(info.get(0));
        request.setUrl(info.get(1));
        request.setHttpVersion(info.get(2));

        int i = StringUtil.indexOf(request.getUrl(), '?');

        if (i != -1) {
            String substring = request.getUrl().substring(i + 1);
            HttpUtil.getUrlParam(request, substring);
        }
        for (byte[] bytes : info$header) {
            ArrayList<byte[]> headerName_value = ArrayUtil.splitByTarget(bytes, ArrayUtil.COLON_SPACE);
            request.putHeader(headerName_value.get(0), headerName_value.size() == 2 ? headerName_value.get(1) : null);
        }
    }

    public static void setInstanceFalse(top.yqingyu.httpserver.common.Session session) {
        session.setNewInstance();
    }

    public static void write(MultipartFile file, byte[] bytes) throws IOException {
        file.write(bytes);
    }

    public static void endWrite(MultipartFile file) throws IOException {
        file.endWrite();
    }

    public static void setParseEnd(Request request) {
        request.setParseEnd();
    }

    public static void setMethod(Request request, byte[] method) {
        request.setMethod(method);
    }

    public static void setHttpVersion(Request request, byte[] httpVersion) {
        request.setHttpVersion(httpVersion);
    }

    public static void setUrl(Request request, byte[] url) {
        request.setUrl(url);
    }

    public static void setBody(Request request, byte[] body) {
        request.setBody(body);
    }

    public static void setSession(Request request, top.yqingyu.httpserver.common.Session session) {
        request.setSession(session);
    }

    public static void putHeader(Request request, byte[] key, byte[] obj) {
        request.putHeader(key, obj);
    }


    public static boolean canCompress(Request request) {
        return request.canCompress();
    }

    public static void setMultipartFile(Request request, MultipartFile multipartFile) {
        request.setMultipartFile(multipartFile);
    }

}
