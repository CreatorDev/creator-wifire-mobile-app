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


package com.imgtec.hobbyist.wifire;

import android.support.annotation.NonNull;

import com.imgtec.hobbyist.ds.pojo.Client;

import java.util.Comparator;


public class WifireDevice implements Comparable<WifireDevice> {
  private Client client;
  private String networkSSID;
  private boolean networkState = false;  //connected or not
  private String networkRSSIdBm;
  private String boardHealth;
  private String status;

  private String uptime;

  public WifireDevice(Client client) {
    this.client = client;
  }


  public Client getClient() {
    return client;
  }

  @Override
  public String toString() {
    return client.getName();
  }

  public String getName() {
    return client.getName();
  }

  public void setName(String name) {
    client.setName(name);
  }

  public String getUptime() {
    return uptime;
  }

  public void setUptime(String uptime) {
    this.uptime = uptime;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getBoardHealth() {
    return boardHealth;
  }

  public void setBoardHealth(String boardHealth) {
    this.boardHealth = boardHealth;
  }

  public String getNetworkRSSIdBm() {
    return networkRSSIdBm;
  }

  public void setNetworkRSSIdBm(String networkRSSIdBm) {
    this.networkRSSIdBm = networkRSSIdBm;
  }

  public boolean isNetworkConnected() {
    return networkState;
  }

  public void setNetworkState(boolean networkState) {
    this.networkState = networkState;
  }

  public String getNetworkSSID() {
    return networkSSID;
  }

  public void setNetworkSSID(String networkSSID) {
    this.networkSSID = networkSSID;
  }

  @Override
  public int compareTo(@NonNull WifireDevice another) {
    return this.getName().compareTo(another.getName());
  }

  @Override
  public boolean equals(Object rhs) {
    return rhs instanceof WifireDevice && this.getName().equals(((WifireDevice) rhs).getName());
  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }

  public static final Comparator<WifireDevice> COMPARATOR = new Comparator<WifireDevice>() {

    @Override
    public int compare(WifireDevice lhs, WifireDevice rhs) {
      return lhs.getName().compareTo(rhs.getName());
    }
  };
}
