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
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.activities.CreatorActivity;
import com.imgtec.hobbyist.adapters.ConnectedDevicesAdapter;
import com.imgtec.hobbyist.di.component.AppComponent;
import com.imgtec.hobbyist.ds.DSService;
import com.imgtec.hobbyist.ds.pojo.Client;
import com.imgtec.hobbyist.ds.pojo.Clients;
import com.imgtec.hobbyist.fragments.common.FragmentWithTitle;
import com.imgtec.hobbyist.wifire.DeviceHelper;
import com.imgtec.hobbyist.wifire.WifireDevice;
import com.imgtec.hobbyist.fragments.navigationdrawer.NDMenuItem;
import com.imgtec.hobbyist.utils.NDMenuMode;
import com.imgtec.hobbyist.utils.Preferences;
import com.imgtec.hobbyist.utils.SimpleFragmentFactory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Fragment with list of devices (boards) owned by currently logged in Flow account.
 * ConnectedDevicesFragment is updated (and short sound is played) if one of connected
 * devices returns new presence state, e.g.:
 * device was offline, but is becoming online right now, and vice-versa.
 * There is an appropriate view if there are no devices connected with Flow account.
 */
public class ConnectedDevicesFragment extends FragmentWithTitle implements
    SwipeRefreshLayout.OnRefreshListener {

  public static final String TAG = "ConnectedDevicesFragment";

  @Inject Preferences preferences;
  @Inject DSService DSService;
  @Inject DeviceHelper deviceHelper;

  @BindView(R.id.deviceListView) ListView deviceListView;
  @BindView(R.id.devicesLayout) LinearLayout devicesLayout;
  @BindView(R.id.progressBar) ProgressBar progressBar;
  @BindView(R.id.interactWithSelected) Button interactWithSelected;
  @BindView(R.id.swipeLayout) SwipeRefreshLayout swipeLayout;

  private Unbinder unbinder;
  private List<WifireDevice> devices;
  private boolean cabActive = false;

  public static ConnectedDevicesFragment newInstance() {
    return new ConnectedDevicesFragment();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.frag_connected_devices, container, false);
    unbinder = ButterKnife.bind(this, rootView);
    swipeLayout.setOnRefreshListener(this);
    swipeLayout.setColorSchemeResources(R.color.theme_purple, R.color.theme_light_purple,
        R.color.theme_light_purple, R.color.theme_dark_purple);
    return rootView;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
  }

  @Override
  public void onResume() {
    super.onResume();
    initDeviceList();
    if (menuListener != null) {
      menuListener.onTitleChange(getActivity().getString(R.string.connected_devices));
    }
  }

  @Override
  public void onPause() {
    super.onPause();
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


  private void initDeviceList() {
    progressBar.setVisibility(View.VISIBLE);
    getDevices();
  }

  private void getDevices() {

    ListenableFuture<Clients> future = DSService.getClients(0, 100);
    Futures.addCallback(future, new FutureCallback<Clients>() {
      @Override
      public void onSuccess(final Clients clients) {
        getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
            devices = new ArrayList<>();
            for (Client client : clients.getItems()) {
              if (client.getName().startsWith("WiFire")) {
                devices.add(new WifireDevice(client));
              }
            }

            afterGetDevices();
            progressBar.setVisibility(View.GONE);
            swipeLayout.setRefreshing(false);
          }
        });
      }

      @Override
      public void onFailure(Throwable t) {
        getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
            progressBar.setVisibility(View.GONE);
            swipeLayout.setRefreshing(false);
          }
        });
      }
    });
  }

  private void afterGetDevices() {
    if (devices == null || devices.isEmpty()) {
      updateNoDevices();
    } else {
      updateDevices();
    }
  }

  private void selectDevice() {
    interactWithSelected.setEnabled(true);
  }

  private void updateNoDevices() {
    if (menuListener != null) {
      menuListener.onFragmentChangeWithBackstackClear(SimpleFragmentFactory.createFragment(NoConnectedDevicesFragment.TAG));
      ((CreatorActivity) menuListener).setUIMode(NDMenuMode.Initial);
    }
  }

  private void updateDevices() {
    restartDeviceList();
    devicesLayout.setVisibility(View.VISIBLE);
    interactWithSelected.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (menuListener != null) {
          preferences.interactiveModeHasStartedAtLeastOnce();
          setInteractiveMode();
          menuListener.onFragmentChangeWithBackstackClear(SimpleFragmentFactory.createFragment(InteractiveFragment.TAG));
          menuListener.onSelectionAndTitleChange(NDMenuItem.Commands);
        }
      }

      private void setInteractiveMode() {
        deviceHelper.setDevice(devices.get(deviceListView.getCheckedItemPosition()));
        ((CreatorActivity) menuListener).setUIMode(NDMenuMode.Interactive);
      }
    });
  }

  private void restartDeviceList() {
    deviceListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
    ConnectedDevicesAdapter devicesAdapter = new ConnectedDevicesAdapter(appContext, devices);
    devicesAdapter.sort(WifireDevice.COMPARATOR);
    deviceListView.setAdapter(devicesAdapter);

    deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!cabActive) { //not called in cab except for first choice. First call after cab ends sets choice mode.
          deviceListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
          selectDevice();
        }
        deviceListView.clearChoices();
        deviceListView.setItemChecked(position, true);
        deviceListView.requestLayout();
      }
    });
    resetSelectionToFirstDevice();
  }


  private void resetSelectionToFirstDevice() {
    deviceListView.setItemChecked(0, true);
    selectDevice();
  }

  @Override
  public void onRefresh() {
    getDevices();
  }

  @Override
  protected void setupComponent(AppComponent appComponent) {
    appComponent.inject(this);
  }

}
