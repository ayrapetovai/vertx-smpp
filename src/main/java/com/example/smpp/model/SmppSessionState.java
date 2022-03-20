package com.example.smpp.model;

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

import com.example.smpp.SmppConstants;

/**
 * SMPP_v5_0.pdf
 * 2.4 Operation Matrix - table with eligible operations for particular states.
 */
public enum SmppSessionState {
  OPENED {  // Before sending or receiving bind_*(resp)
    @Override
    public boolean canSend(boolean isServer, int commandId) {
      if (isServer) {
        switch (commandId) {
          case SmppConstants.CMD_ID_BIND_TRANSCEIVER_RESP:
          case SmppConstants.CMD_ID_BIND_RECEIVER_RESP:
          case SmppConstants.CMD_ID_BIND_TRANSMITTER_RESP:
          case SmppConstants.CMD_ID_ENQUIRE_LINK:
          case SmppConstants.CMD_ID_ENQUIRE_LINK_RESP:
          case SmppConstants.CMD_ID_GENERIC_NACK:
          case SmppConstants.CMD_ID_OUTBIND:
            return true;
          default:
            return false;
        }
      } else {
        switch (commandId) {
          case SmppConstants.CMD_ID_BIND_TRANSCEIVER:
          case SmppConstants.CMD_ID_BIND_RECEIVER:
          case SmppConstants.CMD_ID_BIND_TRANSMITTER:
          case SmppConstants.CMD_ID_ENQUIRE_LINK:
          case SmppConstants.CMD_ID_ENQUIRE_LINK_RESP:
          case SmppConstants.CMD_ID_GENERIC_NACK:
            return true;
          default:
            return false;
        }
      }
    }

    @Override
    public boolean canReceive(boolean isServer, int commandId) {
      if (isServer) {
        switch (commandId) {
          case SmppConstants.CMD_ID_BIND_TRANSCEIVER:
          case SmppConstants.CMD_ID_BIND_RECEIVER:
          case SmppConstants.CMD_ID_BIND_TRANSMITTER:
          case SmppConstants.CMD_ID_ENQUIRE_LINK:
          case SmppConstants.CMD_ID_ENQUIRE_LINK_RESP:
          case SmppConstants.CMD_ID_GENERIC_NACK:
            return true;
          default:
            return false;
        }
      } else {
        switch (commandId) {
          case SmppConstants.CMD_ID_BIND_TRANSCEIVER_RESP:
          case SmppConstants.CMD_ID_BIND_RECEIVER_RESP:
          case SmppConstants.CMD_ID_BIND_TRANSMITTER_RESP:
          case SmppConstants.CMD_ID_ENQUIRE_LINK:
          case SmppConstants.CMD_ID_ENQUIRE_LINK_RESP:
          case SmppConstants.CMD_ID_GENERIC_NACK:
            return true;
          default:
            return false;
        }
      }
    }
  },
  BOUND_TX {
    @Override
    public boolean canSend(boolean isServer, int commandId) {
      if (isServer) {
        switch (commandId) {
          case SmppConstants.CMD_ID_BROADCAST_SM_RESP:
          case SmppConstants.CMD_ID_CANCEL_BROADCAST_SM_RESP:
          case SmppConstants.CMD_ID_CANCEL_SM_RESP:
          case SmppConstants.CMD_ID_DATA_SM_RESP:
          case SmppConstants.CMD_ID_ENQUIRE_LINK:
          case SmppConstants.CMD_ID_ENQUIRE_LINK_RESP:
          case SmppConstants.CMD_ID_GENERIC_NACK:
          case SmppConstants.CMD_ID_QUERY_BROADCAST_SM_RESP:
          case SmppConstants.CMD_ID_QUERY_SM_RESP:
          case SmppConstants.CMD_ID_SUBMIT_MULTI_RESP:
          case SmppConstants.CMD_ID_UNBIND:
          case SmppConstants.CMD_ID_UNBIND_RESP:
            return true;
          default:
            return false;
        }
      } else {
        switch (commandId) {
          case SmppConstants.CMD_ID_BROADCAST_SM:
          case SmppConstants.CMD_ID_CANCEL_BROADCAST_SM:
          case SmppConstants.CMD_ID_CANCEL_SM:
          case SmppConstants.CMD_ID_DATA_SM:
          case SmppConstants.CMD_ID_ENQUIRE_LINK:
          case SmppConstants.CMD_ID_ENQUIRE_LINK_RESP:
          case SmppConstants.CMD_ID_GENERIC_NACK:
          case SmppConstants.CMD_ID_QUERY_BROADCAST_SM:
          case SmppConstants.CMD_ID_QUERY_SM:
          case SmppConstants.CMD_ID_SUBMIT_MULTI:
          case SmppConstants.CMD_ID_UNBIND:
          case SmppConstants.CMD_ID_UNBIND_RESP:
            return true;
          default:
            return false;
        }
      }
    }

    @Override
    public boolean canReceive(boolean isServer, int commandId) {
      if (isServer) {
        switch (commandId) {
          case SmppConstants.CMD_ID_BROADCAST_SM:
          case SmppConstants.CMD_ID_CANCEL_BROADCAST_SM:
          case SmppConstants.CMD_ID_CANCEL_SM:
          case SmppConstants.CMD_ID_DATA_SM:
          case SmppConstants.CMD_ID_ENQUIRE_LINK:
          case SmppConstants.CMD_ID_ENQUIRE_LINK_RESP:
          case SmppConstants.CMD_ID_GENERIC_NACK:
          case SmppConstants.CMD_ID_QUERY_BROADCAST_SM:
          case SmppConstants.CMD_ID_QUERY_SM:
          case SmppConstants.CMD_ID_SUBMIT_MULTI:
          case SmppConstants.CMD_ID_UNBIND:
          case SmppConstants.CMD_ID_UNBIND_RESP:
            return true;
          default:
            return false;
        }
      } else {
        switch (commandId) {
          case SmppConstants.CMD_ID_BROADCAST_SM_RESP:
          case SmppConstants.CMD_ID_CANCEL_BROADCAST_SM_RESP:
          case SmppConstants.CMD_ID_CANCEL_SM_RESP:
          case SmppConstants.CMD_ID_DATA_SM_RESP:
          case SmppConstants.CMD_ID_ENQUIRE_LINK:
          case SmppConstants.CMD_ID_ENQUIRE_LINK_RESP:
          case SmppConstants.CMD_ID_GENERIC_NACK:
          case SmppConstants.CMD_ID_QUERY_BROADCAST_SM_RESP:
          case SmppConstants.CMD_ID_QUERY_SM_RESP:
          case SmppConstants.CMD_ID_SUBMIT_MULTI_RESP:
          case SmppConstants.CMD_ID_UNBIND:
          case SmppConstants.CMD_ID_UNBIND_RESP:
            return true;
          default:
            return false;
        }
      }
    }
  },
  BOUND_RX {
    @Override
    public boolean canSend(boolean isServer, int commandId) {
      if (isServer) {
        switch (commandId) {
          case SmppConstants.CMD_ID_ALERT_NOTIFICATION:
          case SmppConstants.CMD_ID_DATA_SM:
          case SmppConstants.CMD_ID_DELIVER_SM:
          case SmppConstants.CMD_ID_ENQUIRE_LINK:
          case SmppConstants.CMD_ID_ENQUIRE_LINK_RESP:
          case SmppConstants.CMD_ID_GENERIC_NACK:
          case SmppConstants.CMD_ID_QUERY_BROADCAST_SM_RESP:
          case SmppConstants.CMD_ID_UNBIND:
          case SmppConstants.CMD_ID_UNBIND_RESP:
            return true;
          default:
            return false;
        }
      } else {
        switch (commandId) {
          case SmppConstants.CMD_ID_DATA_SM_RESP:
          case SmppConstants.CMD_ID_DELIVER_SM_RESP:
          case SmppConstants.CMD_ID_ENQUIRE_LINK:
          case SmppConstants.CMD_ID_ENQUIRE_LINK_RESP:
          case SmppConstants.CMD_ID_GENERIC_NACK:
          case SmppConstants.CMD_ID_UNBIND:
          case SmppConstants.CMD_ID_UNBIND_RESP:
            return true;
          default:
            return false;
        }
      }
    }

    @Override
    public boolean canReceive(boolean isServer, int commandId) {
      if (isServer) {
        switch (commandId) {
          case SmppConstants.CMD_ID_DATA_SM_RESP:
          case SmppConstants.CMD_ID_DELIVER_SM_RESP:
          case SmppConstants.CMD_ID_ENQUIRE_LINK:
          case SmppConstants.CMD_ID_ENQUIRE_LINK_RESP:
          case SmppConstants.CMD_ID_GENERIC_NACK:
          case SmppConstants.CMD_ID_UNBIND:
          case SmppConstants.CMD_ID_UNBIND_RESP:
            return true;
          default:
            return false;
        }
      } else {
        switch (commandId) {
          case SmppConstants.CMD_ID_ALERT_NOTIFICATION:
          case SmppConstants.CMD_ID_DATA_SM:
          case SmppConstants.CMD_ID_DELIVER_SM:
          case SmppConstants.CMD_ID_ENQUIRE_LINK:
          case SmppConstants.CMD_ID_ENQUIRE_LINK_RESP:
          case SmppConstants.CMD_ID_GENERIC_NACK:
          case SmppConstants.CMD_ID_QUERY_BROADCAST_SM_RESP:
          case SmppConstants.CMD_ID_UNBIND:
          case SmppConstants.CMD_ID_UNBIND_RESP:
            return true;
          default:
            return false;
        }
      }
    }
  },
  BOUND_TRX {
    @Override
    public boolean canSend(boolean isServer, int commandId) {
      if (isServer) {
        switch (commandId) {
          case SmppConstants.CMD_ID_ALERT_NOTIFICATION:
          case SmppConstants.CMD_ID_BROADCAST_SM_RESP:
          case SmppConstants.CMD_ID_CANCEL_BROADCAST_SM_RESP:
          case SmppConstants.CMD_ID_CANCEL_SM_RESP:
          case SmppConstants.CMD_ID_DATA_SM:
          case SmppConstants.CMD_ID_DATA_SM_RESP:
          case SmppConstants.CMD_ID_DELIVER_SM:
          case SmppConstants.CMD_ID_ENQUIRE_LINK:
          case SmppConstants.CMD_ID_ENQUIRE_LINK_RESP:
          case SmppConstants.CMD_ID_GENERIC_NACK:
          case SmppConstants.CMD_ID_QUERY_BROADCAST_SM_RESP:
          case SmppConstants.CMD_ID_QUERY_SM_RESP:
          case SmppConstants.CMD_ID_SUBMIT_MULTI_RESP:
          case SmppConstants.CMD_ID_SUBMIT_SM_RESP:
          case SmppConstants.CMD_ID_UNBIND:
          case SmppConstants.CMD_ID_UNBIND_RESP:
            return true;
          default:
            return false;
        }
      } else {
        switch (commandId) {
          case SmppConstants.CMD_ID_BROADCAST_SM:
          case SmppConstants.CMD_ID_CANCEL_BROADCAST_SM:
          case SmppConstants.CMD_ID_CANCEL_SM:
          case SmppConstants.CMD_ID_DATA_SM:
          case SmppConstants.CMD_ID_DATA_SM_RESP:
          case SmppConstants.CMD_ID_DELIVER_SM_RESP:
          case SmppConstants.CMD_ID_ENQUIRE_LINK:
          case SmppConstants.CMD_ID_ENQUIRE_LINK_RESP:
          case SmppConstants.CMD_ID_GENERIC_NACK:
          case SmppConstants.CMD_ID_QUERY_BROADCAST_SM:
          case SmppConstants.CMD_ID_QUERY_SM:
          case SmppConstants.CMD_ID_SUBMIT_MULTI:
          case SmppConstants.CMD_ID_SUBMIT_SM:
          case SmppConstants.CMD_ID_UNBIND:
          case SmppConstants.CMD_ID_UNBIND_RESP:
            return true;
          default:
            return false;
        }
      }
    }

    @Override
    public boolean canReceive(boolean isServer, int commandId) {
      if (isServer) {
        switch (commandId) {
          case SmppConstants.CMD_ID_BROADCAST_SM:
          case SmppConstants.CMD_ID_CANCEL_BROADCAST_SM:
          case SmppConstants.CMD_ID_CANCEL_SM:
          case SmppConstants.CMD_ID_DATA_SM:
          case SmppConstants.CMD_ID_DATA_SM_RESP:
          case SmppConstants.CMD_ID_DELIVER_SM_RESP:
          case SmppConstants.CMD_ID_ENQUIRE_LINK:
          case SmppConstants.CMD_ID_ENQUIRE_LINK_RESP:
          case SmppConstants.CMD_ID_GENERIC_NACK:
          case SmppConstants.CMD_ID_QUERY_BROADCAST_SM:
          case SmppConstants.CMD_ID_QUERY_SM:
          case SmppConstants.CMD_ID_SUBMIT_MULTI:
          case SmppConstants.CMD_ID_SUBMIT_SM:
          case SmppConstants.CMD_ID_UNBIND:
          case SmppConstants.CMD_ID_UNBIND_RESP:
            return true;
          default:
            return false;
        }
      } else {
        switch (commandId) {
          case SmppConstants.CMD_ID_ALERT_NOTIFICATION:
          case SmppConstants.CMD_ID_BROADCAST_SM_RESP:
          case SmppConstants.CMD_ID_CANCEL_BROADCAST_SM_RESP:
          case SmppConstants.CMD_ID_CANCEL_SM_RESP:
          case SmppConstants.CMD_ID_DATA_SM:
          case SmppConstants.CMD_ID_DATA_SM_RESP:
          case SmppConstants.CMD_ID_DELIVER_SM:
          case SmppConstants.CMD_ID_ENQUIRE_LINK:
          case SmppConstants.CMD_ID_ENQUIRE_LINK_RESP:
          case SmppConstants.CMD_ID_GENERIC_NACK:
          case SmppConstants.CMD_ID_QUERY_BROADCAST_SM_RESP:
          case SmppConstants.CMD_ID_QUERY_SM_RESP:
          case SmppConstants.CMD_ID_SUBMIT_MULTI_RESP:
          case SmppConstants.CMD_ID_SUBMIT_SM_RESP:
          case SmppConstants.CMD_ID_UNBIND:
          case SmppConstants.CMD_ID_UNBIND_RESP:
            return true;
          default:
            return false;
        }
      }
    }
  },
  UNBOUND { // after unbind received
    @Override
    public boolean canSend(boolean isServer, int commandId) {
      switch (commandId) {
        case SmppConstants.CMD_ID_ENQUIRE_LINK:
        case SmppConstants.CMD_ID_ENQUIRE_LINK_RESP:
        case SmppConstants.CMD_ID_GENERIC_NACK:
          return true;
        default:
          return false;
      }
    }

    @Override
    public boolean canReceive(boolean isServer, int commandId) {
      switch (commandId) {
        case SmppConstants.CMD_ID_ENQUIRE_LINK:
        case SmppConstants.CMD_ID_ENQUIRE_LINK_RESP:
        case SmppConstants.CMD_ID_GENERIC_NACK:
          return true;
        default:
          return false;
      }
    }
  },
  CLOSED { // after UNBIND
    @Override
    public boolean canSend(boolean isServer, int commandId) {
      return false;
    }

    @Override
    public boolean canReceive(boolean isServer, int commandId) {
      return false;
    }
  },
  OUTBOUND { // same as OPEN, but when MC claimed a connection.
    @Override
    public boolean canSend(boolean isServer, int commandId) {
      return false;
    }

    @Override
    public boolean canReceive(boolean isServer, int commandId) {
      return false;
    }
  }
  ;

  public abstract boolean canSend(boolean isServer, int commandId);

  public abstract boolean canReceive(boolean isServer, int commandId);
}
