package com.github.daggerok.ddd.app.cqrsandeventsourcing;

import com.github.daggerok.ddd.app.cqrsandeventsourcing.event.BankAccountCreated;
import com.github.daggerok.ddd.app.cqrsandeventsourcing.event.Deposited;
import com.github.daggerok.ddd.app.cqrsandeventsourcing.event.DomainEvent;
import com.github.daggerok.ddd.app.cqrsandeventsourcing.event.Withdrawn;
import io.vavr.API;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.Predicates.instanceOf;
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
    apply(new BankAccountCreated(aggregateId, now(), ZERO));
  }

  private BankAccount apply(BankAccountCreated event) {
    this.aggregateId = event.getAggregateId();
    this.balance = event.getBalance();
    dirtyEvents.add(event);
    return this;
  }

  public void deposit(BigDecimal amount) {
    if (canNotDeposit(amount)) throw new IllegalArgumentException("cannot deposit " + amount);
    apply(new Deposited(aggregateId, now(), amount));
  }

  private boolean canNotDeposit(BigDecimal amount) {
    return amount == null || amount.signum() <= 0;
  }

  private BankAccount apply(Deposited event) {
    balance = balance.add(event.getAmount());
    dirtyEvents.add(event);
    return this;
  }

  public void withdraw(BigDecimal amount) {
    if (canNotWithdrawAmount(amount)) throw new IllegalArgumentException("cannot withdraw " + amount);
    apply(new Withdrawn(aggregateId, now(), amount));
  }

  private boolean canNotWithdrawAmount(BigDecimal amount) {
    return amount == null || balance.compareTo(amount) < 0;
  }

  private BankAccount apply(Withdrawn event) {
    balance = balance.subtract(event.getAmount());
    dirtyEvents.add(event);
    return this;
  }

  public static BankAccount restore(UUID aggregateId, List<DomainEvent> eventStream) {
    return io.vavr.collection.List.ofAll(eventStream)
                                  .foldLeft(new BankAccount(aggregateId), BankAccount::applyAny);
  }

  private BankAccount applyAny(DomainEvent domainEvent) {
    return API.Match(domainEvent).of(
        Case($(instanceOf(BankAccountCreated.class)), this::apply),
        Case($(instanceOf(Deposited.class)), this::apply),
        Case($(instanceOf(Withdrawn.class)), this::apply),
        /* Case($(), this) // fallback to itself... */ Case($(), o -> {
          System.out.printf("cannot apply %s\n", o);
          throw new IllegalStateException("cannot apply " + o.getType() + " for " + o.getAggregateId());
        })
    );
  }
}
