package com.learning.learningtransactional;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;

class Phase02ProxyTest extends LabTest {
  @Autowired SemanticsLab lab;

  @Test
  void externalCallCrossesProxy() {
    assertTrue(AopUtils.isAopProxy(lab));
    assertTrue(lab.active());
  }

  @Test
  void selfInvocationBypassesAdvice() {
    assertFalse(lab.selfActive());
    assertThrows(IllegalStateException.class, () -> lab.selfInvocation("self"));
    assertTrue(entries.exists("self"));
  }

  @Test
  void movingCallerOutsideBeanRestoresRollback() {
    assertThrows(IllegalStateException.class, () -> lab.runtime("external"));
    assertFalse(entries.exists("external"));
  }

  public interface Greeting {
    String hello();
  }

  public static class GreetingService implements Greeting {
    public String hello() {
      return "hello";
    }
  }

  @Test
  void jdkAndClassBasedProxy() {
    var factory = new ProxyFactory(new GreetingService());
    assertTrue(AopUtils.isJdkDynamicProxy(factory.getProxy()));
    factory.setProxyTargetClass(true);
    assertTrue(AopUtils.isCglibProxy(factory.getProxy()));
  }
}
