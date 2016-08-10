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

package com.imgtec.hobbyist.fragments.menu;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.di.component.AppComponent;
import com.imgtec.hobbyist.fragments.common.FragmentWithTitle;
import com.imgtec.hobbyist.fragments.menu.setupguide.SetUpWifireDeviceFragment;
import com.imgtec.hobbyist.fragments.navigationdrawer.NDMenuItem;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class NoConnectedDevicesFragment extends FragmentWithTitle {

  public static final String TAG = "NoConnectedDevicesFragment";

  private Unbinder unbinder;

  public static NoConnectedDevicesFragment newInstance() {

    Bundle args = new Bundle();

    NoConnectedDevicesFragment fragment = new NoConnectedDevicesFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.frag_no_connected_devices, container, false);
    unbinder = ButterKnife.bind(this, v);
    return v;
  }

  @Override
  public void onDestroyView() {
    unbinder.unbind();
    super.onDestroyView();
  }

  @Override
  protected String getActionBarTitleText() {
    return getString(R.string.connected_devices);
  }

  @Override
  protected void setupComponent(AppComponent appComponent) {
    appComponent.inject(this);
  }

  @OnClick(R.id.retry_button)
  void onRetryClicked() {
    if (menuListener != null) {
      menuListener.onFragmentChangeWithBackstackClear(ConnectedDevicesFragment.newInstance());
    }
  }

  @OnClick(R.id.setup_device_button)
  void onSetupDeviceClicked() {
    if (menuListener != null) {
      menuListener.onFragmentChangeWithBackstackClear(SetUpWifireDeviceFragment.newInstance());
      menuListener.onSelectionAndTitleChange(NDMenuItem.SetupDevice);
    }
  }
}
