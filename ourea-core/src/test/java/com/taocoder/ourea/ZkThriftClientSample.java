/*
 * Copyright (c) 2015 taocoder.com. All Rights Reserved.
 */
package com.taocoder.ourea;

import com.taocoder.ourea.core.common.PropertiesUtils;
import com.taocoder.ourea.core.config.ZkConfig;
import com.taocoder.ourea.core.consumer.ConsumerProxyFactory;

import java.util.Properties;

/**
 * @author tao.ke Date: 16/4/27 Time: 下午5:03
 */
public class ZkThriftClientSample {

    public static void main(String[] args) throws Exception {

        Properties properties = PropertiesUtils.load("consumer.properties");

        ConsumerProxyFactory factory = new ConsumerProxyFactory();

        final Ourea.Iface client = factory.getProxyClient(Ourea.Iface.class,
                new ZkConfig(properties.getProperty("zkAddress")));


        long start = System.currentTimeMillis();
        int count = 0;
                    while (count++ < 1000) {
                        Thread.sleep(1000);
                        System.out.println(client.queryEcho("hello")); ;
                    }


        System.out.println(System.currentTimeMillis() - start);
    }

}
