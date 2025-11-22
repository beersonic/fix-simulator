package com.beersonic;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import quickfix.Application;
import quickfix.DefaultMessageFactory;
import quickfix.FileLogFactory;
import quickfix.FileStoreFactory;
import quickfix.LogFactory;
import quickfix.Message;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;
import quickfix.SocketInitiator;

@Slf4j
@Service
public class QuickFixService {

  private final ConcurrentHashMap<String, SessionHolder> sessions = new ConcurrentHashMap<>();
  private final AtomicInteger idCounter = new AtomicInteger(1);
  private final FixSessionFactory sessionFactory;

  public QuickFixService(FixSessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  // For Spring Boot default construction
  public QuickFixService() {
    this(
        new FixSessionFactory() {
          @Override
          public SocketInitiator createInitiator(
              Application app,
              MessageStoreFactory storeFactory,
              SessionSettings settings,
              LogFactory logFactory,
              MessageFactory messageFactory)
              throws Exception {
            return new SocketInitiator(app, storeFactory, settings, logFactory, messageFactory);
          }

          @Override
          public SocketAcceptor createAcceptor(
              Application app,
              MessageStoreFactory storeFactory,
              SessionSettings settings,
              LogFactory logFactory,
              MessageFactory messageFactory)
              throws Exception {
            return new SocketAcceptor(app, storeFactory, settings, logFactory, messageFactory);
          }
        });
  }

  /**
   * Create and start a session based on the provided config map. Returns a generated sessionId
   * string which can be used to query/stop the session.
   */
  public String createSession(Map<String, String> config) throws Exception {
    String type = Optional.ofNullable(config.get("type")).orElse("initiator");
    String sender = config.get("senderCompID");
    String target = config.get("targetCompID");
    String host = Optional.ofNullable(config.get("host")).orElse("127.0.0.1");
    String port = Optional.ofNullable(config.get("port")).orElse("9876");
    String heartBtInt = Optional.ofNullable(config.get("heartBtInt")).orElse("30");
    String beginString = Optional.ofNullable(config.get("beginString")).orElse("FIXT.1.1");
    String applVer = Optional.ofNullable(config.get("defaultApplVerID")).orElse("FIX.5.0SP2");
    String resetOnLogon = config.get("resetOnLogon");

    if (sender == null || target == null) {
      throw new IllegalArgumentException("senderCompID and targetCompID are required");
    }

    // Compose session name (QuickFIX SessionID string)
    String sessionName = String.format("%s:%s->%s", beginString, sender, target);
    // Prevent duplicate session name
    for (SessionHolder sh : sessions.values()) {
      if (sh.sessionName != null && sh.sessionName.equals(sessionName)) {
        throw new IllegalArgumentException("Duplicate session name: " + sessionName);
      }
    }

    String sid = "s" + idCounter.getAndIncrement();

    StringBuilder cfg = new StringBuilder();
    cfg.append("[session]\n");
    cfg.append("BeginString=").append(beginString).append("\n");
    cfg.append("SenderCompID=").append(sender).append("\n");
    cfg.append("TargetCompID=").append(target).append("\n");
    cfg.append("ConnectionType=").append(type).append("\n");
    cfg.append("FileStorePath=store/").append(sid).append("\n");
    cfg.append("FileLogPath=log/").append(sid).append("\n");
    cfg.append("HeartBtInt=").append(heartBtInt).append("\n");
    cfg.append("DefaultApplVerID=").append(applVer).append("\n");
    if (resetOnLogon != null) {
      cfg.append("ResetOnLogon=").append(resetOnLogon).append("\n");
    }
    // Default session time window (allow full-day sessions by default)
    cfg.append("StartTime=00:00:00").append("\n");
    cfg.append("EndTime=23:59:59").append("\n");

    if ("initiator".equalsIgnoreCase(type)) {
      cfg.append("SocketConnectHost=").append(host).append("\n");
      cfg.append("SocketConnectPort=").append(port).append("\n");
    } else {
      cfg.append("SocketAcceptHost=").append(host).append("\n");
      cfg.append("SocketAcceptPort=").append(port).append("\n");
    }

    InputStream is = new ByteArrayInputStream(cfg.toString().getBytes(StandardCharsets.UTF_8));
    SessionSettings settings = new SessionSettings(is);

    MessageStoreFactory storeFactory = new FileStoreFactory(settings);
    LogFactory logFactory = new FileLogFactory(settings);
    MessageFactory messageFactory = new DefaultMessageFactory();

    InternalApplication application = new InternalApplication(sid);

    if ("initiator".equalsIgnoreCase(type)) {
      SocketInitiator initiator =
          sessionFactory.createInitiator(
              application, storeFactory, settings, logFactory, messageFactory);
      initiator.start();
      sessions.put(
          sid, new SessionHolder(sid, initiator, null, application, Instant.now(), sessionName));
      log.info(
          "Started initiator session {} -> {} (id={}; name={})", sender, target, sid, sessionName);
    } else {
      SocketAcceptor acceptor =
          sessionFactory.createAcceptor(
              application, storeFactory, settings, logFactory, messageFactory);
      acceptor.start();
      sessions.put(
          sid, new SessionHolder(sid, null, acceptor, application, Instant.now(), sessionName));
      log.info(
          "Started acceptor session {} -> {} (id={}; name={})", sender, target, sid, sessionName);
    }

    return sid;
  }

  public List<Map<String, Object>> listSessions() {
    List<Map<String, Object>> out = new ArrayList<>();
    for (SessionHolder sh : sessions.values()) {
      Map<String, Object> m = new java.util.HashMap<>(sh.toMap());
      boolean loggedOn = false;
      try {
        // Try to get QuickFIX/J session status
        if (sh.initiator != null && sh.initiator.getSessions().size() > 0) {
          quickfix.SessionID sid = sh.initiator.getSessions().get(0);
          loggedOn =
              quickfix.Session.lookupSession(sid) != null
                  && quickfix.Session.lookupSession(sid).isLoggedOn();
        } else if (sh.acceptor != null && sh.acceptor.getSessions().size() > 0) {
          quickfix.SessionID sid = sh.acceptor.getSessions().get(0);
          loggedOn =
              quickfix.Session.lookupSession(sid) != null
                  && quickfix.Session.lookupSession(sid).isLoggedOn();
        }
      } catch (Exception e) {
        loggedOn = false;
      }
      m.put("loggedOn", loggedOn);
      out.add(m);
    }
    return out;
  }

  public List<String> getMessages(String sessionId) {
    SessionHolder sh = sessions.get(sessionId);
    if (sh == null) return Collections.emptyList();
    return sh.getRecentMessages();
  }

  public boolean stopSession(String sessionId) {
    SessionHolder sh = sessions.remove(sessionId);
    if (sh == null) return false;
    sh.stop();
    log.info("Stopped session id={}", sessionId);
    return true;
  }

  // Small holder classes to keep session runtime objects
  private static class SessionHolder {
    final String id;
    final SocketInitiator initiator;
    final SocketAcceptor acceptor;
    final InternalApplication application;
    final Instant startedAt;
    final String sessionName;

    SessionHolder(
        String id,
        SocketInitiator initiator,
        SocketAcceptor acceptor,
        InternalApplication application,
        Instant startedAt,
        String sessionName) {
      this.id = id;
      this.initiator = initiator;
      this.acceptor = acceptor;
      this.application = application;
      this.startedAt = startedAt;
      this.sessionName = sessionName;
    }

    void stop() {
      try {
        if (initiator != null) initiator.stop();
        if (acceptor != null) acceptor.stop();
      } catch (Exception e) {
        // log but don't rethrow
        // Using slf4j logger from outer class isn't directly accessible here
        System.err.println("Error stopping session " + id + ": " + e.getMessage());
      }
    }

    List<String> getRecentMessages() {
      return application.getMessages();
    }

    Map<String, Object> toMap() {
      return Map.of(
          "id",
          id,
          "startedAt",
          startedAt.toString(),
          "messageCount",
          application.messageCount(),
          "active",
          true,
          "sessionName",
          sessionName);
    }
  }

  private static class InternalApplication implements Application {
    private final String sessionKey;
    private final ConcurrentLinkedDeque<String> messages = new ConcurrentLinkedDeque<>();
    private final int MAX_MESSAGES = 1000;

    InternalApplication(String sessionKey) {
      this.sessionKey = sessionKey;
    }

    @Override
    public void onCreate(SessionID sessionId) {
      // no-op
    }

    @Override
    public void onLogon(SessionID sessionId) {
      String m = String.format("[%s] Logon: %s", sessionKey, sessionId);
      push(m);
      log.info(m);
    }

    @Override
    public void onLogout(SessionID sessionId) {
      String m = String.format("[%s] Logout: %s", sessionKey, sessionId);
      push(m);
      log.info(m);
    }

    @Override
    public void toAdmin(Message message, SessionID sessionId) {
      String m = String.format("[%s] ToAdmin: %s", sessionKey, message);
      push(m);
      log.debug(m);
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionId) {
      String m = String.format("[%s] FromAdmin: %s", sessionKey, message);
      push(m);
      log.debug(m);
    }

    @Override
    public void toApp(Message message, SessionID sessionId) {
      String m = String.format("[%s] ToApp: %s", sessionKey, message);
      push(m);
      log.debug(m);
    }

    @Override
    public void fromApp(Message message, SessionID sessionKey) {
      String m = String.format("[%s] FromApp: %s", sessionKey, message);
      push(m);
      log.debug(m);
    }

    private void push(String m) {
      messages.addFirst(m);
      while (messages.size() > MAX_MESSAGES) {
        messages.removeLast();
      }
    }

    List<String> getMessages() {
      return new ArrayList<>(messages);
    }

    int messageCount() {
      return messages.size();
    }
  }
}
