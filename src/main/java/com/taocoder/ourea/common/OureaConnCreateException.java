/*
 * Copyright (c) 2015 taocoder.com. All Rights Reserved.
 */
package com.taocoder.ourea.common;

/**
 * 专门针对创建连接失败的时候,抛出去的异常.这种异常处理,外部将对应的服务连接拿掉,不在参与到负载均衡上来
 * 
 * @author tao.ke Date: 16/4/28 Time: 下午11:09
 */
public class OureaConnCreateException extends RuntimeException {

    public OureaConnCreateException(String message) {
        super(message);
    }
}
