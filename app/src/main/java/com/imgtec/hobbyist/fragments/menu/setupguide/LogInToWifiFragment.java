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

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.imgtec.hobbyist.App;
import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.activities.ActivitiesAndFragmentsHelper;
import com.imgtec.hobbyist.activities.CreatorActivity;
import com.imgtec.hobbyist.di.component.AppComponent;
import com.imgtec.hobbyist.ds.DSService;
import com.imgtec.hobbyist.fragments.common.FragmentWithProgressBar;
import com.imgtec.hobbyist.retrofit.pojos.softap.DeviceServer;
import com.imgtec.hobbyist.retrofit.pojos.softap.NetworkConfig;
import com.imgtec.hobbyist.utils.AnimationUtils;
import com.imgtec.hobbyist.utils.Constants;
import com.imgtec.hobbyist.utils.NDMenuMode;
import com.imgtec.hobbyist.utils.OnTextChangedListener;
import com.imgtec.hobbyist.utils.Preferences;
import com.imgtec.hobbyist.utils.SetupGuideInfoSingleton;
import com.imgtec.hobbyist.utils.SimpleFragmentFactory;

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
 * Details of WiFi connection to be used by device are inputted on this screen.
 */
public class LogInToWifiFragment extends FragmentWithProgressBar {

  public static final String TAG = "LogInToWifiFragment";
  public static final String UNKNOWN_SSID = "<unknown ssid>"; // Android default SSID value for no Internet connectivity.

  private enum StateOfSecurityProtocol {
    WEP(R.string.wep),
    WPA(R.string.wpa),
    WPA2(R.string.wpa2),
    OPEN(R.string.open);

    private final int textId;

    StateOfSecurityProtocol(int textId) {
      this.textId = textId;
    }

    public int getTextId() {
      return textId;
    }
  }

  @Inject
  @Named("UI")
  Handler handler;
  @Inject DSService DSService;
  @Inject Preferences preferences;

  @BindView(R.id.headerTv) TextView headerTv;
  @BindView(R.id.yourDevice) TextView yourDevice;
  @BindView(R.id.yourWifiNetwork) TextView yourWifiNetwork;
  @BindView(R.id.ssidField) EditText ssidField;
  @BindView(R.id.passwordField) EditText passwordField;
  @BindView(R.id.staticIpField) EditText staticIpField;
  @BindView(R.id.staticDnsField) EditText staticDnsField;
  @BindView(R.id.staticNetmaskField) EditText staticNetmaskField;
  @BindView(R.id.staticGatewayField) EditText staticGatewayField;
  @BindView(R.id.securityProtocolChoice) RadioGroup securityProtocolChoice;
  @BindView(R.id.networkingProtocolChoice) RadioGroup networkingProtocolChoice;
  @BindView(R.id.connect) Button connect;
  @BindView(R.id.linksLayout) LinearLayout linksLayout;
  @BindView(R.id.selectAnotherNetwork) Button selectAnotherNetwork;
  @BindView(R.id.manualConfiguration) Button manualConfiguration;
  @BindView(R.id.leds12Animation) ImageView leds12Animation;

  Unbinder unbinder;

  private boolean isManual = false;
  private boolean isStaticIp = false;
  private StateOfSecurityProtocol stateOfSecurityProtocol = StateOfSecurityProtocol.WPA2;

  public static LogInToWifiFragment newInstance() {
    return new LogInToWifiFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.frag_login_to_wifi, container, false);
    unbinder = ButterKnife.bind(this, rootView);
    return rootView;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ((CreatorActivity) menuListener).setUIMode(NDMenuMode.Setup);
    //performGettingDeviceInfoRequest();
    initLedsAnimation();
    initLinks();
    initTextViews();
    initViewsListeners();
  }

  @Override
  public void onDestroyView() {
    unbinder.unbind();
    super.onDestroyView();
  }

  @Override
  protected String getActionBarTitleText() {
    return getString(R.string.log_in_to_wifi);
  }

  private void initLedsAnimation() {
    AnimationUtils.startAnimation(leds12Animation, R.drawable.led1on2flashing);
  }

  private void initTextViews() {
    String boardSSID = SetupGuideInfoSingleton.getBoardSsid();
    yourDevice.setText(boardSSID);
    String currentNetworkSSID = SetupGuideInfoSingleton.getSsid();
    yourWifiNetwork.setText(currentNetworkSSID);
    connect.setEnabled(validatePassword(passwordField.getText().toString()));
  }

  private void initLinks() {
    selectAnotherNetwork.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        menuListener.onFragmentChange(SimpleFragmentFactory.createFragment(NetworkChoiceFragment.TAG, false));
      }
    });

    manualConfiguration.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        AnimationUtils.animateViewSetVisible(true, ssidField);
        AnimationUtils.animateViewSetVisible(false, linksLayout);
        AnimationUtils.animateViewSetVisible(false, headerTv);
        AnimationUtils.animateViewSetVisible(false, yourWifiNetwork);
        AnimationUtils.animateViewSetVisible(true, networkingProtocolChoice);
        isManual = true;
      }
    });
  }

  private void initViewsListeners() {
    securityProtocolChoice.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
          case R.id.wep:
            stateOfSecurityProtocol = StateOfSecurityProtocol.WEP;
            break;
          case R.id.wpa:
            stateOfSecurityProtocol = StateOfSecurityProtocol.WPA;
            break;
          case R.id.wpa2:
            stateOfSecurityProtocol = StateOfSecurityProtocol.WPA2;
            break;
          case R.id.open:
            stateOfSecurityProtocol = StateOfSecurityProtocol.OPEN;
            passwordField.setError(null);
            break;
        }
      }
    });

    networkingProtocolChoice.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.dhcp) {
          AnimationUtils.animateViewSetVisible(false, staticIpField);
          AnimationUtils.animateViewSetVisible(false, staticDnsField);
          AnimationUtils.animateViewSetVisible(false, staticNetmaskField);
          AnimationUtils.animateViewSetVisible(false, staticGatewayField);
          isStaticIp = false;
        } else if (checkedId == R.id.staticIP) {
          AnimationUtils.animateViewSetVisible(true, staticIpField);
          AnimationUtils.animateViewSetVisible(true, staticDnsField);
          AnimationUtils.animateViewSetVisible(true, staticNetmaskField);
          AnimationUtils.animateViewSetVisible(true, staticGatewayField);
          isStaticIp = true;
        }
      }
    });

    connect.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (validate()) {
          ActivitiesAndFragmentsHelper.hideSoftInput(appContext, ssidField, passwordField,
              staticDnsField, staticGatewayField, staticIpField, staticNetmaskField);
          showProgress(getString(R.string.please_wait_with_dots));
          NetworkConfig networkConfig = getNetworkConfig();
          performConnectWithDevice(networkConfig);
        }
      }

      private boolean validate() {
        if (!(stateOfSecurityProtocol == StateOfSecurityProtocol.OPEN)) {
          if (!isFieldValid(passwordField, R.string.password_is_required)) {
            return false;
          }
        }
        if (isManual) {
          if (!isFieldValid(ssidField, R.string.wireless_name_is_required)) {
            return false;
          }
          if (isStaticIp) {
            if (!isFieldValid(staticIpField, R.string.static_ip_is_required)) {
              return false;
            }
            if (!isFieldValid(staticDnsField, R.string.dns_is_required)) {
              return false;
            }
            if (!isFieldValid(staticNetmaskField, R.string.netmask_is_required)) {
              return false;
            }
            if (!isFieldValid(staticGatewayField, R.string.gateway_is_required)) {
              return false;
            }
          }
        }
        if (yourWifiNetwork.getText().toString().equalsIgnoreCase(UNKNOWN_SSID)) {
          ActivitiesAndFragmentsHelper.showFragmentChangeDialog(
              R.string.ssid_is_wrong,
              R.string.back_to_set_up_a_device,
              (CreatorActivity) menuListener,
              SimpleFragmentFactory.createFragment(SetUpWifireDeviceFragment.TAG));
          return false;
        }
        return true;
      }

      private boolean isFieldValid(EditText field, int errorRes) {
        if (TextUtils.isEmpty(field.getText().toString())) {
          field.setError(appContext.getString(errorRes));
          field.requestFocus();
          return false;
        } else {
          field.setError(null);
          return true;
        }
      }
    });

    new OnTextChangedListener(passwordField) {
      @Override
      public void onTextChanged(CharSequence s) {
        connect.setEnabled(validatePassword(s.toString()));
      }
    };
  }

  private NetworkConfig getNetworkConfig() {
    NetworkConfig networkConfig;
    if (isStaticIp) {
      networkConfig = new NetworkConfig(
          isManual ? ssidField.getText().toString() : yourWifiNetwork.getText().toString(),
          appContext.getString(stateOfSecurityProtocol.getTextId()),
          passwordField.getText().toString(),
          appContext.getString(R.string.static_ip),
          staticDnsField.getText().toString(),
          staticIpField.getText().toString(),
          staticNetmaskField.getText().toString(),
          staticGatewayField.getText().toString());

    } else {
      networkConfig = new NetworkConfig(
          isManual ? ssidField.getText().toString() : yourWifiNetwork.getText().toString(),
          appContext.getString(stateOfSecurityProtocol.getTextId()),
          passwordField.getText().toString(),
          appContext.getString(R.string.dhcp));
    }
    return networkConfig;
  }

  private void performConnectWithDevice(NetworkConfig networkConfig) {
    ((App) getActivity().getApplication()).getSoftAPRetrofitService().setNetworkConfig(networkConfig).enqueue(new Callback<Void>() {
      @Override
      public void onResponse(Call<Void> call, Response<Void> response) {
        handler.postDelayed(new Runnable() {

          @Override
          public void run() {

            DeviceServer deviceServer = new DeviceServer(
                SetupGuideInfoSingleton.getBootstrapUrl(),
                "PSK",
                SetupGuideInfoSingleton.getPskIdentity(),
                SetupGuideInfoSingleton.getPskSecret(),
                null,
                null

            );
            performSetServerConfig(deviceServer);

          }
        }, 1000);
      }

      @Override
      public void onFailure(Call<Void> call, Throwable t) {
        if (LogInToWifiFragment.this.isAdded()) {
          hideProgress();
          ActivitiesAndFragmentsHelper.showToast(appContext, R.string.check_connectivity, handler);
        }
      }
    });
  }

  private void performSetServerConfig(DeviceServer deviceServer) {
    ((App) getActivity().getApplication()).getSoftAPRetrofitService().setDeviceServer(deviceServer).enqueue(new Callback<Void>() {
      @Override
      public void onResponse(Call<Void> call, Response<Void> response) {
        handler.postDelayed(new Runnable() {
          @Override
          public void run() {
            performRebootRequest();
          }
        }, 1000);
      }

      @Override
      public void onFailure(Call<Void> call, Throwable t) {
        if (LogInToWifiFragment.this.isAdded()) {
          hideProgress();
          ActivitiesAndFragmentsHelper.showToast(appContext, R.string.check_connectivity, handler);
        }
      }
    });
  }

  private void performRebootRequest() {
    ((App) getActivity().getApplication()).getSoftAPRetrofitService().rebootDevice().enqueue(new Callback<Void>() {
      @Override
      public void onResponse(Call<Void> call, Response<Void> response) {
        startFragmentAfterRestartedDevice();
      }

      @Override
      public void onFailure(Call<Void> call, Throwable t) {
        if (LogInToWifiFragment.this.isAdded()) {
          hideProgress();
        }
        ActivitiesAndFragmentsHelper.showToast(appContext, R.string.connection_failure_try_again, handler);
      }
    });
  }

  private void startFragmentAfterRestartedDevice() {
    if (this.isAdded()) {
      hideProgress();
      if (menuListener != null) {
        menuListener.onFragmentChange(SimpleFragmentFactory.createFragment(ConnectingFragment.TAG));
      }
    }
  }

  private boolean validatePassword(@NonNull String password) {
    return (password.length() >= Constants.MIN_WIFI_PASSWORD_LENGTH &&
        password.length() <= Constants.MAX_WIFI_PASSWORD_LENGTH);
  }

  @Override
  protected void setupComponent(AppComponent appComponent) {
    appComponent.inject(this);
  }

}
