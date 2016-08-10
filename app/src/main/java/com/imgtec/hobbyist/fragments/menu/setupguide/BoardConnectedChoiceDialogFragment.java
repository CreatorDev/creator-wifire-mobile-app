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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.activities.CreatorActivity;
import com.imgtec.hobbyist.fragments.navigationdrawer.NDMenuItem;
import com.imgtec.hobbyist.utils.SimpleFragmentFactory;

/**
 * Dialog that redirects to {@link com.imgtec.hobbyist.fragments.menu.setupguide.LogInToWifiFragment}
 */
public class BoardConnectedChoiceDialogFragment extends DialogFragment {

  public static final String TAG = "BoardConnectedChoiceDialogFragment";

  public static BoardConnectedChoiceDialogFragment newInstance() {
    return new BoardConnectedChoiceDialogFragment();
  }

  @Override
  @NonNull
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    // Use the Builder class for convenient dialog construction
    final Activity activity = getActivity();
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    Context appContext = activity.getApplicationContext();
    builder.setTitle(appContext.getString(R.string.you_are_connected_to_your_device));
    builder.setMessage(appContext.getString(R.string.board_connected_choice_message))
        .setPositiveButton(R.string.connect_button_text, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            ((CreatorActivity) activity).onFragmentChange(SimpleFragmentFactory.createFragment(LogInToWifiFragment.TAG));
            ((CreatorActivity) activity).onSelectionAndTitleChange(NDMenuItem.SetupDevice);
          }
        });
    // Create the AlertDialog object and return it
    return builder.create();
  }
}
