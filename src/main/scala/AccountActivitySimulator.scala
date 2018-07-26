import monix.eval.Task

class AccountActivitySimulator(service: AccountService) {
  def simulateCredits(numberOfCredits: Int, amount: Long): Task[Unit] = Task.unit

  def simulateDebits(numberOfDebits: Int, amount: Long): Task[Unit] = Task.unit

  def simulateDailyActivity(
      numberOfCredits: Int,
      creditAmount: Long,
      numberOfDebits: Int,
      debitAmount: Long
  ): Task[Unit] = Task.unit
}
