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


package com.imgtec.hobbyist.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.wifire.WifireDevice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Adapter to link connected devices data with UI
 * More information about Android adapter: {@link android.widget.Adapter}
 */

public class ConnectedDevicesAdapter extends AbstractListAdapter<WifireDevice, ConnectedDevicesAdapter.RowHolder> {

  private Map<Integer, Boolean> contextualChecked = new HashMap<>();

  public ConnectedDevicesAdapter(Context context, List<WifireDevice> connectedDevices) {
    super(context, R.layout.wdgt_connected_device_item, connectedDevices);
    this.dataList = connectedDevices;
  }

  /**
   * Sets checked status of an item at desired position
   *
   * @param position - position of item
   */
  public void switchContextualChecked(int position) {
    if (isContextualChecked(position)) {
      contextualChecked.put(position, false);
    } else {
      contextualChecked.put(position, true);
    }
    notifyDataSetChanged();
  }

  /**
   * Checks status of an item at desired position
   *
   * @param position - position of item
   */
  public boolean isContextualChecked(int position) {
    Boolean result = contextualChecked.get(position);
    return result == null ? false : result;
  }

  /**
   * Returns list of all checked devices
   */
  public List<WifireDevice> getContextualSelectedDevices() {
    List<WifireDevice> selectedDevices = new CopyOnWriteArrayList<>();
    for (Map.Entry<Integer, Boolean> entry : contextualChecked.entrySet()) {
      if (entry.getValue()) {
        selectedDevices.add(dataList.get(entry.getKey()));
      }
    }
    return selectedDevices;
  }

  /**
   * Clears list of checked items
   */
  public void clearContextualSelected() {
    contextualChecked.clear();
  }

  protected RowHolder createViewHolder(View rowView) {
    RowHolder rowHolder = new RowHolder();
    rowHolder.myDevice = (TextView) rowView.findViewById(R.id.myDevice);
    rowHolder.connectedDeviceLayout = (RelativeLayout) rowView.findViewById(R.id.connectedDeviceLayout);
    return rowHolder;
  }

  @TargetApi(16)
  protected void fillViewHolder(final RowHolder viewHolder, int position, ViewGroup parent) {
    viewHolder.myDevice.setText(dataList.get(position).toString());
    boolean isPositionChecked = isItemChecked(position, (ListView) parent);
    viewHolder.myDevice.setTextColor(isPositionChecked
        ? ContextCompat.getColor(context, R.color.nice_lavender)
        : ContextCompat.getColor(context, R.color.nice_dark_gray));
    Drawable checkedBackground = ContextCompat.getDrawable(context, R.drawable.very_light_background_with_stroke_shape);
    int checkedColor = ContextCompat.getColor(context, R.color.theme_purple);
    int regularColot = ContextCompat.getColor(context, R.color.theme_dark_grey);
    Drawable backgroundShape;
    if (position % 2 == 1) {
      backgroundShape = ContextCompat.getDrawable(context, R.drawable.device_list_transparent_background);
    } else {
      backgroundShape = ContextCompat.getDrawable(context, R.drawable.device_list_white_background);
    }
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
      //noinspection deprecation
      viewHolder.connectedDeviceLayout.setBackgroundDrawable(isPositionChecked ? checkedBackground : backgroundShape);
      viewHolder.myDevice.setTextColor(isPositionChecked ? checkedColor : regularColot);
      viewHolder.myDevice.setTypeface(null, Typeface.BOLD);
    } else {
      viewHolder.connectedDeviceLayout.setBackground(isPositionChecked ? checkedBackground : backgroundShape);
      viewHolder.myDevice.setTextColor(isPositionChecked ? checkedColor : regularColot);
      viewHolder.myDevice.setTypeface(null, Typeface.NORMAL);
    }
  }

  private boolean isItemChecked(int position, ListView listView) {
    if (listView.getChoiceMode() == AbsListView.CHOICE_MODE_SINGLE) { //normal mode
      int checkedItemPosition = listView.getCheckedItemPosition();
      return checkedItemPosition == position;
    } else { //contextual mode
      return isContextualChecked(position);
    }
  }

  static class RowHolder {
    RelativeLayout connectedDeviceLayout;
    TextView myDevice;
  }

}
