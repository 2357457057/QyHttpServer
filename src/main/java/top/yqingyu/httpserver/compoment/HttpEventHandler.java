package top.yqingyu.httpserver.compoment;


import cn.hutool.core.date.LocalDateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.common.nio$server.core.EventHandler;
import top.yqingyu.common.nio$server.core.RebuildSelectorException;
import top.yqingyu.common.nio$server.core.OperatingRecorder;
import top.yqingyu.common.qydata.DataList;
import top.yqingyu.common.qydata.DataMap;
import top.yqingyu.common.utils.UnitUtil;
import top.yqingyu.common.utils.YamlUtil;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static top.yqingyu.httpserver.compoment.DoRequest.*;
import static top.yqingyu.httpserver.compoment.DoResponse.*;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.event$handler.HttpEventHandler
 * @description
 * @createTime 2022年09月09日 18:05:00
 */

public class HttpEventHandler extends EventHandler {
    public HttpEventHandler() throws IOException {
        super();
    }

    private static final OperatingRecorder<Integer> SOCKET_CHANNEL_RECORD = OperatingRecorder.createNormalRecorder(1024L * 1024 * 2);
    static final OperatingRecorder<Integer> SOCKET_CHANNEL_ACK = OperatingRecorder.createAckRecorder(10L);
    public static int port;
    public static int handlerNumber;
    public static int perHandlerWorker;
    private static long connectTimeMax;
    private static final Logger log = LoggerFactory.getLogger(HttpEventHandler.class);

    public HttpEventHandler(Selector selector) throws IOException {
        super(selector);
        SocketChannelMonitor monitor = new SocketChannelMonitor();
        Thread th = new Thread(monitor);
        th.setName("Monitor" + Thread.currentThread().getName());
        th.setDaemon(true);
        th.start();
    }

    /**
     * 加载URL
     *
     * @author YYJ
     * @description
     */
    @Override
    protected void loading() {

        DataMap yamlUtil = YamlUtil.loadYaml("server-cfg", YamlUtil.LoadType.BOTH).getCfgData();
        DataMap cfg = yamlUtil.getNotNUllData("server-cfg.yml");
        if (cfg.size() == 0)
            cfg = yamlUtil.getNotNUllData("server-cfg.template.yml");
        {

            DataMap server = cfg.getNotNUllData("server");
            {
                port = server.getIntValue("port", 4732);
                handlerNumber = server.getIntValue("handler-num", 4);
                perHandlerWorker = server.getIntValue("per-worker-num", 4);
                Long workerKeepLiveTime = server.$2MILLS("worker-keep-live-time", UnitUtil.$2MILLS("2H"));
                connectTimeMax = server.$2MILLS("connect-time-max", UnitUtil.$2MILLS("15S"));
                boolean open_resource = server.getBooleanValue("open-resource", true);
                boolean open_controller = server.getBooleanValue("open-controller", true);

                if (open_resource) {
                    DataList pathList = server.getDataList("local-resource-path");
                    if (pathList == null || pathList.size() == 0) {
                        String path = System.getProperty("user.dir");
                        LocationMapping.loadingFileResource(path);
                    } else {
                        for (int i = 0; i < pathList.size(); i++) {
                            LocationMapping.loadingFileResource(pathList.getString(i));
                        }
                    }

                }

                if (open_controller) {
                    DataList scan_packages = server.getDataList("controller-package");
                    if (scan_packages == null || scan_packages.size() == 0) {
                        LocationMapping.loadingBeanResource("top.yqingyu.httpserver.web.controller");
                    } else {
                        for (int i = 0; i < scan_packages.size(); i++) {
                            LocationMapping.loadingBeanResource(scan_packages.getString(i));
                        }
                    }
                }

            }

            DataMap transfer = cfg.getNotNUllData("transfer");
            {
                DataMap request = transfer.getNotNUllData("request");
                {
                    DEFAULT_BUF_LENGTH = request.$2B("parse-buffer-size", UnitUtil.$2B("1KB"));
                    MAX_HEADER_SIZE = request.$2B("max-header-size", UnitUtil.$2B("64KB"));
                    MAX_BODY_SIZE = request.$2B("max-body-size", UnitUtil.$2B("128MB"));
                }
                DataMap response = transfer.getNotNUllData("response");
                {
                    DEFAULT_SEND_BUF_LENGTH = response.$2B("send-buf-size", UnitUtil.$2B("2MB"));
                }
            }
            DataMap file_compress = cfg.getNotNUllData("file-compress");
            {
                FILE_COMPRESS_ON = file_compress.getBoolean("open", true);
                MAX_SINGLE_FILE_COMPRESS_SIZE = file_compress.$2B("max-single-file-compress-size", UnitUtil.$2B("128MB"));
                DataMap compressPool = file_compress.getNotNUllData("compress-cache-pool");
                {
                    MAX_FILE_CACHE_SIZE = compressPool.$2B("max-file-cache-size", UnitUtil.$2B("0.5GB"));
                    CACHE_POOL_ON = compressPool.getBoolean("open", true);
                }
            }

            DataMap session = cfg.getNotNUllData("session");
            SESSION_TIME_OUT = session.$2S("session-timeout", UnitUtil.$2S("7DAY"));

        }
    }


    @Override
    public void read(Selector selector, SocketChannel socketChannel) throws Exception {
        socketChannel.register(selector, SelectionKey.OP_WRITE);
        int i = socketChannel.hashCode();
        SOCKET_CHANNELS.get(i).put("LocalDateTime", LocalDateTime.now());
//        READ_POOL.submit(new DoRequest(socketChannel, QUEUE));
//        WRITE_POOL.submit(new DoResponse(QUEUE, selector));
        DoRequest doRequest = new DoRequest(socketChannel, QUEUE);
        doRequest.call();
        DoResponse doResponse = new DoResponse(QUEUE, selector);//,SOCKET_CHANNEL_ACK);
        doResponse.call();
    }


    @Override
    public void write(Selector selector, SocketChannel socketChannel) throws Exception {
        try {
            SOCKET_CHANNEL_RECORD.add2(socketChannel.hashCode());
        } catch (RebuildSelectorException e) {
            SOCKET_CHANNEL_RECORD.remove(socketChannel.hashCode());
            socketChannel.close();
        }
    }


    @Override
    public void assess(Selector selector, SocketChannel socketChannel) throws Exception {
    }


    class SocketChannelMonitor implements Runnable {
        @Override
        public void run() {
            while (true) {
                LocalDateTime a = LocalDateTime.now();
                SOCKET_CHANNELS.forEach((i, s) -> {
                    try {
                        LocalDateTime b = s.get("LocalDateTime", LocalDateTime.class);
                        SocketChannel socketChannel = s.get("SocketChannel", SocketChannel.class);
                        long between = LocalDateTimeUtil.between(b, a, ChronoUnit.MILLIS);
                        if (between > connectTimeMax && !socketChannel.isConnectionPending()) {
                            SOCKET_CHANNELS.remove(i);
                            socketChannel.close();
                        }
                    } catch (Exception e) {
                        log.error("断链异常", e);
                    }

                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error("线程异常", e);
                }
            }
        }
    }
}
