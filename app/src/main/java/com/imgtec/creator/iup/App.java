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
package com.imgtec.creator.iup;


import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.imgtec.creator.iup.di.component.AppComponent;
import com.imgtec.creator.iup.di.component.DaggerAppComponent;
import com.imgtec.creator.iup.di.module.AppModule;
import com.imgtec.creator.iup.retrofit.OkHttpsClient;
import com.imgtec.creator.iup.retrofit.SoftAPRetrofitService;
import com.imgtec.creator.iup.utils.Preferences;
import com.imgtec.creator.iup.utils.SetupGuideInfoSingleton;
import com.imgtec.creator.iup.utils.WifiObserver;
import com.imgtec.creator.iup.utils.WifiUtil;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.net.SocketFactory;

import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;


public class App extends Application implements WifiObserver.WiFiConnectionListener {

  private static App app;

  private AppComponent appComponent;

  private SharedPreferences settingsPreferences;

  private SoftAPRetrofitService softAPRetrofitService;
  private WifiUtil wifiUtil;
  private final ConnectionBroadcastReceiver connectionBroadcastReceiver =
      new ConnectionBroadcastReceiver();
  private Set<WifiStateListener> wifiStateListeners = new CopyOnWriteArraySet<>();


  public interface WifiStateListener {
    void onWiFiConnected(NetworkInfo networkInfo);

    void onWiFiDisconnected();
  }

  public static App get() {
    return app;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    app = this;
    appComponent = DaggerAppComponent.builder()
        .appModule(new AppModule(this))
        .build();

    settingsPreferences = getSharedPreferences(Preferences.SETTINGS, Context.MODE_PRIVATE);

    wifiUtil = WifiUtil.create(getApplicationContext());
    WifiObserver wifiObserver = WifiObserver.create(this);
    wifiObserver.addListener(this);
    if (wifiUtil.isWifiNotBoardConnected()) {
      SetupGuideInfoSingleton.setSsid(wifiUtil.getCurrentWifiSSID());
    }
    getApplicationContext().registerReceiver(connectionBroadcastReceiver,
        new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    createRetrofit(null);

  }

  public SharedPreferences getSettingsPreferences() {
    return settingsPreferences;
  }

  public AppComponent getAppComponent() {
    return appComponent;
  }

  public synchronized SoftAPRetrofitService getSoftAPRetrofitService() {
    return softAPRetrofitService;
  }

  public WifiUtil getWifiUtil() {
    return wifiUtil;
  }

  private synchronized void createRetrofit(SocketFactory socketFactory) {
    SharedPreferences sharedPreferences = getSharedPreferences(Preferences.SETTINGS, Context.MODE_PRIVATE);

    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(sharedPreferences.getString(Preferences.WIFIRE_URL, Preferences.WIFIRE_URL_BOARD_WEBSERVICE_URL))
        .addConverterFactory(SimpleXmlConverterFactory.create())
        .client(OkHttpsClient.newOkHttpsClient(socketFactory))
        .build();

    softAPRetrofitService = retrofit.create(SoftAPRetrofitService.class);

  }

  public void addWifiConnectionListener(WifiStateListener listener) {
    wifiStateListeners.add(listener);
  }

  public void removeWifiConnectionListener(WifiStateListener listener) {
    wifiStateListeners.remove(listener);
  }

  @Override
  public void onWiFiConnected(NetworkInfo networkInfo, SocketFactory socketFactory) {
    createRetrofit(socketFactory);
    for (WifiStateListener listener : wifiStateListeners) {
      listener.onWiFiConnected(networkInfo);
    }
  }

  @Override
  public void onWiFiDisconnected() {
    for (WifiStateListener listener : wifiStateListeners) {
      listener.onWiFiDisconnected();
    }
  }

  class ConnectionBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (wifiUtil.isWifiNotBoardConnected()) {
        SetupGuideInfoSingleton.setSsid(wifiUtil.getCurrentWifiSSID());
      }
    }
  }
}
