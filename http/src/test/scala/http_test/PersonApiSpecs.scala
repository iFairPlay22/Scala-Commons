package http_test

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import commons.testing.TestUtils
import http_test.model.{PersonController, PersonDTO}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec

class PersonApiSpecs
    extends AnyWordSpecLike
    with ScalatestRouteTest
    with Matchers
    with ScalaFutures
    with TestUtils {

  private val personsRoutes: Route = new PersonController().routes

  f"Vehicles API" should {

    f"return empty vehicles list" in {

      Get("/api/persons") ~> personsRoutes ~> check {

        status.shouldEqual(StatusCodes.OK)

        val persons: Seq[PersonDTO] = decodeResponse[Seq[PersonDTO]](responseAs[String])
        persons.shouldEqual(List(PersonDTO("Philipp", 23), PersonDTO("Daniel", 56)))
      }

    }

    f"return vehicles list with 1 element" in {

      Get("/api/person") ~> personsRoutes ~> check {

        status.shouldEqual(StatusCodes.OK)

        val person: PersonDTO = decodeResponse[PersonDTO](responseAs[String])
        person.shouldEqual(PersonDTO("Philipp", 23))
      }

    }
  }
}
