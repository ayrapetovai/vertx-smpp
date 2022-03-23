package io.vertx.smpp.client;

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
import io.vertx.core.net.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SmppClientOptions extends NetClientOptions {

  @Override
  public SmppClientOptions setSendBufferSize(int sendBufferSize) {
    super.setSendBufferSize(sendBufferSize);
    return this;
  }

  @Override
  public SmppClientOptions setReceiveBufferSize(int receiveBufferSize) {
    super.setReceiveBufferSize(receiveBufferSize);
    return this;
  }

  @Override
  public SmppClientOptions setReuseAddress(boolean reuseAddress) {
    super.setReuseAddress(reuseAddress);
    return this;
  }

  @Override
  public SmppClientOptions setReusePort(boolean reusePort) {
    super.setReusePort(reusePort);
    return this;
  }

  @Override
  public SmppClientOptions setTrafficClass(int trafficClass) {
    super.setTrafficClass(trafficClass);
    return this;
  }

  @Override
  public SmppClientOptions setTcpNoDelay(boolean tcpNoDelay) {
    super.setTcpNoDelay(tcpNoDelay);
    return this;
  }

  @Override
  public SmppClientOptions setTcpKeepAlive(boolean tcpKeepAlive) {
    super.setTcpKeepAlive(tcpKeepAlive);
    return this;
  }

  @Override
  public SmppClientOptions setSoLinger(int soLinger) {
    super.setSoLinger(soLinger);
    return this;
  }

  @Override
  public SmppClientOptions setIdleTimeout(int idleTimeout) {
    super.setIdleTimeout(idleTimeout);
    return this;
  }

  @Override
  public SmppClientOptions setReadIdleTimeout(int idleTimeout) {
    super.setReadIdleTimeout(idleTimeout);
    return this;
  }

  @Override
  public SmppClientOptions setWriteIdleTimeout(int idleTimeout) {
    super.setWriteIdleTimeout(idleTimeout);
    return this;
  }

  @Override
  public SmppClientOptions setIdleTimeoutUnit(TimeUnit idleTimeoutUnit) {
    super.setIdleTimeoutUnit(idleTimeoutUnit);
    return this;
  }

  @Override
  public SmppClientOptions setSsl(boolean ssl) {
    super.setSsl(ssl);
    return this;
  }

  @Override
  public SmppClientOptions setKeyCertOptions(KeyCertOptions options) {
    super.setKeyCertOptions(options);
    return this;
  }

  @Override
  public SmppClientOptions setKeyStoreOptions(JksOptions options) {
    super.setKeyStoreOptions(options);
    return this;
  }

  @Override
  public SmppClientOptions setPfxKeyCertOptions(PfxOptions options) {
    return (SmppClientOptions) super.setPfxKeyCertOptions(options);
  }

  @Override
  public SmppClientOptions setPemKeyCertOptions(PemKeyCertOptions options) {
    return (SmppClientOptions) super.setPemKeyCertOptions(options);
  }

  @Override
  public SmppClientOptions setTrustOptions(TrustOptions options) {
    super.setTrustOptions(options);
    return this;
  }

  @Override
  public SmppClientOptions setTrustStoreOptions(JksOptions options) {
    super.setTrustStoreOptions(options);
    return this;
  }

  @Override
  public SmppClientOptions setPemTrustOptions(PemTrustOptions options) {
    return (SmppClientOptions) super.setPemTrustOptions(options);
  }

  @Override
  public SmppClientOptions setPfxTrustOptions(PfxOptions options) {
    return (SmppClientOptions) super.setPfxTrustOptions(options);
  }

  @Override
  public SmppClientOptions addEnabledCipherSuite(String suite) {
    super.addEnabledCipherSuite(suite);
    return this;
  }

  @Override
  public SmppClientOptions addEnabledSecureTransportProtocol(final String protocol) {
    super.addEnabledSecureTransportProtocol(protocol);
    return this;
  }

  @Override
  public SmppClientOptions removeEnabledSecureTransportProtocol(String protocol) {
    return (SmppClientOptions) super.removeEnabledSecureTransportProtocol(protocol);
  }

  @Override
  public SmppClientOptions setUseAlpn(boolean useAlpn) {
    return (SmppClientOptions) super.setUseAlpn(useAlpn);
  }

  @Override
  public SmppClientOptions setSslEngineOptions(SSLEngineOptions sslEngineOptions) {
    return (SmppClientOptions) super.setSslEngineOptions(sslEngineOptions);
  }

  @Override
  public SmppClientOptions setJdkSslEngineOptions(JdkSSLEngineOptions sslEngineOptions) {
    return (SmppClientOptions) super.setJdkSslEngineOptions(sslEngineOptions);
  }

  @Override
  public SmppClientOptions setTcpFastOpen(boolean tcpFastOpen) {
    return (SmppClientOptions) super.setTcpFastOpen(tcpFastOpen);
  }

  @Override
  public SmppClientOptions setTcpCork(boolean tcpCork) {
    return (SmppClientOptions) super.setTcpCork(tcpCork);
  }

  @Override
  public SmppClientOptions setTcpQuickAck(boolean tcpQuickAck) {
    return (SmppClientOptions) super.setTcpQuickAck(tcpQuickAck);
  }

  @Override
  public ClientOptionsBase setOpenSslEngineOptions(OpenSSLEngineOptions sslEngineOptions) {
    return super.setOpenSslEngineOptions(sslEngineOptions);
  }

  @Override
  public SmppClientOptions addCrlPath(String crlPath) throws NullPointerException {
    return (SmppClientOptions) super.addCrlPath(crlPath);
  }

  @Override
  public SmppClientOptions addCrlValue(Buffer crlValue) throws NullPointerException {
    return (SmppClientOptions) super.addCrlValue(crlValue);
  }

  @Override
  public SmppClientOptions setTrustAll(boolean trustAll) {
    super.setTrustAll(trustAll);
    return this;
  }

  @Override
  public SmppClientOptions setConnectTimeout(int connectTimeout) {
    super.setConnectTimeout(connectTimeout);
    return this;
  }

  @Override
  public SmppClientOptions setMetricsName(String metricsName) {
    return (SmppClientOptions) super.setMetricsName(metricsName);
  }

  public SmppClientOptions setReconnectAttempts(int attempts) {
    super.setReconnectAttempts(attempts);
    return this;
  }

  @Override
  public SmppClientOptions setReconnectInterval(long interval) {
    super.setReconnectInterval(interval);
    return this;
  }


  @Override
  public SmppClientOptions setHostnameVerificationAlgorithm(String hostnameVerificationAlgorithm) {
    super.setHostnameVerificationAlgorithm(hostnameVerificationAlgorithm);
    return this;
  }

  @Override
  public SmppClientOptions setApplicationLayerProtocols(List<String> protocols) {
    super.setApplicationLayerProtocols(protocols);
    return this;
  }

  @Override
  public SmppClientOptions setLogActivity(boolean logEnabled) {
    return (SmppClientOptions) super.setLogActivity(logEnabled);
  }

  @Override
  public SmppClientOptions setActivityLogDataFormat(ByteBufFormat activityLogDataFormat) {
    return (SmppClientOptions) super.setActivityLogDataFormat(activityLogDataFormat);
  }

  public SmppClientOptions setProxyOptions(ProxyOptions proxyOptions) {
    return (SmppClientOptions) super.setProxyOptions(proxyOptions);
  }

  @Override
  public SmppClientOptions setNonProxyHosts(List<String> nonProxyHosts) {
    return (SmppClientOptions) super.setNonProxyHosts(nonProxyHosts);
  }

  @Override
  public SmppClientOptions addNonProxyHost(String nonProxyHost) {
    return (SmppClientOptions) super.addNonProxyHost(nonProxyHost);
  }

  @Override
  public SmppClientOptions setLocalAddress(String localAddress) {
    return (SmppClientOptions) super.setLocalAddress(localAddress);
  }

  @Override
  public SmppClientOptions setEnabledSecureTransportProtocols(Set<String> enabledSecureTransportProtocols) {
    return (SmppClientOptions) super.setEnabledSecureTransportProtocols(enabledSecureTransportProtocols);
  }

  @Override
  public SmppClientOptions setSslHandshakeTimeout(long sslHandshakeTimeout) {
    return (SmppClientOptions) super.setSslHandshakeTimeout(sslHandshakeTimeout);
  }

  @Override
  public SmppClientOptions setSslHandshakeTimeoutUnit(TimeUnit sslHandshakeTimeoutUnit) {
    return (SmppClientOptions) super.setSslHandshakeTimeoutUnit(sslHandshakeTimeoutUnit);
  }
}
