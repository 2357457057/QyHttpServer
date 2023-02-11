package top.yqingyu.httpserver.annotation;

import top.yqingyu.httpserver.common.HttpMethod;

import java.lang.annotation.*;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.httpserver.annotation.QyController
 * @description
 * @createTime 2022年09月14日 11:26:00
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface QyController {

    HttpMethod[] method() default {HttpMethod.POST};

    String path() default "";

    //TODO 待支持
    boolean restful() default false;
    //跨域
    boolean cors() default false;

}
