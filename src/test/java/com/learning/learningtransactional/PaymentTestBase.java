package com.learning.learningtransactional;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

abstract class PaymentTestBase extends LabTest {
  @Autowired AccountRepository accounts;
  @Autowired PaymentRepository payments;
  @Autowired OutboxRepository outbox;
  @Autowired PaymentService service;
  @Autowired PaymentEvents events;
  @Autowired OutboxRelay relay;

  @BeforeEach
  void resetWallets() {
    payments.deleteAll();
    outbox.deleteAll();
    accounts.deleteAll();
    accounts.saveAll(List.of(new Account(1L, 1000), new Account(2L, 1000)));
    events.clear();
    relay.clear();
  }

  long balance(long id) {
    return accounts.findById(id).orElseThrow().getBalance();
  }
}
