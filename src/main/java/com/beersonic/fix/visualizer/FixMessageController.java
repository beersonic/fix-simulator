package com.beersonic.fix.visualizer;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fix-messages")
public class FixMessageController {

  private final FixMessageVisualizerService visualizerService;

  @Autowired
  public FixMessageController(FixMessageVisualizerService visualizerService) {
    this.visualizerService = visualizerService;
  }

  @GetMapping
  public List<VisualizedFixMessage> getMessages() {
    return visualizerService.getMessages();
  }
}
