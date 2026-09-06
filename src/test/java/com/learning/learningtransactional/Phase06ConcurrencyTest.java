package com.learning.learningtransactional;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.*;
import java.sql.*;
import java.util.concurrent.*;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class Phase06ConcurrencyTest extends PaymentTestBase {
  @Autowired DataSource ds;
  @Autowired EntityManagerFactory emf;

  void setupJdbc() throws SQLException {
    try (var c = ds.getConnection();
        var s = c.createStatement()) {
      s.executeUpdate("delete from concurrency_account");
      s.executeUpdate("insert into concurrency_account values (1,100)");
    }
  }

  int read(Connection c) throws SQLException {
    try (var s = c.createStatement();
        var r = s.executeQuery("select balance from concurrency_account where id=1")) {
      r.next();
      return r.getInt(1);
    }
  }

  void write(Connection c, int n) throws SQLException {
    try (var s = c.prepareStatement("update concurrency_account set balance=? where id=1")) {
      s.setInt(1, n);
      s.executeUpdate();
    }
  }

  @Test
  void readCommittedAllowsNonRepeatableRead() throws Exception {
    assertReadVisibility(Connection.TRANSACTION_READ_COMMITTED, 200);
  }

  @Test
  void repeatableReadKeepsSnapshot() throws Exception {
    assertReadVisibility(Connection.TRANSACTION_REPEATABLE_READ, 100);
  }

  void assertReadVisibility(int isolation, int expected) throws Exception {
    setupJdbc();
    try (var a = ds.getConnection();
        var b = ds.getConnection()) {
      a.setTransactionIsolation(isolation);
      a.setAutoCommit(false);
      assertEquals(100, read(a));
      write(b, 200);
      assertEquals(expected, read(a));
      a.rollback();
    }
  }

  @Test
  void staleReadModifyWriteLosesUpdateWithoutVersion() throws Exception {
    setupJdbc();
    try (var a = ds.getConnection();
        var b = ds.getConnection()) {
      a.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
      b.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
      a.setAutoCommit(false);
      b.setAutoCommit(false);
      int oldA = read(a), oldB = read(b);
      write(a, oldA - 10);
      a.commit();
      write(b, oldB - 20);
      b.commit();
      assertEquals(80, read(a));
      a.rollback(); // Correct combined debit would be 70.
    }
  }

  @Test
  void optimisticVersionRejectsStaleEntity() {
    var a = emf.createEntityManager();
    var b = emf.createEntityManager();
    try {
      a.getTransaction().begin();
      b.getTransaction().begin();
      var first = a.find(Account.class, 1L);
      var stale = b.find(Account.class, 1L);
      first.debit(10);
      a.getTransaction().commit();
      stale.debit(20);
      assertThrows(OptimisticLockException.class, b::flush);
      b.getTransaction().rollback();
      assertEquals(990, balance(1));
    } finally {
      if (a.getTransaction().isActive()) a.getTransaction().rollback();
      if (b.getTransaction().isActive()) b.getTransaction().rollback();
      a.close();
      b.close();
    }
  }

  @Test
  void pessimisticLockSerializesTwoDebits() throws Exception {
    var firstHasLock = new CountDownLatch(1);
    var release = new CountDownLatch(1);
    var secondStarted = new CountDownLatch(1);
    var executor = Executors.newFixedThreadPool(2);
    try {
      var one =
          executor.submit(
              () ->
                  jpaTx()
                      .executeWithoutResult(
                          s -> {
                            var a = accounts.locked(1L).orElseThrow();
                            firstHasLock.countDown();
                            await(release);
                            a.debit(10);
                          }));
      assertTrue(firstHasLock.await(5, TimeUnit.SECONDS));
      var two =
          executor.submit(
              () ->
                  jpaTx()
                      .executeWithoutResult(
                          s -> {
                            secondStarted.countDown();
                            accounts.locked(1L).orElseThrow().debit(20);
                          }));
      assertTrue(secondStarted.await(5, TimeUnit.SECONDS));
      assertThrows(
          TimeoutException.class,
          () -> two.get(200, TimeUnit.MILLISECONDS)); // Blocked until first commits.
      release.countDown();
      one.get(5, TimeUnit.SECONDS);
      two.get(5, TimeUnit.SECONDS);
      assertEquals(970, balance(1));
    } finally {
      release.countDown();
      executor.shutdownNow();
      assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
    }
  }

  static void await(CountDownLatch latch) {
    try {
      if (!latch.await(5, TimeUnit.SECONDS))
        throw new IllegalStateException("Timed out waiting for test coordination");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e);
    }
  }
}
