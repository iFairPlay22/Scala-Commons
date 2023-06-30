package broker_test.model

import akka.actor.ActorSystem
import broker._BrokerProducerSystem
import io.circe.generic.auto._

class PersonProducer(implicit val system: ActorSystem)
    extends _BrokerProducerSystem[PersonId, Person] {}
