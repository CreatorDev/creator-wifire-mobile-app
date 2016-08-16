/*
 * Copyright (c) 2016, Imagination Technologies Limited and/or its affiliated group companies
 * and/or licensors
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions
 *     and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 *     conditions and the following disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 *     endorse or promote products derived from this software without specific prior written
 *     permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */


package com.imgtec.creator.iup.utils;

/**
 * Class used during device setup process. It stores information about device and its setup progress.
 */
public class SetupGuideInfoSingleton {

  private static String lastOpenedLoggedInAppSSID;
  private static String boardSSID;
  private static String deviceName;
  private static String pskIdentity;
  private static String pskSecret;
  private static String bootstrapUrl;

  private SetupGuideInfoSingleton() {
  }


  public static synchronized String getSsid() {
    return lastOpenedLoggedInAppSSID;
  }

  public static synchronized void setSsid(String ssid) {
    lastOpenedLoggedInAppSSID = ssid;
  }

  public static synchronized String getBoardSsid() {
    return boardSSID;
  }

  public static synchronized void setBoardSsid(String bSSID) {
    boardSSID = bSSID;
  }

  public static synchronized String getDeviceName() {
    return deviceName;
  }

  public static synchronized void setDeviceName(String name) {
    deviceName = name;
  }

  public static String getPskSecret() {
    return pskSecret;
  }

  public static void setPskSecret(String pskSecret) {
    SetupGuideInfoSingleton.pskSecret = pskSecret;
  }

  public static String getPskIdentity() {
    return pskIdentity;
  }

  public static void setPskIdentity(String pskIdentity) {
    SetupGuideInfoSingleton.pskIdentity = pskIdentity;
  }

  public static void setBootstrapUrl(String bootstrapUrl) {
    SetupGuideInfoSingleton.bootstrapUrl = bootstrapUrl;
  }

  public static String getBootstrapUrl() {
    return bootstrapUrl;
  }
}
