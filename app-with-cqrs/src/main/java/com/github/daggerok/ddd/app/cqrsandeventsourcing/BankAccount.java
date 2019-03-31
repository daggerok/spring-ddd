package com.github.daggerok.ddd.app.cqrsandeventsourcing;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.math.BigDecimal.ZERO;
import static java.time.LocalDateTime.now;

@ToString
@EqualsAndHashCode
public class BankAccount {

  @Getter
  private UUID aggregateId;

  @Getter
  private BigDecimal balance;

  @Getter
  private final List<DomainEvent> dirtyEvents = new CopyOnWriteArrayList<>();

  public BankAccount(UUID aggregateId) {
    if (null == aggregateId) throw new IllegalArgumentException("cannot create bank account with null aggregateId");
    on(new BankAccountCreated(aggregateId, now(), ZERO));
  }

  private void on(BankAccountCreated event) {
    this.aggregateId = event.getAggregateId();
    this.balance = event.getBalance();
    dirtyEvents.add(event);
  }

  public void deposit(BigDecimal amount) {
    if (canNotDeposit(amount)) throw new IllegalArgumentException("cannot deposit " + amount);
    on(new Deposited(aggregateId, now(), amount));
  }

  private boolean canNotDeposit(BigDecimal amount) {
    return amount == null || amount.signum() <= 0;
  }

  private void on(Deposited event) {
    balance = balance.add(event.getAmount());
    dirtyEvents.add(event);
  }

  public void withdraw(BigDecimal amount) {
    if (canNotWithdrawAmount(amount)) throw new IllegalArgumentException("cannot withdraw " + amount);
    on(new Withdrawn(aggregateId, now(), amount));
  }

  private boolean canNotWithdrawAmount(BigDecimal amount) {
    return amount == null || balance.compareTo(amount) < 0;
  }

  private void on(Withdrawn event) {
    balance = balance.subtract(event.getAmount());
    dirtyEvents.add(event);
  }
}
