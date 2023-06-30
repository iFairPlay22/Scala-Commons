package commons.testing

import scala.concurrent.duration._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

trait TestUtils {
  final implicit val awaitDuration: Duration = 2 minutes

  def await[T](f: Future[T]): T =
    Await.result(f, awaitDuration)
}
