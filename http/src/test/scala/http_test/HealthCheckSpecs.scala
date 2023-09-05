package http_test

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import http.health._HealthCheckSystem
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class HealthCheckSpecs
  extends AnyWordSpecLike
    with ScalatestRouteTest
    with Matchers
    with ScalaFutures {

  f"Valid HealthCheck system" should {
    f"provide a valid health check" in {
      Get("/health-check") ~> new HealthCheckOk().routes ~> check {
        status.shouldEqual(StatusCodes.OK)
        responseAs[String].shouldEqual("System healthy")
      }
    }
  }

  f"Invalid HealthCheck system" should {
    f"provide a invalid health check" in {
      Get("/health-check") ~> new HealthCheckKo().routes ~> check {
        status.shouldEqual(StatusCodes.InternalServerError)
        responseAs[String].shouldEqual("System unhealthy")
      }
    }
  }
}

class HealthCheckOk(implicit override val system: ActorSystem) extends _HealthCheckSystem {
  override def healthCondition() : Boolean = true
}

class HealthCheckKo(implicit override val system: ActorSystem) extends _HealthCheckSystem {
  override def healthCondition() : Boolean = false
}
