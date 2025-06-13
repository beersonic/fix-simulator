package com.beersonic.fix;

import static org.junit.jupiter.api.Assertions.*;

class FixMessageUtilTest {

  @org.junit.jupiter.api.Test
  void convertPipeToSoh() {
    String fixMessage = "8=FIX.4.2|9=12|35=AE";
    String expected = "8=FIX.4.2\u00019=12\u000135=AE";
    String actual = FixMessageUtil.convertPipeToSoh(fixMessage);
    assertEquals(expected, actual);
  }
}
