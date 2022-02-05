import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

class AccountActivitySimulatorSpec extends AsyncWordSpec with Matchers {
  "account activity simulator" should {
    "credit amount multiple times" in {
      val service   = new AccountService(new InMemoryAccountStateRepository)
      val simulator = new AccountActivitySimulator(service)

      (for {
        _         <- simulator.simulateCredits(10, 125)
        balance   <- service.balance
        assertion <- Task.eval(balance shouldEqual 1250)
      } yield assertion).runToFuture
    }

    "debit amount multiple times" in {
      val service   = new AccountService(new InMemoryAccountStateRepository)
      val simulator = new AccountActivitySimulator(service)

      (for {
        _         <- service.credit(1250)
        _         <- simulator.simulateDebits(10, 125)
        balance   <- service.balance
        assertion <- Task.eval(balance shouldEqual 0)
      } yield assertion).runToFuture
    }

    "credit and debit amounts multiple times" in {
      val repository = new InMemoryAccountStateRepository
      val service    = new AccountService(repository)
      val simulator  = new AccountActivitySimulator(service)

      val numberOfCredits = 25
      val numberOfDebits  = 10

      (for {
        _       <- simulator.simulateDailyActivity(numberOfCredits, 50, numberOfDebits, 125)
        balance <- service.balance
        version <- Task.deferFuture(repository.read.map(_.version))
        assertion <- Task.eval {
          balance shouldEqual 0
          version shouldEqual numberOfCredits + numberOfDebits
        }
      } yield assertion).runToFuture
    }
  }
}
