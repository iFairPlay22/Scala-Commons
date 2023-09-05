package http.health

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import http.{_HttpControllerSystem, _HttpServerSystem}

import scala.concurrent.Future
class _HealthCheckSystem(healthCondition: () => Boolean = () => true)(implicit val system: ActorSystem) extends _HttpServerSystem with _HttpControllerSystem {

  override protected val SERVER_HOST: String = config.getString("health-check.server-host")
  override protected val SERVER_PORT: Int = config.getInt("health-check.server-port")

  override val routes: Route =
    pathPrefix("health-check") {
      if (healthCondition())
        complete(StatusCodes.OK, "System healthy")
      else
        complete(StatusCodes.InternalServerError, "System unhealthy")
    }

  def startHealthCheck(): Future[Done] =
    startServer()
}

