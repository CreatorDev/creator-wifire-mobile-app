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
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.imgtec.hobbyist.App;
import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.activities.CreatorActivity;
import com.imgtec.hobbyist.di.component.AppComponent;
import com.imgtec.hobbyist.fragments.navigationdrawer.NDListeningFragment;
import com.imgtec.hobbyist.retrofit.pojos.softap.DeviceInfo;
import com.imgtec.hobbyist.utils.NDMenuMode;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Fragment representing a screen with WiFire board details.
 * Information is retrieved from REST API (board itself), not from Flow.
 */
public class DeviceInfoFragment extends NDListeningFragment {

  public static final String TAG = "DeviceInfoFragment";

  @BindView(R.id.macValue) TextView macValue;
  @BindView(R.id.serialNumberValue) TextView serialNumberValue;
  @BindView(R.id.deviceTypeValue) TextView deviceTypeValue;
  @BindView(R.id.softwareVersionValue) TextView softwareVersionValue;
  @BindView(R.id.nameValue) TextView nameValue;
  @BindView(R.id.progressBar) ProgressBar progressBar;


  @Inject
  @Named("UI")
  Handler handler;

  public static DeviceInfoFragment newInstance() {
    return new DeviceInfoFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.frag_device_info, container, false);
    ButterKnife.bind(this, rootView);
    return rootView;
  }

  @Override
  public void onStart() {
    super.onStart();
    performGetDeviceInfoRequest();
  }

  private void performGetDeviceInfoRequest() {
    progressBar.setVisibility(View.VISIBLE);
    ((App) getActivity().getApplication()).getSoftAPRetrofitService().getDeviceInfo().enqueue(new Callback<DeviceInfo>() {
      @Override
      public void onResponse(Call<DeviceInfo> call, Response<DeviceInfo> response) {
        if (DeviceInfoFragment.this.isAdded()) {
          updateDeviceInfo(response.body());
        }
      }

      @Override
      public void onFailure(Call<DeviceInfo> call, Throwable t) {
        if (DeviceInfoFragment.this.isAdded()) {
          progressBar.setVisibility(View.GONE);
          Toast.makeText(appContext, appContext.getString(R.string.check_connectivity), Toast.LENGTH_SHORT).show();
        }
      }
    });
  }

  private void updateDeviceInfo(DeviceInfo deviceInfo) {
    ((CreatorActivity) menuListener).setUIMode(NDMenuMode.Setup);// for NDFragmentMenu actualization

    macValue.setText(deviceInfo.getMACAddress());
    serialNumberValue.setText(deviceInfo.getSerialNumber());
    deviceTypeValue.setText(deviceInfo.getDeviceType());
    softwareVersionValue.setText(deviceInfo.getSoftwareVersion());
    String deviceName = deviceInfo.getDeviceName();
    nameValue.setText(deviceName);
    if (menuListener != null) {
      menuListener.onTitleChange(deviceName);
    }
    progressBar.setVisibility(View.GONE);

  }

  @Override
  protected void setupComponent(AppComponent appComponent) {
    appComponent.inject(this);
  }

}

