package com.beersonic;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AppTest {

  @Autowired private TestRestTemplate restTemplate;

  @Test
  void contextLoads() {
    // Verify that the Spring context loads successfully
  }

  @Test
  void homeEndpointReturnsGreeting() {
    String body = this.restTemplate.getForObject("/", String.class);
    assertEquals("Hello from Spring Boot fix-simulator!", body);
  }
}
