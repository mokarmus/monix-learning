import monix.eval.Task
import monix.reactive.{ Consumer, Observable }

class AccountActivitySimulator(service: AccountService) {
  def simulateCredits(numberOfCredits: Int, amount: Long): Task[Unit] =
    Observable
      .range(0, numberOfCredits)
      .map(_ => amount)
      .consumeWith(Consumer.foreachParallelAsync(5)(service.credit))

  def simulateDebits(numberOfDebits: Int, amount: Long): Task[Unit] =
    Observable
      .range(0, numberOfDebits)
      .map(_ => amount)
      .consumeWith(Consumer.foreachParallelAsync(5) { amount =>
        service
          .debit(amount)
          .onErrorRestartIf {
            case _: InsufficientBalance => true
            case _                      => false
          }
      })

  def simulateDailyActivity(
      numberOfCredits: Int,
      creditAmount: Long,
      numberOfDebits: Int,
      debitAmount: Long
  ): Task[Unit] =
    Task
      .gatherUnordered(
        List(
          simulateCredits(numberOfCredits, creditAmount),
          simulateDebits(numberOfDebits, debitAmount)
        )
      )
      .map(_ => ())
}
