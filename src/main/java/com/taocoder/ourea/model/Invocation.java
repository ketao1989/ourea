/*
 * Copyright (c) 2015 ketao1989.github.com. All Rights Reserved.
 */
package com.taocoder.ourea.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * @author tao.ke Date: 16/3/3 Time: 下午3:40
 */
public class Invocation implements Serializable {

    private static final long serialVersionUID = 3518301260123528398L;

    private String interfaceName;

    private String methodName;

    private Class<?>[] parameterTypes;

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        Invocation that = (Invocation) o;

        if (!StringUtils.equals(methodName, that.getMethodName())) {
            return false;
        }

        int size = parameterTypes.length;

        for (int i = 0; i < size; i++) {
            if (!StringUtils.equals(parameterTypes[i].getCanonicalName(),
                    that.getParameterTypes()[i].getCanonicalName())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(methodName).append(parameterTypes).toHashCode();
    }

}
