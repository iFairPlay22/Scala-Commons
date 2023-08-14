package http

import akka.Done
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import commons.actor._WithActorSystem
import commons.exceptions.{_AlreadyStartedServerException, _AlreadyStoppedServerException, _NotStartedServerException, _UnableToStartServerException}
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
 * System that creates an HTTP server from Akka routes. It will be launched in the host
 * "api.server-host" and port "api.server-port".
 */
trait _HttpServerSystem extends _WithActorSystem {

  private val logger = LoggerFactory.getLogger(getClass)

  // Server configurations
  val routes: Route
  private val SERVER_HOST: String = config.getString("api.server-host")
  private val SERVER_PORT: Int = config.getInt("api.server-port")

// Lifecycle
  private var server: Option[Future[Http.ServerBinding]] = None

  def startServer(): Future[Done] =
    server match {
      case Some(_) =>
        throw new _AlreadyStartedServerException()
      case None =>
        logger.info(f"Launching an HTTP server in $SERVER_HOST:$SERVER_PORT")

        server = Some(
          Http()
            .newServerAt(SERVER_HOST, SERVER_PORT)
            .bind(Route.seal(routes))
            .andThen(_ => logger.info(f"HTTP server is launched!")))

        server.get.map(_ => Done)
    }

  private var stopped: Boolean = false

  def stopServer(): Future[Done] =
    server match {
      case None =>
        throw new _NotStartedServerException()
      case Some(s) =>
        if (stopped) {
          throw new _AlreadyStoppedServerException()
        } else {
          logger.info(f"Stopping HTTP server")

          s.flatMap {
            _.unbind()
              .andThen(_ => stopped = true)
              .andThen(_ => logger.info(f"HTTP server was stopped!"))
          }.recover {
            throw new _UnableToStartServerException()
          }
        }
    }

}
