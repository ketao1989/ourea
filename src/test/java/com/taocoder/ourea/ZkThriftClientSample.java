/*
 * Copyright (c) 2015 taocoder.com. All Rights Reserved.
 */
package com.taocoder.ourea;

import java.util.Properties;

import com.taocoder.ourea.client.ConsumerProxyFactory;
import com.taocoder.ourea.common.PropertiesUtils;
import com.taocoder.ourea.config.ZkConfig;

/**
 * @author tao.ke Date: 16/4/27 Time: 下午5:03
 */
public class ZkThriftClientSample {

    public static void main(String[] args) throws Exception {

        Properties properties = PropertiesUtils.load("consumer.properties");

        Ourea.Iface client = ConsumerProxyFactory.getProxyClient(Ourea.Iface.class,
                new ZkConfig(properties.getProperty("zkAddress")));
        int count = 0;
        long start = System.currentTimeMillis();
        while (count++ < 1000) {
            System.out.println(count + "-----" + client.queryEcho("hello"));
        }
        System.out.println(System.currentTimeMillis() - start);
    }

}
