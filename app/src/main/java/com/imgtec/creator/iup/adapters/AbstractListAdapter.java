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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Abstract version of ListAdapter
 * More information about Android adapter: {@link android.widget.Adapter}
 */

public abstract class AbstractListAdapter<T, S> extends ArrayAdapter<T> {

  protected final Context context;
  protected List<T> dataList;
  protected int resource;
  protected LayoutInflater inflater;

  public AbstractListAdapter(Context context, int resource, List<T> dataList) {
    super(context, resource, dataList);
    this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    this.context = context;
    this.resource = resource;
    this.dataList = dataList;
  }

  /**
   * Implementation of Adapter.getView(). It's called by list and should not be called by user.
   */
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View rowView = convertView;
    S viewHolder;
    if (rowView == null || rowView.getTag() == null) {
      rowView = inflater.inflate(resource, parent, false);
      viewHolder = createViewHolder(rowView);
      rowView.setTag(viewHolder);
    } else {
      viewHolder = (S) rowView.getTag();
    }
    fillViewHolder(viewHolder, position, parent);
    return rowView;
  }

  /**
   * Method creating a new view holder for row view
   *
   * @param rowView
   * @return new view holder
   */
  protected abstract S createViewHolder(View rowView);

  /**
   * Fill UI items
   *
   * @param viewHolder views holder to fill (never null)
   * @param position   position of item in list
   * @param parent     parent of textView
   */
  protected abstract void fillViewHolder(final S viewHolder, int position, ViewGroup parent);

}
