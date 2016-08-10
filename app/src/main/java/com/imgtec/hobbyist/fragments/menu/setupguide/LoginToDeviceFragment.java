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


package com.imgtec.hobbyist.fragments.menu.setupguide;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.imgtec.hobbyist.App;
import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.activities.ActivitiesAndFragmentsHelper;
import com.imgtec.hobbyist.activities.BaseActivity;
import com.imgtec.hobbyist.activities.CreatorActivity;
import com.imgtec.hobbyist.di.component.AppComponent;
import com.imgtec.hobbyist.fragments.common.FragmentWithProgressBar;
import com.imgtec.hobbyist.retrofit.pojos.softap.DeviceInfo;
import com.imgtec.hobbyist.utils.Constants;
import com.imgtec.hobbyist.utils.NDMenuMode;
import com.imgtec.hobbyist.utils.OnTextChangedListener;
import com.imgtec.hobbyist.utils.SetupGuideInfoSingleton;
import com.imgtec.hobbyist.utils.SimpleFragmentFactory;
import com.imgtec.hobbyist.utils.WifiUtil;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment representing one of the screens of device setup.
 * Connecting with device's WiFi takes place here.
 */
public class LoginToDeviceFragment extends FragmentWithProgressBar implements WifiUtil.WifiScanListener, App.WifiStateListener {

  public static final String TAG = "LoginToDeviceFragment";
  private static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;

  private int wifiScanCount = 0;
  @BindView(R.id.passwordField) EditText passwordField;
  @BindView(R.id.chosenSSIDField) TextView chosenSSIDField;
  @BindView(R.id.connect) Button connect;
  String boardSSID;

  @Inject
  @Named("UI")
  Handler handler;

  Unbinder unbinder;
  private Runnable showWrongPasswordRunnable;

  public static LoginToDeviceFragment newInstance() {
    return new LoginToDeviceFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.frag_login_to_device, container, false);
    unbinder = ButterKnife.bind(this, rootView);
    return rootView;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    boardSSID = SetupGuideInfoSingleton.getBoardSsid();
    chosenSSIDField.setText(boardSSID);
    initListeners();
  }

  @Override
  public void onPause() {
    ((App) getActivity().getApplication()).removeWifiConnectionListener(this);
    handler.removeCallbacks(showWrongPasswordRunnable);
    super.onPause();
  }

  @Override
  public void onStart() {
    super.onStart();

  }

  @Override
  public void onDestroyView() {
    unbinder.unbind();
    super.onDestroyView();
  }

  private void initListeners() {
    new OnTextChangedListener(passwordField) {
      @Override
      public void onTextChanged(CharSequence s) {
        if (s.length() < Constants.WEP_64_BIT_SECRET_KEY_HEXADECIMAL_LENGTH || !isHexadecimal(s.toString())) {
          connect.setEnabled(false);
        } else {
          connect.setEnabled(true);
        }
      }
    };

    connect.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (Constants.WIFIRE_BOARD_REQUESTS_MODE) {
          showProgress(getString(R.string.please_wait_with_dots));
          wifiUtil.requestBoardWifiList(LoginToDeviceFragment.this);
        } else {
          startFragmentAfterDeviceConnection(true);
        }
      }
    });
  }

  @Override
  public void onWifiScanCompleted(List<String> wifiSSIDs) {
    if (menuListener != null) {
      if (wifiSSIDs.contains(boardSSID)) {
        if (wifiScanCount < 1) {
          //scan again, because Android does not forget no longer existing networks after one scan
          wifiUtil.requestBoardWifiList(LoginToDeviceFragment.this);
          ++wifiScanCount;
        } else {
          startWiFiConnectionTimeout();
          startWiFiBoardConnection();
          wifiScanCount = 0;
        }
      } else {
        hideProgress();
        showAlertDialog(getString(R.string.board_wifi_not_available) + boardSSID,
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                connect.setEnabled(false);
                dialog.dismiss();
                ActivitiesAndFragmentsHelper.hideSoftInput(getActivity(), passwordField);
                Fragment fragment = SimpleFragmentFactory.createFragment(SetUpWifireDeviceFragment.TAG);
                ActivitiesAndFragmentsHelper.replaceFragmentWithBackStackClear((BaseActivity) getActivity(), fragment);
                ((CreatorActivity) getActivity()).setUIMode(NDMenuMode.Initial);
              }
            });
      }
    }
  }

  private void showAlertDialog(final String message, DialogInterface.OnClickListener buttonListener) {
    final AlertDialog.Builder builder = new AlertDialog.Builder((Activity) menuListener);
    builder
        .setMessage(message)
        .setPositiveButton("OK", buttonListener)
        .create()
        .show();
  }

  private void startWiFiConnectionTimeout() {
    showWrongPasswordRunnable = new Runnable() {
      @Override
      public void run() {
        hideProgress();
        showAlertDialog(getString(R.string.wrong_board_wifi_password) + boardSSID,
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                connect.setEnabled(false);
                dialog.dismiss();
              }
            });
        ((App) getActivity().getApplication()).removeWifiConnectionListener(LoginToDeviceFragment.this);
      }
    };
    handler.postDelayed(showWrongPasswordRunnable, Constants.THIRTY_SECONDS_MILLIS);
  }

  private void startWiFiBoardConnection() {

    if (wifiUtil.connectToWepNetwork(boardSSID, passwordField.getText().toString()) == false) {
      handler.removeCallbacks(showWrongPasswordRunnable);
      hideProgress();
      ActivitiesAndFragmentsHelper.showWifiSettingsChangeDialog(
          appContext.getString(R.string.unable_to_configure_network_title),
          appContext.getString(R.string.unable_to_configure_network_message, boardSSID),
          appContext.getString(R.string.go_to_wifi_settings),
          getActivity()
      );
      return;
    }
    ActivitiesAndFragmentsHelper.hideSoftInput(appContext, passwordField);
    ((App) getActivity().getApplication()).addWifiConnectionListener(this);
  }

  private void performGettingDeviceInfoRequest() {
    ((App) getActivity().getApplication()).getSoftAPRetrofitService().getDeviceInfo().enqueue(new Callback<DeviceInfo>() {
      @Override
      public void onResponse(Call<DeviceInfo> call, Response<DeviceInfo> response) {
        if (LoginToDeviceFragment.this.isAdded()) {
          SetupGuideInfoSingleton.setDeviceName(response.body().getDeviceName());
          menuListener.onFragmentChange(SimpleFragmentFactory.createFragment(LogInToWifiFragment.TAG));
        }
      }

      @Override
      public void onFailure(Call<DeviceInfo> call, Throwable t) {
        if (LoginToDeviceFragment.this.isAdded()) {
          hideProgress();
          ActivitiesAndFragmentsHelper.showToast(appContext, R.string.check_wifire_connection, handler);
        }
      }
    });
  }

  private void startFragmentAfterDeviceConnection(final boolean canSetup) {
    if (getActivity() != null) {
      hideProgress();
      getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {

          menuListener.onFragmentChange(SimpleFragmentFactory.createFragment(LogInToWifiFragment.TAG));

        }
      });
    }
  }


  @Override
  protected String getActionBarTitleText() {
    return getString(R.string.log_in_to_device);
  }

  public static boolean isHexadecimal(String text) {
    try {
      Long.parseLong(text, 16);
      return true;
    } catch (NumberFormatException ex) {
      // Error handling code...
      return false;
    }
  }

  @Override
  public void onWiFiConnected(NetworkInfo networkInfo) {
    Log.d(TAG, "onWiFiConnected() called with: networkInfo = [" + networkInfo + "]");
    if (wifiUtil.isBoardConnected()) {
      Log.d(TAG, "board connected");
      handler.removeCallbacks(showWrongPasswordRunnable);
      ((App) getActivity().getApplication()).removeWifiConnectionListener(LoginToDeviceFragment.this);


      handler.postDelayed(new Runnable() {
        @Override
        public void run() {
          performGettingDeviceInfoRequest();
        }
      }, 500); // need to delay request a little bit to be sure connection is settled
    }
  }

  @Override
  public void onWiFiDisconnected() {

  }

  @Override
  protected void setupComponent(AppComponent appComponent) {
    appComponent.inject(this);
  }


}
