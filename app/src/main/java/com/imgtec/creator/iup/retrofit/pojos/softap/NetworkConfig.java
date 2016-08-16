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


package com.imgtec.creator.iup.retrofit.pojos.softap;


import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "NetworkConfig")
public class NetworkConfig {
  public NetworkConfig(@Element(name = "SSID") String ssid,
                       @Element(name = "Encryption") String encryption,
                       @Element(name = "Password") String password,
                       @Element(name = "AddrMethod") String addrMethod,
                       @Element(name = "StaticDNS") String staticDNS,
                       @Element(name = "StaticIP") String staticIP,
                       @Element(name = "StaticNetmask") String staticNetmask,
                       @Element(name = "StaticGateway") String staticGateway) {

    this.ssid = ssid;
    this.encryption = encryption;
    this.password = password;
    this.addrMethod = addrMethod;
    this.staticDNS = staticDNS;
    this.staticIP = staticIP;
    this.staticNetmask = staticNetmask;
    this.staticGateway = staticGateway;
  }

  public NetworkConfig(@Element(name = "SSID") String ssid,
                       @Element(name = "Encryption") String encryption,
                       @Element(name = "Password") String password,
                       @Element(name = "AddrMethod") String addrMethod) {

    this.ssid = ssid;
    this.encryption = encryption;
    this.password = password;
    this.addrMethod = addrMethod;

  }

  @Element(name = "SSID")
  private String ssid;
  @Element(name = "Encryption", required = false)
  private String encryption;
  @Element(name = "Password", required = false)
  private String password; // Is always empty because of security issues. Date of comment: 13.05.2014
  @Element(name = "AddrMethod", required = false)
  private String addrMethod;
  @Element(name = "StaticDNS", required = false)
  private String staticDNS;
  @Element(name = "StaticIP", required = false)
  private String staticIP;
  @Element(name = "StaticNetmask", required = false)
  private String staticNetmask;
  @Element(name = "StaticGateway", required = false)
  private String staticGateway;

  public String getSsid() {
    return ssid;
  }

  public String getEncryption() {
    return encryption;
  }

  public String getAddrMethod() {
    return addrMethod;
  }

  public String getStaticDNS() {
    return staticDNS;
  }

  public String getStaticIP() {
    return staticIP;
  }

  public String getStaticNetmask() {
    return staticNetmask;
  }

  public String getStaticGateway() {
    return staticGateway;
  }
}
