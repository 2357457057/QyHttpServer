package top.yqingyu.httpserver.annotation;

import java.lang.annotation.*;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.httpserver.annotation.PathValue
 * @description
 * @createTime 2022年09月15日 22:51:00
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Target(ElementType.PARAMETER)
public @interface PathValue {
    String name();
}
