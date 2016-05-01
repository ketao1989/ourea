/*
 * Copyright (c) 2015 taocoder.com. All Rights Reserved.
 */
package com.taocoder.ourea;

import com.taocoder.ourea.core.common.PropertiesUtils;
import com.taocoder.ourea.core.config.ThriftServerConfig;
import com.taocoder.ourea.core.config.ZkConfig;
import com.taocoder.ourea.core.provider.ServiceProviderFactory;

import java.util.Properties;

/**
 * @author tao.ke Date: 16/4/25 Time: 下午2:27
 */
public class ZkThriftServerSample {

    public static void main(String[] args) {

        System.out.println(Ourea.Processor.class.getCanonicalName());

        Properties properties = PropertiesUtils.load("provider.properties");

        ThriftServerConfig config = new ThriftServerConfig(Integer.valueOf(properties.getProperty("port")));
        config.setGroup(properties.getProperty("group"));
        new ServiceProviderFactory().exposeService(new OureaImpl(), new ZkConfig(properties.getProperty("zkAddress")),
                config);

    }

}
