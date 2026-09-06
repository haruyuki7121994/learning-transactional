package com.learning.learningtransactional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {
  private final AccountRepository accounts;
  private final PaymentRepository payments;
  private final OutboxRepository outbox;
  private final ApplicationEventPublisher events;

  public PaymentService(
      AccountRepository accounts,
      PaymentRepository payments,
      OutboxRepository outbox,
      ApplicationEventPublisher events) {
    this.accounts = accounts;
    this.payments = payments;
    this.outbox = outbox;
    this.events = events;
  }

  public record Paid(String id) {}

  // External controller -> proxy -> TransactionInterceptor -> service -> flush -> commit.
  @Transactional
  public void pay(String id, long from, long to, long amount, boolean fail) {
    if (id == null || id.isBlank() || id.length() > 100 || from == to || amount <= 0)
      throw new IllegalArgumentException("Invalid payment");
    // Consistent lock ordering avoids the common opposite-transfer deadlock.
    Account first = accounts.locked(Math.min(from, to)).orElseThrow();
    Account second = accounts.locked(Math.max(from, to)).orElseThrow();
    if (payments.existsById(id)) throw new IllegalArgumentException("Payment id already used");
    Account sender = from == first.getId() ? first : second;
    Account receiver = to == first.getId() ? first : second;
    sender.debit(amount);
    if (fail) throw new IllegalStateException("Simulated failure between debit and credit");
    receiver.credit(amount);
    payments.save(new Payment(id, sender, amount));
    outbox.save(new OutboxMessage(id, "Payment completed: " + id));
    events.publishEvent(new Paid(id));
    // No save(accounts) needed: managed entities use dirty checking.
  }
}
