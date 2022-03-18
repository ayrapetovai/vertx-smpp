package com.example.smpp.util;

import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.pdu.*;
import com.cloudhopper.smpp.tlv.Tlv;
import com.cloudhopper.smpp.tlv.TlvConvertException;
import com.example.smpp.model.SmppBindType;
import com.example.smpp.model.SmppSessionState;

public class Helper {
  public static BaseBind<? extends BaseBindResp> bindRequesstByBinType(SmppBindType bindType) {
    switch (bindType) {
      case TRANSMITTER:
        return new BindTransmitter();
      case RECEIVER:
        return new BindReceiver();
      case TRANSCEIVER:
        return new BindTransceiver();
      default:
        throw new IllegalStateException("no such enumerator " + bindType);
    }
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
}
