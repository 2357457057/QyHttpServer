package top.yqingyu.httpserver.compoment;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.httpserver.entity.Bean
 * @description
 * @createTime 2022年09月14日 20:53:00
 */
public class Bean {
    private Type type;
    private Object obj;
    private Method method;

    private String[] methodParamName;

    private String[] methodParamType;

    private HttpMethod[] httpMethods;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String[] getMethodParamName() {
        return methodParamName;
    }

    public void setMethodParamName(String[] methodParamName) {
        this.methodParamName = methodParamName;
    }

    public top.yqingyu.httpserver.compoment.HttpMethod[] getHttpMethods() {
        return httpMethods;
    }

    public void setHttpMethods(top.yqingyu.httpserver.compoment.HttpMethod[] httpMethods) {
        this.httpMethods = httpMethods;
    }

    public String[] getMethodParamType() {
        return methodParamType;
    }

    public void setMethodParamType(String[] methodParamType) {
        this.methodParamType = methodParamType;
    }

    @Override
    public String toString() {
        return "Bean{" +
                "type=" + type +
                ", obj=" + obj +
                ", method=" + method +
                ", methodParamName=" + Arrays.toString(methodParamName) +
                ", methodParamType=" + Arrays.toString(methodParamType) +
                ", httpMethods=" + Arrays.toString(httpMethods) +
                '}';
    }
}
