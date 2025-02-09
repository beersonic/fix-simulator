package com.beersonic.fix;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import quickfix.*;

@Slf4j
@Component
public class FixInitiator implements Application {

  public FixInitiator() throws ConfigError {
    SessionSettings settings = new SessionSettings("config/quickfixj.cfg");
    MessageStoreFactory storeFactory = new FileStoreFactory(settings);
    LogFactory logFactory = new FileLogFactory(settings);
    MessageFactory messageFactory = new DefaultMessageFactory();
    Initiator initiator =
        new SocketInitiator(this, storeFactory, settings, logFactory, messageFactory);
    initiator.start();

    log.info("Initiator is started");
  }

  @Override
  public void onCreate(SessionID sessionId) {}

  @Override
  public void onLogon(SessionID sessionId) {}

  @Override
  public void onLogout(SessionID sessionId) {}

  @Override
  public void toAdmin(Message message, SessionID sessionId) {}

  @Override
  public void fromAdmin(Message message, SessionID sessionId) {}

  @Override
  public void toApp(Message message, SessionID sessionId) {}

  @Override
  public void fromApp(Message message, SessionID sessionId) {}
}
