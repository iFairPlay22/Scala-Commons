package http_test.model

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import http._HttpControllerSystem
import http.health._HealthCheckSystem
import io.circe.generic.auto._

import scala.concurrent.Future

class PersonController(implicit override val system: ActorSystem) extends _HttpControllerSystem  {

  override val routes: Route =
    pathPrefix("api") {
      path("persons") {
        get {
          complete(Future {
            Seq(PersonDTO("Philipp", 23), PersonDTO("Daniel", 56))
          })
        }
      } ~
        path("person") {
          get {
            complete(Future { PersonDTO("Philipp", 23) })
          }
        }
    }

}
