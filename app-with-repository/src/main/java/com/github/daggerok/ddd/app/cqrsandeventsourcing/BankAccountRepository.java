package com.github.daggerok.ddd.app.cqrsandeventsourcing;

import com.github.daggerok.ddd.app.cqrsandeventsourcing.event.DomainEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class BankAccountRepository {

  private final Map<UUID, List<DomainEvent>> eventStream = new ConcurrentHashMap<>();

  public void save(BankAccount aggregate) {
    UUID aggregateId = aggregate.getAggregateId();
    List<DomainEvent> domainEvents = eventStream.getOrDefault(aggregateId, new CopyOnWriteArrayList<>());
    domainEvents.addAll(aggregate.getDirtyEvents());
    aggregate.getDirtyEvents().clear();
    eventStream.put(aggregateId, domainEvents);
  }

  public BankAccount load(UUID aggregateId) {
    final List<DomainEvent> historyEvents = eventStream.getOrDefault(aggregateId, new CopyOnWriteArrayList<>());
    return BankAccount.restore(aggregateId, historyEvents);
  }
}
