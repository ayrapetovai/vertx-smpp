package com.example.smpp.model;

import com.cloudhopper.smpp.pdu.BaseBind;
import com.cloudhopper.smpp.pdu.BaseBindResp;

public class BindInfo<T extends BaseBindResp> {
  private final BaseBind<T> bindRequest;

  public BindInfo(BaseBind<T> bindRequest) {
    this.bindRequest = bindRequest;
  }

  public BaseBind<T> getBindRequest() {
    return bindRequest;
  }
}
