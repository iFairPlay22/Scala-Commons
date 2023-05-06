package commons.system.broker

import akka.Done
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.{Keep, RunnableGraph, Sink, Source}
import com.typesafe.scalalogging.Logger
import commons.data._Serde
import commons.exceptions.{_AlreadyStartedBrokerConsumerException, _AlreadyStoppedBrokerConsumerException, _NotYetStartedBrokerConsumerException, _UnableToLaunchBrokerConsumerException}
import commons.system.actor.{_ActorSystem, _WithActorSystem}
import io.circe.{Decoder, Encoder}
import org.apache.kafka.clients.consumer.ConsumerRecord

import scala.concurrent.Future

abstract class _BrokerConsumerSystem[K >: Null: Decoder: Encoder, V >: Null: Decoder: Encoder]
    extends _WithActorSystem {

  private val logger: Logger = Logger(getClass)

  val topic: String;

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
        logger.info(f"Running broker consumer in topic $topic")

        val (control, future) = Consumer
          .plainSource(settings, Subscriptions.topics(topic))
          .mapAsync(1)(applyCallbacks)
          .toMat(Sink.ignore)(Keep.both)
          .run()

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
        if (!stopped) {
          logger.info("Shutting down broker consumer")
          c.shutdown()
            .andThen(_ => stopped = true)
            .andThen(_ => logger.info("Broker consumer is down"))
        } else {
          throw new _AlreadyStoppedBrokerConsumerException()
        }
    }

}
