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


package com.imgtec.creator.iup.fragments.menu.setupguide;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.imgtec.creator.iup.R;
import com.imgtec.creator.iup.activities.ActivitiesAndFragmentsHelper;
import com.imgtec.creator.iup.activities.BaseActivity;
import com.imgtec.creator.iup.di.component.AppComponent;
import com.imgtec.creator.iup.fragments.common.FragmentWithProgressBar;
import com.imgtec.creator.iup.fragments.menu.ConnectedDevicesFragment;
import com.imgtec.creator.iup.fragments.navigationdrawer.NDMenuItem;
import com.imgtec.creator.iup.utils.SetupGuideInfoSingleton;
import com.imgtec.creator.iup.utils.SimpleFragmentFactory;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Fragment representing one of the screens of device setup.
 * Gets some required data about user's devices before allowing to continue the process.
 */
public class SetUpWifireDeviceFragment extends FragmentWithProgressBar {

  public static final String TAG = "SetUpAWifireDeviceFragment";
  public static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;

  @BindView(R.id.setupInfo) TextView setupInfo;
  @BindView(R.id.setupDeviceWifi) LinearLayout setupDeviceWifi;
  @BindView(R.id.setupDeviceNoWifi) RelativeLayout setupDeviceNoWifi;
  @BindView(R.id.startSetup) Button startSetup;
  @BindView(R.id.cancel) Button cancel;


  public static SetUpWifireDeviceFragment newInstance() {
    return new SetUpWifireDeviceFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.frag_setup_wifire_device, container, false);
    ButterKnife.bind(this, rootView);
    return rootView;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initButtonListeners();
  }

  private void initButtonListeners() {
    startSetup.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        checkPermissions();
      }
    });

    cancel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (menuListener != null) {
          menuListener.onFragmentChange(SimpleFragmentFactory.createFragment(ConnectedDevicesFragment.TAG));
        }
      }
    });
  }

  @Override
  public void onResume() {
    super.onResume();
    setupUI();
  }

  @Override
  protected String getActionBarTitleText() {
    return getString(NDMenuItem.SetupDevice.getTextId());
  }

  @Override
  protected void onNetworkStateChanged() {
    setupUI();
  }

  private void setupUI() {
    hideProgress();
    if (checkForWiFi()) {
      initConnected();
    } else {
      initNotConnected();
    }
  }

  private void initNotConnected() {
    setupDeviceWifi.setVisibility(View.GONE);
    setupDeviceNoWifi.setVisibility(View.VISIBLE);
  }

  private void initConnected() {
    setupDeviceWifi.setVisibility(View.VISIBLE);
    setupDeviceNoWifi.setVisibility(View.GONE);
    initSetupInfo();
  }

  private void initSetupInfo() {
    String currentNetworkSSID = SetupGuideInfoSingleton.getSsid();
    setupInfo.setText(getString(R.string.setup_device_info_wifi, currentNetworkSSID));
  }

  private void afterSetupComplete() {
    final BaseActivity baseActivity = (BaseActivity) getActivity();
    if (baseActivity != null) {
      hideProgress();
      ActivitiesAndFragmentsHelper.replaceFragment(baseActivity,
          SimpleFragmentFactory.createFragment(SetupModeFragment.TAG));
    }
  }

  @Override
  protected void setupComponent(AppComponent appComponent) {
    appComponent.inject(this);
  }

  private void checkPermissions() {
    if (ContextCompat.checkSelfPermission(getContext(),
        Manifest.permission.ACCESS_COARSE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
        showPermissionRationaleDialog();
      } else {
        requestPermissions(
            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
            PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
      }

    } else {
      onPermissionsGranted();
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode,
                                         @NonNull String permissions[], @NonNull int[] grantResults) {
    switch (requestCode) {
      case PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          onPermissionsGranted();
          return;
        }
        onPermissionsRejected();
      }
    }
  }

  void onPermissionsGranted() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      final LocationManager manager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

      if (!manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        showEnableLocationDialog();
      } else {
        afterSetupComplete();
      }
    } else {
      afterSetupComplete();
    }
  }

  void onPermissionsRejected() {

  }


  void showPermissionRationaleDialog() {
    new AlertDialog.Builder(getContext())
        .setMessage(R.string.location_permission_rationale)
        .setTitle(R.string.location_oermission_required)
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            requestPermissions(
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            dialogInterface.dismiss();
          }
        })
        .setCancelable(false)
        .create()
        .show();
  }

  void showEnableLocationDialog() {
    new AlertDialog.Builder(getContext())
        .setMessage(R.string.enable_location_message)
        .setTitle(R.string.location_disabled)
        .setPositiveButton(R.string.go_to_settings, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            dialogInterface.dismiss();
          }
        })
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            dialogInterface.dismiss();
          }
        })
        .create()
        .show();
  }
}

