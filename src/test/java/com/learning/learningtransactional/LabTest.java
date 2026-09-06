package com.learning.learningtransactional;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
abstract class LabTest {
  @Autowired EntryStore entries;

  @Autowired
  @Qualifier("jdbcTxManager")
  PlatformTransactionManager jdbcManager;

  @Autowired
  @Qualifier("transactionManager")
  PlatformTransactionManager jpaManager;

  @BeforeEach
  void clearEntries() {
    entries.clear();
  }

  TransactionTemplate jdbcTx() {
    return new TransactionTemplate(jdbcManager);
  }

  TransactionTemplate jpaTx() {
    return new TransactionTemplate(jpaManager);
  }
  // Deliberately NOT @Transactional: assertions observe real committed database state.
}
