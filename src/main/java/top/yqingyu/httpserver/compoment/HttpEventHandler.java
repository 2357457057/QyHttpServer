package top.yqingyu.httpserver.compoment;


import cn.hutool.core.date.LocalDateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.common.bean.NetChannel;
import top.yqingyu.common.qydata.ConcurrentQyMap;
import top.yqingyu.common.server$nio.core.EventHandler;
import top.yqingyu.common.server$nio.core.RebuildSelectorException;
import top.yqingyu.common.server$nio.core.OperatingRecorder;
import top.yqingyu.common.qydata.DataList;
import top.yqingyu.common.qydata.DataMap;
import top.yqingyu.common.utils.UnitUtil;
import top.yqingyu.common.utils.YamlUtil;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
    private static final AtomicInteger Monitor = new AtomicInteger(1);
    private static final Logger log = LoggerFactory.getLogger(HttpEventHandler.class);

    public HttpEventHandler(Selector selector) throws IOException {
        super(selector);
        SocketChannelMonitor monitor = new SocketChannelMonitor();
        Thread th = new Thread(monitor);
        th.setName("Monitor-Handler" + Monitor.getAndIncrement());
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
        ServerConfig.load();
    }


    @Override
    public void read(Selector selector, NetChannel socketChannel) throws Exception {
        socketChannel.register(selector, SelectionKey.OP_WRITE);
        ConcurrentQyMap<String, Object> status = NET_CHANNELS.get(socketChannel.hashCode());
        try {
            status.put("LocalDateTime", LocalDateTime.now());
            status.put("isResponseEnd", Boolean.FALSE);
            DoRequest doRequest = new DoRequest(socketChannel, QUEUE);
            READ_POOL.execute(doRequest);
            DoResponse doResponse = new DoResponse(selector, QUEUE, status);
            WRITE_POOL.execute(doResponse);
        } catch (Exception e) {
            status.put("isResponseEnd", Boolean.TRUE);
            throw e;
        }
    }


    @Override
    public void write(Selector selector, NetChannel socketChannel) throws Exception {
    }


    @Override
    public void assess(Selector selector, NetChannel socketChannel) throws Exception {
    }


    class SocketChannelMonitor implements Runnable {
        private static final Logger log = LoggerFactory.getLogger(SocketChannelMonitor.class);

        @Override
        public void run() {
            while (Thread.interrupted()) {
                LocalDateTime a = LocalDateTime.now();
                NET_CHANNELS.forEach((i, s) -> {
                    NetChannel socketChannel = null;
                    LocalDateTime b = null;
                    Boolean end = null;
                    try {
                        socketChannel = s.get("NetChannel", NetChannel.class);
                        b = s.get("LocalDateTime", LocalDateTime.class);
                        end = s.get("isResponseEnd", Boolean.class);
                        long between = LocalDateTimeUtil.between(b, a, ChronoUnit.MILLIS);
                        if (between > ServerConfig.connectTimeMax && end && !socketChannel.isConnectionPending()) {
                            NET_CHANNELS.remove(i);
                            socketChannel.close();
                            log.trace("满足关闭条件-关闭channel hash: {}", i);
                        }
                    } catch (Exception e) {
                        NET_CHANNELS.remove(i);
                        if (socketChannel != null) {
                            try {
                                socketChannel.close();
                                log.trace("断链异常-关闭channel hash: {}", i);
                            } catch (IOException ex) {
                                log.error("关闭异常 hash: {}", i, ex);
                            }
                        }
                        log.error("断链异常 hash: {} , acceptTime: {} ,end: {} ,channel: {}", i, b, end, s, e);
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
