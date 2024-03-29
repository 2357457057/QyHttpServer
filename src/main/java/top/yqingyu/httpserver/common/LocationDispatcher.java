package top.yqingyu.httpserver.common;

import com.alibaba.fastjson2.JSON;
import top.yqingyu.common.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.common.asm.impl.MethodParamGetter;
import top.yqingyu.common.qydata.DataMap;
import top.yqingyu.common.utils.ClazzUtil;
import top.yqingyu.common.utils.ResourceUtil;
import top.yqingyu.common.utils.StringUtil;
import top.yqingyu.common.utils.UUIDUtil;
import top.yqingyu.httpserver.annotation.QyController;
import top.yqingyu.httpserver.exception.HttpException;

import java.lang.reflect.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.httpserver.entity.LocationDispatcher
 * @description
 * @createTime 2022年09月10日 22:56:00
 */
public class LocationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(LocationDispatcher.class);

    public static ConcurrentHashMap<String, String> FILE_RESOURCE_MAPPING = new ConcurrentHashMap<>();
    public static List<String> RootPathArray = new ArrayList<>();
    static final ConcurrentHashMap<String, ConcurrentHashMap<String, LocalDateTime>> $304_CACHING = new ConcurrentHashMap<>();
    static final ConcurrentHashMap<String, WebFile> FILE_CACHING = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, Bean> BEAN_RESOURCE_MAPPING = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, Bean> MULTIPART_BEAN_RESOURCE_MAPPING = new ConcurrentHashMap<>();

    static final String FORM = "{\"body\"}";

    static final String[] FILE_SUFFIX = {".html", "index.html", "index.htm"};


    static void loadingFileResource(String rootPath) {
        HashMap<String, String> mapping = ResourceUtil.getFilePathMapping(rootPath);
        FILE_RESOURCE_MAPPING.putAll(mapping);
        RootPathArray.add(rootPath);
        log.debug("loading  {} resource mapping", rootPath);
    }

    static void loadingBeanResource(String packageName) {

        List<Class<?>> classes = ClazzUtil.getClassListByAnnotation(packageName, QyController.class);

        for (Class<?> aClass : classes) {
            QyController annotation = aClass.getAnnotation(QyController.class);
            String parentPath = annotation.path();
            Object instance = null;
            Method[] methods = aClass.getDeclaredMethods();

            Constructor<?>[] aClassConstructors = aClass.getConstructors();

            for (Constructor<?> constructor : aClassConstructors) {
                Parameter[] parameters = constructor.getParameters();
                if (parameters.length == 0) {
                    try {
                        instance = constructor.newInstance();
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        log.error("bean mapping error constructor error ", e);
                    }
                }
            }

            for (Method method : methods) {
                if (method.trySetAccessible()) {
                    method.setAccessible(true);
                }

                QyController methodAnnotation = method.getAnnotation(QyController.class);

                if (methodAnnotation != null) {
                    Bean bean = new Bean();
                    String methodPath = methodAnnotation.path();
                    HttpMethod[] httpMethods = methodAnnotation.method();
                    bean.setType(aClass);
                    bean.setObj(instance);
                    bean.setHttpMethods(httpMethods);
                    bean.setMethod(method);
                    String[] parameterNames = MethodParamGetter.doGetParameterNames(method);
                    bean.setMethodParamName(parameterNames);
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    String[] parameterTypeName = new String[parameterTypes.length];
                    for (int i = 0; i < parameterTypes.length; i++) {
                        parameterTypeName[i] = parameterTypes[i].getName();
                    }
                    bean.setMethodParamType(parameterTypeName);

                    if (methodPath.indexOf("/") != 0) {
                        methodPath = "/" + methodPath;
                    }
                    String path = parentPath + methodPath;

                    if (path.indexOf("/") != 0) {
                        path = "/" + path;
                    }
                    BEAN_RESOURCE_MAPPING.put(path, bean);

                    if (parameterNames != null && Arrays.asList(parameterTypeName).contains(MultipartFile.class.getName()))
                        MULTIPART_BEAN_RESOURCE_MAPPING.put(path, bean);
                    log.debug("add bean mapping {}  > {}", path, bean);
                }
            }
        }
    }


    public static void fileResourceMapping(Request request, Response response) {
        HttpMethod method = request.getMethod();
        if (!HttpMethod.GET.equals(method))
            return;

        String urlOrg = request.getUrl();
        String[] urls = urlOrg.split("[?]");
        String url = urls[0];

        WebFile file = FILE_CACHING.get(url);
        if (file == null) {
            String s = FILE_RESOURCE_MAPPING.get(url);
            s = fillUrl(url, s, response);
            if (StringUtil.isBlank(s)) {
                removeResource(url);
                file = fromDisk(url);
                if (file == null) {
                    return;
                }
            } else {
                file = new WebFile(s);
                FILE_CACHING.put(url, file);
            }
        }

        if (!file.exists()) {
            removeResource(url);
            HttpStatue.$404.setResponse(response);
            return;
        }

        String stateCode = "200";
        //浏览器传来是否缓存校验数据唯一ID
        String eTag = request.getHeader("If-None-Match");
        ConcurrentHashMap<String, LocalDateTime> eTagCache = $304_CACHING.computeIfAbsent(url, k -> new ConcurrentHashMap<>());

        if (StringUtil.isEmpty(eTag)) {
            eTag = StringUtil.fillBrace("W/\"{}\"", UUIDUtil.randomUUID().toString2());
        }

        if (eTagCache.containsKey(eTag) && !file.isModify()) {
            stateCode = "304";
        }
        eTagCache.put(eTag, LocalDateTime.now());
        if (StringUtil.isEmpty(response.getStatue_code())) {
            response.setStatue_code(stateCode);
        }
        response
                .putHeaderContentType(file.getContentType())
                .putHeaderCROS()
                .setStatue_code(stateCode)
                .putHeader("ETag", eTag)
                .setAssemble(true)
                .setFile_body(file);

    }

    static void removeResource(String url) {
        $304_CACHING.remove(url);
        FILE_RESOURCE_MAPPING.remove(url);
    }

    public static void beanResourceMapping(Request request, Response response, boolean $throw) throws InvocationTargetException, IllegalAccessException {
        String url = request.getUrl();
        String[] urls = url.split("[?]");
        url = urls[0];

        Bean bean = BEAN_RESOURCE_MAPPING.get(url);
        try {

            if (bean != null) {
                ContentType requestCtTyp = ContentType.parse(request.getHeader("Content-Type"));

                //校验请求方法
                matchHttpMethod(bean, request);

                Object invokeObj = bean.getObj();
                Method invokeMethod = bean.getMethod();

                Parameter[] parameters = invokeMethod.getParameters();
                Object[] args = new Object[parameters.length];
                String[] paramName = bean.getMethodParamName();
                DataMap urlParam = request.getUrlParam();

                if (HttpMethod.GET.equals(request.getMethod())) {
                    //================================================================
                    //==       优先拼装 request/response 其次一个 其次属性名称           ==
                    //================================================================
                    if (parameters.length >= 1) {
                        for (int i = 0; i < parameters.length; i++) {
                            Type paramType = parameters[i].getParameterizedType();
                            String typeName = paramType.getTypeName();
                            int finalI = i;
                            if (Request.class.getName().equals(typeName)) {
                                args[i] = request;
                            } else if (Response.class.getName().equals(typeName)) {
                                args[i] = response;
                            } else if (parameters.length == 1 && urlParam.size() == 1) {
                                Set<String> keySet = urlParam.keySet();
                                Iterator<String> iterator = keySet.iterator();
                                args[i] = urlParam.get(iterator.next());
                            } else {
                                urlParam.forEach((k, v) -> {
                                    if (paramName[finalI].equals(k)) {
                                        args[finalI] = v;
                                    } else if (paramName[finalI].equalsIgnoreCase(k)) {
                                        args[finalI] = v;
                                    }
                                });
                            }
                        }
                    }
                } else if (HttpMethod.POST.equals(request.getMethod()) && ContentType.APPLICATION_FORM_URLENCODED.isSameMimeType(requestCtTyp)) {
                    //=======================================================================================
                    //==    表单数据拼装 属性名拼装           ==
                    //=======================================================================================
                    if (parameters.length >= 1) {
                        for (int i = 0; i < parameters.length; i++) {
                            Type paramType = parameters[i].getParameterizedType();
                            String typeName = paramType.getTypeName();
                            if (Request.class.getName().equals(typeName)) {
                                args[i] = request;
                            } else if (Response.class.getName().equals(typeName)) {
                                args[i] = request;
                            } else if (ClazzUtil.canValueof(paramType)) {
                                args[i] = new String(request.gainBody(), requestCtTyp.getCharset() == null ? StandardCharsets.UTF_8 : requestCtTyp.getCharset());
                            } else {
                                String body = new String(request.gainBody(), requestCtTyp.getCharset() == null ? StandardCharsets.UTF_8 : requestCtTyp.getCharset());
                                body = body.replaceAll("&", "\",\"").replaceAll("=", "\":\"");
                                body = URLDecoder.decode(body, requestCtTyp.getCharset() == null ? StandardCharsets.UTF_8 : requestCtTyp.getCharset());
                                body = FORM.replace("body", body);
                                args[i] = JSON.parseObject(body, paramType);
                            }
                        }

                    }
                } else if (HttpMethod.POST.equals(request.getMethod())) {
                    //=======================================================================================
                    //==   优先拼装 request/response > url名称属性&&基础属性 > baseParam(包含String) > Object   ==
                    //=======================================================================================
                    if (parameters.length >= 1) {
                        for (int i = 0; i < parameters.length; i++) {
                            Type paramType = parameters[i].getParameterizedType();
                            String typeName = paramType.getTypeName();
                            if (Request.class.getName().equals(typeName)) {
                                args[i] = request;
                            } else if (Response.class.getName().equals(typeName)) {
                                args[i] = request;
                            } else if (StringUtil.isNotBlank(urlParam.getString(paramName[i])) && ClazzUtil.canValueof(paramType)) {
                                args[i] = urlParam.getString(paramName[i]);
                            } else if (MultipartFile.class.getName().equals(typeName)) {
                                args[i] = request.getMultipartFile();
                            } else if (ClazzUtil.canValueof(paramType)) {
                                byte[] body = request.gainBody();
                                if (body == null)
                                    args[i] = null;
                                else
                                    args[i] = new String(body, requestCtTyp.getCharset() == null ? StandardCharsets.UTF_8 : requestCtTyp.getCharset());
                            } else {
                                byte[] body = request.gainBody();
                                if (body == null)
                                    args[i] = null;
                                else
                                    args[i] = JSON.parseObject(request.gainBody(), paramType);
                            }
                        }
                    }
                }
                //================================================================
                //==              执行目标方法                                    ==
                Object methodReturn = invokeMethod.invoke(invokeObj, args);
                //==                                                            ==
                //================================================================
                if (null != methodReturn)
                    if (ClazzUtil.canValueof(methodReturn.getClass())) {
                        response.setString_body((String) methodReturn)
                                .putHeaderContentType(ContentType.TEXT_HTML);
                    } else {
                        response
                                .setString_body(JSON.toJSONString(methodReturn))
                                .putHeaderContentType(ContentType.APPLICATION_JSON);
                    }
                else
                    response.putHeaderContentLength(0);

                response
                        .setStatue_code("200")
                        .setAssemble(true);
            }

        } catch (HttpException.MethodNotSupposedException e) {
            if ($throw) {
                throw e;
            }
            response
                    .setString_body("请求方法不支持")
                    .setStatue_code("400")
                    .putHeaderContentType(ContentType.TEXT_PLAIN)
                    .putHeaderDate(ZonedDateTime.now())
                    .setAssemble(true);

        } catch (Exception e) {
            if ($throw) {
                throw e;
            }
            response
                    .setString_body("呜呜，人家坏掉了")
                    .setStatue_code("500")
                    .putHeaderContentType(ContentType.TEXT_PLAIN)
                    .putHeaderDate(ZonedDateTime.now())
                    .setAssemble(true);
            log.error("", e);
        }

    }

    /**
     * index 重定向
     */
    static String fillUrl(String url, String s, Response response) {

        for (String fileSuffix : FILE_SUFFIX) {
            if (StringUtil.isNotBlank(s))
                return s;
            s = FILE_RESOURCE_MAPPING.get(url + fileSuffix);
            if (StringUtil.isNotBlank(s))
                url += fileSuffix;

            if (StringUtil.isNotBlank(s)) {
                continue;
            }
            s = FILE_RESOURCE_MAPPING.get(url + "/" + fileSuffix);
            if (StringUtil.isBlank(s)) {
                continue;
            }
            url = url + "/" + fileSuffix;
            if (url.indexOf("/") != 0)
                url = "/" + url;
            response
                    .setStatue_code("301")
                    .putHeaderRedirect(url);
        }
        return s;
    }

    static void matchHttpMethod(Bean bean, Request request) throws HttpException.MethodNotSupposedException {
        HttpMethod[] httpMethods = bean.getHttpMethods();
        HttpMethod method = request.getMethod();
        boolean $throws = true;
        for (HttpMethod httpMethod : httpMethods) {
            if (method.equals(httpMethod)) {
                $throws = false;
                break;
            }
        }
        if ($throws) throw new HttpException.MethodNotSupposedException("请求方法不支持！");
    }


    static WebFile fromDisk(String url) {
        for (String s : RootPathArray) {
            WebFile webFile = new WebFile(s + url);
            if (webFile.exists() && webFile.isFile() &&
                    //防越界。
                    webFile.getAbsolutePath().startsWith(s)) {
                return webFile;
            }
        }
        return null;
    }
}
