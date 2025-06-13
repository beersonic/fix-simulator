package com.beersonic.fix;

import com.beersonic.fix.visualizer.FixMessageVisualizerService; // Import the service
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import quickfix.*;

@Slf4j
@Component
public class FixInitiator implements Application {

  private final FixMessageVisualizerService visualizerService;

    @Autowired // Use constructor injection
  public FixInitiator(FixMessageVisualizerService visualizerService) throws ConfigError {
    this.visualizerService = visualizerService;
        // Store settings
        SessionSettings settings = new SessionSettings("config/quickfixj.cfg"); // Load from config dir

        // Initialize the dictionary in the service
    this.visualizerService.initializeDictionary(settings);

    MessageStoreFactory storeFactory = new FileStoreFactory(settings);
    LogFactory logFactory =
        new FileLogFactory(settings); // Consider SLF4JLogFactory if you want QF/J logs via SLF4J
    MessageFactory messageFactory = new DefaultMessageFactory();
    Initiator initiator =
        new SocketInitiator(this, storeFactory, settings, logFactory, messageFactory);
    initiator.start();

    log.info("FixInitiator started and attempting to connect.");
  }

  @Override
  public void onCreate(SessionID sessionId) {
    log.info("Session CREATED: {}", sessionId);
    // Optionally, add a system message to the visualizer
    // visualizerService.addSystemEvent("Session CREATED: " + sessionId);
  }

  @Override
  public void onLogon(SessionID sessionId) {
    log.info("Session LOGGED ON: {}", sessionId);
    // visualizerService.addSystemEvent("Session LOGGED ON: " + sessionId);
  }

  @Override
  public void onLogout(SessionID sessionId) {
    log.info("Session LOGGED OUT: {}", sessionId);
    // visualizerService.addSystemEvent("Session LOGGED OUT: " + sessionId);
  }

  @Override
  public void toAdmin(Message message, SessionID sessionId) {
    log.debug("ToAdmin: {}", message.toString().replace((char) 1, '|'));
    visualizerService.addMessage(message, sessionId, "OUTGOING (Admin)");
  }

  @Override
  public void fromAdmin(Message message, SessionID sessionId) {
    log.debug("FromAdmin: {}", message.toString().replace((char) 1, '|'));
    visualizerService.addMessage(message, sessionId, "INCOMING (Admin)");
  }

  @Override
  public void toApp(Message message, SessionID sessionId) {
    log.debug("ToApp: {}", message.toString().replace((char) 1, '|'));
    visualizerService.addMessage(message, sessionId, "OUTGOING (App)");
  }

  @Override
  public void fromApp(Message message, SessionID sessionId) {
    log.debug("FromApp: {}", message.toString().replace((char) 1, '|'));
    visualizerService.addMessage(message, sessionId, "INCOMING (App)");
  }
}
