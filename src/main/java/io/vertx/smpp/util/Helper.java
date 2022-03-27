package io.vertx.smpp.util;

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

import io.vertx.smpp.SmppConstants;
import io.vertx.smpp.pdu.*;
import io.vertx.smpp.session.SmppSessionImpl;
import io.vertx.smpp.model.SmppBindType;
import io.vertx.smpp.model.SmppSessionState;
import io.vertx.smpp.tlv.Tlv;
import io.vertx.smpp.tlv.TlvConvertException;

public class Helper {

  public static BaseBind<? extends BaseBindResp> bindRequestByBindType(SmppSessionImpl session) {
    var bindType = session.getOptions().getBindType();
    BaseBind<? extends BaseBindResp> bindRequest;
    switch (bindType) {
      case TRANSMITTER:
        bindRequest = new BindTransmitter(); break;
      case RECEIVER:
        bindRequest = new BindReceiver(); break;
      case TRANSCEIVER:
        bindRequest = new BindTransceiver(); break;
      default:
        throw new IllegalStateException("no such enumerator " + bindType);
    }

    bindRequest.setSystemId(session.getOptions().getSystemId());
    bindRequest.setPassword(session.getOptions().getPassword());
    bindRequest.setInterfaceVersion(session.getThisInterfaceVersion());
    bindRequest.setSystemType(session.getOptions().getSystemType());
    bindRequest.setAddressRange(session.getOptions().getAddressRange());

    return bindRequest;
  }

  public static SmppSessionState sessionStateByBindType(SmppBindType bindType) {
    switch (bindType) {
      case TRANSMITTER:
        return SmppSessionState.BOUND_TX;
      case RECEIVER:
        return SmppSessionState.BOUND_RX;
      case TRANSCEIVER:
        return SmppSessionState.BOUND_TRX;
      default:
        throw new IllegalStateException("no such bind type " + bindType);
    }
  }

  public static SmppSessionState sessionStateByCommandId(int commandId) {
    switch (commandId) {
      case SmppConstants.CMD_ID_BIND_RECEIVER: return SmppSessionState.BOUND_RX;
      case SmppConstants.CMD_ID_BIND_TRANSMITTER: return SmppSessionState.BOUND_TX;
      case SmppConstants.CMD_ID_BIND_TRANSCEIVER: return SmppSessionState.BOUND_TRX;
      default:
        throw new IllegalStateException("unexpected pdu type " + commandId);
    }
  }

  public static byte intVerFromTlv(BaseBindResp bindResp) {
    var intVer = SmppConstants.VERSION_3_3;
    Tlv scInterfaceVersion = bindResp.getOptionalParameter(SmppConstants.TAG_SC_INTERFACE_VERSION);
    if (scInterfaceVersion != null) {
      try {
        byte tempInterfaceVersion = scInterfaceVersion.getValueAsByte();
        if (tempInterfaceVersion >= SmppConstants.VERSION_3_4) {
          intVer = SmppConstants.VERSION_3_4;
        } else {
          intVer = SmppConstants.VERSION_3_3;
        }
      } catch (TlvConvertException e) {
//        log.warn("Unable to convert sc_interface_version to a byte value: {}", e.getMessage());
        intVer = SmppConstants.VERSION_3_3;
      }
    }
    return intVer;
  }

  public static void addInterfaceVersionTlv(BaseBindResp bindResp, byte ourInterfaceVersion, byte theirInterfaceVersion) {
    if (ourInterfaceVersion >= SmppConstants.VERSION_3_4 && theirInterfaceVersion >= SmppConstants.VERSION_3_4) {
      Tlv interfaceVersionTlv = new Tlv(SmppConstants.TAG_SC_INTERFACE_VERSION, new byte[] { ourInterfaceVersion });
      bindResp.addOptionalParameter(interfaceVersionTlv);
    }
  }

  public static SmppBindType bindTypeByCommandId(BaseBind<?> bindRequest) {
    var commandId = bindRequest.getCommandId();
    switch (commandId) {
      case SmppConstants.CMD_ID_BIND_TRANSMITTER: return SmppBindType.TRANSMITTER;
      case SmppConstants.CMD_ID_BIND_RECEIVER: return SmppBindType.RECEIVER;
      case SmppConstants.CMD_ID_BIND_TRANSCEIVER: return SmppBindType.TRANSCEIVER;
      default:
        throw new IllegalStateException("command id does not correspond to bind type, got " + commandId);
    }
  }
}
