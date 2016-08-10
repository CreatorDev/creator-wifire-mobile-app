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


package com.imgtec.hobbyist.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.fragments.navigationdrawer.NDMenuItem;

import org.slf4j.LoggerFactory;

/**
 * Class defining operations which often happen in Activities and Fragments throughout the app.
 */
public class ActivitiesAndFragmentsHelper {

  static AlertDialog.Builder dialogBuilder;

  /**
   * Start new activity and finish previous one.
   *
   * @param activity is current activity to finish
   * @param intent   of new activity to start
   */
  public static void startActivityAndFinishPreviousOne(Activity activity, Intent intent) {
    activity.startActivity(intent);
    activity.finish();
  }

  /**
   * Replaces current fragment with fragment from second parameter.
   *
   * @param activity current activity
   * @param fragment to display
   */
  public static void replaceFragment(BaseActivity activity, Fragment fragment) {
    if (activity == null) {
      LoggerFactory.getLogger(ActivitiesAndFragmentsHelper.class).warn("Skipping replaceFragment. Activity is null!");
      return;
    }
    FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
    fragmentTransaction.replace(R.id.content, fragment);
    fragmentTransaction.addToBackStack(null);
    fragmentTransaction.commitAllowingStateLoss();

  }

  public static void replaceFragment(BaseActivity activity, Fragment fragment, boolean addToBackstack) {
    if (activity == null) {
      LoggerFactory.getLogger(ActivitiesAndFragmentsHelper.class).warn("Skipping replaceFragment. Activity is null!");
      return;
    }
    FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
    fragmentTransaction.replace(R.id.content, fragment);
    if (addToBackstack)
      fragmentTransaction.addToBackStack(null);
    fragmentTransaction.commitAllowingStateLoss();

  }

  /**
   * Replaces current fragment with fragment from second parameter and clears backstack.
   *
   * @param activity current activity
   * @param fragment to display
   */
  public static void replaceFragmentWithBackStackClear(BaseActivity activity, Fragment fragment) {
    if (activity == null) {
      LoggerFactory.getLogger(ActivitiesAndFragmentsHelper.class).warn("Skipping replaceFragmentWithBackStackClear. Activity is null!");
      return;
    }
    FragmentManager fragmentManager = activity.getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    clearBackStack(fragmentManager);
    fragmentTransaction.replace(R.id.content, fragment, fragment.getClass().getSimpleName());
    fragmentTransaction.commitAllowingStateLoss();
  }

  /**
   * Removes specified fragment from backstack.
   *
   * @param activity current activity
   * @param fragment to remove
   */
  public static void removeFragment(BaseActivity activity, Fragment fragment) {
    FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
    fragmentTransaction.remove(fragment);
    fragmentTransaction.commitAllowingStateLoss();
    activity.getSupportFragmentManager().popBackStack();
  }

  public static void popFragment(BaseActivity activity) {
    activity.getSupportFragmentManager().popBackStack();
  }

  /**
   * Used to clear the fragments back stack. Particularly helpful when we click another NDMenuItem
   */
  private static void clearBackStack(FragmentManager fragmentManager) {
    fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
  }

  /**
   * Function use through the app to show Toast information. Function is called so often from
   * background, that it's content is explicitly called from UIThread.
   *
   * @param appContext       - application context
   * @param stringResourceId of text we show
   * @param handler          to UIThread
   */
  public static void showToast(final Context appContext, final int stringResourceId, Handler handler) {
    handler.post(new Runnable() {
      @Override
      public void run() {
        if (appContext != null) {
          Toast.makeText(appContext, appContext.getString(stringResourceId), Toast.LENGTH_SHORT).show();
        }
      }
    });
  }

  /**
   * To hide system's keyboard.
   */
  public static void hideSoftInput(Context appContext, View... views) {
    InputMethodManager inputManager = (InputMethodManager) appContext.getSystemService(Context.INPUT_METHOD_SERVICE);
    for (View view : views) {
      inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
  }

  /**
   * Shows dialog window.
   * After dismissing, current fragment will be replaced with specified fragment,
   * and state of the navigation drawer will be reset to Initial.
   * Backstack is not preserved.
   *
   * @param titleTextID
   * @param messageTextID
   * @param activity
   * @param fragment      - Fragment to appear after dismiss
   */
  public static void showFragmentChangeDialog(final int titleTextID, final int messageTextID,
                                              final Activity activity, final Fragment fragment) {
    if (activity != null) {
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (dialogBuilder == null) {
            dialogBuilder = new AlertDialog.Builder(activity);
            dialogBuilder.setPositiveButton(messageTextID, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                ((CreatorActivity) activity).setInteractiveToInitialMode();
                ((CreatorActivity) activity).onFragmentChangeWithBackstackClear(fragment);
                ((CreatorActivity) activity).onSelectionAndTitleChange(NDMenuItem.ConnectedDevices);
                dialogBuilder = null;
              }
            }).setTitle(titleTextID).setCancelable(false).create().show();
          }
        }
      });
    }
  }


  public static void showWifiSettingsChangeDialog(final String titleText, final String messageText,
                                                  final String buttonText, final Activity activity) {

    if (activity != null) {
      if (dialogBuilder == null) {
        dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder
            .setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialogBuilder = null;
                activity.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
              }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialogBuilder = null;
                dialog.dismiss();
              }
            })
            .setTitle(titleText)
            .setMessage(messageText)
            .setCancelable(true).create().show();
      }
    }

  }

}
