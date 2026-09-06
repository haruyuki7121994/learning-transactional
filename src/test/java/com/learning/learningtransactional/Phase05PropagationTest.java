package com.learning.learningtransactional;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.IllegalTransactionStateException;

class Phase05PropagationTest extends LabTest {
  @Autowired PropagationLab inner;
  @Autowired BoundaryLab boundary;

  @Test
  void requiresNewCommitsEvenWhenOuterRollsBack() {
    assertThrows(IllegalStateException.class, boundary::independentAudit);
    assertTrue(entries.exists("audit"));
    assertFalse(entries.exists("outer"));
  }

  @Test
  void nestedRollsBackOnlyToSavepoint() {
    boundary.nested();
    assertTrue(entries.exists("outer"));
    assertTrue(entries.exists("after"));
    assertFalse(entries.exists("inner"));
  }

  @Test
  void supportsWithAndWithoutOuter() {
    assertFalse(inner.supports());
    jdbcTx().executeWithoutResult(s -> assertTrue(inner.supports()));
  }

  @Test
  void mandatoryRequiresOuter() {
    assertThrows(IllegalTransactionStateException.class, inner::mandatory);
    jdbcTx().executeWithoutResult(s -> assertTrue(inner.mandatory()));
  }

  @Test
  void notSupportedSuspendsOuter() {
    jdbcTx()
        .executeWithoutResult(
            s -> {
              assertFalse(inner.notSupported());
              assertTrue(inner.supports());
            });
  }

  @Test
  void neverRejectsOuter() {
    assertFalse(inner.never());
    assertThrows(
        IllegalTransactionStateException.class, () -> jdbcTx().execute(s -> inner.never()));
  }
}
