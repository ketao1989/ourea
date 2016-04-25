/*
* Copyright (c) 2015 taocoder.com. All Rights Reserved.
*/
package com.taocoder.ourea;

import com.facebook.fb303.fb_status;

import org.apache.thrift.TException;

import java.util.Map;

/**
 * @author tao.ke Date: 16/4/25 Time: 下午2:25
 */
public class OureaImpl implements Ourea.Iface {

  @Override
  public String getName() throws TException {
    return "facebook-ourea-test";
  }

  @Override
  public String getVersion() throws TException {
    return "1.0.1";
  }

  @Override
  public fb_status getStatus() throws TException {
    return fb_status.ALIVE;
  }

  @Override
  public String getStatusDetails() throws TException {
    return null;
  }

  @Override
  public Map<String, Long> getCounters() throws TException {
    return null;
  }

  @Override
  public long getCounter(String key) throws TException {
    return 0;
  }

  @Override
  public void setOption(String key, String value) throws TException {

  }

  @Override
  public String getOption(String key) throws TException {
    return null;
  }

  @Override
  public Map<String, String> getOptions() throws TException {
    return null;
  }

  @Override
  public String getCpuProfile(int profileDurationInSec) throws TException {
    return null;
  }

  @Override
  public long aliveSince() throws TException {
    return 0;
  }

  @Override
  public void reinitialize() throws TException {

  }

  @Override
  public void shutdown() throws TException {

  }

  @Override
  public String queryEcho(String request) throws TException {
    return request;
  }
}
