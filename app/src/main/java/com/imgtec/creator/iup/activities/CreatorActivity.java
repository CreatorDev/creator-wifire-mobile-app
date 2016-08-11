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


package com.imgtec.creator.iup.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.View;
import android.widget.Toast;

import com.imgtec.creator.iup.App;
import com.imgtec.creator.iup.R;
import com.imgtec.creator.iup.di.component.AppComponent;
import com.imgtec.creator.iup.wifire.DeviceHelper;
import com.imgtec.creator.iup.fragments.menu.setupguide.BoardConnectedChoiceDialogFragment;
import com.imgtec.creator.iup.fragments.navigationdrawer.NDMenuFragment;
import com.imgtec.creator.iup.fragments.navigationdrawer.NDMenuItem;
import com.imgtec.creator.iup.fragments.navigationdrawer.NDMenuListener;
import com.imgtec.creator.iup.retrofit.pojos.softap.DeviceName;
import com.imgtec.creator.iup.utils.NDMenuMode;
import com.imgtec.creator.iup.utils.SetupGuideInfoSingleton;
import com.imgtec.creator.iup.utils.WifiUtil;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Activity seen when we are logged in to Flow.
 * --------------------------------|
 * Navigation|       Content       |
 * Drawer    |                     |
 * |                     |
 * |                     |
 * |                     |
 * |                     |
 * |                     |
 * |                     |
 * |                     |
 * |                     |
 * |                     |
 * |                     |
 * |                     |
 * |                     |
 * _________________________________
 */

public class CreatorActivity extends BaseToolbarActivity implements NDMenuListener {

  NDMenuFragment leftNDMenuFragment;

  /**
   * drawerToggle is an object on the ActionBar, which is used to change Navigation Drawer visibility.
   */
  private ActionBarDrawerToggle drawerToggle;
  private CharSequence actionBarTitle;
  private long lastBackClickTime = 0;

  @Inject WifiUtil wifiUtil;
  @Inject DeviceHelper deviceHelper;
  @Inject
  @Named("UI")
  Handler handler;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initUI();
  }

  private void initUI() {
    setContentView(R.layout.actv_creator);
    ButterKnife.bind(this);
    initActionBar();

    DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    leftNDMenuFragment = (NDMenuFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
    leftNDMenuFragment.setUp(R.id.navigation_drawer, drawerLayout);
    drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
      public void onDrawerClosed(View view) {

      }

      public void onDrawerOpened(View drawerView) {
      }
    };
    drawerLayout.addDrawerListener(drawerToggle);
    setUIMode(NDMenuMode.Initial);
  }

  @Override
  protected void initActionBar() {
    super.initActionBar();
    setSupportActionBar(toolbar);
    if (actionBarTitle == null) {
      actionBarTitle = getApplicationContext().getString(R.string.connected_devices);
    }
    toolbar.setTitle(actionBarTitle);
  }

  @Override
  protected void onResume() {
    super.onResume();
    setProperUIMode();
    setToolbarProgressbarVisibility(false);
    if (wifiUtil.isBoardConnected()) {
      setToolbarProgressbarVisibility(true);
      performGetDeviceNameRequest();
    }
  }

  @Override
  public void onPause() {
    super.onPause();
  }

  private void performGetDeviceNameRequest() {
    ((App) getApplication()).getSoftAPRetrofitService().getDeviceName().enqueue(new Callback<DeviceName>() {
      @Override
      public void onResponse(Call<DeviceName> call, Response<DeviceName> response) {
        setToolbarProgressbarVisibility(false);
        SetupGuideInfoSingleton.setDeviceName(response.body().getName());
        setUIMode(NDMenuMode.Setup);
        showDialog();
      }

      @Override
      public void onFailure(Call<DeviceName> call, Throwable t) {
        setToolbarProgressbarVisibility(false);
        ActivitiesAndFragmentsHelper.showToast(getApplicationContext(), R.string.check_connectivity, handler);
      }
    });
  }

  private void showDialog() {
    BoardConnectedChoiceDialogFragment.newInstance().show(getSupportFragmentManager(), BoardConnectedChoiceDialogFragment.TAG);
  }

  /**
   * -------------------------------drawerToggle functions start-----------------------------------
   */
  @Override
  public boolean onOptionsItemSelected(android.view.MenuItem item) {
    // The action bar home/up action should open or close the drawer.
    // ActionBarDrawerToggle will take care of this.
    return (drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item));
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    // Sync the toggle state after onRestoreInstanceState has occurred.
    drawerToggle.syncState();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    drawerToggle.onConfigurationChanged(newConfig);
  }
  /**-------------------------------drawerToggle functions end------------------------------------*/

  /**
   * Change ActionBar title for selected NDMenuItem
   *
   * @param menuItem chosen NavigationDrawer item
   */
  @Override
  public void onSelectionAndTitleChange(NDMenuItem menuItem) {
    leftNDMenuFragment.setSelectionState(menuItem);
    if (menuItem.getTextId() > 0) { //if text for menuItem exists
      onTitleChange(getApplicationContext().getString(menuItem.getTextId()));
    }
  }

  @Override
  public void onTitleChange(String title) {
    actionBarTitle = title;
    toolbar.setTitle(actionBarTitle);
  }

  /**
   * Replace shown fragment
   *
   * @param fragment - new fragment
   */
  @Override
  public void onFragmentChange(Fragment fragment) {
    ActivitiesAndFragmentsHelper.replaceFragment(this, fragment);
  }

  /**
   * Replace shown fragment with backstack clear
   *
   * @param fragment - new fragment
   */
  @Override
  public void onFragmentChangeWithBackstackClear(Fragment fragment) {
    ActivitiesAndFragmentsHelper.replaceFragmentWithBackStackClear(this, fragment);
  }

  /**
   * There are 3 options of NavigationDrawer MenuItems:
   * {@link com.imgtec.creator.iup.fragments.navigationdrawer.NDMenuItem#initialValues()} ()}
   * {@link com.imgtec.creator.iup.fragments.navigationdrawer.NDMenuItem#wifiNetworkModeValues()} ()}
   * {@link com.imgtec.creator.iup.fragments.navigationdrawer.NDMenuItem#interactiveModeValues()} ()}
   * and this states change drawer only after calling this function:
   */
  @Override
  public void onMenuChange(NDMenuMode mode) {
    leftNDMenuFragment.restartNavigationDrawer(mode);
  }

  /**
   * Should be used carefully, because it change the NDMenuFragment menu items!
   */
  public void setUIMode(NDMenuMode mode) {
    onMenuChange(mode);
  }

  public void setInteractiveToInitialMode() {
    if (NDMenuMode.getMode() == NDMenuMode.Interactive) {
      setUIMode(NDMenuMode.Initial);
    }
  }

  @Override
  public void onBackPressed() {
    if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
      if (System.currentTimeMillis() - lastBackClickTime < 3000) {
        super.onBackPressed();
      } else {
        Toast.makeText(this, R.string.tap_again_to_exit, Toast.LENGTH_LONG).show();
        lastBackClickTime = System.currentTimeMillis();
      }
    } else {
      super.onBackPressed();
    }
  }

  /**
   * Sets home button into one of two modes. True means up navigation, false means navigation drawer.
   *
   * @param upEnabled
   */
  public void setUpNavigationEnabled(boolean upEnabled) {
    if (upEnabled) {
      //trick to get up button icon to show
      drawerToggle.setDrawerIndicatorEnabled(false);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      drawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          getSupportFragmentManager().popBackStack();
        }
      });
    } else {
      drawerToggle.setDrawerIndicatorEnabled(true);

    }
  }

  private void setProperUIMode() {
    if (wifiUtil.isBoardConnected()) {
      setUIMode(NDMenuMode.Setup);
    } else if (deviceHelper.getDevice() != null) {
      setUIMode(NDMenuMode.Interactive);
    } else {
      setUIMode(NDMenuMode.Initial);
    }
  }

  @Override
  void setupComponent(AppComponent appComponent) {
    appComponent.inject(this);
  }
}
