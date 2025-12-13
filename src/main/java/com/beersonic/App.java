package com.beersonic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
@Controller
@Slf4j
public class App implements ApplicationListener<ApplicationReadyEvent> {
  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }

  @GetMapping("/")
  public String home() {
    // Forward root requests to the static index.html so the React app loads at '/'
    return "forward:/index.html";
  }

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    try {
      Environment env = event.getApplicationContext().getEnvironment();
      String port = env.getProperty("server.port", "8080");
      String contextPath = env.getProperty("server.servlet.context-path", "");
      if (contextPath == null) contextPath = "";
      if (!contextPath.isEmpty() && !contextPath.startsWith("/")) {
        contextPath = "/" + contextPath;
      }
      String url = String.format("http://localhost:%s%s/", port, contextPath);
      log.info("UI available at {} (serving static/index.html)", url);
    } catch (Exception e) {
      log.warn("Failed to log UI URL on startup: {}", e.getMessage());
    }
  }

  // small method to exercise in tests
  public static String greet() {
    return "Hello";
  }
}
