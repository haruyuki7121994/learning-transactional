package com.learning.learningtransactional;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.*;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;

class Phase07JpaTest extends PaymentTestBase {
  @PersistenceContext EntityManager em;

  @Test
  void persistenceContextIdentityAndDirtyChecking() {
    jpaTx()
        .executeWithoutResult(
            s -> {
              var a = em.find(Account.class, 1L);
              assertSame(a, em.find(Account.class, 1L));
              a.debit(50);
            });
    assertEquals(950, balance(1));
  }

  @Test
  void flushIsNotCommit() {
    jpaTx()
        .executeWithoutResult(
            s -> {
              em.find(Account.class, 1L).debit(50);
              em.flush();
              s.setRollbackOnly();
            });
    assertEquals(1000, balance(1));
  }

  @Test
  void lazyAssociationNeedsOpenPersistenceContext() {
    service.pay("p1", 1, 2, 100, false);
    var detached = jpaTx().execute(s -> em.find(Payment.class, "p1"));
    assertThrows(LazyInitializationException.class, () -> detached.getSender().getBalance());
  }

  @Test
  void timeoutIsDetectedOnNextJdbcStatement() {
    var tx = jdbcTx();
    tx.setTimeout(1);
    assertThrows(
        org.springframework.transaction.TransactionTimedOutException.class,
        () ->
            tx.executeWithoutResult(
                s -> {
                  try {
                    Thread.sleep(1200);
                  } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                  }
                  entries.add("too-late");
                }));
    assertFalse(entries.exists("too-late"));
  }

  @Test
  void readOnlyIsAHintVisibleToSpring() {
    var tx = jpaTx();
    tx.setReadOnly(true);
    tx.executeWithoutResult(
        s ->
            assertTrue(
                org.springframework.transaction.support.TransactionSynchronizationManager
                    .isCurrentTransactionReadOnly()));
  }
}
