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
package com.imgtec.hobbyist.retrofit.pojos.softap;


import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "DeviceServer")
public class DeviceServer {

  public DeviceServer(@Element(name = "BootstrapUrl") String bootstrapUrl,
                      @Element(name = "SecurityMode") String securityMode,
                      @Element(name = "PublicKey") String publicKey,
                      @Element(name = "PrivateKey") String privateKey,
                      @Element(name = "Certificate") String certificate,
                      @Element(name = "BootstrapCertChain") String bootstrapCertChain) {

    this.bootstrapUrl = bootstrapUrl;
    this.securityMode = securityMode;
    this.publicKey = publicKey;
    this.privateKey = privateKey;
    this.certificate = certificate;
    this.bootstrapCertChain = bootstrapCertChain;

  }

  public DeviceServer(@Element(name = "BootstrapUrl") String bootstrapUrl,
                      @Element(name = "SecurityMode") String securityMode,
                      @Element(name = "Certificate") String certificate) {

    this.bootstrapUrl = bootstrapUrl;
    this.securityMode = securityMode;
    this.certificate = certificate;

  }

  @Element(name = "BootstrapUrl")
  private String bootstrapUrl;
  @Element(name = "SecurityMode")
  private String securityMode;
  @Element(name = "PublicKey", required = false)
  private String publicKey;
  @Element(name = "PrivateKey", required = false)
  private String privateKey;
  @Element(name = "Certificate", required = false)
  private String certificate;
  @Element(name = "BootstrapCertChain", required = false)
  private String bootstrapCertChain;

  public String getBootstrapUrl() {
    return bootstrapUrl;
  }

  public String getSecurityMode() {
    return securityMode;
  }

  public String getPublicKey() {
    return publicKey;
  }

  public String getPrivateKey() {
    return privateKey;
  }

  public String getCertificate() {
    return certificate;
  }

  public String getBootstrapCertChain() {
    return bootstrapCertChain;
  }
}
