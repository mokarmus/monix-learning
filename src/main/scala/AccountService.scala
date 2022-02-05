import monix.eval.Task

case class InsufficientBalance(currentBalance: Long, debitAmount: Long)
    extends RuntimeException(s"Attempted to debit $debitAmount but current balance is $currentBalance")

class AccountService(repository: AccountStateRepository) {
  def credit(amount: Long): Task[Unit] = for {
    currentState <- Task.deferFutureAction(ec => repository.read(ec))
    newState = State(version = currentState.version + 1, balance = currentState.balance + amount)
    _ <- Task.deferFutureAction(ec => repository.write(currentState, newState)(ec))
  } yield ()

  def debit(amount: Long): Task[Unit] = for {
    currentState <- Task.deferFutureAction(ec => repository.read(ec))
    _ <- if (currentState.balance < amount) Task.raiseError(InsufficientBalance(currentState.balance, amount))
    else Task.deferFutureAction(ec => repository.write(currentState, State(currentState.version + 1, currentState.balance - amount))(ec))
  } yield ()

  def balance: Task[Long] = for {
    state <- Task.deferFutureAction(ec => repository.read(ec))
    balance = state.balance
  } yield balance
}
