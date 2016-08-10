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
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.di.component.AppComponent;
import com.imgtec.hobbyist.ds.DSService;
import com.imgtec.hobbyist.ds.pojo.Bootstrap;
import com.imgtec.hobbyist.ds.pojo.PSK;
import com.imgtec.hobbyist.fragments.common.FragmentWithTitle;
import com.imgtec.hobbyist.utils.Preferences;
import com.imgtec.hobbyist.utils.SetupGuideInfoSingleton;
import com.imgtec.hobbyist.utils.SimpleFragmentFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class FetchCertificateFragment extends FragmentWithTitle {

  public static final String TAG = "FetchCertificateFragment";

  @Inject DSService DSService;
  @Inject Preferences preferences;
  @Inject ExecutorService executor;


  Unbinder unbinder;

  @BindView(R.id.progressBar) ProgressBar progressBar;
  @BindView(R.id.info) TextView info;
  @BindView(R.id.continueButton) Button continueButton;
  @BindView(R.id.retryButton) Button retryButton;
  @BindView(R.id.certImage) ImageView certImage;


  public static FetchCertificateFragment newInstance() {

    Bundle args = new Bundle();

    FetchCertificateFragment fragment = new FetchCertificateFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.frag_fetch_cert, container, false);
    ButterKnife.bind(this, rootView);

    return rootView;
  }


  @Override
  public void onResume() {
    super.onResume();
    continueButton.setEnabled(false);
    startFetching();
  }

  @OnClick(R.id.continueButton)
  void onContinueClicked() {
    if (menuListener != null) {
      menuListener.onFragmentChange(SimpleFragmentFactory.createFragment(NetworkChoiceFragment.TAG, true));
    }
  }

  @OnClick(R.id.retryButton)
  void onRetryClicked() {
    startFetching();
  }

  private void startFetching() {
    progressBar.setVisibility(View.VISIBLE);

    executor.submit(new Runnable() {
      @Override
      public void run() {
        try {
          PSK psk = DSService.generatePSK().get();
          Bootstrap bootstrap = DSService.getBootstrap().get();

          SetupGuideInfoSingleton.setPskIdentity(psk.getIdentity());
          SetupGuideInfoSingleton.setPskSecret(psk.getSecret());
          SetupGuideInfoSingleton.setBootstrapUrl(bootstrap.getUrl());
          getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
              progressBar.setVisibility(View.GONE);
              certImage.setVisibility(View.VISIBLE);
              info.setText("Successfully fetched certificate from Device Server.");
              retryButton.setVisibility(View.GONE);
              continueButton.setVisibility(View.VISIBLE);
              continueButton.setEnabled(true);
            }
          });

        } catch (ExecutionException e) {
          if (FetchCertificateFragment.this.isAdded()) {
            getActivity().runOnUiThread(new Runnable() {
              @Override
              public void run() {
                progressBar.setVisibility(View.INVISIBLE);
                continueButton.setVisibility(View.GONE);
                retryButton.setVisibility(View.VISIBLE);
                info.setText("Failed to fetch certificate from Device Server.");
              }
            });
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });
  }


  @Override
  protected String getActionBarTitleText() {
    return getString(R.string.fetching_certificate);
  }

  @Override
  protected void setupComponent(AppComponent appComponent) {
    appComponent.inject(this);
  }
}
