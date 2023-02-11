package top.yqingyu.httpserver.compomentv2;

import top.yqingyu.common.qydata.DataList;
import top.yqingyu.common.qydata.DataMap;
import top.yqingyu.common.utils.UnitUtil;
import top.yqingyu.common.utils.YamlUtil;

import static top.yqingyu.httpserver.compomentv2.DoRequest.*;
import static top.yqingyu.httpserver.compomentv2.DoResponse.*;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.httpserver.compoment.ServerConfig
 * @description
 * @createTime 2023年02月10日 02:30:00
 */
public class ServerConfig {

    static long resourceReloadingTime;
    public static int port;
    public static int handlerNumber;
    public static int perHandlerWorker;
    public static long connectTimeMax;
    public static void load() {
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
                resourceReloadingTime = server.$2MILLS("resource-reloading-time", UnitUtil.$2MILLS("30S"));
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
}
