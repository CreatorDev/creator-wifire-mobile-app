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


package com.imgtec.hobbyist.fragments.loginsignup;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.activities.ActivitiesAndFragmentsHelper;
import com.imgtec.hobbyist.activities.LogInActivity;
import com.imgtec.hobbyist.di.component.AppComponent;
import com.imgtec.hobbyist.ds.DSService;
import com.imgtec.hobbyist.ds.exceptions.ConflictException;
import com.imgtec.hobbyist.ds.exceptions.NetworkException;
import com.imgtec.hobbyist.ds.pojo.UserCreatedResponse;
import com.imgtec.hobbyist.fragments.common.FragmentWithProgressBar;
import com.imgtec.hobbyist.utils.Constants;
import com.imgtec.hobbyist.utils.OnTextChangedListener;
import com.imgtec.hobbyist.utils.Preferences;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Fragment representing a sign up screen.
 */
public class SignUpFragment extends FragmentWithProgressBar {

  public static final String TAG = "SignUpFragment";

  private SignUpFragment.FragmentInteractionListener fragmentInteractionListener;

  @Inject
  DSService DSService;

  @Inject
  Preferences preferences;

  @BindView(R.id.signUp) Button signUpButton;
  @BindView(R.id.username) EditText usernameField;
  @BindView(R.id.emailAddress) EditText emailAddressField;
  @BindView(R.id.password) EditText passwordField;

  Unbinder unbinder;

  public static Fragment newInstance() {
    return new SignUpFragment();
  }

  @Override
  protected String getActionBarTitleText() {
    return getString(R.string.sign_up_title);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.frag_sign_up, container, false);
    unbinder = ButterKnife.bind(this, rootView);
    return rootView;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initListeners();
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof LogInOrSignUpFragment.OnFragmentInteractionListener) {
      fragmentInteractionListener = (SignUpFragment.FragmentInteractionListener) context;
    } else {
      throw new IllegalArgumentException(this.getClass().getSimpleName() + " does not implement " +
          SignUpFragment.FragmentInteractionListener.class.getSimpleName());
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    ((LogInActivity) getActivity()).getToolbar().setVisibility(View.VISIBLE);
    ((LogInActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    ((LogInActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);
  }

  @Override
  public void onDestroyView() {
    unbinder.unbind();
    super.onDestroyView();
  }

  private void initListeners() {
    signUpButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View signUpButton) {
        final String username = usernameField.getText().toString().toLowerCase(Locale.US);
        final String email = emailAddressField.getText().toString();
        final String password = passwordField.getText().toString();
        if (validate(username, email, password)) {
          showProgress(appContext.getString(R.string.signing_up));
          signUp(username, email, password);
        }
      }
    });

    new OnTextChangedListener(passwordField) {
      @Override
      public void onTextChanged(CharSequence s) {
        if (s.length() < Constants.CREATOR_ACCOUNT_MINIMUM_CHARACTERS_COUNT) {
          signUpButton.setEnabled(false);
        } else {
          signUpButton.setEnabled(true);
        }
      }
    };
  }

  private boolean validate(String username, String email, String password) {
    boolean result = true;
    int usernameLength = username.length();
    if (usernameLength < 5) {
      setErrorOnField(usernameField, R.string.username_too_short);
      result = false;
    } else {
      usernameField.setError(null);
    }
    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      setErrorOnField(emailAddressField, R.string.email_address_is_required);
      result = false;
    } else {
      emailAddressField.setError(null);
    }
    final int passLength = password.length();
    if (passLength < 5 || passLength > Constants.DEFAULT_MAXIMUM_FIELD_CHARACTERS_COUNT) {
      setErrorOnField(passwordField, R.string.incorrect_password_character_count);
      result = false;
    } else {
      passwordField.setError(null);
    }
    return result;
  }

  private void setErrorOnField(final EditText field, final int stringRes) {
    //works on UIThread
    handler.post(new Runnable() {
      @Override
      public void run() {
        field.setError(stringRes != 0 ? getString(stringRes) : null);
        field.requestFocus();
      }
    });
  }

  private void signUp(final String username, final String email, final String password) {
    ListenableFuture<UserCreatedResponse> future = DSService.createAccount(username, password, email);
    Futures.addCallback(future, new FutureCallback<UserCreatedResponse>() {
      @Override
      public void onSuccess(UserCreatedResponse result) {
        preferences.saveEmailCredential(username);
        fragmentInteractionListener.onSignUpSucceeded();
      }

      @Override
      public void onFailure(Throwable t) {
        handleSignUpError(t);
      }
    });
  }

  @Override
  protected void setupComponent(AppComponent appComponent) {
    appComponent.inject(this);
  }

  private void handleSignUpError(Throwable e) {
    int errorStringRes;
    if (e instanceof ConflictException) {
      errorStringRes = R.string.error_conflict_data;
    } else if (e instanceof NetworkException) {
      errorStringRes = R.string.error_network;
    } else {
      errorStringRes = R.string.error_unknown;
    }
    hideProgress();
    ActivitiesAndFragmentsHelper.showToast(appContext, errorStringRes, handler);
  }

  public interface FragmentInteractionListener {
    void onSignUpSucceeded();
  }


}
