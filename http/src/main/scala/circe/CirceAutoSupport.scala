package circe

import scala.concurrent.Future
import akka.http.scaladsl.marshalling.Marshaller
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.ContentTypeRange
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.http.scaladsl.unmarshalling.Unmarshaller
import io.circe._
import io.circe.generic.AutoDerivation
import io.circe.parser._
import io.circe.syntax._

/**
 * Provides Automatic Derivation for Encoding/Decoding domain classes, as well as ZoneDateTime
 * fields.
 */
trait CirceAutoSupport extends CirceSupport with AutoDerivation

/**
 * Provides JSON Encoding support with Circe. To use automatic codec derivation, user needs to
 * import `io.circe.generic.auto._`.
 */
trait CirceSupport {

  private val printer = Printer.noSpaces.copy(dropNullValues = true)

  private def jsonContentTypes: List[ContentTypeRange] =
    List(`application/json`)

  implicit final def unmarshaller[A: Decoder]: FromEntityUnmarshaller[A] =
    Unmarshaller.stringUnmarshaller
      .forContentTypes(jsonContentTypes: _*)
      .flatMap(ctx => mat => json => decode[A](json).fold(Future.failed, Future.successful))

  implicit final def marshaller[A: Encoder]: ToEntityMarshaller[A] =
    Marshaller.withFixedContentType(`application/json`) { a =>
      HttpEntity(`application/json`, printer.print(a.asJson))
    }
}
