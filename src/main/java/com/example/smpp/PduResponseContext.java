package com.example.smpp;

import com.cloudhopper.smpp.pdu.PduResponse;

// TODO to PduResponseContext
/*
 * Gets the original request associated with the response.
 * @return The original request
 */
//public PduRequest getRequest();

/*
 * Gets the response from the remote endpoint.
 * @return The response
 */
//public PduResponse getResponse();

/*
 * Gets the size of the window after this request was added.
 * @return The size of the window after this request was added.
 */
//public int getWindowSize();

/*
 * Gets the amount of time required to accept the request into the session
 * send window (for a free slot to open up).
 * @return The amount of time (in ms) to accept the request into the send window
 */
//public long getWindowWaitTime();

/*
 * Gets the amount of time required for the remote endpoint to acknowledge
 * the request with a response.  This value is based on the time the request
 * went out on the wire till a response was received on the wire.  Does not
 * include any time required waiting for a slot in the window to become
 * available.
 * <br><br>
 * NOTE: If the window size is > 1, this value can be somewhat misleading.
 * The remote endpoint would process X number of requests ahead of this one
 * that went out ahead of it in the window.  This does represent the total
 * response time, but doesn't mean the remote endpoint is this slow at processing
 * one request.  In cases of high load where the window is always full, the
 * windowWaitTime actually represents how fast the remote endpoint is processing
 * requests.
 * @return The amount of time (in ms) to receive a response from remote endpoint
 */
// public long getResponseTime();

/*
 * Gets an estimate of the processing time required by the remote endpoint
 * to process this request.  The value is calculated with the following
 * formula: "response time" divided by the "window size" at the time of the
 * request.
 * @return The amount of estimated time (in ms) to receive a response from
 *      the remote endpoint just for this request (as opposed to potentially
 *      this request and all requests ahead of it in the window).
 */
//public long getEstimatedProcessingTime();
public class PduResponseContext {
  private final PduResponse response;
  private final SmppSession session;

  public PduResponseContext(PduResponse response, SmppSession session) {
    this.response = response;
    this.session = session;
  }

  public PduResponse getResponse() {
    return response;
  }

  public SmppSession getSession() {
    return session;
  }

  @Override
  public String toString() {
    return response.toString();
  }
}
