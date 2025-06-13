package com.beersonic.fix.visualizer;

import java.time.LocalDateTime;
import java.util.*;
import java.util.Iterator; // Explicit import for Field iterator
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import quickfix.*;
import quickfix.field.MsgType; // Crucial for getting message type string

@Slf4j
@Service
public class FixMessageVisualizerService {

  private final List<VisualizedFixMessage> messages = new CopyOnWriteArrayList<>();
  private static final int MAX_MESSAGES = 200; // Keep a rolling window
  private DataDictionary dataDictionary;

  /**
   * Initializes the DataDictionary based on session settings. This should be called after
   * SessionSettings are loaded.
   *
   * @param settings The SessionSettings for the FIX session.
   */
  // In FixMessageVisualizerService.java

  public void initializeDictionary(SessionSettings settings) {
    try {
      if (settings.sectionIterator().hasNext()) {
        SessionID firstSessionID = settings.sectionIterator().next();
        String dictionaryPath = null;

        // Check if BeginString is FIXT.1.1 to prioritize AppDataDictionary
        String beginString = settings.getString(firstSessionID, "BeginString");
        if ("FIXT.1.1".equals(beginString)
            && settings.isSetting(firstSessionID, Session.SETTING_APP_DATA_DICTIONARY)) {
          dictionaryPath = settings.getString(firstSessionID, Session.SETTING_APP_DATA_DICTIONARY);
          log.info("Using AppDataDictionary for FIXT.1.1 session: {}", dictionaryPath);
        } else if (settings.isSetting(firstSessionID, Session.SETTING_DATA_DICTIONARY)) {
          // Fallback or for non-FIXT.1.1 sessions
          dictionaryPath = settings.getString(firstSessionID, Session.SETTING_DATA_DICTIONARY);
          log.info("Using DataDictionary: {}", dictionaryPath);
        } else if (settings.isSetting(firstSessionID, Session.SETTING_APP_DATA_DICTIONARY)) {
          // Fallback to AppDataDictionary if DataDictionary is missing but AppDataDictionary is
          // present
          dictionaryPath = settings.getString(firstSessionID, Session.SETTING_APP_DATA_DICTIONARY);
          log.info(
              "DataDictionary not found, falling back to AppDataDictionary: {}", dictionaryPath);
        }

        if (dictionaryPath != null) {
          this.dataDictionary = new DataDictionary(dictionaryPath);
          log.info("DataDictionary loaded successfully from: {}", dictionaryPath);
        } else {
          log.warn(
              "No suitable DataDictionary (DataDictionary or AppDataDictionary) setting found for session {}. Message field/value names might not be resolved.",
              firstSessionID);
        }

      } else {
        log.warn(
            "No sessions defined in settings. DataDictionary cannot be initialized from session config.");
      }
    } catch (ConfigError e) {
      log.error(
          "ConfigError accessing session settings for DataDictionary: {}. Ensure relevant dictionary setting (DataDictionary or AppDataDictionary) is set in quickfixj.cfg.",
          e.getMessage(),
          e);
    } catch (Exception e) {
      log.error(
          "Failed to load DataDictionary: {}. Ensure the dictionary file is on the classpath or path is correct.",
          e.getMessage(),
          e);
    }

    if (this.dataDictionary == null) {
      log.warn(
          "DataDictionary could not be initialized. FIX messages will show raw tags and values.");
    }
  }

  public void addMessage(quickfix.Message message, SessionID sessionId, String direction) {
    Map<String, String> readableFields = new LinkedHashMap<>();
    String msgTypeString = "Unknown";
    String rawMsgType;

    try {
      if (message.getHeader().isSetField(MsgType.FIELD)) {
        rawMsgType = message.getHeader().getString(MsgType.FIELD);
        if (dataDictionary != null && dataDictionary.isMsgType(rawMsgType)) {
          msgTypeString = dataDictionary.getMsgType(rawMsgType) + " (" + rawMsgType + ")";
        } else {
          msgTypeString = "Unknown MsgType (" + rawMsgType + ")";
        }
      }
    } catch (FieldNotFound e) {
      log.warn("MsgType field not found in header for message: {}", message, e);
    }

    // Process Header
    processGroup(message.getHeader().iterator(), readableFields, message.getHeader());
    // Process Body
    processGroup(message.iterator(), readableFields, message); // message itself for body fields
    // Process Trailer
    processGroup(message.getTrailer().iterator(), readableFields, message.getTrailer());

    VisualizedFixMessage vizMsg =
        new VisualizedFixMessage(
            LocalDateTime.now(),
            direction,
            msgTypeString,
            message.toString().replace((char) 1, '|'), // Replace SOH with pipe for readability
            readableFields);

    messages.add(vizMsg);
    if (messages.size() > MAX_MESSAGES) {
      messages.removeFirst();
    }
    log.debug("Visualized message added: {}", vizMsg.getMessageType());
  }

  private void processGroup(
      Iterator<Field<?>> fieldIterator, Map<String, String> readableFields, FieldMap fieldMap) {
    while (fieldIterator.hasNext()) {
      Field<?> field = fieldIterator.next();
      int tag = field.getField();
      String value = field.getObject().toString();
      String fieldName = String.valueOf(tag);
      String valueName = value;

      if (dataDictionary != null) {
        fieldName =
            dataDictionary.getFieldName(tag) != null
                ? dataDictionary.getFieldName(tag) + " (" + tag + ")"
                : String.valueOf(tag);
        if (dataDictionary.hasFieldValue(tag)) {
          valueName =
              dataDictionary.getValueName(tag, value) != null
                  ? dataDictionary.getValueName(tag, value) + " (" + value + ")"
                  : value;
        }
      }
      readableFields.put(fieldName, valueName);

      // Check for groups
      fieldMap.isSetField(tag);// This check might be too generic
      // More robust group handling would be needed for deeply nested groups
    }
  }

  public List<VisualizedFixMessage> getMessages() {
    // Return a reversed list so newest messages are first
    List<VisualizedFixMessage> reversedMessages = new ArrayList<>(messages);
    Collections.reverse(reversedMessages);
    return reversedMessages;
  }
}
