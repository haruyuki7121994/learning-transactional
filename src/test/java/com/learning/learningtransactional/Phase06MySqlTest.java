package com.learning.learningtransactional;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.*;
import java.util.List;
import java.util.concurrent.*;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;

@EnabledIfSystemProperty(named = "spring.profiles.active", matches = "mysql")
class Phase06MySqlTest extends LabTest {
  @Autowired DataSource ds;

  void reset() throws SQLException {
    try (var c = ds.getConnection();
        var s = c.createStatement()) {
      s.executeUpdate("delete from concurrency_account");
      s.executeUpdate("insert into concurrency_account values (1,1000),(2,1000)");
    }
  }

  int query(Connection c, String sql) throws SQLException {
    try (var s = c.createStatement();
        var r = s.executeQuery(sql)) {
      r.next();
      return r.getInt(1);
    }
  }

  void update(Connection c, String sql) throws SQLException {
    try (var s = c.createStatement()) {
      s.executeUpdate(sql);
    }
  }

  @Test
  void dirtyReadSeesValueThatLaterRollsBack() throws Exception {
    reset();
    try (var a = ds.getConnection();
        var b = ds.getConnection()) {
      a.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
      a.setAutoCommit(false);
      b.setAutoCommit(false);
      try {
        update(b, "update concurrency_account set balance=900 where id=1");
        assertEquals(900, query(a, "select balance from concurrency_account where id=1"));
        b.rollback();
        assertEquals(1000, query(a, "select balance from concurrency_account where id=1"));
      } finally {
        a.rollback();
        b.rollback();
      }
    }
  }

  @Test
  void readCommittedSeesPhantom() throws Exception {
    phantom(Connection.TRANSACTION_READ_COMMITTED, 3);
  }

  @Test
  void repeatableReadSnapshotDoesNotSeePhantom() throws Exception {
    phantom(Connection.TRANSACTION_REPEATABLE_READ, 2);
  }

  void phantom(int level, int expected) throws Exception {
    reset();
    try (var a = ds.getConnection();
        var b = ds.getConnection()) {
      a.setTransactionIsolation(level);
      a.setAutoCommit(false);
      try {
        assertEquals(2, query(a, "select count(*) from concurrency_account where balance>=1000"));
        update(b, "insert into concurrency_account values (3,1000)");
        assertEquals(
            expected, query(a, "select count(*) from concurrency_account where balance>=1000"));
      } finally {
        a.rollback();
      }
    }
  }

  @Test
  void oppositeLockOrderCreatesOneDeadlockVictim() throws Exception {
    reset();
    var bothLocked = new CountDownLatch(2);
    var executor = Executors.newFixedThreadPool(2);
    try {
      var a = executor.submit(() -> deadlockWorker(1, 2, bothLocked));
      var b = executor.submit(() -> deadlockWorker(2, 1, bothLocked));
      var codes = List.of(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS));
      assertTrue(codes.contains(0));
      assertTrue(codes.contains(1213), "Expected MySQL deadlock (1213), got " + codes);
    } finally {
      executor.shutdownNow();
      assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
    }
  }

  int deadlockWorker(int first, int second, CountDownLatch bothLocked) throws Exception {
    try (var c = ds.getConnection()) {
      c.setAutoCommit(false);
      try {
        update(c, "update concurrency_account set balance=balance-1 where id=" + first);
        bothLocked.countDown();
        if (!bothLocked.await(5, TimeUnit.SECONDS))
          throw new IllegalStateException("Peer did not acquire first lock");
        update(c, "update concurrency_account set balance=balance-1 where id=" + second);
        c.commit();
        return 0;
      } catch (SQLException e) {
        c.rollback();
        if (e.getErrorCode() == 1213) return 1213;
        throw e;
      } finally {
        c.rollback();
      }
    }
  }
}
