/*
 * Copyright (c) 2015 ketao1989.github.com. All Rights Reserved.
 */
package io.github.ketao1989.ourea.common;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.Map;

/**
 * @author tao.ke Date: 16/3/4 Time: 下午4:02
 */
public class PathUtils {

    public static String buildPath(String pathInfo) {

        Map<String, String> registerInfo = Splitter.on('&').withKeyValueSeparator('=').split(pathInfo);

        // create node
        String parentPath = Joiner.on(Constants.PATH_SEPARATOR)
                .join(Lists.newArrayList(Constants.ZK_PATH_PREFIX, registerInfo.get(Constants.INTERFACE_KEY),
                        registerInfo.get(Constants.GROUP_KEY), registerInfo.get(Constants.VERSION_KEY),
                        registerInfo.get(Constants.INVOKER_KEY)));

        return parentPath;
    }
}
