package com.learning.learningtransactional;

import jakarta.persistence.*;

@Entity
@Table(name = "wallet_account")
public class Account {
  @Id private Long id;
  @Version private Long version;
  private long balance;

  protected Account() {}

  public Account(Long id, long balance) {
    this.id = id;
    this.balance = balance;
  }

  public Long getId() {
    return id;
  }

  public Long getVersion() {
    return version;
  }

  public long getBalance() {
    return balance;
  }

  public void debit(long amount) {
    if (amount <= 0 || balance < amount)
      throw new IllegalArgumentException("Invalid amount or insufficient funds");
    balance -= amount;
  }

  public void credit(long amount) {
    if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
    balance += amount;
  }
}
