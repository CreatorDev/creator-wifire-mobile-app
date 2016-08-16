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

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.imgtec.creator.iup.R;
import com.imgtec.creator.iup.wifire.DeviceHelper;
import com.imgtec.creator.iup.wifire.WifireDevice;
import com.imgtec.creator.iup.fragments.navigationdrawer.NDMenuItem;
import com.imgtec.creator.iup.utils.NDMenuMode;
import com.imgtec.creator.iup.utils.Preferences;
import com.imgtec.creator.iup.utils.SetupGuideInfoSingleton;

/**
 * Adapter to service NavigationDrawer and its 3 options menu possibilities defined in
 * {@link com.imgtec.creator.iup.fragments.navigationdrawer.NDMenuItem}
 * <p/>
 * More information about Android adapter: {@link android.widget.Adapter}
 */

public class NDMenuAdapter extends ArrayAdapter {

  private final Context context;
  private final LayoutInflater inflater;
  private final NDMenuItem[] ndMenuItems;
  private final Preferences preferences;
  private DeviceHelper deviceHelper;

  public NDMenuAdapter(Context context, NDMenuItem[] ndMenuItems, Preferences preferences, DeviceHelper deviceHelper) {
    super(context, 0);
    this.context = context;
    this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    this.ndMenuItems = ndMenuItems;
    this.preferences = preferences;
    this.deviceHelper = deviceHelper;
  }

  @Override
  public int getCount() {
    return ndMenuItems.length;
  }

  @Override
  public Object getItem(int position) {
    return ndMenuItems[position];
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    final NDMenuItem menuItem = ndMenuItems[position];
    View rowView = convertView;
    RowHolder viewHolder;
    if (rowView == null || rowView.getTag() == null) {
      if (!menuItem.isSeparator() && !menuItem.isMyDevice()) {
        rowView = inflater.inflate(R.layout.wdgt_navigation_drawer_item, parent, false);
        viewHolder = createViewHolder(rowView);
        rowView.setTag(viewHolder);
        fillViewHolder(viewHolder, menuItem);
      } else if (menuItem.isSeparator()) {
        rowView = inflater.inflate(R.layout.wdgt_navigation_drawer_separator, parent, false);
      } else {
        rowView = inflater.inflate(R.layout.wdgt_navigation_drawer_item, parent, false);
        viewHolder = createViewHolder(rowView);
        rowView.setTag(viewHolder);
        fillViewHolder(viewHolder, menuItem);
      }
    } else if (!menuItem.isSeparator()) {
      viewHolder = (RowHolder) rowView.getTag();
      fillViewHolder(viewHolder, menuItem);
    }
    return rowView;
  }

  private RowHolder createViewHolder(View rowView) {
    RowHolder rowHolder = new RowHolder();
    rowHolder.title = (TextView) rowView.findViewById(R.id.navigationDrawerItemText);
    rowHolder.icon = (ImageView) rowView.findViewById(R.id.icon);
    return rowHolder;
  }

  private void fillViewHolder(final RowHolder viewHolder, final NDMenuItem menuItem) {
    //viewHolder.selectionMark.setVisibility(NDMenuItem.isChecked(menuItem) ? View.VISIBLE : View.INVISIBLE);
    viewHolder.title.setTextColor(NDMenuItem.isChecked(menuItem) ? ContextCompat.getColor(getContext(), R.color.theme_purple) : ContextCompat.getColor(getContext(), R.color.theme_dark_grey));
    if (menuItem.getIconId() != -1) {
      viewHolder.icon.setImageResource(menuItem.getIconId());
    }
    if (menuItem.isMyDevice()) {
      fillDeviceInfo(viewHolder);
    } else if (menuItem.isUserName()) {
      String userName = preferences.getUserName();
      viewHolder.title.setSingleLine();
      viewHolder.title.setEllipsize(TextUtils.TruncateAt.END);
      viewHolder.title.setText(userName);
    } else {
      viewHolder.title.setText(context.getString(menuItem.getTextId()));
    }
  }

  private void fillDeviceInfo(RowHolder viewHolder) {
    if (NDMenuMode.getMode() == NDMenuMode.Interactive) {
      fillInteractiveModeDevice(viewHolder);
    } else if (NDMenuMode.getMode() == NDMenuMode.Setup) {
      fillWifiNetworkModeDevice(viewHolder);
    }
  }

  private void fillInteractiveModeDevice(RowHolder viewHolder) {
    WifireDevice device = deviceHelper.getDevice();
    viewHolder.title.setText(device.getName());
  }

  private void fillWifiNetworkModeDevice(RowHolder viewHolder) {
    viewHolder.title.setText(SetupGuideInfoSingleton.getDeviceName());
  }

  /**
   * Disable possibility of clicking on {@link com.imgtec.creator.iup.fragments.navigationdrawer.NDMenuItem#Separator}
   *
   * @param position of non-clickable NavigationDrawer Separator
   * @return
   */
  @Override
  public boolean isEnabled(int position) {
    if (ndMenuItems[position].isUserName()) {
      return false;
    }
    return !ndMenuItems[position].isSeparator();
  }

  static class RowHolder {
    ImageView selectionMark;
    TextView title;
    ImageView deviceStatusImage;
    ImageView icon;
  }

}
