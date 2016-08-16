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


package com.imgtec.creator.iup.fragments.menu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.reflect.TypeToken;
import com.imgtec.creator.iup.R;
import com.imgtec.creator.iup.di.component.AppComponent;
import com.imgtec.creator.iup.ds.DSService;
import com.imgtec.creator.iup.ds.pojo.DeviceInfo;
import com.imgtec.creator.iup.ds.pojo.Instances;
import com.imgtec.creator.iup.wifire.DeviceHelper;
import com.imgtec.creator.iup.fragments.common.FragmentWithTitle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Fragment representing a screen with WiFire board details.
 * Information is retrieved from Flow.
 */
public class CreatorDeviceInfoFragment extends FragmentWithTitle {

  public static final String TAG = "CreatorDeviceInfoFragment";

  private static final Logger LOGGER = LoggerFactory.getLogger(CreatorDeviceInfoFragment.class);

  @BindView(R.id.macValue) TextView macValue;
  @BindView(R.id.serialNumberValue) TextView serialNumberValue;
  @BindView(R.id.deviceTypeValue) TextView deviceTypeValue;
  @BindView(R.id.softwareVersionValue) TextView softwareVersionValue;
  @BindView(R.id.nameValue) TextView nameValue;
  @BindView(R.id.progressBar) ProgressBar progressBar;


  @Inject DSService caller;
  @Inject DeviceHelper deviceHelper;

  Unbinder unbinder;

  public static CreatorDeviceInfoFragment newInstance() {
    return new CreatorDeviceInfoFragment();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.frag_device_info, container, false);
    unbinder = ButterKnife.bind(this, rootView);
    return rootView;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    getDeviceInfo();
  }

  @Override
  public void onDestroyView() {
    unbinder.unbind();
    super.onDestroyView();
  }

  @Override
  protected String getActionBarTitleText() {
    return deviceHelper.getDevice().getName();
  }

  private void getDeviceInfo() {
    ListenableFuture<Instances<DeviceInfo>> future = caller.getInstances(deviceHelper.getDevice().getClient(), 3, new TypeToken<Instances<DeviceInfo>>() {
    });
    Futures.addCallback(future, new FutureCallback<Instances<DeviceInfo>>() {
      @Override
      public void onSuccess(Instances<DeviceInfo> result) {
        if (result.getItems().size() == 1) {
          updateDeviceInfo(result.getItems().get(0));
        }
      }

      @Override
      public void onFailure(Throwable t) {
        t.printStackTrace();
      }
    });
  }

  private void updateDeviceInfo(final DeviceInfo device) {
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (device != null) {
          serialNumberValue.setText(device.getSerialNumber());
          deviceTypeValue.setText(device.getDeviceType());
          softwareVersionValue.setText(device.getSoftwareVersion());
          macValue.setText(R.string.na);
          nameValue.setText(R.string.na);

        }
        progressBar.setVisibility(View.GONE);
      }
    });
  }


  @Override
  protected void setupComponent(AppComponent appComponent) {
    appComponent.inject(this);
  }


}
