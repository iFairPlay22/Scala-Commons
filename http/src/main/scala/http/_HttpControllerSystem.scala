package http

import akka.http.scaladsl.server.{Directives, Route}
import com.typesafe.scalalogging.Logger
import commons.actor._WithActorSystem
import io.circe.Encoder
import io.circe.syntax._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
 * System that provides useful methods to generate HTTP requests.
 */
trait _HttpControllerSystem extends _WithActorSystem with Directives {

  private val logger: Logger = Logger(getClass)

  // Controller configurations
  val routes: Route

  private def toJson[R: Encoder](k: Future[R]): Future[String] =
    k.map {
      _.asJson.spaces4SortKeys
    }.recover { exception =>
      exception.printStackTrace()
      logger.error("Unable to encode response!")
      exception.getMessage.asJson.spaces4SortKeys
    }

  def response[R: Encoder](f: () => Future[R]): Route = {
    complete(toJson(f()))
  }
}
