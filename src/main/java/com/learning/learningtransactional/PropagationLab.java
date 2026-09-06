package com.learning.learningtransactional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class PropagationLab {
  private final EntryStore store;

  public PropagationLab(EntryStore store) {
    this.store = store;
  }

  @Transactional(value = "jdbcTxManager", propagation = Propagation.REQUIRED)
  public void requiredFailure(String id) {
    store.add(id);
    throw new IllegalStateException("Inner failure");
  }

  @Transactional(value = "jdbcTxManager", propagation = Propagation.REQUIRES_NEW)
  public void independent(String id) {
    store.add(id);
  }

  @Transactional(value = "jdbcTxManager", propagation = Propagation.NESTED)
  public void nestedFailure(String id) {
    store.add(id);
    throw new IllegalStateException("Savepoint rollback");
  }

  @Transactional(value = "jdbcTxManager", propagation = Propagation.SUPPORTS)
  public boolean supports() {
    return active();
  }

  @Transactional(value = "jdbcTxManager", propagation = Propagation.MANDATORY)
  public boolean mandatory() {
    return active();
  }

  @Transactional(value = "jdbcTxManager", propagation = Propagation.NOT_SUPPORTED)
  public boolean notSupported() {
    return active();
  }

  @Transactional(value = "jdbcTxManager", propagation = Propagation.NEVER)
  public boolean never() {
    return active();
  }

  private boolean active() {
    return TransactionSynchronizationManager.isActualTransactionActive();
  }
}
