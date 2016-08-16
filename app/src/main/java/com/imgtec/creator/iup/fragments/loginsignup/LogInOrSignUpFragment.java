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


package com.imgtec.creator.iup.fragments.loginsignup;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.imgtec.creator.iup.R;
import com.imgtec.creator.iup.activities.ActivitiesAndFragmentsHelper;
import com.imgtec.creator.iup.activities.CreatorActivity;
import com.imgtec.creator.iup.activities.LogInActivity;
import com.imgtec.creator.iup.di.component.AppComponent;
import com.imgtec.creator.iup.ds.DSService;
import com.imgtec.creator.iup.ds.exceptions.NetworkException;
import com.imgtec.creator.iup.ds.exceptions.NotFoundException;
import com.imgtec.creator.iup.ds.exceptions.UnauthorizedException;
import com.imgtec.creator.iup.ds.pojo.CreatorVoid;
import com.imgtec.creator.iup.fragments.common.FragmentWithProgressBar;
import com.imgtec.creator.iup.utils.Constants;
import com.imgtec.creator.iup.utils.Preferences;

import java.util.UUID;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class LogInOrSignUpFragment extends FragmentWithProgressBar {

  public static final String TAG = "LogInOrSignUpFragment";

  @BindView(R.id.logIn) Button logIn;
  @BindView(R.id.keepLoggedIn) CheckBox keepLoggedIn;

  @Inject
  DSService dsService;

  @Inject
  Preferences preferences;

  boolean allowAutologin = true;

  private Runnable loginTimeoutRunnable;

  Unbinder unbinder;

  public static Fragment newInstance() {
    return new LogInOrSignUpFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    setHasOptionsMenu(true);
    View view = inflater.inflate(R.layout.frag_log_in_or_sign_up, container, false);
    unbinder = ButterKnife.bind(this, view);
    return view;
  }

  @Override
  protected String getActionBarTitleText() {
    return getString(R.string.welcome);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    keepLoggedIn.setChecked(preferences.getAutologin());
  }

  @Override
  public void onResume() {
    super.onResume();
    ((LogInActivity) getActivity()).getToolbar().setVisibility(View.GONE);
    ((LogInActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    ((LogInActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
    initListeners();
    if (dsService.isAutoLoginEnabled() && allowAutologin) {
      allowAutologin = false;
      showProgress(appContext.getString(R.string.logging_in));
      loginUsingRefreshToken();
    }
  }

  @Override
  protected void setupComponent(AppComponent appComponent) {
    appComponent.inject(this);
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
  protected void onNetworkStateChanged() {

  }

  private void login() {

    final String client_id ="41e2bb5a-8e66-43dc-a4bb-31b7a9041b8f";
    final Uri redirectUri = Uri.parse("io.creatordev.iup:/callback");
    final String nonce = UUID.randomUUID().toString();
    final String state = "dummy_state";

    Intent browserIntent = new Intent(Intent.ACTION_VIEW,
        Uri.parse("https://id.creatordev.io/oauth2/auth?"+
            "client_id=" + client_id + "&" +
            "scope=core+openid+offline&" +
            "redirect_uri=" + redirectUri + "&" +
            "state=" + state + "&" +
            "nonce=" + nonce + "&" +
            "response_type=id_token"));
    browserIntent.setFlags(browserIntent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
    startActivity(browserIntent);

  }

  private void loginUsingRefreshToken() {
    loginTimeoutRunnable = new Runnable() {
      @Override
      public void run() {
        hideProgress();
        ActivitiesAndFragmentsHelper.showToast(appContext, R.string.error_network, handler);
        handler.removeCallbacks(this);
      }
    };
    handler.postDelayed(loginTimeoutRunnable, Constants.SIXTY_SECONDS_MILLIS / 2);


    ListenableFuture<CreatorVoid> future = dsService.login();
    Futures.addCallback(future, new FutureCallback<CreatorVoid>() {
      @Override
      public void onSuccess(final CreatorVoid result) {
        getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
            hideProgress();
            handler.removeCallbacks(loginTimeoutRunnable);
            afterLogin();
          }
        });
      }

      @Override
      public void onFailure(Throwable t) {
        handleLoginFailure(t);
      }
    });
  }

  public void handleLoginFailure(final Throwable t) {
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        hideProgress();
        handler.removeCallbacks(loginTimeoutRunnable);
        int errorStringID;
        if (t instanceof UnauthorizedException) {
          errorStringID = R.string.error_login_unauthorized;
        } else if (t instanceof NotFoundException) {
          errorStringID = R.string.error_login_not_found;
        } else if (t instanceof NetworkException) {
          errorStringID = R.string.error_network;
        } else {
          errorStringID = R.string.error_unknown;
        }

        ActivitiesAndFragmentsHelper.showToast(appContext, errorStringID, handler);
      }
    });
  }


  private void afterLogin() {
    handler.post(new Runnable() {
      @Override
      public void run() {

        final Activity activity = getActivity();
        if (activity != null) {
          ActivitiesAndFragmentsHelper.startActivityAndFinishPreviousOne(activity, new Intent(activity, CreatorActivity.class));
        }

      }
    });
  }


  private void initListeners() {
    logIn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        showProgress(appContext.getString(R.string.logging_in));
        login();
      }
    });

  }

  public boolean isRememberMechecked() {
    return keepLoggedIn.isChecked();
  }

}
