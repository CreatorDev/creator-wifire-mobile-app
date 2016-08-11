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
package com.imgtec.creator.iup.di.module;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.imgtec.creator.iup.App;
import com.imgtec.creator.iup.di.scope.ForApplication;
import com.imgtec.creator.iup.ds.DSService;
import com.imgtec.creator.iup.wifire.DeviceHelper;
import com.imgtec.creator.iup.utils.Preferences;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

  private App app;

  public AppModule(App app) {
    this.app = app;
  }

  @Provides
  @Singleton
  @Named("Settings")
  SharedPreferences provideSharedPreferences() {
    return app.getSettingsPreferences();
  }

  @Provides
  @ForApplication
  Context provideAppContext() {
    return app.getApplicationContext();
  }

  @Provides
  @Singleton
  @Named("UI")
  Handler provideUIHandler() {
    return new Handler(Looper.getMainLooper());
  }

  @Provides
  @Singleton
  Preferences providePreferences(@Named("Settings") SharedPreferences sharedPreferences) {
    return new Preferences(sharedPreferences);
  }

  @Provides
  @Singleton
  DSService provideDeviceServerCaller(Preferences preferences) {
    return new DSService("https://deviceserver.creatordev.io", preferences);
  }

  @Provides
  @Singleton
  DeviceHelper provideDeviceHelper() {
    return new DeviceHelper();
  }

  @Provides
  @Singleton
  ExecutorService provideExecutor() {
    return Executors.newFixedThreadPool(4);
  }


}
