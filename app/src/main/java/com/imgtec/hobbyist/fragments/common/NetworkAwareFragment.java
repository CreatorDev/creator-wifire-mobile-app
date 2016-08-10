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
package com.imgtec.hobbyist.fragments.common;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.imgtec.hobbyist.App;
import com.imgtec.hobbyist.utils.BroadcastReceiverWithRegistrationState;
import com.imgtec.hobbyist.utils.WifiUtil;

public abstract class NetworkAwareFragment extends DIFragment {

  private ConnectivityReceiver connectivityReceiver;
  protected Context appContext;
  protected WifiUtil wifiUtil;


  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    appContext = getActivity().getApplicationContext();
    wifiUtil = ((App) appContext).getWifiUtil();
  }

  @Override
  public void onDetach() {
    wifiUtil = null;
    appContext = null;
    super.onDetach();
  }

  @Override
  public void onResume() {
    super.onResume();
    connectivityReceiver = new ConnectivityReceiver((new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)));
    connectivityReceiver.register(appContext);
  }

  @Override
  public void onPause() {
    connectivityReceiver.unregister(appContext);
    super.onPause();
  }

  protected boolean checkForWiFi() {

    return wifiUtil.isWifiNotBoardConnected();
  }

  protected void onNetworkStateChanged() {
  }

  private class ConnectivityReceiver extends BroadcastReceiverWithRegistrationState {
    public ConnectivityReceiver(IntentFilter intentFilter) {
      super(intentFilter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      onNetworkStateChanged();
    }
  }
}
