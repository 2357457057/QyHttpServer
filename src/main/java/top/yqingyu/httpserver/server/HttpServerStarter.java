package top.yqingyu.httpserver.server;

import org.apache.commons.lang3.StringUtils;
import top.yqingyu.httpserver.compoment.HttpEventHandler;
import top.yqingyu.httpserver.compomentv2.HttpEventHandlerV2;
import top.yqingyu.httpserver.compomentv2.ServerConfig;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static top.yqingyu.httpserver.compoment.ServerConfig.*;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.httpserver.server.HttpServer
 * @description 简易Http服务
 * @createTime 2022年09月09日 20:35:00
 */
public class HttpServerStarter {

    public static final String SERVER_NAME;

    static {
        String temp;

        temp = System.getProperty("server.name");
        if (StringUtils.isBlank(temp)) {
            temp = "QyHttp";
        }
        SERVER_NAME = temp;
    }

    public static void main(String[] args) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        SERVER1();
    }

    public static void SERVER2() throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        banner();
        ServerConfig.load();
        top.yqingyu.common.server$aio
                .CreateServer
                .create()
                .setServerName(SERVER_NAME)
                .setThreadNo(32)
                .setHandler(HttpEventHandlerV2.class)
                .bind(ServerConfig.port)
                .start();
    }

    public static void SERVER1() throws IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        banner();
        top.yqingyu.common.server$nio
                .CreateServer
                .createDefault(SERVER_NAME)
                .implEvent(HttpEventHandler.class)
                .loadingEventResource()
                .defaultFixRouter(handlerNumber, perHandlerWorker)
                .listenPort(port)
                .start();
    }

    static void banner() {
        System.out.println("""

                HTTP Server Starting...
                             *                 *                  *              *        \s
                                                                          *             * \s
                                            *            *                             ___\s
                      *               *                                          |     | |\s
                            *              _________##                 *        / \\    | |\s
                                          @\\\\\\\\\\\\\\\\\\##    *     |              |--o|===|-|\s
                      *                  @@@\\\\\\\\\\\\\\\\##\\       \\|/|/            |---|   |C|\s
                                        @@ @@\\\\\\\\\\\\\\\\\\\\\\    \\|\\\\|//|/     *   /     \\  |N|\s
                                 *     @@@@@@@\\\\\\\\\\\\\\\\\\\\\\    \\|\\|/|/         |  C    | |S|\s
                                      @@@@@@@@@----------|    \\\\|//          |  H    |=|A|\s
                           __         @@ @@@ @@__________|     \\|/           |  N    | | |\s
                      ____|_@|_       @@@@@@@@@__________|     \\|/           |_______| |_|\s
                    =|__ _____ |=     @@@@ .@@@__________|      |             |@| |@|  | |\s
                    ____0_____0__\\|/__@@@@__@@@__________|_\\|/__|___\\|/__\\|/___________|_|_
                                                                                              \s
                                                                                 -- by Qy Severus\s
                """);
    }
}
