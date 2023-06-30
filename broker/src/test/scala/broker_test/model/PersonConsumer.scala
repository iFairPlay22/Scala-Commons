package broker_test.model

import akka.Done
import akka.actor.ActorSystem
import broker._BrokerConsumerSystem
import com.typesafe.scalalogging.Logger
import io.circe.generic.auto._

import scala.concurrent.Future

class PersonConsumer(callback: (PersonId, Person) => Unit)(implicit val system: ActorSystem)
    extends _BrokerConsumerSystem[PersonId, Person] {

  private val logger: Logger = Logger(getClass)

  override val callbacks: Set[(PersonId, Person) => Future[Done]] =
    Set((personId: PersonId, person: Person) => {
      logger.info(f"Consuming (key = $personId, value = $person)")
      callback(personId, person)
      Future.successful {
        Done
      }
    })

}
