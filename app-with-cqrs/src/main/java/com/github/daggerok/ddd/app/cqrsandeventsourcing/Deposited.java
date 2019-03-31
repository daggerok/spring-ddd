package com.github.daggerok.ddd.app.cqrsandeventsourcing;

import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class Deposited implements DomainEvent {
  UUID aggregateId;
  LocalDateTime when;
  BigDecimal amount;
}
