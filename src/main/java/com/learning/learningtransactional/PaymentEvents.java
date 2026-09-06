package com.learning.learningtransactional;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PaymentEvents {
  private final List<String> committed = new CopyOnWriteArrayList<>();

  @TransactionalEventListener
  public void afterCommit(PaymentService.Paid event) {
    committed.add(event.id());
  }

  public List<String> committed() {
    return List.copyOf(committed);
  }

  public void clear() {
    committed.clear();
  }
}
