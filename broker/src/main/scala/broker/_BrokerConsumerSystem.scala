package broker

import akka.Done
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.{Keep, Sink, Source}
import com.typesafe.scalalogging.Logger
import commons.actor._WithActorSystem
import commons.exceptions.{_AlreadyStartedBrokerConsumerException, _AlreadyStoppedBrokerConsumerException, _NotYetStartedBrokerConsumerException, _UnableToLaunchBrokerConsumerException}
import io.circe.{Decoder, Encoder}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory

import scala.concurrent.Future

/**
 * System that allow to execute some callbacks everytime that a Kafka topic receives messages of
 * shape (key: K, value: V)
 * @param Decoder[K]
 *   How to decode the Kafka key of the message
 * @param Encoder[V]
 *   How to encode the Kafka value of the message
 * @tparam K
 *   Message key type
 * @tparam V
 *   Message value type
 */
abstract class _BrokerConsumerSystem[K >: Null: Decoder: Encoder, V >: Null: Decoder: Encoder]
    extends _WithActorSystem {

  private val logger = LoggerFactory.getLogger(getClass)

  // Broker configurations
  private val bootstrapServer = config.getString("kafka.consumer.kafka-clients.bootstrap.servers")
  private val topic: String = config.getString("broker_consumer.topic")
  val callbacks: Set[(K, V) => Future[Done]]

  private val settings: ConsumerSettings[K, V] =
    ConsumerSettings(
      config.getConfig("kafka.consumer"),
      _Serde.serde[K].deserializer(),
      _Serde.serde[V].deserializer())

  private def applyCallbacks(record: ConsumerRecord[K, V]): Future[Done] =
    Source(callbacks)
      .mapAsync(3) { callback =>
        logger.info(f"Consuming data in topic $topic")
        callback(record.key(), record.value())
      }
      .run()

  private var consumerControl: Option[Consumer.Control] = None

  def startBrokerConsumer(): Future[Done] =
    consumerControl match {
      case Some(_) =>
        throw new _AlreadyStartedBrokerConsumerException()
      case None =>
        logger.info(f"Running broker consumer in server $bootstrapServer and topic $topic")

        val (control, future) = Consumer
          .plainSource(settings, Subscriptions.topics(topic))
          .mapAsync(1)(record => {
            logger.info(f"Record is consumed in broker!")
            applyCallbacks(record)
          })
          .toMat(Sink.ignore)(Keep.both)
          .run()

        consumerControl = Some(control)

        future
          .andThen(_ => {
            logger.info(f"Broker consumer is running!")
            consumerControl = Some(control)
          })
          .recover { error =>
            error.printStackTrace()
            logger.error(f"Unable to launch broker consumer!", error)
            throw new _UnableToLaunchBrokerConsumerException()
          }
    }

  private var stopped: Boolean = false

  def stopBrokerConsumer(): Future[Done] =
    consumerControl match {
      case None =>
        throw new _NotYetStartedBrokerConsumerException()
      case Some(c) =>
        if (stopped) {
          throw new _AlreadyStoppedBrokerConsumerException()
        } else {
          logger.info("Shutting down broker consumer")
          c.shutdown()
            .andThen(_ => stopped = true)
            .andThen(_ => logger.info("Broker consumer is down"))
        }
    }

}
