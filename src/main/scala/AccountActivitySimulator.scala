import monix.eval.Task
import monix.reactive.{Consumer, Observable}

class AccountActivitySimulator(service: AccountService) {
  def simulateCredits(numberOfCredits: Int, amount: Long): Task[Unit] = {
    Observable.range(0, numberOfCredits, 1).map(_ => amount)
      .consumeWith(Consumer.foreachParallelTask(5)(a =>
        service.credit(a)
          .onErrorRestartIf {
            case _: OptimisticConcurrencyException => true        //I am restarting in case of optimistic lock
            case _                                 => false
          })
      )
  }

  def simulateDebits(numberOfDebits: Int, amount: Long): Task[Unit] =
    Observable.range(0, numberOfDebits, 1).map(_ => amount)
      .consumeWith(Consumer.foreachParallelTask(5)(a =>
        service.debit(a)
        .onErrorRestartIf {
        case _: InsufficientBalance => true                                  //I am restarting because some cash could appear in the meantime
        case _: OptimisticConcurrencyException => true                      //I am restarting in case of optimistic lock
        case _ => false
      }))

  def simulateDailyActivity(
      numberOfCredits: Int,
      creditAmount: Long,
      numberOfDebits: Int,
      debitAmount: Long
  ): Task[Unit] =
    Task.parSequenceUnordered(
      List(simulateCredits(numberOfCredits, creditAmount), simulateDebits(numberOfDebits, debitAmount))
    ).map(_ => ())
}
