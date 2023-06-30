package http

import akka.http.scaladsl.server.{Directives, Route}
import circe.CirceAutoSupport
import commons.actor._WithActorSystem

/**
 * System that provides useful methods to generate HTTP requests.
 */
trait _HttpControllerSystem extends _WithActorSystem with CirceAutoSupport with Directives {
  val routes: Route
}
