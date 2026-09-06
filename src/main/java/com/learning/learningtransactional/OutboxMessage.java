package com.learning.learningtransactional;

import jakarta.persistence.*;

@Entity
@Table(name = "lab_outbox")
public class OutboxMessage {
  @Id private String id;
  private String payload;
  private boolean delivered;

  protected OutboxMessage() {}

  public OutboxMessage(String id, String payload) {
    this.id = id;
    this.payload = payload;
  }

  public String getId() {
    return id;
  }

  public String getPayload() {
    return payload;
  }

  public boolean isDelivered() {
    return delivered;
  }

  public void markDelivered() {
    delivered = true;
  }
}
