package com.github.daggerok.ddd.app.cqrsandeventsourcing.event;

import java.time.LocalDateTime;
import java.util.UUID;

public interface DomainEvent {

  UUID getAggregateId();

  LocalDateTime getWhen();

  default String getType() {
    return this.getClass().getName();
  }
}
