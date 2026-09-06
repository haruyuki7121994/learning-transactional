package com.learning.learningtransactional;

import jakarta.persistence.*;

@Entity
@Table(name = "lab_payment")
public class Payment {
  @Id private String id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Account sender;

  private long amount;

  protected Payment() {}

  public Payment(String id, Account sender, long amount) {
    this.id = id;
    this.sender = sender;
    this.amount = amount;
  }

  public Account getSender() {
    return sender;
  }
}
