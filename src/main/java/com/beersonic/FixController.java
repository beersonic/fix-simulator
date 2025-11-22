package com.beersonic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    try {
      String id = quickFixService.createSession(config);
      return ResponseEntity.ok(id);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Failed to create session: " + e.getMessage());
    }
  }

  @GetMapping("/sessions")
  public ResponseEntity<List<Map<String, Object>>> listSessions() {
    List<Map<String, Object>> list = quickFixService.listSessions();
    return ResponseEntity.ok(list);
  }

  @GetMapping("/sessions/{id}/messages")
  public ResponseEntity<List<String>> getMessages(@PathVariable("id") String id) {
    List<String> msgs = quickFixService.getMessages(id);
    return ResponseEntity.ok(msgs);
  }

  @PostMapping("/sessions/{id}/stop")
  public ResponseEntity<Map<String, Object>> stopSession(@PathVariable("id") String id) {
    boolean stopped = quickFixService.stopSession(id);
    Map<String, Object> resp = new HashMap<>();
    resp.put("id", id);
    resp.put("stopped", stopped);
    if (stopped) return ResponseEntity.ok(resp);
    else return ResponseEntity.status(404).body(resp);
  }
}
