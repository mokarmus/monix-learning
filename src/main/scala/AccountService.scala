import monix.eval.Task

case class InsufficientBalance(currentBalance: Long, debitAmount: Long)
    extends RuntimeException(s"Attempted to debit $debitAmount but current balance is $currentBalance")

class AccountService(repository: AccountStateRepository) {
  def credit(amount: Long): Task[Unit] = Task.unit

  def debit(amount: Long): Task[Unit] = Task.unit

  def balance: Task[Long] = Task.now(Long.MinValue)
}
