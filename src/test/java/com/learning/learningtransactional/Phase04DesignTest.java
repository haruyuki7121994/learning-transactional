package com.learning.learningtransactional;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class Phase04DesignTest extends PaymentTestBase {
  @Test
  void serviceBoundaryCommitsBothWalletsAndOutbox() {
    service.pay("p1", 1, 2, 100, false);
    assertEquals(900, balance(1));
    assertEquals(1100, balance(2));
    assertEquals(1, outbox.count());
    assertEquals(java.util.List.of("p1"), events.committed());
  }

  @Test
  void failureBetweenDebitAndCreditRollsBackEverything() {
    assertThrows(IllegalStateException.class, () -> service.pay("p1", 1, 2, 100, true));
    assertEquals(1000, balance(1));
    assertEquals(1000, balance(2));
    assertEquals(0, payments.count());
    assertEquals(0, outbox.count());
    assertTrue(events.committed().isEmpty());
  }

  @Test
  void duplicatePaymentDoesNotDebitTwice() {
    service.pay("p1", 1, 2, 100, false);
    assertThrows(IllegalArgumentException.class, () -> service.pay("p1", 1, 2, 100, false));
    assertEquals(900, balance(1));
  }

  @Test
  void outboxSurvivesCrashWindowAndReceiverDeduplicates() {
    service.pay("p1", 1, 2, 100, false);
    assertThrows(IllegalStateException.class, () -> relay.relay(true));
    assertFalse(outbox.findById("p1").orElseThrow().isDelivered());
    relay.relay(false);
    assertEquals(1, relay.receivedCount());
    assertTrue(outbox.findById("p1").orElseThrow().isDelivered());
  }
}
