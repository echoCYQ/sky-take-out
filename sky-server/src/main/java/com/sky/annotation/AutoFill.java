package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解,用于标识某个方法需要进行字段自动填充处理
 */
//表示我们注解可以写在方法上面,其他地方不能写
@Target(ElementType.METHOD)
//表示着我们的注解可以再任意事情存在
// 如果写Source,那么只能在源码阶段存在,利用反射无法解析
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    //数据库操作类型 : UPDATE INSERT
    OperationType value();
}
