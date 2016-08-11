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

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.util.concurrent.ListenableFuture;
import com.imgtec.creator.iup.R;
import com.imgtec.creator.iup.activities.CreatorActivity;
import com.imgtec.creator.iup.di.component.AppComponent;
import com.imgtec.creator.iup.ds.DSService;
import com.imgtec.creator.iup.ds.pojo.Client;
import com.imgtec.creator.iup.ds.pojo.Clients;
import com.imgtec.creator.iup.fragments.menu.ConnectedDevicesFragment;
import com.imgtec.creator.iup.fragments.navigationdrawer.NDListeningFragment;
import com.imgtec.creator.iup.fragments.navigationdrawer.NDMenuItem;
import com.imgtec.creator.iup.utils.AnimationUtils;
import com.imgtec.creator.iup.utils.BroadcastReceiverWithRegistrationState;
import com.imgtec.creator.iup.utils.Constants;
import com.imgtec.creator.iup.utils.NDMenuMode;
import com.imgtec.creator.iup.utils.SetupGuideInfoSingleton;
import com.imgtec.creator.iup.utils.SimpleFragmentFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Fragment showing WiFire board registration progress. After board successfully registers in Flow,
 * {@link com.imgtec.creator.iup.fragments.menu.ConnectedDevicesFragment} is automatically shown.
 */
public class ConnectingFragment extends NDListeningFragment {

  public static final String TAG = "ConnectingFragment";

  @BindView(R.id.ledsAnimation) ImageView ledsAnimation;
  @BindView(R.id.connectionFailed) TextView connectionFailed;
  @BindView(R.id.connecting) TextView connecting;
  @BindView(R.id.cancelButton) Button cancelButton;
  @BindView(R.id.tryAgainButton) Button tryAgainButton;
  @BindView(R.id.doneButton) Button doneButton;
  @BindView(R.id.doneButtonContainer) LinearLayout doneButtonContainer;

  Unbinder unbinder;

  @Inject
  @Named("UI")
  Handler handler;
  @Inject DSService caller;

  private NetworkChangeReceiver networkChangeReceiver =
      new NetworkChangeReceiver(new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
  private Runnable waitForConnectionRunnable;
  private Runnable waitForDeviceRunnable;
  private BoardState currentBoardState = BoardState.WIFI_CONNECTING;

  /**
   * Possible states. WIFI_CONNECTED_MOCK, DS_CONNECTING_MOCK are mocked, because we cannot check them easily.
   * Device and mobile are in the same WiFi and mobile should know somehow how to get information about
   * WiFi network devices connected. It is possible, but for now we left it mocked.
   * {@link #DS_CONNECTING_MOCK} is actually {@link #DS_CONNECTED}.
   * <p>
   * Can be upgraded IN THE FUTURE.
   */
  private enum BoardState {
    WIFI_CONNECTING, WIFI_CONNECTED_MOCK, DS_CONNECTING_MOCK, TAKING_LONG, DS_CONNECTED
  }

  public static ConnectingFragment newInstance() {
    return new ConnectingFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.frag_connecting, container, false);
    unbinder = ButterKnife.bind(this, rootView);
    return rootView;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    currentBoardState = BoardState.WIFI_CONNECTING;
    initUIActions();
    updateUI();
  }

  @Override
  public void onDestroyView() {
    unbinder.unbind();
    super.onDestroyView();
  }

  private void initUIActions() {

    cancelButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        networkChangeReceiver.unregister(appContext);
        if (menuListener != null) {
          menuListener.onFragmentChangeWithBackstackClear(SimpleFragmentFactory.createFragment(ConnectedDevicesFragment.TAG));
        }
      }
    });
    tryAgainButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        networkChangeReceiver.unregister(appContext);
        if (menuListener != null) {
          menuListener.onFragmentChangeWithBackstackClear(SimpleFragmentFactory.createFragment(SetupModeFragment.TAG));
          ((CreatorActivity) menuListener).setUIMode(NDMenuMode.Initial);
        }
      }
    });
    doneButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        networkChangeReceiver.unregister(appContext);
        if (menuListener != null) {
          menuListener.onSelectionAndTitleChange(NDMenuItem.ConnectedDevices);
          menuListener.onFragmentChangeWithBackstackClear(SimpleFragmentFactory.createFragment(ConnectedDevicesFragment.TAG));
        }
      }
    });
  }

  private void updateUI() {
    switch (currentBoardState) {

      case WIFI_CONNECTING:
        AnimationUtils.startAnimation(ledsAnimation, R.drawable.led12on3flashing);
        connectionFailed.setVisibility(View.GONE);
        connecting.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.GONE);
        tryAgainButton.setVisibility(View.GONE);
        break;
      case WIFI_CONNECTED_MOCK:
        AnimationUtils.startAnimation(ledsAnimation, R.drawable.led12on3flashing);
        break;
      case DS_CONNECTING_MOCK:
        AnimationUtils.startAnimation(ledsAnimation, R.drawable.led123on4flashing);
        break;
      case TAKING_LONG:
        AnimationUtils.animateViewSetVisible(true, cancelButton);
        AnimationUtils.animateViewSetVisible(true, tryAgainButton);
        AnimationUtils.animateViewSetVisible(true, connectionFailed);
        AnimationUtils.animateViewSetVisible(false, connecting);
        //AnimationUtils.animateViewSetVisible(false, ledsAnimation);
        ledsAnimation.setVisibility(View.INVISIBLE);
        doneButtonContainer.setVisibility(View.GONE);
        break;
      case DS_CONNECTED:
        ledsAnimation.setBackgroundResource(R.drawable.leds_1234_lit);
        connecting.setText(R.string.device_setup_succeeded);
        doneButton.setVisibility(View.VISIBLE);
        break;
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    ((CreatorActivity) menuListener).setUIMode(NDMenuMode.Initial);
    enableChosenNetwork();
    startConnecting();
  }

  private void startConnecting() {
    menuListener.onTitleChange(appContext.getString(R.string.device_setup));
    networkChangeReceiver.register(appContext);
    waitForConnectionRunnable = new Runnable() {
      @Override
      public void run() {
        if (currentBoardState != BoardState.DS_CONNECTING_MOCK && currentBoardState != BoardState.DS_CONNECTED) {
          currentBoardState = BoardState.TAKING_LONG;
          updateUI();
          handler.removeCallbacks(this);
        }
      }
    };
    handler.postDelayed(waitForConnectionRunnable, 3 * Constants.SIXTY_SECONDS_MILLIS);
  }

  private void enableChosenNetwork() {
    String ssid = "\"" + SetupGuideInfoSingleton.getSsid() + "\"";
    wifiUtil.enableChosenNetwork(ssid);
  }

  @Override
  public void onPause() {
    handler.removeCallbacks(waitForConnectionRunnable);
    handler.removeCallbacks(waitForDeviceRunnable);
    networkChangeReceiver.unregister(appContext);
    super.onPause();
  }

  private class NetworkChangeReceiver extends BroadcastReceiverWithRegistrationState {
    private static final int DELAY_TIME = 5000;
    private int priorCount;
    private Date expiryTime;

    public NetworkChangeReceiver(IntentFilter intentFilter) {
      super(intentFilter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      if (wifiUtil.isInternetNotBoardConnected()) {
        if (currentBoardState == BoardState.WIFI_CONNECTING) {
          this.unregister(appContext);
          startCheckingDeviceConnectedToDeviceServer();
        }
      }
    }

    /**
     * For future's sake: background thread could not work correctly on Android API < 11 in BroadcastReceiver's onReceive().
     */
    private void startCheckingDeviceConnectedToDeviceServer() {
      handler.post(new Runnable() {
        @Override
        public void run() {
          currentBoardState = BoardState.WIFI_CONNECTED_MOCK;
          updateUI();
        }
      });

      //ToDo decide what to do
      //priorCount = flowDeviceHelper.getCachedDevices().size();
      Calendar cal = Calendar.getInstance();
      cal.setTime(new Date());
      cal.add(Calendar.SECOND, 60);
      expiryTime = cal.getTime();


      waitForDeviceRunnable = new Runnable() {
        @Override
        public void run() {
          try {
            ListenableFuture<Clients> future = caller.getClients(0, 100);
            Clients clients = future.get();
            for (Client client : clients.getItems()) {
              if (client.getName().equals(SetupGuideInfoSingleton.getBoardSsid())) {
                currentBoardState = BoardState.DS_CONNECTED;
                handler.post(new Runnable() {
                  @Override
                  public void run() {
                    updateUI();

                  }
                });

              }
            }
            if (new Date().before(expiryTime)) {
              handler.postDelayed(waitForDeviceRunnable, DELAY_TIME);
            }

          } catch (ExecutionException e) {
            //ToDo handle this exception properly
            e.printStackTrace();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      };
      handler.postDelayed(waitForDeviceRunnable, DELAY_TIME);
    }

  }


  @Override
  protected void setupComponent(AppComponent appComponent) {
    appComponent.inject(this);
  }
}
