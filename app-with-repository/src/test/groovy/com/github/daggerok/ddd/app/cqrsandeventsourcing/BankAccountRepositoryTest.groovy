package com.github.daggerok.ddd.app.cqrsandeventsourcing

import spock.lang.Specification

class BankAccountRepositoryTest extends Specification {

  def 'should save and load bank account'() {
    given:
      UUID aggregateId = UUID.randomUUID()
    and:
      BankAccount bankAccount = new BankAccount(aggregateId)
    and:
      bankAccount.deposit 100
    when:
    BankAccountRepository repository = new BankAccountRepository()
    and:
      repository.save bankAccount
    then:
      BankAccount loadedBankAccount = repository.load aggregateId
    and:
      loadedBankAccount.balance == 100
  }
}
