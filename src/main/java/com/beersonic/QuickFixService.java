package com.beersonic;

import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import quickfix.*;
import quickfix.field.*;

@Slf4j
@Service
public class QuickFixService {

  private SocketInitiator initiator;

  public void start() {
    try {
      InputStream configStream = getClass().getClassLoader().getResourceAsStream("quickfixj.cfg");
      if (configStream == null) {
        throw new RuntimeException("quickfixj.cfg not found");
      }

      SessionSettings settings = new SessionSettings(configStream);
      Application application = new ApplicationAdapter();
      MessageStoreFactory storeFactory = new FileStoreFactory(settings);
      LogFactory logFactory = new FileLogFactory(settings);
      MessageFactory messageFactory = new DefaultMessageFactory();

      initiator =
          new SocketInitiator(application, storeFactory, settings, logFactory, messageFactory);
      initiator.start();

      log.info("QuickFIX/J service started.");
    } catch (Exception e) {
      log.error("Failed to start QuickFIX/J service", e);
    }
  }

  public void stop() {
    if (initiator != null) {
      initiator.stop();
      log.info("QuickFIX/J service stopped.");
    }
  }

  private static class ApplicationAdapter extends quickfix.ApplicationAdapter {
    @Override
    public void onLogon(SessionID sessionId) {
      log.info("Logon: {}", sessionId);
    }

    @Override
    public void onLogout(SessionID sessionId) {
      log.info("Logout: {}", sessionId);
    }

    @Override
    public void toAdmin(Message message, SessionID sessionId) {
      log.info("To Admin: {}", message);
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionId) {
      log.info("From Admin: {}", message);
    }

    @Override
    public void toApp(Message message, SessionID sessionId) {
      log.info("To App: {}", message);
    }

    @Override
    public void fromApp(Message message, SessionID sessionId) {
      log.info("From App: {}", message);
    }
  }
}
