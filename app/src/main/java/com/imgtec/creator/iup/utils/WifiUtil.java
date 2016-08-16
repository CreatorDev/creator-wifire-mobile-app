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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Couple of Wifi utils.
 */
public class WifiUtil {

  private static final String TAG = "WifiUtil";
  private static final Logger LOGGER = LoggerFactory.getLogger(WifiUtil.class);

  private final Object lock = new Object();
  private final WifiManager wifiManager;
  private final Context appContext;
  private int originalNetworkId;

  WifiUtil(Context appContext) {
    this.appContext = appContext;
    wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
  }

  public static WifiUtil create(@NonNull Context applicationContext) {
    return new WifiUtil(applicationContext);
  }

  /**
   * @return current Wifi's SSID without embracing apostrophe character.
   */
  public String getCurrentWifiSSID() {
    WifiInfo wifiInfo = wifiManager.getConnectionInfo(); // WiFiInfo object always needs re-initialization
    return (wifiInfo.getSSID() == null) ? null : wifiInfo.getSSID().replace("\"", "");
  }

  /**
   * Saves or updates WEP network of specified ssid and password and tries to connect to it.
   * May result in asynchronous events being delivered.
   *
   * @param ssid     of network
   * @param password of network
   * @return true if network is saved or updated successfully, false otherwise. The latter may happen on Android > 5 due to unsufficient permissions.
   */
  public boolean connectToWepNetwork(String ssid, String password) {

    setOriginalNetworkId(wifiManager.getConnectionInfo().getNetworkId());
    wifiManager.setWifiEnabled(true);
    int networkId = getConfiguredNetworkId(ssid);
    if (networkId == -1) {
      // there is network with specified ssid saved. Create new one and save it.
      WifiConfiguration wifiConfiguration = createWifiConfiguration(ssid, password);
      networkId = wifiManager.addNetwork(wifiConfiguration);
      wifiManager.saveConfiguration();
    } else {
      // there already is a network with specified ssid. Try to update it with new password.
      WifiConfiguration wifiConfiguration = new WifiConfiguration();
      wifiConfiguration.networkId = networkId;
      wifiConfiguration.wepKeys[0] = password;
      int canUpdate = wifiManager.updateNetwork(wifiConfiguration);
      // if network cannot be updated due to app not having permissions (since Android 6)
      // return false
      if (canUpdate == -1) {
        return false;
      }
      wifiManager.saveConfiguration();
      networkId = getConfiguredNetworkId(ssid);
    }
    //wifiManager.disconnect();
    return wifiManager.enableNetwork(networkId, true);
    //wifiManager.reconnect();
    //return true;
  }

  public void resetConnection() {
    wifiManager.disconnect();
    wifiManager.enableNetwork(getOriginalNetworkId(), true);
    wifiManager.reconnect();
  }

  private WifiConfiguration getConfiguredNetwork(String ssid) {
    List<WifiConfiguration> savedConfigurations = wifiManager.getConfiguredNetworks();
    for (WifiConfiguration configuration : savedConfigurations) {
      if (configuration.SSID.equals("\"" + ssid + "\"")) {
        return configuration;
      }
    }
    return null;
  }

  private int getConfiguredNetworkId(String ssid) {
    WifiConfiguration configuration = getConfiguredNetwork(ssid);
    if (configuration == null) {
      return -1;
    }
    return configuration.networkId;
  }

  /**
   * Every board is based on WEP, therefore we use WEP over here. There is possibility to extend it in the future.
   */
  private WifiConfiguration createWifiConfiguration(String ssid, String password) {
    WifiConfiguration wifiConfiguration = new WifiConfiguration();
    wifiConfiguration.SSID = "\"" + ssid + "\""; //This should be in Quotes!!
    wifiConfiguration.wepKeys[0] = password; //This is the WEP Password
    wifiConfiguration.hiddenSSID = true;
    wifiConfiguration.status = WifiConfiguration.Status.DISABLED;
    wifiConfiguration.priority = 40;
    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
    wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
    wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
    wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
    wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
    wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
    wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
    wifiConfiguration.wepTxKeyIndex = 0;
    return wifiConfiguration;
  }

  /**
   * Starts scan for boards' wifi networks. When done, results are returned to called via callback.
   */
  public void requestBoardWifiList(WifiScanListener wifiScanListener) {
    startWifiScan(wifiScanListener, true);
  }

  /**
   * Starts scan for wifi networks. When done, results are returned to called via callback.
   */
  public void requestAvailableWifiList(WifiScanListener wifiScanListener) {
    startWifiScan(wifiScanListener, false);
  }

  private void startWifiScan(WifiScanListener wifiScanListener, boolean boardsOnly) {
    LOGGER.debug("startWifiScan, boardsOnly : {}", boardsOnly);
    WifiScanReceiver wifiScanReceiver = new WifiScanReceiver(
        appContext,
        wifiManager,
        new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION),
        wifiScanListener,
        boardsOnly);
    wifiScanReceiver.register(appContext);
    wifiManager.startScan();
  }

  /**
   * For checking WiFi connection. It is a matter if there is WiFi connection.
   */
  public boolean isWifiConnected() {
    NetworkInfo netInfo;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      netInfo = getWifiNetworkInfo();
    } else {
      netInfo = getNetworkInfo();
    }

    return netInfo != null && netInfo.isConnected() && netInfo.getType() == ConnectivityManager.TYPE_WIFI;
  }

  /**
   * For checking Internet connection. Doesn't matter if WiFi or if 3G.
   */
  public boolean isInternetConnected() {
    NetworkInfo netInfo = getNetworkInfo();
    return netInfo != null && netInfo.isConnected();
  }

  /**
   * For checking if connection is board's WiFi one.
   * May not work in case of normal network with ssid same as board's.
   */
  public boolean isBoardConnected() {
    if (isWifiConnected()) {
      WifiInfo wifiInfo = wifiManager.getConnectionInfo();
      final String bssid = wifiInfo.getBSSID();
      return bssid != null && bssid.startsWith(Constants.BOARD_MAC_ADDRESS_PREFIX);
    } else {
      return false;
    }
  }

  /**
   * For checking if current Internet connection is not board's WiFi one.
   */
  public boolean isInternetNotBoardConnected() {
    return isInternetConnected() && !isBoardConnected();
  }

  /**
   * For checking if current WiFi connection is not board's WiFi one.
   */
  public boolean isWifiNotBoardConnected() {
    return isWifiConnected() && !isBoardConnected();
  }

  /**
   * Enable chosen ssid's network.
   *
   * @param ssid chosen ssid
   * @return if success
   */
  public boolean enableChosenNetwork(String ssid) {
    for (WifiConfiguration wifiConfiguration : wifiManager.getConfiguredNetworks()) {
      if (ssid.equals(wifiConfiguration.SSID)) {
        return wifiManager.enableNetwork(wifiConfiguration.networkId, true);
      }
    }
    return false;
  }

  public NetworkInfo getNetworkInfo() {
    ConnectivityManager conMan = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    return conMan.getActiveNetworkInfo();
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public NetworkInfo getWifiNetworkInfo() {
    ConnectivityManager conMan = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    for (Network network : conMan.getAllNetworks()) {
      NetworkInfo networkInfo = conMan.getNetworkInfo(network);
      if (networkInfo != null && conMan.getNetworkInfo(network).getType() == ConnectivityManager.TYPE_WIFI) {
        return networkInfo;
      }
    }
    return null;
  }

  void setOriginalNetworkId(int networkId) {
    synchronized (lock) {
      this.originalNetworkId = networkId;
    }
  }

  int getOriginalNetworkId() {
    synchronized (lock) {
      return originalNetworkId;
    }
  }

  private static class WifiScanReceiver extends BroadcastReceiverWithRegistrationState {

    private Context context;
    private WifiManager wifiManager;
    private WifiScanListener wifiScanListener;
    private boolean boardsOnly;

    public WifiScanReceiver(Context appContext,
                            WifiManager wifiManager,
                            IntentFilter intentFilter,
                            WifiScanListener wifiScanListener,
                            boolean boardsOnly) {
      super(intentFilter);
      this.context = appContext;
      this.wifiManager = wifiManager;
      this.wifiScanListener = wifiScanListener;
      this.boardsOnly = boardsOnly;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      LOGGER.debug("WifiScanReceiver#onReceive, intent : {}", intent);
      List<String> result = new ArrayList<>();
      try {
        if (boardsOnly) {
          LOGGER.debug("show boards only");
          for (ScanResult wifi : wifiManager.getScanResults()) {
            LOGGER.debug("wifi ssid: {}, bssid: {}", wifi.SSID, wifi.BSSID);
            if (wifi.BSSID.toLowerCase(Locale.US).startsWith(Constants.BOARD_MAC_ADDRESS_PREFIX)) {
              LOGGER.debug("This wifi is a board wifi and will be added to result.");
              result.add(wifi.SSID);
            }
          }
        } else {
          LOGGER.debug("show all wifis");
          for (ScanResult wifi : wifiManager.getScanResults()) {
            LOGGER.debug("wifi ssid: {}, bssid: {}", wifi.SSID, wifi.BSSID);
            result.add(wifi.SSID);
          }
        }
      } finally {
        LOGGER.debug("wifiScanListener != null : {}", wifiScanListener != null);
        if (wifiScanListener != null) {
          wifiScanListener.onWifiScanCompleted(result);
        }
        this.unregister(context);
        wifiManager = null;
        wifiScanListener = null;
      }
    }
  }

  public interface WifiScanListener {
    void onWifiScanCompleted(List<String> wifiSSIDs);
  }

  public WifiInfo getWiFiInfo() {
    return wifiManager.getConnectionInfo();
  }

}
