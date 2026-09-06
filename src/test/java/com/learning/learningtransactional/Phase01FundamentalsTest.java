package com.learning.learningtransactional;

import static org.junit.jupiter.api.Assertions.*;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.DefaultTransactionDefinition;

class Phase01FundamentalsTest extends LabTest {
  @Autowired DataSource ds;

  @Test
  void jdbcCommitAndRollback() throws Exception {
    try (var c = ds.getConnection()) {
      c.setAutoCommit(false);
      try (var s = c.prepareStatement("insert into lab_entry(id,note) values (?,?)")) {
        s.setString(1, "jdbc-commit");
        s.setString(2, "manual");
        s.executeUpdate();
        c.commit();
        s.setString(1, "jdbc-rollback");
        s.executeUpdate();
        c.rollback();
      } finally {
        c.setAutoCommit(true);
      }
    }
    assertTrue(entries.exists("jdbc-commit"));
    assertFalse(entries.exists("jdbc-rollback"));
  }

  @Test
  void managerDefinitionAndStatus() {
    var definition = new DefaultTransactionDefinition();
    definition.setName("phase-01");
    var status = jdbcManager.getTransaction(definition);
    try {
      assertTrue(status.isNewTransaction());
      entries.add("manual");
      jdbcManager.rollback(status);
    } finally {
      if (!status.isCompleted()) jdbcManager.rollback(status);
    }
    assertFalse(entries.exists("manual"));
    assertTrue(status.isCompleted());
  }

  @Test
  void templateCanMarkRollbackOnly() {
    jdbcTx()
        .executeWithoutResult(
            status -> {
              entries.add("template");
              status.setRollbackOnly();
            });
    assertFalse(entries.exists("template"));
  }
}
