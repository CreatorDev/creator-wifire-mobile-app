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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.di.component.AppComponent;
import com.imgtec.hobbyist.ds.DSService;
import com.imgtec.hobbyist.ds.pojo.CreatorVoid;
import com.imgtec.hobbyist.fragments.loginsignup.LogInOrSignUpFragment;
import com.imgtec.hobbyist.fragments.loginsignup.SignUpFragment;
import com.imgtec.hobbyist.utils.Preferences;
import com.imgtec.hobbyist.utils.SimpleFragmentFactory;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Simple activity encapsulated LogIn, SignUp and Settings fragments.
 */
public class LogInActivity extends BaseToolbarActivity implements LogInOrSignUpFragment.OnFragmentInteractionListener, SignUpFragment.FragmentInteractionListener {

  private static final String TAG = "LogInActivity";

  @Inject
  @Named("UI")
  Handler handler;
  @Inject Preferences preferences;
  @Inject DSService dsServcie;

  private LogInOrSignUpFragment logInOrSignUpFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.actv_login);
    initActionBar();
    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ActivitiesAndFragmentsHelper.popFragment(LogInActivity.this);
      }
    });
    logInOrSignUpFragment = (LogInOrSignUpFragment) SimpleFragmentFactory.createFragment(LogInOrSignUpFragment.TAG);
    ActivitiesAndFragmentsHelper.replaceFragmentWithBackStackClear(this, logInOrSignUpFragment);
  }

  @Override
  public void signUpButtonListener() {
    ActivitiesAndFragmentsHelper.replaceFragment(this, SimpleFragmentFactory.createFragment(SignUpFragment.TAG));
  }

  @Override
  public void onSignUpSucceeded() {
    ActivitiesAndFragmentsHelper.showToast(this, R.string.account_created_successfully, handler);
    ActivitiesAndFragmentsHelper.popFragment(this);
    ActivitiesAndFragmentsHelper.replaceFragment(this, SimpleFragmentFactory.createFragment(LogInOrSignUpFragment.TAG));
  }

  @Override
  void setupComponent(AppComponent appComponent) {
    appComponent.inject(this);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    Uri uri = intent.getData();
    if (uri!=null){
      handleResponseIntent(uri);
    }
  }

  private void handleResponseIntent(final Uri uri) {

    String token = uri.toString().split("#")[1].split("=")[1];
    ListenableFuture<CreatorVoid> future = dsServcie.login(token, logInOrSignUpFragment.isRememberMechecked());
    Futures.addCallback(future, new FutureCallback<CreatorVoid>() {
      @Override
      public void onSuccess(CreatorVoid result) {
        ActivitiesAndFragmentsHelper.startActivityAndFinishPreviousOne(LogInActivity.this, new Intent(LogInActivity.this, CreatorActivity.class));
      }

      @Override
      public void onFailure(Throwable t) {
        if (logInOrSignUpFragment != null) {
          logInOrSignUpFragment.handleLoginFailure(t);
        }
      }
    });

  }
}
