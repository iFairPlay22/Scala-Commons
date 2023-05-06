package commons.system.broker

import akka.Done
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.SendProducer
import com.typesafe.scalalogging.Logger
import commons.data._Serde
import commons.exceptions.{_AlreadyStartedBrokerProducerException, _AlreadyStoppedBrokerProducerException, _NotYetStartedBrokerProducerException, _UnableToProduceInBrokerException}
import commons.system.actor.{_ActorSystem, _WithActorSystem}
import io.circe.{Decoder, Encoder}
import org.apache.kafka.clients.producer.{ProducerRecord, RecordMetadata}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

abstract class _BrokerProducerSystem[K >: Null: Decoder: Encoder, V >: Null: Decoder: Encoder]
    extends _WithActorSystem {

  private val logger: Logger = Logger(getClass)

  val topic: String

  private val settings: ProducerSettings[K, V] =
    ProducerSettings(
      config.getConfig("kafka.producer"),
      _Serde.serde[K].serializer(),
      _Serde.serde[V].serializer())

  // Producer
  private var producer: Option[SendProducer[K, V]] = None

  def startBrokerProducer(): Future[Done] =
    producer match {
      case Some(_) =>
        throw new _AlreadyStartedBrokerProducerException()
      case None =>
        logger.info(f"Starting broker producer in topic $topic!")
        producer = Some(SendProducer(settings))
        logger.info(f"Broker producer is running!")
        Future.successful(Done)

    }

  def produce(key: K, value: V): Future[RecordMetadata] =
    producer match {
      case None =>
        throw new _NotYetStartedBrokerProducerException()
      case Some(p) =>
        logger.info(f" Producing in topic $topic")

        p
          .send(new ProducerRecord(topic, key, value))
          .andThen(_ => logger.info(f"Record is produced in broker!"))
          .recover { error =>
            error.printStackTrace()
            logger.error(f"Failed to produce record in broker", error)
            throw new _UnableToProduceInBrokerException()
          }
    }

  private var stopped: Boolean = false

  def stopBrokerProducer(): Future[Done] =
    producer match {
      case None =>
        throw new _NotYetStartedBrokerProducerException()
      case Some(p) =>
        if (!stopped) {
          logger.info("Stopping broker producer")
          p.close()
            .andThen(_ => stopped = true)
            .andThen(_ => logger.info(f"Broker producer was stopped!"))
        } else {
          throw new _AlreadyStoppedBrokerProducerException()
        }
    }

}
