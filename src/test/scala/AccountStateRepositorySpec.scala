import org.scalatest.{ AsyncWordSpec, Matchers }

import scala.concurrent.Future

class AccountStateRepositorySpec extends AsyncWordSpec with Matchers {
  "in memory repository" should {
    "be initialised with 0.0 balance and -1 version" in {
      (new InMemoryAccountStateRepository).read.map(_ shouldEqual State(0, 0L))
    }

    "accept write of new version that was calculated based on current state" in {
      val repository = new InMemoryAccountStateRepository
      for {
        currentState <- repository.read
        newState     <- Future.successful(State(currentState.version + 1, 30))
        result       <- repository.write(currentState, newState)
        savedState   <- repository.read
      } yield {
        result shouldEqual ()
        savedState shouldEqual newState
      }
    }

    "reject write if state doesn't match" in {
      (new InMemoryAccountStateRepository).write(State(5, 10L), State(6, 2L)).failed.map { ex =>
        ex shouldEqual OptimisticConcurrencyException(
          s"Unable to apply update to an outdated version. Current state: ${State(0, 0L)}"
        )
      }
    }

    "reject write if version is not monotonically increasing" in {

      val repository = new InMemoryAccountStateRepository
      for {
        currentState <- repository.read
        newState     <- Future.successful(State(currentState.version + 2, 30))
        error        <- repository.write(currentState, newState).failed
      } yield {
        error shouldEqual OptimisticConcurrencyException(
          s"Updated version is not monotonically increasing. Expected: 1, got: 2"
        )
      }
    }
  }
}
