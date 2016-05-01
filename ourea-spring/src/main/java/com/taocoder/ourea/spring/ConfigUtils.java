/*
* Copyright (c) 2015 taocoder.com. All Rights Reserved.
*/
package com.taocoder.ourea.spring;

import com.taocoder.ourea.core.config.ZkConfig;
import org.apache.commons.lang3.StringUtils;

import java.util.Properties;

/**
 * @author tao.ke Date: 16/5/1 Time: 下午6:48
 */
public class ConfigUtils {

    public static ZkConfig buildConfig(Properties properties){

        assertEmpty("zkAddress", properties.getProperty("zkAddress"));
        ZkConfig zkConfig = new ZkConfig(properties.getProperty("zkAddress"));

        if (StringUtils.isNoneEmpty(properties.getProperty("baseSleepTimeMs"))){
            zkConfig.setBaseSleepTimeMs(Integer.parseInt(properties.getProperty("baseSleepTimeMs")));
        }

        if (StringUtils.isNoneEmpty(properties.getProperty("maxRetries"))){
            zkConfig.setMaxRetries(Integer.parseInt(properties.getProperty("maxRetries")));
        }

        if (StringUtils.isNoneEmpty(properties.getProperty("maxSleepTimeMs"))){
            zkConfig.setMaxSleepTimeMs(Integer.parseInt(properties.getProperty("maxSleepTimeMs")));
        }

        if (StringUtils.isNoneEmpty(properties.getProperty("zkTimeout"))){
            zkConfig.setZkTimeout(Integer.parseInt(properties.getProperty("zkTimeout")));
        }

        return zkConfig;
    }

    public static void assertEmpty(String key, String val) {
        if (StringUtils.isBlank(val)) {
            throw new IllegalArgumentException(key + "value do not can be empty.");
        }
    }
}
