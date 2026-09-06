package com.learning.learningtransactional;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class Phase08CrossCuttingTest extends PaymentTestBase {
  @Autowired AsyncLab async;

  public static class OrderedWork {
    private final java.util.List<String> trace;

    public OrderedWork(java.util.List<String> trace) {
      this.trace = trace;
    }

    public void run() {
      trace.add("inside:" + TransactionSynchronizationManager.isActualTransactionActive());
    }
  }

  @Test
  void outerAdviceWrapsTransactionInterceptor() {
    var trace = new java.util.ArrayList<String>();
    var source =
        new org.springframework.transaction.interceptor.MatchAlwaysTransactionAttributeSource();
    source.setTransactionAttribute(
        new org.springframework.transaction.interceptor.DefaultTransactionAttribute());
    var interceptor = new org.springframework.transaction.interceptor.TransactionInterceptor();
    interceptor.setTransactionManager(jdbcManager);
    interceptor.setTransactionAttributeSource(source);
    var proxy = new org.springframework.aop.framework.ProxyFactory(new OrderedWork(trace));
    proxy.addAdvice(
        (org.aopalliance.intercept.MethodInterceptor)
            invocation -> {
              trace.add("before:" + TransactionSynchronizationManager.isActualTransactionActive());
              var result = invocation.proceed();
              trace.add("after:" + TransactionSynchronizationManager.isActualTransactionActive());
              return result;
            });
    proxy.addAdvice(interceptor);
    ((OrderedWork) proxy.getProxy()).run();
    assertEquals(java.util.List.of("before:false", "inside:true", "after:false"), trace);
  }

  @Test
  void asyncDoesNotInheritCallerTransaction() {
    jdbcTx()
        .executeWithoutResult(
            s -> {
              try {
                assertFalse(async.withoutTransaction().get(5, TimeUnit.SECONDS));
                assertTrue(async.ownTransaction().get(5, TimeUnit.SECONDS));
              } catch (Exception e) {
                throw new AssertionError(e);
              }
            });
  }

  @Test
  void retryWrapsFreshTransactionPerAttempt() {
    var attempts = new AtomicInteger();
    for (int n = 0; n < 3; n++) {
      try {
        jdbcTx()
            .executeWithoutResult(
                s -> {
                  assertTrue(s.isNewTransaction());
                  entries.add("retry");
                  if (attempts.incrementAndGet() < 3)
                    throw new org.springframework.dao.CannotAcquireLockException(
                        "simulated transient failure");
                });
        break;
      } catch (org.springframework.dao.CannotAcquireLockException e) {
        if (n == 2) throw e;
      }
    }
    assertEquals(3, attempts.get());
    assertTrue(entries.exists("retry"));
  }

  @Test
  void explicitManagersAreDifferentImplementations() {
    assertInstanceOf(org.springframework.orm.jpa.JpaTransactionManager.class, jpaManager);
    assertInstanceOf(
        org.springframework.jdbc.datasource.DataSourceTransactionManager.class, jdbcManager);
  }

  @Test
  void transactionalListenerDoesNotRunForRolledBackPublish() {
    jpaTx()
        .executeWithoutResult(
            s -> {
              service.pay("event", 1, 2, 10, false);
              s.setRollbackOnly();
            });
    assertTrue(events.committed().isEmpty());
    assertEquals(0, outbox.count());
  }
}
