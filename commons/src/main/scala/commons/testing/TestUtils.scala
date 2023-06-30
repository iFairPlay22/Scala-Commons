package commons.testing

import io.circe
import io.circe.Decoder

import scala.concurrent.duration._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

trait TestUtils {
  final implicit val awaitDuration: Duration = 2 minutes

  def await(duration: Duration): Unit =
    Thread.sleep(duration.toMillis)

  def await[T](f: Future[T]): T =
    Await.result(f, awaitDuration)

  def decodeResponse[T: Decoder](resp: String): T = {
    val eitherErrorOrT = circe.jawn
      .decode[T](resp)

    eitherErrorOrT match {
      case Right(r) => r
      case Left(err) =>
        println("Unable to decode API response")
        throw err
    }
  }
}
