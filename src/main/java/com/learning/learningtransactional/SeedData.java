package com.learning.learningtransactional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;

@Configuration
public class SeedData {
  @Bean
  CommandLineRunner seed(AccountRepository accounts) {
    return args -> {
      if (accounts.count() == 0)
        accounts.saveAll(java.util.List.of(new Account(1L, 1000), new Account(2L, 1000)));
    };
  }
}
