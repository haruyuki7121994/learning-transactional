package com.learning.learningtransactional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class SemanticsLab {
  private final EntryStore store;

  public SemanticsLab(EntryStore store) {
    this.store = store;
  }

  @Transactional("jdbcTxManager")
  public void commit(String id) {
    store.add(id);
  }

  @Transactional("jdbcTxManager")
  public void runtime(String id) {
    store.add(id);
    throw new IllegalStateException("Runtime failure");
  }

  @Transactional("jdbcTxManager")
  public void checked(String id) throws Exception {
    store.add(id);
    throw new Exception("Checked failure");
  }

  @Transactional(value = "jdbcTxManager", rollbackFor = Exception.class)
  public void checkedRollback(String id) throws Exception {
    store.add(id);
    throw new Exception("Rollback requested");
  }

  @Transactional(value = "jdbcTxManager", noRollbackFor = IllegalArgumentException.class)
  public void noRollback(String id) {
    store.add(id);
    throw new IllegalArgumentException("Business rejection after write");
  }

  @Transactional("jdbcTxManager")
  public void swallowed(String id) {
    store.add(id);
    try {
      throw new IllegalStateException();
    } catch (IllegalStateException ignored) {
      /* Proxy sees normal return. */
    }
  }

  @Transactional("jdbcTxManager")
  public void rollbackOnly(String id) {
    store.add(id);
    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
  }

  public void selfInvocation(String id) {
    runtime(id);
  } // Bypasses this bean's proxy; JDBC auto-commits.

  @Transactional("jdbcTxManager")
  public boolean active() {
    return TransactionSynchronizationManager.isActualTransactionActive();
  }

  public boolean selfActive() {
    return active();
  }
}
