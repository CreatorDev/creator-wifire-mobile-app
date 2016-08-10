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
package com.imgtec.hobbyist.di.component;

import com.imgtec.hobbyist.App;
import com.imgtec.hobbyist.activities.BaseActivity;
import com.imgtec.hobbyist.activities.CreatorActivity;
import com.imgtec.hobbyist.activities.LogInActivity;
import com.imgtec.hobbyist.di.module.AppModule;
import com.imgtec.hobbyist.di.module.NetworkModule;
import com.imgtec.hobbyist.fragments.loginsignup.LogInOrSignUpFragment;
import com.imgtec.hobbyist.fragments.loginsignup.SignUpFragment;
import com.imgtec.hobbyist.fragments.loginsignup.SplashFragment;
import com.imgtec.hobbyist.fragments.menu.AboutFragment;
import com.imgtec.hobbyist.fragments.menu.ConnectedDevicesFragment;
import com.imgtec.hobbyist.fragments.menu.DeviceInfoFragment;
import com.imgtec.hobbyist.fragments.menu.CreatorDeviceInfoFragment;
import com.imgtec.hobbyist.fragments.menu.InteractiveFragment;
import com.imgtec.hobbyist.fragments.menu.NoConnectedDevicesFragment;
import com.imgtec.hobbyist.fragments.menu.setupguide.ConnectingFragment;
import com.imgtec.hobbyist.fragments.menu.setupguide.FetchCertificateFragment;
import com.imgtec.hobbyist.fragments.menu.setupguide.LogInToWifiFragment;
import com.imgtec.hobbyist.fragments.menu.setupguide.LoginToDeviceFragment;
import com.imgtec.hobbyist.fragments.menu.setupguide.NetworkChoiceFragment;
import com.imgtec.hobbyist.fragments.menu.setupguide.SetUpWifireDeviceFragment;
import com.imgtec.hobbyist.fragments.menu.setupguide.SetupModeFragment;
import com.imgtec.hobbyist.fragments.navigationdrawer.NDMenuFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, NetworkModule.class})
public interface AppComponent {

  void inject(App app);

  void inject(BaseActivity baseActivity);

  void inject(CreatorActivity creatorActivity);

  void inject(LogInActivity logInActivity);


  void inject(SplashFragment splashFragment);

  void inject(NDMenuFragment ndMenuFragment);

  void inject(SignUpFragment signUpFragment);

  void inject(AboutFragment aboutFragment);

  void inject(LogInOrSignUpFragment logInOrSignUpFragment);

  void inject(ConnectedDevicesFragment connectedDevicesFragment);

  void inject(LoginToDeviceFragment loginToDeviceFragment);

  void inject(DeviceInfoFragment deviceInfoFragment);

  void inject(CreatorDeviceInfoFragment creatorDeviceInfoFragment);

  void inject(SetUpWifireDeviceFragment setUpWifireDeviceFragment);

  void inject(ConnectingFragment connectingFragment);

  void inject(NetworkChoiceFragment networkChoiceFragment);

  void inject(LogInToWifiFragment logInToWifiFragment);

  void inject(SetupModeFragment setupModeFragment);

  void inject(InteractiveFragment interactiveFragment);

  void inject(FetchCertificateFragment fetchCertificateFragment);

  void inject(NoConnectedDevicesFragment noConnectedDevices);
}
