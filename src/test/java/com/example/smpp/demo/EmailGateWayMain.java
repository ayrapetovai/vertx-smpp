package com.example.smpp.demo;

import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.example.smpp.Smpp;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.mail.LoginOption;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.auth.LoginFailedException;
import org.subethamail.smtp.auth.PlainAuthenticationHandlerFactory;
import org.subethamail.smtp.auth.UsernamePasswordValidator;
import org.subethamail.smtp.helper.SimpleMessageListener;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

class LocalSmtpServer {
  private static final Logger log = LoggerFactory.getLogger(EmailGateWayMain.class);

  public static class MyMessageListener implements SimpleMessageListener {
    @Override
    public boolean accept(String from, String recipient) {
      log.info("accept " + from + " to " + recipient);
      return true;
    }

    @Override
    public void deliver(String from, String recipient, InputStream data) throws IOException {
      StringBuilder textBuilder = new StringBuilder();
      try (Reader reader = new BufferedReader(new InputStreamReader(data, Charset.forName(StandardCharsets.UTF_8.name())))) {
        int c;
        while ((c = reader.read()) != -1) {
          textBuilder.append((char) c);
        }
      }
      log.info("Sending mail from " + from + " to " + recipient + " (size: " + textBuilder.length() + " bytes)");
      log.info("Text: " + textBuilder);
    }
  }

  public static void start(int port) {
    SMTPServer smtpServer = new SMTPServer(new SimpleMessageListenerAdapter(new MyMessageListener()));
    smtpServer.setPort(port);
    smtpServer.start();
  }

  public static void startWithAuth(int port) {
    SMTPServer server = new SMTPServer(new SimpleMessageListenerAdapter(new MyMessageListener()));
    server.setPort(port);
    UsernamePasswordValidator validator = (s, s1) -> {
      if (!"username".equalsIgnoreCase(s) || !"password".equalsIgnoreCase(s1)) {
        throw new LoginFailedException();
      }
    };
    server.setAuthenticationHandlerFactory(new PlainAuthenticationHandlerFactory(validator));
    server.start();
  }
}

public class EmailGateWayMain extends AbstractVerticle {
  private final static Logger log = LoggerFactory.getLogger(EmailGateWayMain.class);

  @Override
  public void start(Promise<Void> started) {

    LocalSmtpServer.startWithAuth(5870);
    log.info("smtp server started");

    MailConfig mailConfig = new MailConfig()
        .setHostname("localhost")
        .setPort(5870)
        //.setStarttls(StartTLSOptions.REQUIRED)
        .setLogin(LoginOption.REQUIRED)
        .setAuthMethods("PLAIN")
        .setUsername("username")
        .setPassword("password");

    MailClient mailClient = MailClient.createShared(vertx, mailConfig);
    Smpp.server(vertx)
        .configure(configurator -> {
          configurator.onRequest(rqCtx -> {
            if (rqCtx.getRequest() instanceof SubmitSm) {
              rqCtx.getSession()
                  .reply(rqCtx.getRequest().createResponse());
              mailClient.sendMail(createEmail((SubmitSm) rqCtx.getRequest()))
                  .onComplete(ar -> {
                    var deliveryReport = new DeliverSm();
                    deliveryReport.setCommandStatus(
                        ar.succeeded()? SmppConstants.STATUS_OK: SmppConstants.STATUS_DELIVERYFAILURE
                    );
                    rqCtx.getSession()
                        .send(deliveryReport)
                        .onComplete(deliveryResult -> {
                          log.info("delivery {}", deliveryResult.succeeded()? "success": "fail");
                        });
                  });
            }
          });
          return true;
        })
        .start("localhost", 2776)
        .onSuccess(s -> {
          log.info("smpp server started");
          started.complete();
        })
        .onFailure(e -> {
          log.error("Could not start smpp server", e);
        });
  }

  private MailMessage createEmail(SubmitSm submitSm) {
    var sender = submitSm.getSourceAddress() != null? submitSm.getSourceAddress().getAddress(): "unknown";
    var recepient = submitSm.getDestAddress() != null? submitSm.getDestAddress().getAddress(): "unknown";
    var text = new String(submitSm.getShortMessage());

    MailMessage email = new MailMessage()
        .setFrom((sender != null? sender: "unknown") + "@cell.operator")
        .setTo((recepient != null? recepient: "unknown") + "@cell.operator")
        .setText(text);

    return email;
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(EmailGateWayMain.class.getCanonicalName());
  }
}