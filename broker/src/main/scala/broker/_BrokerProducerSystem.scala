package broker

import akka.Done
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.SendProducer
import com.typesafe.scalalogging.Logger
import commons.actor._WithActorSystem
import commons.exceptions.{_AlreadyStartedBrokerProducerException, _AlreadyStoppedBrokerProducerException, _NotYetStartedBrokerProducerException, _UnableToProduceInBrokerException}
import io.circe.{Decoder, Encoder}
import org.apache.kafka.clients.producer.{ProducerRecord, RecordMetadata}
import org.slf4j.LoggerFactory

import scala.concurrent.Future

/**
 * System that allow to send messages of shape (key: K, value: V) in a Kafka topic
 * @param Decoder[K]
 *   How to decode the Kafka key of the message
 * @param Encoder[V]
 *   How to encode the Kafka value of the message
 * @tparam K
 *   Message key type
 * @tparam V
 *   Message value type
 */
abstract class _BrokerProducerSystem[K >: Null: Decoder: Encoder, V >: Null: Decoder: Encoder]
    extends _WithActorSystem {

  private val logger = LoggerFactory.getLogger(getClass)

  // Broker configurations
  private val bootstrapServer = config.getString("kafka.producer.kafka-clients.bootstrap.servers")
  private val topic: String = config.getString("broker_producer.topic")

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
        logger.info(f"Running broker producer in server $bootstrapServer and topic $topic")
        producer = Some(SendProducer(settings))
        logger.info(f"Broker producer is running!")
        Future.successful(Done)
    }

  def produce(key: K, value: V): Future[RecordMetadata] =
    producer match {
      case None =>
        throw new _NotYetStartedBrokerProducerException()
      case Some(p) =>
        p
          .send(new ProducerRecord(topic, key, value))
          .andThen(_ => logger.info(f"Record is produced in broker in topic $topic!"))
          .recover { error =>
            error.printStackTrace()
            logger.error(f"Failed to produce record in broker", error)
            throw new _UnableToProduceInBrokerException()
          }
    }

  var isBrokerProducerStopped: Boolean = false

  def stopBrokerProducer(): Future[Done] =
    producer match {
      case None =>
        throw new _NotYetStartedBrokerProducerException()
      case Some(p) =>
        if (isBrokerProducerStopped) {
          throw new _AlreadyStoppedBrokerProducerException()
        } else {
          logger.info("Stopping broker producer")
          p.close()
            .andThen(_ => isBrokerProducerStopped = true)
            .andThen(_ => logger.info(f"Broker producer was stopped!"))
        }
    }

}
