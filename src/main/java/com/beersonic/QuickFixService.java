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

    if (sender == null || target == null) {
      throw new IllegalArgumentException("senderCompID and targetCompID are required");
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
          new SocketInitiator(application, storeFactory, settings, logFactory, messageFactory);
      initiator.start();
      sessions.put(
          sid, new SessionHolder(sid, initiator, null, application, Instant.now(), settings));
      log.info("Started initiator session {} -> {} (id={})", sender, target, sid);
    } else {
      SocketAcceptor acceptor =
          new SocketAcceptor(application, storeFactory, settings, logFactory, messageFactory);
      acceptor.start();
      sessions.put(
          sid, new SessionHolder(sid, null, acceptor, application, Instant.now(), settings));
      log.info("Started acceptor session {} -> {} (id={})", sender, target, sid);
    }

    return sid;
  }

  public List<Map<String, Object>> listSessions() {
    List<Map<String, Object>> out = new ArrayList<>();
    for (SessionHolder sh : sessions.values()) {
      out.add(sh.toMap());
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
    final SessionSettings settings;
    final InternalApplication application;
    final Instant startedAt;

    SessionHolder(
        String id,
        SocketInitiator initiator,
        SocketAcceptor acceptor,
        InternalApplication application,
        Instant startedAt,
        SessionSettings settings) {
      this.id = id;
      this.initiator = initiator;
      this.acceptor = acceptor;
      this.application = application;
      this.startedAt = startedAt;
      this.settings = settings;
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

    private String getSessionField(String key) {
      // Try to get from session settings if available
      try {
        if (settings != null) {
          if (initiator != null && initiator.getSessions().size() > 0) {
            SessionID sid = initiator.getSessions().get(0);
            return settings.getString(sid, key);
          }
          if (acceptor != null && acceptor.getSessions().size() > 0) {
            SessionID sid = acceptor.getSessions().get(0);
            return settings.getString(sid, key);
          }
        }
      } catch (Exception e) {
        // ignore
      }
      return null;
    }

    Map<String, Object> toMap() {
      Map<String, Object> map = new java.util.HashMap<>();
      map.put("id", id);
      map.put("type", getSessionField("ConnectionType"));
      map.put("senderCompID", getSessionField("SenderCompID"));
      map.put("targetCompID", getSessionField("TargetCompID"));
      map.put(
          "host",
          getSessionField("SocketConnectHost") != null
              ? getSessionField("SocketConnectHost")
              : getSessionField("SocketAcceptHost"));
      map.put(
          "port",
          getSessionField("SocketConnectPort") != null
              ? getSessionField("SocketConnectPort")
              : getSessionField("SocketAcceptPort"));
      map.put("heartBtInt", getSessionField("HeartBtInt"));
      map.put("defaultApplVerID", getSessionField("DefaultApplVerID"));
      map.put("startedAt", startedAt.toString());
      map.put("messageCount", application.messageCount());
      map.put("loggedOn", application.isLoggedOn());
      return map;
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
    public void fromApp(Message message, SessionID sessionId) {
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

    boolean isLoggedOn() {
      // This is a simple check: if last message contains "Logon" and not "Logout"
      for (String m : messages) {
        if (m.contains("Logon")) return true;
        if (m.contains("Logout")) return false;
      }
      return false;
    }
  }
}
