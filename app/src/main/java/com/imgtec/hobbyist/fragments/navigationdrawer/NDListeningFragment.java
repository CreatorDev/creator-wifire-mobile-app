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


package com.imgtec.hobbyist.fragments.navigationdrawer;

import android.content.Context;

import com.imgtec.hobbyist.activities.CreatorActivity;
import com.imgtec.hobbyist.fragments.common.NetworkAwareFragment;


/**
 * A fragment which can change contents of NavigationDrawer Menu by switching application state to one of {@link com.imgtec.hobbyist.utils.NDMenuMode}.
 * It works through {@link CreatorActivity}
 * which implements {@link NDMenuListener}.
 * <p/>
 * onAttach and onDetach are just for appropriate activity reference initialization/removing
 */
public abstract class NDListeningFragment extends NetworkAwareFragment {

  protected NDMenuListener menuListener;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof NDMenuListener) {
      this.menuListener = (NDMenuListener) context;
    }
  }

  @Override
  public void onDetach() {
    menuListener = null;
    super.onDetach();
  }
}
