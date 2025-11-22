package com.beersonic;

import quickfix.*;

public interface FixSessionFactory {
  SocketInitiator createInitiator(
      Application app,
      MessageStoreFactory storeFactory,
      SessionSettings settings,
      LogFactory logFactory,
      MessageFactory messageFactory)
      throws Exception;

  SocketAcceptor createAcceptor(
      Application app,
      MessageStoreFactory storeFactory,
      SessionSettings settings,
      LogFactory logFactory,
      MessageFactory messageFactory)
      throws Exception;
}
