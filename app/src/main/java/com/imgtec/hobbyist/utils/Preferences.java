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


package com.imgtec.hobbyist.utils;

import android.content.SharedPreferences;

import javax.inject.Inject;

/**
 * Class that handles saving some persistent values to {@link android.content.SharedPreferences} object.
 */
public class Preferences {

  public static final String SETTINGS = "SETTINGS";

  public static final String EMAIL_CREDENTIAL = "EMAIL_CREDENTIAL";

  public static final String INTERACTIVE_MODE_HAS_STARTED_AT_LEAST_ONCE = "INTERACTIVE_MODE_HAS_STARTED_AT_LEAST_ONCE";

  public static final String WIFIRE_URL = "WIFIRE_URL";

  public static final String WIFIRE_URL_BOARD_WEBSERVICE_URL = "https://192.168.1.25/";

  public static final String DEVICE_SERVER_ACCESS_KEY = "DEVICE_SERVER_ACCESS_KEY";
  public static final String USER_NAME = "USER_NAME";
  public static final String DS_ACCESS_TOKEN = "DS_ACCESS_TOKEN";
  public static final String DS_ACCESS_TOKEN_EXPIRY_TIME = "DS_ACCESS_TOKEN_EXPIRY_TIME";
  public static final String DS_REFRESH_TOKEN = "DS_REFRESH_TOKEN";
  public static final String AUTOLOGIN = "AUTOLOGIN";

  private final SharedPreferences sharedPreferences;

  @Inject
  public Preferences(SharedPreferences sharedPreferences) {
    this.sharedPreferences = sharedPreferences;
  }


  /**
   * Saves a preference saying that user has entered interactive mode with a device.
   * This causes app to skip tour screens on following launches.
   */
  public void interactiveModeHasStartedAtLeastOnce() {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putBoolean(Preferences.INTERACTIVE_MODE_HAS_STARTED_AT_LEAST_ONCE, true);
    editor.apply();
  }

  /**
   * Saves user's email.
   */
  public void saveEmailCredential(String emailCredential) {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString(Preferences.EMAIL_CREDENTIAL, emailCredential);
    editor.apply();
  }

  public void saveUserName(String userName) {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString(USER_NAME, userName);
    editor.apply();
  }

  public String getUserName() {
    return sharedPreferences.getString(USER_NAME, "");
  }

  public void setAccessToken(String token) {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString(DS_ACCESS_TOKEN, token);
    editor.apply();
  }

  public String getAccessToken() {
    return sharedPreferences.getString(DS_ACCESS_TOKEN, "");
  }

  public void setRefreshToken(String token) {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString(DS_REFRESH_TOKEN, token);
    editor.apply();
  }

  public String getRefreshToken() {
    return sharedPreferences.getString(DS_REFRESH_TOKEN, "");
  }


  public void setAccessTokenExpiry(long millis) {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putLong(DS_ACCESS_TOKEN_EXPIRY_TIME, millis);
    editor.apply();
  }

  public long getAccessTokenExpiryTime() {
    return sharedPreferences.getLong(DS_ACCESS_TOKEN_EXPIRY_TIME, 0);
  }

  public void setAutologin(boolean isAutologin) {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putBoolean(AUTOLOGIN, isAutologin);
    editor.apply();
  }

  public boolean getAutologin() {
    return sharedPreferences.getBoolean(AUTOLOGIN, false);
  }
}
