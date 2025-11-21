package com.beersonic;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fix")
public class FixController {

  @Autowired private QuickFixService quickFixService;

  @PostMapping("/session")
  public ResponseEntity<String> createSession(@RequestBody Map<String, String> config) {
    String sessionType = config.get("type");
    if ("acceptor".equalsIgnoreCase(sessionType)) {
      quickFixService.start(); // Placeholder for acceptor logic
      return ResponseEntity.ok("Acceptor session created and running.");
    } else if ("initiator".equalsIgnoreCase(sessionType)) {
      quickFixService.start(); // Placeholder for initiator logic
      return ResponseEntity.ok("Initiator session created and running.");
    } else {
      return ResponseEntity.badRequest()
          .body("Invalid session type. Use 'acceptor' or 'initiator'.");
    }
  }
}
