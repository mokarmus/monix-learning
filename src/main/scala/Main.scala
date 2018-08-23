import com.typesafe.scalalogging.LazyLogging
import monix.eval.Task

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import monix.execution.Scheduler.Implicits.global
import monix.reactive.{ Consumer, Observable }

import scala.concurrent.duration._

object Main extends App with LazyLogging {
  val simulator = new AccountActivitySimulator(new AccountService(new InMemoryAccountStateRepository))

  val consumer = Consumer.foreachAsync[Long] { batch =>
    for {
      _ <- Task.eval(logger.info(s"Processing batch $batch"))
      _ <- simulator.simulateDailyActivity(100, 125, 10, 1250)
    } yield ()
  }

  val task = Observable
    .range(0, 100)
    .delayOnNext(100.millis)
    .consumeWith(consumer)

  Await.result(task.runAsync, Duration.Inf)
}
