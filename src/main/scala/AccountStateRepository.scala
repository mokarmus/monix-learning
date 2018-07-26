import java.util.concurrent.atomic.AtomicReference

import scala.concurrent.{ ExecutionContext, Future }

case class State(version: Int, balance: Long)

case class OptimisticConcurrencyException(message: String) extends RuntimeException(message)

trait AccountStateRepository {
  def read(implicit ec: ExecutionContext): Future[State]
  def write(expectedState: State, newState: State)(implicit ec: ExecutionContext): Future[Unit]
}

class InMemoryAccountStateRepository extends AccountStateRepository {

  private val state = new AtomicReference[State](State(0, 0L))

  override def read(implicit ec: ExecutionContext): Future[State] =
    Future.successful(state.get())

  override def write(expectedState: State, newState: State)(implicit ec: ExecutionContext): Future[Unit] =
    if (expectedState.version + 1 != newState.version) {
      Future
        .failed(
          OptimisticConcurrencyException(
            s"Updated version is not monotonically increasing. Expected: ${expectedState.version + 1}, got: ${newState.version}"
          )
        )
    } else {
      Future.successful(state.compareAndSet(expectedState, newState)).flatMap {
        case false =>
          Future
            .failed(
              OptimisticConcurrencyException(s"Unable to apply update to an outdated version. Current state: $state")
            )
        case true => Future.unit
      }
    }
}
