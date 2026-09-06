package com.learning.learningtransactional;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.UnexpectedRollbackException;

class Phase03RollbackTest extends LabTest {
  @Autowired SemanticsLab lab;
  @Autowired BoundaryLab boundary;

  @Test
  void normalReturnCommits() {
    lab.commit("ok");
    assertTrue(entries.exists("ok"));
  }

  @Test
  void runtimeRollsBack() {
    assertThrows(IllegalStateException.class, () -> lab.runtime("runtime"));
    assertFalse(entries.exists("runtime"));
  }

  @Test
  void checkedCommitsByDefault() {
    assertThrows(Exception.class, () -> lab.checked("checked"));
    assertTrue(entries.exists("checked"));
  }

  @Test
  void rollbackForOverridesDefault() {
    assertThrows(Exception.class, () -> lab.checkedRollback("checked"));
    assertFalse(entries.exists("checked"));
  }

  @Test
  void noRollbackForOverridesDefault() {
    assertThrows(IllegalArgumentException.class, () -> lab.noRollback("business"));
    assertTrue(entries.exists("business"));
  }

  @Test
  void swallowedApplicationExceptionCommits() {
    lab.swallowed("swallow");
    assertTrue(entries.exists("swallow"));
  }

  @Test
  void explicitLocalRollbackOnlyReturnsNormally() {
    lab.rollbackOnly("only");
    assertFalse(entries.exists("only"));
  }

  @Test
  void swallowedInnerRequiredStillPoisonsOuterTransaction() {
    assertThrows(UnexpectedRollbackException.class, boundary::unexpectedRollback);
    assertFalse(entries.exists("outer"));
    assertFalse(entries.exists("inner"));
  }
}
