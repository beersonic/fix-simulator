package com.beersonic.controller;

import com.beersonic.fix.FixMessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import quickfix.InvalidMessage;
import quickfix.Message;
import quickfix.Session;
import quickfix.SessionNotFound;

@RestController
@RequestMapping("/api/fix")
@Slf4j
public class FixController {
  @GetMapping("/ping")
  public String ping() {
    log.info("Received ping request");
    return "pong";
  }

  @PostMapping("/send")
  public String sendFixMessage(@RequestBody String fixMessage) {
    try {
      log.info("Sending message: {}", fixMessage);
      fixMessage = FixMessageUtil.convertPipeToSoh(fixMessage);
      Message message = new Message(fixMessage);
      Session.sendToTarget(message);
      return "Message sent";
    } catch (SessionNotFound e) {
      return "Session not found: " + e.getMessage();
    } catch (InvalidMessage e) {
      return "Invalid message: " + e.getMessage();
    }
  }

  @GetMapping("/receive")
  public String receiveFixMessage() {
    log.info("Receiving messages");
    // Implement logic to receive FIX messages
    return "Received messages";
  }
}
