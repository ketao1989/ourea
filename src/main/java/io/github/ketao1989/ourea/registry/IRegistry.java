/*
 * Copyright (c) 2015 ketao1989.github.com. All Rights Reserved.
 */
package io.github.ketao1989.ourea.registry;

/**
 * @author tao.ke Date: 16/3/3 Time: 下午11:37
 */
public interface IRegistry {

    /**
     * 注册服务，比如：提供者地址，消费者地址，路由规则，覆盖规则，等数据。
     *
     */
    void register(String info);

    /**
     * 取消注册.
     */
    void unRegister(String info);

    /**
     * client订阅相关服务
     * 
     * @param info
     */
    void subscribe(String info);

    /**
     * client取消订阅服务
     * 
     * @param info
     */
    void unSubscribe(String info);

}
