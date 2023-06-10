package top.yqingyu.httpserver.common;

import top.yqingyu.common.qydata.DataList;
import top.yqingyu.common.qydata.DataMap;
import top.yqingyu.common.utils.ResourceUtil;
import top.yqingyu.common.utils.UnitUtil;
import top.yqingyu.common.utils.YamlUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.httpserver.compoment.ServerConfig
 * @description
 * @createTime 2023年02月10日 02:30:00
 */
public class ServerConfig {

    public static long resourceReloadingTime;
    public static int ENGINE;
    public static boolean SSL_ENABLE;
    public static String SSL_CERT_PATH;
    public static String SSL_KEY_PATH;
    public static int port;
    public static int handlerNumber;
    public static int perHandlerWorker;
    public static long connectTimeMax;
    /*============================Request===================================*/
    public static long DEFAULT_BUF_LENGTH;
    //最大Body长度 64M
    public static long MAX_BODY_SIZE;

    //最大header长度 128KB
    public static long MAX_HEADER_SIZE;
    public static boolean ALLOW_UPDATE = true;

    /*============================Response===================================*/
    //最大压缩源文件大小 128MB
    public static long MAX_SINGLE_FILE_COMPRESS_SIZE;
    //是否开启缓存池
    public static boolean CACHE_POOL_ON;
    //最大缓存池大小 1.5GB
    public static long MAX_FILE_CACHE_SIZE;
    public static long SESSION_TIME_OUT;

    public static long DEFAULT_SEND_BUF_LENGTH;

    public static boolean FILE_COMPRESS_ON;
    public static ArrayList<ContentType> UN_DO_COMPRESS_FILE = new ArrayList<>(Arrays.asList(
            ContentType.APPLICATION_OCTET_STREAM,
            ContentType.IMAGE_JPEG,
            ContentType.IMAGE_PNG,
            ContentType.IMAGE_GIF,
            ContentType.IMAGE_WEBP,
            ContentType.IMAGE_BMP,
            ContentType.IMAGE_SVG,
            ContentType.IMAGE_X_ICON,
            ContentType.IMAGE_TIFF,
            ContentType.VIDEO_AVI,
            ContentType.VIDEO_MP4,
            ContentType.VIDEO_MPEG4,
            ContentType.VIDEO_WMV,
            ContentType.VIDEO_WEBM,
            ContentType.AUDIO_MP3
    ));

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
                ENGINE = server.getIntValue("engine", 2);
                resourceReloadingTime = server.$2MILLS("resource-reloading-time", UnitUtil.$2MILLS("60S"));
                connectTimeMax = server.$2MILLS("connect-time-max", UnitUtil.$2MILLS("15S"));
                boolean open_resource = server.getBooleanValue("open-resource", true);
                boolean open_controller = server.getBooleanValue("open-controller", true);

                DataMap ssl = server.getNotNUllData("ssl");
                SSL_ENABLE = ssl.getBoolean("enable", false);
                if (SSL_ENABLE) {
                    try {
                        File sslCrt = ResourceUtil.getFile(ssl.getString("cert-path", "/"));
                        File sslKey = ResourceUtil.getFile(ssl.getString("key-path", "/"));
                        SSL_CERT_PATH = sslCrt.getAbsolutePath();
                        SSL_KEY_PATH = sslKey.getAbsolutePath();
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }

                if (open_resource) {
                    DataList pathList = server.getDataList("local-resource-path");
                    if (pathList == null || pathList.size() == 0) {
                        String path = System.getProperty("user.dir");
                        LocationDispatcher.loadingFileResource(path);
                    } else {
                        for (int i = 0; i < pathList.size(); i++) {
                            LocationDispatcher.loadingFileResource(pathList.getString(i));
                        }
                    }

                }

                if (open_controller) {
                    DataList scan_packages = server.getDataList("controller-package");
                    if (scan_packages == null || scan_packages.size() == 0) {
                        LocationDispatcher.loadingBeanResource("top.yqingyu.httpserver.web.controller");
                    } else {
                        for (int i = 0; i < scan_packages.size(); i++) {
                            LocationDispatcher.loadingBeanResource(scan_packages.getString(i));
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
