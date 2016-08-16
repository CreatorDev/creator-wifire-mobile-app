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


package com.imgtec.creator.iup.fragments.navigationdrawer;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import com.imgtec.creator.iup.R;

/**
 * Enumeration which encapsulates menu items for NavigationDrawer.
 * Order is important - it's order of menu items in UI.
 */
public enum NDMenuItem {

  UserName(R.drawable.ic_account_circle_black_24dp),
  MyDevice(R.string.wifire, R.drawable.ic_developer_board_black_24dp),
  Commands(R.string.commands, R.drawable.ic_touch_app_black_24dp),
  ConfigureWifi(R.string.configure_wifi, R.drawable.ic_wifi_black_24dp),
  Separator(),
  ConnectedDevices(R.string.connected_devices, R.drawable.ic_devices_black_24dp),
  SetupDevice(R.string.setup_device, R.drawable.ic_phonelink_setup_black_24dp),
  About(R.string.about, R.drawable.ic_info_outline_black_24dp),
  SignOut(R.string.log_out, R.drawable.ic_exit_to_app_black_24dp);

  private final int textId;
  private final int iconId;
  private static NDMenuItem checkedItem = NDMenuItem.ConnectedDevices;

  NDMenuItem(@StringRes int textId, @DrawableRes int iconId) {
    this.textId = textId;
    this.iconId = iconId;
  }

  NDMenuItem(@DrawableRes int iconId) {
    textId = -1;
    this.iconId = iconId;
  }


  NDMenuItem() {
    textId = -1;
    iconId = -1;
  }

  public int getTextId() {
    return textId;
  }

  public int getIconId() {
    return iconId;
  }

  public boolean isMyDevice() {
    return (this == MyDevice);
  }

  public boolean isUserName() {
    return (this == UserName);
  }

  public boolean isSeparator() {
    return (this == Separator);
  }

  public static NDMenuItem getCheckedItem() {
    return NDMenuItem.checkedItem;
  }

  public static void setCheckedItem(NDMenuItem checkedItem) {
    NDMenuItem.checkedItem = checkedItem;
  }

  public static boolean isChecked(NDMenuItem menuItem) {
    return menuItem == checkedItem;
  }

  public static NDMenuItem[] initialValues() {
    return new NDMenuItem[]{UserName, ConnectedDevices, SetupDevice, About, SignOut};
  }

  public static NDMenuItem[] wifiNetworkModeValues() {
    return new NDMenuItem[]{UserName, Separator, MyDevice, ConfigureWifi, Separator,
        ConnectedDevices, SetupDevice, About, SignOut};
  }

  public static NDMenuItem[] interactiveModeValues() {
    return new NDMenuItem[]{UserName, Separator, MyDevice, Commands, Separator,
        ConnectedDevices, SetupDevice, About, SignOut};
  }


}
