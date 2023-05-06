package commons.system.http

import akka.Done
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.Logger
import commons.exceptions.{_AlreadyStartedServerException, _AlreadyStoppedServerException, _Exception, _NotStartedServerException, _UnableToStartServerException}
import commons.system.actor.{_ActorSystem, _WithActorSystem}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait _HttpServerSystem extends _WithActorSystem {

  private val logger: Logger = Logger(getClass)

  def routes: Route

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
            .andThen(_ => logger.info(f"HTTP server was launched!")))

        Future.successful(Done)
    }

  private var stopped: Boolean = false

  def stopServer(): Future[Done] =
    server match {
      case None =>
        throw new _NotStartedServerException()
      case Some(s) =>
        if (!stopped) {
          logger.info(f"Stopping HTTP server")

          s.flatMap {
            _.unbind()
              .andThen(_ => stopped = true)
              .andThen(_ => logger.info(f"HTTP server was stopped!"))
          }.recover {
            throw new _UnableToStartServerException()
          }
        } else {
          throw new _AlreadyStoppedServerException()
        }
    }

}
