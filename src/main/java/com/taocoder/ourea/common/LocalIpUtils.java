/*
* Copyright (c) 2015 taocoder.com. All Rights Reserved.
*/
package com.taocoder.ourea.common;

import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * @author tao.ke Date: 16/4/27 Time: 下午2:15
 */
public class LocalIpUtils {

  /**
   * 获取本地ip地址,如果多个,选择第一个
   *
   * @return
   */
  public static String getLocalIp() {

    try {
      for (Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements();) {

        NetworkInterface item = e.nextElement();

        for (InterfaceAddress address : item.getInterfaceAddresses()) {
          if (address.getAddress() instanceof Inet4Address) {
            Inet4Address inet4Address = (Inet4Address) address.getAddress();
            if (inet4Address.isLoopbackAddress()) {
              continue;
            }
            System.out.println(inet4Address.getHostAddress());
            return inet4Address.getHostAddress();
          }
        }

      }
    } catch (Exception e) {
      throw new IllegalStateException("no ip");
    }
    throw new IllegalStateException("no ip");
  }
}
