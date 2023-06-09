package top.yqingyu.httpserver.web.controller;

import top.yqingyu.common.response.R;
import top.yqingyu.common.utils.StringUtil;
import top.yqingyu.httpserver.annotation.QyController;
import top.yqingyu.httpserver.common.HttpMethod;
import top.yqingyu.httpserver.common.LocationDispatcher;
import top.yqingyu.httpserver.common.Request;
import top.yqingyu.httpserver.exception.HttpException;

import java.util.HashMap;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.common.nio$server.event$http.web.controller.Resource
 * @description
 * @createTime 2022年09月15日 16:48:00
 */
//@QyController(path = "root")
public class Resource {

    @QyController(path = "file", method = {HttpMethod.GET})
    public String showResource(String name, String path) {
        if (!"yyj".equals(name))
            throw new HttpException.MethodNotSupposedException("我C");
        StringBuilder sb = new StringBuilder("<table width=\"60%\" border=\"1\" style=\"text-align:left;border-collapse: collapse;\" cellspacing=\"0\">");

        if (StringUtil.isBlank(path))
            path = "/";

        String[] split = path.split("/");

        int length = split.length == 0 ? 1 : split.length;

        String regx = path + ".*";

        HashMap<String, String> dir = new HashMap<>();
        HashMap<String, String> file = new HashMap<>();

        String finalPath = path;
        LocationDispatcher.FILE_RESOURCE_MAPPING.forEach((k, v) -> {
            if (k.indexOf("/") != 0) {
                k = "/" + k;
            }
            if (k.matches(regx)) {
                String[] ks = k.split("/");
                if (ks.length - 1 == length) {
                    file.put(k, ks[Math.min(length, ks.length - 1)]);
                } else if (ks.length - 1 > length) {
                    String value = ("/".equals(finalPath) ? "/" : finalPath + "/") + ks[Math.min(length, ks.length - 1)];
                    dir.put(ks[Math.min(length, ks.length - 1)], value);
                }
            }
        });

        if (!"/".equals(path)) {

            StringBuilder pathBuilder = new StringBuilder();
            int i = 1;
            for (String s : split) {
                if (!"".equals(s) && i++ != length - 1)
                    pathBuilder.append("/").append(s);
            }
            path = pathBuilder.toString();

            sb.append("<tr>")
                    .append("<th colspan='2'>")
                    .append("<a href = '").append("/root/file?name=yyj&path=").append(path).append("'>")
                    .append("..")
                    .append("</a>")
                    .append("</th>")
                    .append("</tr>")
            ;
        }

        dir.forEach((k, v) -> {
            sb
                    .append("<th>")
                    .append("<a href = '").append("/root/file?name=yyj&path=").append(v).append("'>")
                    .append(k)
                    .append("</a>")
                    .append("</th>")

                    .append("<th>")
                    .append(" DIRS")
                    .append("</th>")

                    .append("</tr>");
        });

        file.forEach((k, v) -> {
            sb.append("<tr>")

                    .append("<th>")
                    .append("<a href = '").append(k).append("'>")
                    .append(v)
                    .append("</a>")
                    .append("</th>")

                    .append("<th>")
                    .append("FILE")
                    .append("</th>")

                    .append("</tr>");
        });

        sb.append("</table>");



        return sb.toString();
    }

    @QyController(path = "/getIp",method = HttpMethod.GET)
    public R getIp(Request request){
        return R.ok(request.getInetSocketAddress().getAddress().getHostAddress());
    }
}
