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


package com.imgtec.creator.iup.adapters;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.imgtec.creator.iup.R;

import java.util.List;

/**
 * Adapter to link ssid data with UI
 * More information about Android adapter: {@link android.widget.Adapter}
 */

public class BoardWifiChoiceListAdapter extends AbstractListAdapter<String, BoardWifiChoiceListAdapter.RowHolder> {

  public BoardWifiChoiceListAdapter(Context context, List<String> ssidList) {
    super(context, R.layout.wdgt_board_ssid, ssidList);
    this.dataList = ssidList;
  }

  protected RowHolder createViewHolder(View rowView) {
    RowHolder rowHolder = new RowHolder();
    rowHolder.boardSSID = (TextView) rowView.findViewById(R.id.boardSSID);
    return rowHolder;
  }

  @TargetApi(16)
  protected void fillViewHolder(final RowHolder viewHolder, int position, ViewGroup parent) {
    viewHolder.boardSSID.setText(dataList.get(position));
    Drawable whiteBackgroundShape = ContextCompat.getDrawable(context, R.drawable.purple_white_list_item_selector);
    Drawable grayBackgroundShape = ContextCompat.getDrawable(context, R.drawable.purple_gray_list_item_selector);
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
      //noinspection deprecation
      viewHolder.boardSSID.setBackgroundDrawable(position % 2 == 0 ? whiteBackgroundShape : grayBackgroundShape);
    } else {
      viewHolder.boardSSID.setBackground(position % 2 == 0 ? whiteBackgroundShape : grayBackgroundShape);
    }
  }

  static class RowHolder {
    TextView boardSSID;
  }

}

