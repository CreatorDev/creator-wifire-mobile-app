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

import java.util.ArrayList;
import java.util.List;

/**
 * Constant values used across the app.
 */
public class Constants {

  public static boolean WIFIRE_BOARD_REQUESTS_MODE = true; //false for testing on apiary mocks
  public static int DEFAULT_MAXIMUM_FIELD_CHARACTERS_COUNT = 32;
  public static final int WEP_64_BIT_SECRET_KEY_HEXADECIMAL_LENGTH = 10; //Expected length of Wep key
  public static final int MIN_WIFI_PASSWORD_LENGTH = 8; // min length of wifi password
  public static final int MAX_WIFI_PASSWORD_LENGTH = 64; // min length of wifi password
  public static final int CREATOR_ACCOUNT_MINIMUM_CHARACTERS_COUNT = 5;
  /**
   * All WiFire boards have MAC addresses beginning with this prefix.
   */
  public static final String BOARD_MAC_ADDRESS_PREFIX = "00:1e:c0"; //board's MAC address must begin with this number
  public static final List<String> DEVICE_TYPES = new ArrayList<>();

  static {
    DEVICE_TYPES.add("WiFire");
  }

  public static final int TWO_SECONDS_MILLIS = 2000;
  public static final int THIRTY_SECONDS_MILLIS = 30000;
  public static final int SIXTY_SECONDS_MILLIS = 60000;

  public static String CREATOR_TROUBLESHOOTING_URL = "http://flow.imgtec.com/developers/help/wifire/trouble-shooting";
}
