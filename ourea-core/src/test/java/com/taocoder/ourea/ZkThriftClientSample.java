/*
 * Copyright (c) 2015 taocoder.com. All Rights Reserved.
 */
package com.taocoder.ourea;

import com.taocoder.ourea.core.common.PropertiesUtils;
import com.taocoder.ourea.core.config.ThriftClientConfig;
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

        final Ourea.Iface client1 = factory.getProxyClient(Ourea.Iface.class,new ThriftClientConfig(),
                new ZkConfig(properties.getProperty("zkAddress")));



        long start = System.currentTimeMillis();

        new Thread(new Runnable() {
            @Override public void run() {
                int count = 0;
                try {
                    while (count++ < 1000) {
                        Thread.sleep(1000);
                        String a =  client.queryEcho("hello");
                        if (!a.equals("hellov1998")){
                            System.out.println("------998------error-----------------------------");
                            System.exit(0);
                        }
                    }
                }  catch (Exception E){
                    E.printStackTrace();
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override public void run() {
                int count = 0;
                try {
                    while (count++ < 1000) {
                        Thread.sleep(1000);
                        String a =  client1.queryEcho("hello");
                        if (!a.equals("hellov1999")){
                            System.out.println("----999--------error-----------------------------");
                            System.exit(0);
                        }
                    }
                }  catch (Exception E){
                    E.printStackTrace();
                }
            }
        }).start();

        System.out.println(System.currentTimeMillis() - start);
    }

}
