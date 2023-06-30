package http_test.model

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import http._HttpControllerSystem
import io.circe.generic.auto._

import scala.concurrent.Future

class PersonController(implicit val system: ActorSystem) extends _HttpControllerSystem {

  override val routes: Route =
    pathPrefix("api") {
      path("persons") {
        get {
          response { () =>
            Future.successful(Seq(PersonDTO("Philipp", 23), PersonDTO("Daniel", 56)))
          }
        }
      } ~
        path("person") {
          get {
            response { () =>
              Future.successful(PersonDTO("Philipp", 23))
            }
          }
        }
    }

}
