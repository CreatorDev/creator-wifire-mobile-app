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


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.imgtec.creator.iup.R;
import com.imgtec.creator.iup.activities.ActivitiesAndFragmentsHelper;
import com.imgtec.creator.iup.activities.BaseActivity;
import com.imgtec.creator.iup.activities.CreatorActivity;
import com.imgtec.creator.iup.di.component.AppComponent;
import com.imgtec.creator.iup.ds.DSService;
import com.imgtec.creator.iup.ds.exceptions.DeviceServerException;
import com.imgtec.creator.iup.ds.exceptions.NetworkException;
import com.imgtec.creator.iup.ds.exceptions.NotFoundException;
import com.imgtec.creator.iup.ds.exceptions.ParseException;
import com.imgtec.creator.iup.ds.exceptions.UnauthorizedException;
import com.imgtec.creator.iup.ds.pojo.AnalogInput;
import com.imgtec.creator.iup.ds.pojo.Client;
import com.imgtec.creator.iup.ds.pojo.IPSODigitalInput;
import com.imgtec.creator.iup.ds.pojo.Instances;
import com.imgtec.creator.iup.ds.pojo.LightControl;
import com.imgtec.creator.iup.ds.pojo.Temperature;
import com.imgtec.creator.iup.wifire.DeviceHelper;
import com.imgtec.creator.iup.fragments.common.FragmentWithProgressBar;
import com.imgtec.creator.iup.utils.SimpleFragmentFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class InteractiveFragment extends FragmentWithProgressBar {

  public static final String TAG = "InteractiveFragment";

  private static final int LIGHT_CONTROL_OBJECT_ID = 3311;

  private boolean[] ledStates = new boolean[4];
  private boolean[] buttonStates = new boolean[2];
  private float[] temperatureValues = new float[1];
  private float[] analogInputValues = new float[1];


  @BindView(R.id.led1) ImageView led1;
  @BindView(R.id.led2) ImageView led2;
  @BindView(R.id.led3) ImageView led3;
  @BindView(R.id.led4) ImageView led4;
  @BindView(R.id.sw1) ImageView sw1;
  @BindView(R.id.sw2) ImageView sw2;

  @BindView(R.id.prog) ProgressBar progressBar;
  @BindView(R.id.tempValue) TextView tempValue;
  @BindView(R.id.potentiometerValue) TextView potentiometerValue;

  @Inject
  @Named("UI")
  Handler handler;
  @Inject DSService caller;
  @Inject DeviceHelper deviceHelper;

  Unbinder unbinder;

  ExecutorService executor = Executors.newSingleThreadExecutor();


  public static InteractiveFragment newInstance() {

    Bundle args = new Bundle();

    InteractiveFragment fragment = new InteractiveFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.frag_interactive, container, false);
    unbinder = ButterKnife.bind(this, rootView);
    return rootView;
  }

  @Override
  public void onResume() {
    super.onResume();
    handler.post(refreshTask);
  }

  @Override
  public void onPause() {
    super.onPause();
    hideProgressDialog();
    handler.removeCallbacks(refreshTask);
  }

  @Override
  public void onDetach() {
    super.onDetach();
  }

  @Override
  public void onDestroyView() {
    unbinder.unbind();
    super.onDestroyView();
  }

  @Override
  protected String getActionBarTitleText() {
    return deviceHelper.getDevice().getClient().getName();
  }


  private void refresh() {
    executor.submit(new Runnable() {
      @Override
      public void run() {
        try {
          Client client = caller.getClient(deviceHelper.getDevice().getName()).get();
          final Instances<LightControl> lightControls = caller.getInstances(client, LIGHT_CONTROL_OBJECT_ID, new TypeToken<Instances<LightControl>>() {
          }).get();
          final Instances<IPSODigitalInput> digitalInputs = caller.getInstances(client, 3200, new TypeToken<Instances<IPSODigitalInput>>() {
          }).get();
          final Instances<Temperature> temperatures = caller.getInstances(client, 3303, new TypeToken<Instances<Temperature>>() {
          }).get();
          final Instances<AnalogInput> analogInputs = caller.getInstances(client, 3202, new TypeToken<Instances<AnalogInput>>() {
          }).get();
          if (lightControls.getItems().size() < 4)
            throw new ParseException();
          if (digitalInputs.getItems().size() < 2) {
            throw new ParseException();
          }
          if (temperatures.getItems().size() < 1) {
            throw new ParseException();
          }
          if (analogInputs.getItems().size() < 1) {
            throw new ParseException();
          }
          handler.post(new Runnable() {
            @Override
            public void run() {
              handleRefreshSuccess(lightControls, digitalInputs, temperatures, analogInputs);
            }
          });

        } catch (InterruptedException e) {
          e.printStackTrace();
        } catch (final ExecutionException e) {
          handler.post(new Runnable() {
            @Override
            public void run() {
              handleRefreshFailure((DeviceServerException) e.getCause());
            }
          });
        } catch (final ParseException e) {
          handler.post(new Runnable() {
            @Override
            public void run() {
              handleRefreshFailure(e);
            }
          });
        }
        handler.postDelayed(refreshTask, 2000);
      }
    });

  }

  @Override
  protected void setupComponent(AppComponent appComponent) {
    appComponent.inject(this);
  }

  @UiThread
  private void showProgressDialog() {
    if (isAdded()) {
      ((CreatorActivity) getActivity()).setToolbarProgressbarVisibility(true);
      //refreshBtn.setVisibility(View.INVISIBLE);
      led1.setEnabled(false);
      led2.setEnabled(false);
      led3.setEnabled(false);
      led4.setEnabled(false);
    }
  }

  @UiThread
  private void hideProgressDialog() {
    if (isAdded()) {
      ((CreatorActivity) getActivity()).setToolbarProgressbarVisibility(false);
      //refreshBtn.setVisibility(View.VISIBLE);
      led1.setEnabled(true);
      led2.setEnabled(true);
      led3.setEnabled(true);
      led4.setEnabled(true);
    }
  }

  @UiThread
  private void handleRefreshSuccess(@NonNull Instances<LightControl> leds,
                                    @NonNull Instances<IPSODigitalInput> buttons,
                                    @NonNull Instances<Temperature> temperatures,
                                    @NonNull Instances<AnalogInput> analogInputs) {
    if (isAdded()) {

      List<LightControl> ledItems = leds.getItems();
      ledStates[0] = ledItems.get(0).isOnOff();
      ledStates[1] = ledItems.get(1).isOnOff();
      ledStates[2] = ledItems.get(2).isOnOff();
      ledStates[3] = ledItems.get(3).isOnOff();
      buttonStates[0] = buttons.getItems().get(0).getState();
      buttonStates[1] = buttons.getItems().get(1).getState();
      temperatureValues[0] = temperatures.getItems().get(0).getSensorValue();
      analogInputValues[0] = analogInputs.getItems().get(0).getCurrentValue();
      updateUI();

    }
  }

  @UiThread
  private void handleRefreshFailure(DeviceServerException e) {
    //hideProgressDialog();
    int resID;
    if (e instanceof UnauthorizedException) {
      resID = R.string.error_unauthorized;
    } else if (e instanceof ParseException) {
      resID = R.string.error_invalid_response;
    } else if (e instanceof NetworkException) {
      resID = R.string.error_network;
    } else if (e instanceof NotFoundException) {
      handler.removeCallbacks(refreshTask);
      showDeviceOfflineDialog();
      return;
    } else {
      resID = R.string.error_unknown;
    }
    if (isAdded()) {
      ActivitiesAndFragmentsHelper.showToast(getContext(), resID, handler);
    }
  }

  @OnClick({R.id.led1, R.id.led2, R.id.led3, R.id.led4})
  void onLedClicked(ImageView imageView) {
    showProgressDialog();
    int instanceId = 0;

    switch (imageView.getId()) {
      case R.id.led1:
        instanceId = 0;
        break;
      case R.id.led2:
        instanceId = 1;
        break;
      case R.id.led3:
        instanceId = 2;
        break;
      case R.id.led4:
        instanceId = 3;
        break;
    }
    final LightControl lightControl = new LightControl();
    lightControl.setOnOff(!ledStates[instanceId]);

    final int finalInstanceId = instanceId;

    executor.submit(new Runnable() {
      @Override
      public void run() {
        try {
          Client client = caller.getClient(deviceHelper.getDevice().getName()).get();
          caller.updateInstance(client, LIGHT_CONTROL_OBJECT_ID, finalInstanceId, lightControl, new TypeToken<LightControl>() {
          });
          ledStates[finalInstanceId] = !ledStates[finalInstanceId];
          handler.post(new Runnable() {
            @Override
            public void run() {
              hideProgressDialog();
              updateUI();
            }
          });
        } catch (ExecutionException e) {
          handleSetLedFailure((DeviceServerException) e.getCause());
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });
  }

  private void handleSetLedFailure(final DeviceServerException e) {
    handler.post(new Runnable() {
      @Override
      public void run() {
        hideProgressDialog();
        if (e instanceof NotFoundException) {
          handler.removeCallbacks(refreshTask);
          showDeviceOfflineDialog();
        } else if (e instanceof NetworkException) {
          ActivitiesAndFragmentsHelper.showToast(getContext(), R.string.error_network, handler);
        } else {
          ActivitiesAndFragmentsHelper.showToast(getContext(), R.string.error_unknown, handler);
        }
      }
    });
  }

  private void showDeviceOfflineDialog() {
    if (!isAdded()) {
      return;
    }
    new AlertDialog.Builder(getContext())
        .setTitle(R.string.device_is_offline)
        .setMessage(R.string.device_is_offline_msg)
        .setPositiveButton(R.string.back_to_connected_devices, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            ActivitiesAndFragmentsHelper.replaceFragmentWithBackStackClear((BaseActivity) getActivity(), SimpleFragmentFactory.createFragment(ConnectedDevicesFragment.TAG));
          }
        }).create().show();

  }

  @UiThread
  private void updateUI() {
    if (!isAdded()) {
      return;
    }
    handler.post(new Runnable() {
      @Override
      public void run() {
        led1.setImageResource(ledStates[0] ? R.drawable.led_on : R.drawable.led_off);
        led2.setImageResource(ledStates[1] ? R.drawable.led_on : R.drawable.led_off);
        led3.setImageResource(ledStates[2] ? R.drawable.led_on : R.drawable.led_off);
        led4.setImageResource(ledStates[3] ? R.drawable.led_on : R.drawable.led_off);
        sw1.setImageResource(buttonStates[0] ? R.drawable.button_on : R.drawable.button_off);
        sw2.setImageResource(buttonStates[1] ? R.drawable.button_on : R.drawable.button_off);
        tempValue.setText(getString(R.string.celsius_value, String.format("%.2f", temperatureValues[0])));
        potentiometerValue.setText(getString(R.string.volt_value, String.format("%.2f", analogInputValues[0])));
      }
    });

  }

  private Runnable refreshTask = new Runnable() {
    @Override
    public void run() {
      refresh();
    }
  };


}
