package com.beersonic;

import com.beersonic.fix.FixAcceptor;
import com.beersonic.fix.FixInitiator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@Slf4j
@SpringBootApplication
public class FixSimulatorApplication {
  public static void main(String[] args) {
    ApplicationContext context = SpringApplication.run(FixSimulatorApplication.class, args);
    try {
      context.getBean(FixInitiator.class);
      context.getBean(FixAcceptor.class);
    } catch (Exception e) {
      log.error(e.toString());
    }
  }
}
