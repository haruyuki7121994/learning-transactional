package com.learning.learningtransactional;

import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class LabController {
  private final PaymentService service;
  private final AccountRepository accounts;

  public LabController(PaymentService service, AccountRepository accounts) {
    this.service = service;
    this.accounts = accounts;
  }

  public record Request(String id, long from, long to, long amount, boolean fail) {}

  @PostMapping("/payments")
  public Map<String, String> pay(@RequestBody Request request) {
    service.pay(request.id(), request.from(), request.to(), request.amount(), request.fail());
    return Map.of("result", "committed", "id", request.id());
  }

  @GetMapping("/accounts")
  public List<Account> accounts() {
    return accounts.findAll();
  }

  @ExceptionHandler({
    IllegalArgumentException.class,
    IllegalStateException.class,
    NoSuchElementException.class
  })
  ResponseEntity<Map<String, String>> error(RuntimeException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Map.of("error", Objects.toString(e.getMessage(), "Account missing")));
  }
}
