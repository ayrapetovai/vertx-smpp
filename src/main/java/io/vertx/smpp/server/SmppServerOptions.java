package io.vertx.smpp.server;

//   Copyright 2022 Artem Ayrapetov
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

import io.netty.handler.logging.ByteBufFormat;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ClientAuth;
import io.vertx.core.net.*;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SmppServerOptions extends NetServerOptions {
  private String name; // for identification on local JVM?
  private int maxSessions;
  private boolean autoNegotiateInterfaceVersion;
  private boolean jmxEnabled;
  private String jmxDomain;

  @Override
  public SmppServerOptions setSendBufferSize(int sendBufferSize) {
    super.setSendBufferSize(sendBufferSize);
    return this;
  }

  @Override
  public SmppServerOptions setReceiveBufferSize(int receiveBufferSize) {
    super.setReceiveBufferSize(receiveBufferSize);
    return this;
  }

  @Override
  public SmppServerOptions setReuseAddress(boolean reuseAddress) {
    super.setReuseAddress(reuseAddress);
    return this;
  }

  @Override
  public SmppServerOptions setReusePort(boolean reusePort) {
    super.setReusePort(reusePort);
    return this;
  }

  @Override
  public SmppServerOptions setTrafficClass(int trafficClass) {
    super.setTrafficClass(trafficClass);
    return this;
  }

  @Override
  public SmppServerOptions setTcpNoDelay(boolean tcpNoDelay) {
    super.setTcpNoDelay(tcpNoDelay);
    return this;
  }

  @Override
  public SmppServerOptions setTcpKeepAlive(boolean tcpKeepAlive) {
    super.setTcpKeepAlive(tcpKeepAlive);
    return this;
  }

  @Override
  public SmppServerOptions setSoLinger(int soLinger) {
    super.setSoLinger(soLinger);
    return this;
  }

  @Override
  public SmppServerOptions setIdleTimeout(int idleTimeout) {
    super.setIdleTimeout(idleTimeout);
    return this;
  }

  @Override
  public SmppServerOptions setReadIdleTimeout(int idleTimeout) {
    super.setReadIdleTimeout(idleTimeout);
    return this;
  }

  @Override
  public SmppServerOptions setWriteIdleTimeout(int idleTimeout) {
    super.setWriteIdleTimeout(idleTimeout);
    return this;
  }

  @Override
  public SmppServerOptions setIdleTimeoutUnit(TimeUnit idleTimeoutUnit) {
    super.setIdleTimeoutUnit(idleTimeoutUnit);
    return this;
  }

  @Override
  public SmppServerOptions setSsl(boolean ssl) {
    super.setSsl(ssl);
    return this;
  }

  @Override
  public SmppServerOptions setUseAlpn(boolean useAlpn) {
    super.setUseAlpn(useAlpn);
    return this;
  }

  @Override
  public SmppServerOptions setSslEngineOptions(SSLEngineOptions sslEngineOptions) {
    super.setSslEngineOptions(sslEngineOptions);
    return this;
  }

  @Override
  public SmppServerOptions setJdkSslEngineOptions(JdkSSLEngineOptions sslEngineOptions) {
    return (SmppServerOptions) super.setSslEngineOptions(sslEngineOptions);
  }

  @Override
  public SmppServerOptions setOpenSslEngineOptions(OpenSSLEngineOptions sslEngineOptions) {
    return (SmppServerOptions) super.setSslEngineOptions(sslEngineOptions);
  }

  @Override
  public SmppServerOptions setKeyCertOptions(KeyCertOptions options) {
    super.setKeyCertOptions(options);
    return this;
  }

  @Override
  public SmppServerOptions setKeyStoreOptions(JksOptions options) {
    super.setKeyStoreOptions(options);
    return this;
  }

  @Override
  public SmppServerOptions setPfxKeyCertOptions(PfxOptions options) {
    return (SmppServerOptions) super.setPfxKeyCertOptions(options);
  }

  @Override
  public SmppServerOptions setPemKeyCertOptions(PemKeyCertOptions options) {
    return (SmppServerOptions) super.setPemKeyCertOptions(options);
  }

  @Override
  public SmppServerOptions setTrustOptions(TrustOptions options) {
    super.setTrustOptions(options);
    return this;
  }

  @Override
  public SmppServerOptions setTrustStoreOptions(JksOptions options) {
    super.setTrustStoreOptions(options);
    return this;
  }

  @Override
  public SmppServerOptions setPfxTrustOptions(PfxOptions options) {
    return (SmppServerOptions) super.setPfxTrustOptions(options);
  }

  @Override
  public SmppServerOptions setPemTrustOptions(PemTrustOptions options) {
    return (SmppServerOptions) super.setPemTrustOptions(options);
  }

  @Override
  public SmppServerOptions addEnabledCipherSuite(String suite) {
    super.addEnabledCipherSuite(suite);
    return this;
  }

  @Override
  public SmppServerOptions addEnabledSecureTransportProtocol(final String protocol) {
    super.addEnabledSecureTransportProtocol(protocol);
    return this;
  }

  @Override
  public SmppServerOptions removeEnabledSecureTransportProtocol(String protocol) {
    return (SmppServerOptions) super.removeEnabledSecureTransportProtocol(protocol);
  }

  @Override
  public SmppServerOptions setTcpFastOpen(boolean tcpFastOpen) {
    return (SmppServerOptions) super.setTcpFastOpen(tcpFastOpen);
  }

  @Override
  public SmppServerOptions setTcpCork(boolean tcpCork) {
    return (SmppServerOptions) super.setTcpCork(tcpCork);
  }

  @Override
  public SmppServerOptions setTcpQuickAck(boolean tcpQuickAck) {
    return (SmppServerOptions) super.setTcpQuickAck(tcpQuickAck);
  }

  @Override
  public SmppServerOptions addCrlPath(String crlPath) throws NullPointerException {
    return (SmppServerOptions) super.addCrlPath(crlPath);
  }

  @Override
  public SmppServerOptions addCrlValue(Buffer crlValue) throws NullPointerException {
    return (SmppServerOptions) super.addCrlValue(crlValue);
  }

  @Override
  public SmppServerOptions setEnabledSecureTransportProtocols(Set<String> enabledSecureTransportProtocols) {
    return (SmppServerOptions) super.setEnabledSecureTransportProtocols(enabledSecureTransportProtocols);
  }

  @Override
  public SmppServerOptions setSslHandshakeTimeout(long sslHandshakeTimeout) {
    return (SmppServerOptions) super.setSslHandshakeTimeout(sslHandshakeTimeout);
  }

  @Override
  public SmppServerOptions setSslHandshakeTimeoutUnit(TimeUnit sslHandshakeTimeoutUnit) {
    return (SmppServerOptions) super.setSslHandshakeTimeoutUnit(sslHandshakeTimeoutUnit);
  }

  @Override
  public SmppServerOptions setAcceptBacklog(int acceptBacklog) {
    super.setAcceptBacklog(acceptBacklog);
    return this;
  }

  @Override
  public SmppServerOptions setPort(int port) {
    super.setPort(port);
    return this;
  }

  @Override
  public SmppServerOptions setHost(String host) {
    super.setHost(host);
    return this;
  }

  @Override
  public SmppServerOptions setClientAuth(ClientAuth clientAuth) {
    super.setClientAuth(clientAuth);
    return this;
  }

  @Override
  public SmppServerOptions setLogActivity(boolean logEnabled) {
    return (SmppServerOptions) super.setLogActivity(logEnabled);
  }

  @Override
  public SmppServerOptions setActivityLogDataFormat(ByteBufFormat activityLogDataFormat) {
    return (SmppServerOptions) super.setActivityLogDataFormat(activityLogDataFormat);
  }

  @Override
  public SmppServerOptions setSni(boolean sni) {
    super.setSni(sni);
    return this;
  }

  @Override
  public SmppServerOptions setUseProxyProtocol(boolean useProxyProtocol) {
    super.setUseProxyProtocol(useProxyProtocol);
    return this;
  }

  @Override
  public SmppServerOptions setProxyProtocolTimeout(long proxyProtocolTimeout) {
    super.setProxyProtocolTimeout(proxyProtocolTimeout);
    return this;
  }

  @Override
  public SmppServerOptions setProxyProtocolTimeoutUnit(TimeUnit proxyProtocolTimeoutUnit) {
    super.setProxyProtocolTimeoutUnit(proxyProtocolTimeoutUnit);
    return this;
  }
}
