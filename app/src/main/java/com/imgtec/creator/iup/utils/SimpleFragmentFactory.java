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

import android.support.v4.app.Fragment;

import com.imgtec.creator.iup.fragments.loginsignup.LogInOrSignUpFragment;
import com.imgtec.creator.iup.fragments.menu.AboutFragment;
import com.imgtec.creator.iup.fragments.menu.ConnectedDevicesFragment;
import com.imgtec.creator.iup.fragments.menu.CreatorDeviceInfoFragment;
import com.imgtec.creator.iup.fragments.menu.DeviceInfoFragment;
import com.imgtec.creator.iup.fragments.menu.InteractiveFragment;
import com.imgtec.creator.iup.fragments.menu.setupguide.ConnectingFragment;
import com.imgtec.creator.iup.fragments.menu.setupguide.FetchCertificateFragment;
import com.imgtec.creator.iup.fragments.menu.setupguide.LogInToWifiFragment;
import com.imgtec.creator.iup.fragments.menu.setupguide.LoginToDeviceFragment;
import com.imgtec.creator.iup.fragments.menu.setupguide.NetworkChoiceFragment;
import com.imgtec.creator.iup.fragments.menu.setupguide.SetUpWifireDeviceFragment;
import com.imgtec.creator.iup.fragments.menu.setupguide.SetupModeFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple factory creating fragments.
 */
public class SimpleFragmentFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleFragmentFactory.class);

  public static Fragment createFragment(final String tag) {
    Fragment fragment;
    switch (tag) {
      case SetUpWifireDeviceFragment.TAG:
        fragment = SetUpWifireDeviceFragment.newInstance();
        break;
      case SetupModeFragment.TAG:
        fragment = SetupModeFragment.newInstance();
        break;
      case LoginToDeviceFragment.TAG:
        fragment = LoginToDeviceFragment.newInstance();
        break;
      case LogInToWifiFragment.TAG:
        fragment = LogInToWifiFragment.newInstance();
        break;
      case ConnectingFragment.TAG:
        fragment = ConnectingFragment.newInstance();
        break;
/**----------------------------------------------------------------------------------*/
      case AboutFragment.TAG:
        fragment = AboutFragment.newInstance();
        break;
      case DeviceInfoFragment.TAG:
        fragment = DeviceInfoFragment.newInstance();
        break;
      case CreatorDeviceInfoFragment.TAG:
        fragment = CreatorDeviceInfoFragment.newInstance();
        break;
      case LogInOrSignUpFragment.TAG:
        fragment = LogInOrSignUpFragment.newInstance();
        break;
      case ConnectedDevicesFragment.TAG:
        fragment = ConnectedDevicesFragment.newInstance();
        break;
      case InteractiveFragment.TAG:
        fragment = InteractiveFragment.newInstance();
        break;
      case FetchCertificateFragment.TAG:
        fragment = FetchCertificateFragment.newInstance();
        break;
      default:
        LOGGER.debug("Wrong fragment instantiated");
        fragment = ConnectedDevicesFragment.newInstance();
    }
    return fragment;
  }

  public static Fragment createFragment(final String tag, boolean isSomething) {
    Fragment fragment;
    switch (tag) {
      case NetworkChoiceFragment.TAG:
        fragment = NetworkChoiceFragment.newInstance(isSomething);
        break;
      default:
        LOGGER.debug("Wrong fragment instantiated");
        fragment = ConnectedDevicesFragment.newInstance();
    }
    return fragment;
  }

}
