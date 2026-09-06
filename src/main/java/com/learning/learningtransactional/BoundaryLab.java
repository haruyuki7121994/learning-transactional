package com.learning.learningtransactional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BoundaryLab {
  private final PropagationLab inner;
  private final EntryStore store;

  public BoundaryLab(PropagationLab inner, EntryStore store) {
    this.inner = inner;
    this.store = store;
  }

  @Transactional("jdbcTxManager")
  public void unexpectedRollback() {
    store.add("outer");
    try {
      inner.requiredFailure("inner");
    } catch (IllegalStateException ignored) {
    }
    // Shared transaction is rollback-only. Commit raises UnexpectedRollbackException.
  }

  @Transactional("jdbcTxManager")
  public void independentAudit() {
    store.add("outer");
    inner.independent("audit");
    throw new IllegalStateException();
  }

  @Transactional("jdbcTxManager")
  public void nested() {
    store.add("outer");
    try {
      inner.nestedFailure("inner");
    } catch (IllegalStateException ignored) {
    }
    store.add("after");
  }
}
