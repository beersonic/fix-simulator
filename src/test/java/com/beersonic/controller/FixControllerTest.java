package com.beersonic.controller;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import quickfix.InvalidMessage;
import quickfix.Message;
import quickfix.Session;
import quickfix.SessionNotFound;

@WebMvcTest(FixController.class)
public class FixControllerTest {

  @Autowired private MockMvc mockMvc;

  @Mock private Session session;

    public FixControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @BeforeEach
  void setUp() {
    Mockito.reset(session);
  }

  @Test
  void pingReturnsPong() throws Exception {
    mockMvc
        .perform(get("/api/fix/ping"))
        .andExpect(status().isOk())
        .andExpect(content().string("pong"));
  }

  @Test
  void sendFixMessageSuccessfully() throws Exception {
    String fixMessage =
        "8=FIX.5.0SP2|9=178|35=AE|34=1|49=ClientCompID|56=ServerCompID|55=AAPL|22=1|150=0|17=123456|31=150.50|32=100|54=1|60=20240209-12:30:00.000|10=121|";
    doNothing().when(session).sendToTarget(Mockito.any(Message.class));
    mockMvc
        .perform(post("/api/fix/send").content(fixMessage).contentType("application/json"))
        .andExpect(status().isOk())
        .andExpect(content().string("Message sent"));
  }

  @Test
  void sendFixMessageSessionNotFound() throws Exception {
    String fixMessage = "8=FIX.4.2|9=12|35=D|...";
    Mockito.doThrow(new SessionNotFound("Session not found"))
        .when(session)
        .sendToTarget(Mockito.any(Message.class));
    mockMvc
        .perform(post("/api/fix/send").content(fixMessage).contentType("application/json"))
        .andExpect(status().isOk())
        .andExpect(content().string("Session not found: Session not found"));
  }

  @Test
  void sendFixMessageInvalidMessage() throws Exception {
    String fixMessage = "invalid message";
    Mockito.doThrow(new InvalidMessage("Invalid message"))
        .when(session)
        .sendToTarget(Mockito.any(Message.class));
    mockMvc
        .perform(post("/api/fix/send").content(fixMessage).contentType("application/json"))
        .andExpect(status().isOk())
        .andExpect(content().string("Invalid message: Invalid message"));
  }

  @Test
  void receiveFixMessage() throws Exception {
    mockMvc
        .perform(get("/api/fix/receive"))
        .andExpect(status().isOk())
        .andExpect(content().string("Received messages"));
  }
}
