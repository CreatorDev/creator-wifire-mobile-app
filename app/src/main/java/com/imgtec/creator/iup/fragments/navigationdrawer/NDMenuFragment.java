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


package com.imgtec.creator.iup.fragments.navigationdrawer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.imgtec.creator.iup.R;
import com.imgtec.creator.iup.activities.ActivitiesAndFragmentsHelper;
import com.imgtec.creator.iup.activities.LogInActivity;
import com.imgtec.creator.iup.adapters.NDMenuAdapter;
import com.imgtec.creator.iup.di.component.AppComponent;
import com.imgtec.creator.iup.wifire.DeviceHelper;
import com.imgtec.creator.iup.fragments.menu.AboutFragment;
import com.imgtec.creator.iup.fragments.menu.ConnectedDevicesFragment;
import com.imgtec.creator.iup.fragments.menu.DeviceInfoFragment;
import com.imgtec.creator.iup.fragments.menu.CreatorDeviceInfoFragment;
import com.imgtec.creator.iup.fragments.menu.InteractiveFragment;
import com.imgtec.creator.iup.fragments.menu.setupguide.LogInToWifiFragment;
import com.imgtec.creator.iup.fragments.menu.setupguide.SetUpWifireDeviceFragment;
import com.imgtec.creator.iup.utils.NDMenuMode;
import com.imgtec.creator.iup.utils.Preferences;
import com.imgtec.creator.iup.utils.SimpleFragmentFactory;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * NavigationDrawer with 3 possible states. Adapter for list of them is created in
 * {@link #restartNavigationDrawer(NDMenuMode menu)}.
 * States are of type {@link com.imgtec.creator.iup.utils.NDMenuMode}.
 * <p>
 * There is also item selection, as well as appropriate fragment selection provided.
 */
public class NDMenuFragment extends NDListeningFragment {

  public static final String TAG = "NDMenuFragment";

  private DrawerLayout drawerLayout;
  private View fragmentContainerView;
  @BindView(R.id.drawerListView) ListView drawerListView;
  private NDMenuAdapter ndMenuAdapter;

  @Inject
  @Named("UI")
  Handler handler;
  @Inject DeviceHelper deviceHelper;
  @Inject Preferences preferences;

  Unbinder unbinder;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.frag_navigation_drawer, container, false);
    unbinder = ButterKnife.bind(this, rootView);

    drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectFragment((NDMenuItem) ndMenuAdapter.getItem(position));
      }
    });
    restartNavigationDrawer(NDMenuMode.Initial);
    return rootView;
  }

  @Override
  public void onResume() {
    super.onResume();
    restartNavigationDrawer(NDMenuMode.getMode());
  }

  public void restartNavigationDrawer(NDMenuMode mode) {
    NDMenuMode.setMode(mode);
    switch (mode) {
      case Initial:
        ndMenuAdapter = new NDMenuAdapter((Activity) menuListener, NDMenuItem.initialValues(), preferences, deviceHelper);
        break;
      case Setup:
        ndMenuAdapter = new NDMenuAdapter((Activity) menuListener, NDMenuItem.wifiNetworkModeValues(), preferences, deviceHelper);
        break;
      case Interactive:
        ndMenuAdapter = new NDMenuAdapter((Activity) menuListener, NDMenuItem.interactiveModeValues(), preferences, deviceHelper);
        break;
      default:
        ndMenuAdapter = new NDMenuAdapter((Activity) menuListener, NDMenuItem.initialValues(), preferences, deviceHelper);
    }

    drawerListView.setAdapter(ndMenuAdapter);
  }

  /**
   * Users of this fragment must call this method to set up the navigation drawer interactions.
   *
   * @param fragmentId   The android:id of this fragment in its activity's layout.
   * @param drawerLayout The DrawerLayout containing this fragment's UI.
   */
  public void setUp(int fragmentId, DrawerLayout drawerLayout) {
    fragmentContainerView = getActivity().findViewById(fragmentId);
    this.drawerLayout = drawerLayout;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    setHasOptionsMenu(true); // Indicate that this fragment would like to influence the set of actions in the action bar.
    selectInitializationFragment(NDMenuItem.getCheckedItem());
  }

  private void selectInitializationFragment(NDMenuItem menuItem) {
    closeDrawer();
    if (menuListener != null) {
      showInitializationFragment(menuItem);
    }
  }

  private void showInitializationFragment(NDMenuItem menuItem) {
    String tag = getSelectedItemTag(menuItem);
    Fragment fragment = getFragmentManager().findFragmentByTag(tag);
    if (fragment == null) {
      fragment = SimpleFragmentFactory.createFragment(tag);
      menuListener.onFragmentChangeWithBackstackClear(fragment);
    }
    menuListener.onSelectionAndTitleChange(menuItem);
  }

  private void selectFragment(NDMenuItem menuItem) {
    closeDrawer();
    if (menuListener != null) {
      if (!menuItem.equals(NDMenuItem.SignOut)) {
        showFragment(menuItem);
      } else {
        logout();
      }
    }
  }

  private void showFragment(NDMenuItem menuItem) {
    String tag = getSelectedItemTag(menuItem);

    menuListener.onFragmentChangeWithBackstackClear(SimpleFragmentFactory.createFragment(tag));
    menuListener.onSelectionAndTitleChange(menuItem);
  }

  private String getSelectedItemTag(NDMenuItem menuItem) {
    String tag = "";
    switch (menuItem) {
      case MyDevice:
        if (NDMenuMode.getMode() == NDMenuMode.Setup) {
          tag = DeviceInfoFragment.TAG;
        } else if (NDMenuMode.getMode() == NDMenuMode.Interactive) {
          tag = CreatorDeviceInfoFragment.TAG;
        }
        break;
      case Commands:
        tag = InteractiveFragment.TAG;
        break;
      case ConfigureWifi:
        tag = LogInToWifiFragment.TAG;
        break;
      case ConnectedDevices:
        tag = ConnectedDevicesFragment.TAG;
        break;
      case SetupDevice:
        tag = SetUpWifireDeviceFragment.TAG;
        break;
      case About:
        tag = AboutFragment.TAG;
        break;
      case SignOut:
        break;
      default:
        tag = ConnectedDevicesFragment.TAG;
    }
    return tag;
  }

  private void logout() {
    logoutUser();
  }

  private void logoutUser() {
    NDMenuMode.setMode(NDMenuMode.Initial);
    preferences.setAutologin(false);
    preferences.setRefreshToken("");
    preferences.setAccessToken("");
    afterLogoutUser();
  }

  private void afterLogoutUser() {
    handler.post(new Runnable() {
      @Override
      public void run() {
        ActivitiesAndFragmentsHelper.startActivityAndFinishPreviousOne((Activity) menuListener, new Intent(getActivity(), LogInActivity.class));
        setSelectionState(NDMenuItem.ConnectedDevices); //set drawer menu to have default selection when this activity is resumed
      }
    });
  }


  public void setSelectionState(NDMenuItem menuItem) {

    NDMenuItem.setCheckedItem(menuItem);
    if (ndMenuAdapter != null) {
      ndMenuAdapter.notifyDataSetChanged();
    }
  }

  private void closeDrawer() {
    if (drawerLayout != null) {
      drawerLayout.closeDrawer(fragmentContainerView);
    }
  }

  @Override
  public void onPause() {
    super.onPause();
  }

  @Override
  protected void setupComponent(AppComponent appComponent) {
    appComponent.inject(this);
  }
}
