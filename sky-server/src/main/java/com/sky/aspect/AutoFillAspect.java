package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面,实现公共字段自动填充处理
 */
@Aspect  //这是Spring AOP的注解，表示这个类是一个切面（Aspect）。切面是AOP的核心概念，用于封装横切关注点，比如日志、安全等
@Component //这是Spring的注解，用于将类标记为Bean，那么Spring就会将其加入到应用上下文中，实现自动化管理。
@Slf4j //这是Lombok项目提供的注解，用于在类中自动生成一个名称为log的slf4j日志对象，它使得日志记录操作变得非常简洁
public class AutoFillAspect { //定义一个类AutoFillAspect，自定义它的责任就是作为一个切面，执行特定的AOP任务。
    /**
     * 切入点
     */
    //切入点表达式 : *拦截所有 com.sky.mapper 这个包下 * 所有的类 * 所有的方法 (..) 所有的参数类型
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    //表示只拦截标记了@AutoFill注解的方法。这样我们就可以精确控制什么样的方法需要进行公共字段的自动填充
    public void autoFillPointCut(){

    }

    /**
     * 前置通知,在通知中进行公共字段的赋值
     * @param joinPoint
     */
    @Before("autoFillPointCut()") //是一个前置通知，表明这个方法需要在被拦截的方法执行前被调用
    //通知（Advice）：在切面的某个特定的连接点上执行的动作。有许多类型的通知，包括“around”、“before”和“after”通知。
    public void autoFill(JoinPoint joinPoint) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException { //joinPoint 连接点对象
        //表示在程序执行过程中某个特定的“点”，相当于程序的运行时状态。--  接下来的代码主要是按照不同的数据库操作类型，使用反射给实体的相应字段进行赋值
        log.info("开始进行公共字段自动填充...");

        //获取到当前被拦截的方法上的数据库操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature(); //方法签名对象
        //这行代码获取被拦截方法的签名对象，并且将其转换为MethodSignature类型。它可以用来获取被拦截方法的各种信息，如方法参数、访问修饰符、返回类型等。
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获得方法上的注解
        //词句通过方法签名获取方法上的@AutoFill注解实例。
        OperationType operationType = autoFill.value();//获得数据库操作类型
        //从@AutoFill获取操作类型，这个类型用于判断是插入操作还是更新操作。

        //获取到当前被拦截的方法参数--实体对象
        Object[] args = joinPoint.getArgs();
        //获取被拦截方法的参数列表。如果方法没有参数，那就没有必要进行后续的自动填充操作，方法直接返回
        if (args == null || args.length==0) return;
        Object entity = args[0];
        // 默认情况下，我们认为拦截的方法的第一个参数是需要被填充的实体对象。

        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
        //BaseContext类通常用于保存线程局部的用户状态信息，如当前用户的ID。这种机制通常是通过ThreadLocal类的实例实现的，确保在多线程环境中为每个线程保留独立的用户信息。

        //根据当前不同的操作类型,为对应的属性通过反射来赋值
        if (operationType == OperationType.INSERT){
            //为4个公共字段赋值
            Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
            // 这段代码是使用反射API获取并调用setCreateTime这个方法，并将当前的时间设定为其参数。这样，就完成了创建时间的自动填充
            Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            //通过反射为对象属性赋值
            setCreateTime.invoke(entity,now);
            setCreateUser.invoke(entity,currentId);
            setUpdateTime.invoke(entity,now);
            setUpdateUser.invoke(entity,currentId);

        }else if(operationType == OperationType.UPDATE){
            //为2个公共字段赋值
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            //通过反射为对象属性赋值
            setUpdateTime.invoke(entity,now);
            setUpdateUser.invoke(entity,currentId);
        }

    }

}
