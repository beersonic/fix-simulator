package com.beersonic.fix.visualizer;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;

@Getter
public class VisualizedFixMessage {
  // Getters
  private final LocalDateTime timestamp;
  private final String direction; // e.g., "INCOMING", "OUTGOING"
  private final String messageType; // e.g., "New Order Single (D)", "Heartbeat (0)"
  private final String rawMessage; // e.g., 8=FIX.4.2|9=...
  private final Map<String, String> fields; // Human-readable: {"SenderCompID (49)": "CLIENT", ...}

  public VisualizedFixMessage(
      LocalDateTime timestamp,
      String direction,
      String messageType,
      String rawMessage,
      Map<String, String> fields) {
    this.timestamp = timestamp;
    this.direction = direction;
    this.messageType = messageType;
    this.rawMessage = rawMessage;
    this.fields = fields;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    VisualizedFixMessage that = (VisualizedFixMessage) o;
    return Objects.equals(timestamp, that.timestamp)
        && Objects.equals(direction, that.direction)
        && Objects.equals(messageType, that.messageType)
        && Objects.equals(rawMessage, that.rawMessage)
        && Objects.equals(fields, that.fields);
  }

  @Override
  public int hashCode() {
    return Objects.hash(timestamp, direction, messageType, rawMessage, fields);
  }

  @Override
  public String toString() {
    return "VisualizedFixMessage{"
        + "timestamp="
        + timestamp
        + ", direction='"
        + direction
        + '\''
        + ", messageType='"
        + messageType
        + '\''
        + ", rawMessage='"
        + rawMessage
        + '\''
        + ", fields="
        + fields
        + '}';
  }
}
