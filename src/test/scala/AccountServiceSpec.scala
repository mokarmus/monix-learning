import monix.eval.Task
import org.scalatest.{ AsyncWordSpec, Matchers }
import monix.execution.Scheduler.Implicits.global

class AccountServiceSpec extends AsyncWordSpec with Matchers {
  "account service" should {
    "allow to credit to account" in {
      val service = new AccountService(new InMemoryAccountStateRepository)

      (for {
        _         <- service.credit(100)
        balance   <- service.balance
        assertion <- Task.eval(balance shouldEqual 100)
      } yield assertion).runAsync
    }

    "allow to debit if current balance is greater than or equal to debit amount" in {
      val service = new AccountService(new InMemoryAccountStateRepository)

      (for {
        _         <- service.credit(100)
        _         <- service.debit(100)
        balance   <- service.balance
        assertion <- Task.eval(balance shouldEqual 0)
      } yield assertion).runAsync
    }

    "error debit if current balance is lower than debit amount" in {
      val service = new AccountService(new InMemoryAccountStateRepository)

      (for {
        result    <- service.debit(100).failed
        assertion <- Task.eval(result shouldEqual InsufficientBalance(0, 100))
      } yield assertion).runAsync
    }
  }
}
