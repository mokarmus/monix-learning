# Introduction to Monix

This repository contains set of exercises that aim to introduce Monix to Scala developers. 

The domain of the exercise is reassembling bank account that can be:
* checked for current balance
* credited
* debited if amount is less than or equal to current balance

The consistency of an account balance is ensured by using `AtomicReference` and its `compareAndSwap` method. This gives us opportunity to explore ways of dealing with concurrency using Monix's apis.

## Structure of the project

`AccountStateRepository` - is an infrastructure service with one in-memory implementation that contains the low level details of dealing with optimistic locking. The implementation is complete and intentionally using Scala's Futures in order to learn how to wrap them in a Task.

`AccountService` - is a domain service that will be implemented during exercise:
* `def credit(amount: Long): Task[Unit]`
* `def debit(amount: Long): Task[Unit]`
* `def def balance: Task[Long]`

`AccountActivitySimulator` - is a simplistic simulator that allows to issue multiple credits or debits (or both at the same time):
- `def simulateCredits(numberOfCredits: Int, amount: Long): Task[Unit]`
- `def simulateDebits(numberOfDebits: Int, amount: Long): Task[Unit]`
- `def simulateDailyActivity(numberOfCredits: Int, creditAmount: Long, numberOfDebits: Int, debitAmount: Long): Task[Unit]`

## Implementation steps

### AccountServiceSpec
* `allow to credit to account` - implement `credit` and `balance` methods using `Task.deferFutureAction` and `Task.eval` and combine tasks using a for comprehension
* `allow to debit if current balance is greater than or equal to debit amount` - implement `debit` in a similar way to credit
* `error debit if current balance is lower than debit amount` - if balance is lower than required amount return an `InsufficientBalance` using `Task.raiseError`

### AccountActivitySimulatorSpec
* `credit amount multiple times` - implement `simulateCredits` using `Observable.range` and `Consumer.foreachAsync`
* `debit amount multiple times` - implement `simulateDebits` in the same way like `simulateCredits`
* `credit and debit amounts multiple times` - implement `simulateDailyActivity` by putting `simulateCredits` and `simulateDebits` into a for comprehension

### Extra fancy
* Change `foreachAsync` in `simulateCredits` and `simulateDebits`  into `foreachParallelAsync`. Handle retries of `OptimisticConcurrencyException` with `onErrorRestartIf`
* Transform for comprehension in `simulateDailyActivity` into a call to `Task.gatherUnordered`. Handle `InsufficientBalance` thrown.
