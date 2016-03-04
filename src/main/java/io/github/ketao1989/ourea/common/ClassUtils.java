/*
* Copyright (c) 2015 ketao1989.github.com. All Rights Reserved.
*/
package io.github.ketao1989.ourea.common;

import org.apache.thrift.TProcessor;

import java.lang.reflect.Constructor;

/**
 * @author tao.ke Date: 16/3/4 Time: 下午5:37
 */
public class ClassUtils {

    public static Class<?> getIface(Class<?> clazz){

            Class<?>[] classes = clazz.getClasses();
            for (Class c : classes)
                if (c.isMemberClass() && c.isInterface() && c.getSimpleName().equals("Iface")) {
                    return c;
                }
            throw new IllegalArgumentException("clazz is not thrift class.");
    }

    public static Constructor<TProcessor> getProcessorConstructorIface(Class<?> clazz) {
        try {
            return getProcessorClass(clazz).getConstructor(getIface(clazz));
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("serviceInterface must contain Sub Class of Processor with Constructor(Iface.class)");
        }
    }


    private static Class<TProcessor> getProcessorClass(Class<?> clazz) {
        Class<?>[] classes = clazz.getClasses();
        for (Class c : classes)
            if (c.isMemberClass() && !c.isInterface() && c.getSimpleName().equals("Processor")) {
                return c;
            }
        throw new IllegalArgumentException("serviceInterface must contain Sub Interface of Processor");
    }
}
