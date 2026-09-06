package com.learning.learningtransactional;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class OutboxRelay {
  private final OutboxRepository outbox;
  private final TransactionTemplate tx;
  // Teaching stand-in for a receiver with a durable UNIQUE(event_id) inbox.
  private final Set<String> received = ConcurrentHashMap.newKeySet();

  public OutboxRelay(
      OutboxRepository outbox,
      @Qualifier("transactionManager") PlatformTransactionManager manager) {
    this.outbox = outbox;
    this.tx = new TransactionTemplate(manager);
  }

  public void relay(boolean crashAfterSend) {
    var batch = tx.execute(status -> outbox.findByDeliveredFalse());
    for (var event : batch) {
      received.add(event.getId()); // Simulated remote send; OUTSIDE the database transaction.
      if (crashAfterSend)
        throw new IllegalStateException("Crash after delivery, before acknowledgement");
      tx.executeWithoutResult(
          status -> outbox.findById(event.getId()).orElseThrow().markDelivered());
    }
  }

  public int receivedCount() {
    return received.size();
  }

  public void clear() {
    received.clear();
  }
}
