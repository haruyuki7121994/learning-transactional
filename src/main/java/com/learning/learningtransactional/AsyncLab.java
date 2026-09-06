package com.learning.learningtransactional;

import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class AsyncLab {
  @Async
  public CompletableFuture<Boolean> withoutTransaction() {
    return CompletableFuture.completedFuture(active());
  }

  @Async
  @Transactional("jdbcTxManager")
  public CompletableFuture<Boolean> ownTransaction() {
    return CompletableFuture.completedFuture(active());
  }

  private boolean active() {
    return TransactionSynchronizationManager.isActualTransactionActive();
  }
}
