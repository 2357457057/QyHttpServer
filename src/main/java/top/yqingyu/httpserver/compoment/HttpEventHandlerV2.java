package top.yqingyu.httpserver.compoment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.common.bean.NetChannel;
import top.yqingyu.common.server$aio.EventHandler;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.httpserver.compoment.HttpEventHandlerV2
 * @description
 * @createTime 2023年02月09日 23:24:00
 */
public class HttpEventHandlerV2 extends EventHandler<HttpEventHandlerV2> {
    public HttpEventHandlerV2() {
        super();
        ServerConfig.load();
    }

    private final static Logger logger = LoggerFactory.getLogger(HttpEventHandlerV2.class);

    @Override
    public void IO(NetChannel channel, HttpEventHandlerV2 attachment) {
        try {
            System.out.println(channel.hashCode());
            channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
            HttpEventEntity httpEventEntity = new DoRequest(null, channel).call();
            DoResponse doResponse = new DoResponse(httpEventEntity, null);
            doResponse.setV2(attachment, new HttpEventHandlerV2W(channel, this));
            doResponse.call();
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    @Override
    public void exception(Throwable exc, HttpEventHandlerV2 attachment) {
        super.exception(exc, attachment);
    }

    class HttpEventHandlerV2W implements CompletionHandler<Integer, HttpEventHandlerV2> {
        protected NetChannel netChannel;
        protected HttpEventHandlerV2 httpEventHandlerV2;

        private final AtomicInteger atomicInteger = new AtomicInteger(0);

        public HttpEventHandlerV2W(NetChannel netChannel, HttpEventHandlerV2 httpEventHandlerV2) {
            this.netChannel = netChannel;
            this.httpEventHandlerV2 = httpEventHandlerV2;
        }

        @Override
        public void completed(Integer result, HttpEventHandlerV2 attachment) {
            logger.info("result {}", result);
            if (result == -1) {
                serverSokChannel.accept(attachment, httpEventHandlerV2);
                return;
            }
            int andIncrement = atomicInteger.getAndIncrement();
            if (andIncrement == 1) {
                serverSokChannel.accept(attachment, httpEventHandlerV2);
                try {
                    netChannel.close();
                } catch (IOException e) {
                    logger.error("", e);
                }
            } else logger.info("andIncrement {}", andIncrement);

        }


        @Override
        public void failed(Throwable exc, HttpEventHandlerV2 attachment) {
            exc.printStackTrace();
        }
    }

}

