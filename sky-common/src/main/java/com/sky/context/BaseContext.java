package com.sky.context;

public class BaseContext {

    // 创建一个ThreadLocal变量来存储类型为Long的对象
    // ThreadLocal 为每个线程提供一个线程局部（即独有）的变量副本
    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    // 提供一个公共静态方法来设置当前线程的ID
    // 该方法接受一个Long类型的ID，将其绑定到当前线程
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    // 提供一个公共静态方法来获取当前线程的ID
    // 如果当前线程先前没有设置ID，将会返回null
    public static Long getCurrentId() {
        return threadLocal.get();
    }

    // 提供一个公共静态方法来移除当前线程的ID
    // 它将清除当前线程保存在ThreadLocal中的ID值
    // 这是个好习惯，因为它可以帮助避免内存泄露问题
    public static void removeCurrentId() {
        threadLocal.remove();
    }

}