package top.yqingyu.httpserver.server;

import top.yqingyu.common.utils.StringUtil;
import top.yqingyu.httpserver.common.ServerConfig;
import top.yqingyu.httpserver.engine0.HttpEventHandler;
import top.yqingyu.httpserver.engine1.HttpEventHandlerV2;
import top.yqingyu.httpserver.engine2.Engine2;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static top.yqingyu.httpserver.common.ServerConfig.*;


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
        if (StringUtil.isBlank(temp)) {
            temp = "QyHttp";
        }
        SERVER_NAME = temp;
    }

    public static void main(String[] args) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, InterruptedException {
        banner();
        ServerConfig.load();
        if (ENGINE == 0) {
            ENGINE0();
        } else if (ENGINE == 1) {
            ENGINE1();
        } else if (ENGINE == 2) {
            ENGINE2();
        }
    }

    public static void ENGINE1() throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        top.yqingyu.common.server$aio
                .CreateServer
                .create()
                .setServerName(SERVER_NAME)
                .setThreadNo(32)
                .setHandler(HttpEventHandlerV2.class)
                .bind(port)
                .start();
    }

    public static void ENGINE0() throws IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        top.yqingyu.common.server$nio
                .CreateServer
                .createDefault(SERVER_NAME)
                .implEvent(HttpEventHandler.class)
                .defaultFixRouter(handlerNumber, perHandlerWorker)
                .listenPort(port)
                .start();
    }

    public static void ENGINE2() throws InterruptedException {
        Engine2.start(SERVER_NAME, port);
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
