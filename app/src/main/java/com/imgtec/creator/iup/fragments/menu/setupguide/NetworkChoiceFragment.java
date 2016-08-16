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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.imgtec.creator.iup.R;
import com.imgtec.creator.iup.adapters.BoardWifiChoiceListAdapter;
import com.imgtec.creator.iup.di.component.AppComponent;
import com.imgtec.creator.iup.fragments.common.FragmentWithProgressBar;
import com.imgtec.creator.iup.utils.SetupGuideInfoSingleton;
import com.imgtec.creator.iup.utils.SimpleFragmentFactory;
import com.imgtec.creator.iup.utils.WifiUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Fragment representing one of the screens of device setup.
 * Allows picking network of a device that is to be configured.
 * Basing on {@link #IS_BOARD_WIFI_LIST_FRAGMENT_ARG} value this fragment can show different lists:
 * true  - showing list of WiFire device's networks.
 * false - showing list of all available networks.
 */
public class NetworkChoiceFragment extends FragmentWithProgressBar implements WifiUtil.WifiScanListener {

  public static final String IS_BOARD_WIFI_LIST_FRAGMENT_ARG = "IS_BOARD_WIFI_LIST_FRAGMENT_ARG";
  public static final String TAG = "NetworkChoiceFragment";
  private static final Logger LOGGER = LoggerFactory.getLogger(NetworkChoiceFragment.class);
  private static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;

  @BindView(R.id.dots) ImageView dots;
  @BindView(R.id.wifiSSIDs) ListView wifiListView;
  @BindView(R.id.selectBoard) TextView selectBoard;

  Unbinder unbinder;

  private List<String> SSIDs = new ArrayList<>();

  private boolean isBoardWifiListFragment;

  public static NetworkChoiceFragment newInstance(boolean isBoardWifiList) {
    Bundle args = new Bundle();
    args.putBoolean(IS_BOARD_WIFI_LIST_FRAGMENT_ARG, isBoardWifiList);
    final NetworkChoiceFragment fragment = new NetworkChoiceFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.frag_board_wifi_choice_fragment, container, false);
    unbinder = ButterKnife.bind(this, rootView);
    return rootView;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    init();
  }

  @Override
  public void onResume() {
    super.onResume();
    LOGGER.debug("onResume");
    updateUI();
  }

  @Override
  public void onPause() {
    super.onPause();
    LOGGER.debug("onPause");
  }

  @Override
  public void onDestroyView() {
    unbinder.unbind();
    super.onDestroyView();
  }

  @Override
  protected String getActionBarTitleText() {
    return getString(isBoardWifiListFragment ? R.string.select_device : R.string.log_in_to_wifi);
  }

  private void init() {
    isBoardWifiListFragment = getArguments().getBoolean(IS_BOARD_WIFI_LIST_FRAGMENT_ARG);
    wifiListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (menuListener != null) {

          if (isBoardWifiListFragment) {
            SetupGuideInfoSingleton.setBoardSsid(SSIDs.get(position));
            menuListener.onFragmentChange(SimpleFragmentFactory.createFragment(LoginToDeviceFragment.TAG));
          } else {
            SetupGuideInfoSingleton.setSsid(SSIDs.get(position));
            menuListener.onFragmentChange(SimpleFragmentFactory.createFragment(LogInToWifiFragment.TAG));
          }
        }
      }
    });
  }

  /**
   * Constant refreshing with interval of {@link com.imgtec.creator.iup.utils.Constants#TWO_SECONDS_MILLIS}
   */
  private void updateUI() {
    // It's asynchronous, but it should be refreshed, because it really speeds up the refreshing process.
    // Android scans networks rarely, by default, because of performance.
    if (isBoardWifiListFragment) {
      wifiUtil.requestBoardWifiList(NetworkChoiceFragment.this);
      dots.setVisibility(View.VISIBLE);
    } else {
      wifiUtil.requestAvailableWifiList(NetworkChoiceFragment.this);
      dots.setVisibility(View.GONE);
      selectBoard.setText(R.string.please_select_your_wifi_network);
    }
  }

  @Override
  public void onWifiScanCompleted(List<String> wifiSSIDs) {
    LOGGER.debug("onWifiScanCompleted: {}", wifiSSIDs);
    if (menuListener != null) {
      if (isBoardWifiListFragment) {
        updateBoardWifiList(wifiSSIDs);
      } else {
        updateWifiList(wifiSSIDs);
      }
    }
    //updateUI();
  }

  private void updateBoardWifiList(List<String> wifiSSIDs) {
    LOGGER.debug("updateBoardWifiList");
    Collections.sort(wifiSSIDs);
    SSIDs = wifiSSIDs;
    LOGGER.debug("Sorted wifi list: {}", SSIDs);
    //ToDo handle possible nullpointer exception
    if (wifiListView != null) {
      wifiListView.setAdapter(new BoardWifiChoiceListAdapter(appContext, SSIDs));
    }
  }

  private void updateWifiList(List<String> wifiSSIDs) {
    LOGGER.debug("updateWiFiList");
    Collections.sort(wifiSSIDs);
    SSIDs = wifiSSIDs;
    LOGGER.debug("Sorted wifi list: {}", SSIDs);
    wifiListView.setAdapter(new BoardWifiChoiceListAdapter(appContext, SSIDs));
  }

  @Override
  protected void setupComponent(AppComponent appComponent) {
    appComponent.inject(this);
  }




}
