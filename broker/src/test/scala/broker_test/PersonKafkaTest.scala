package broker_test

import broker_test.model.{PersonConsumer, PersonMessage, PersonMessageCounter, PersonProducer}
import commons.actor._ActorSystem
import commons.testing.TestUtils
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
class PersonKafkaTest
    extends AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with TestUtils
    with _ActorSystem {

  private val messageCounter = new PersonMessageCounter()
  private val personBrokerProducer = new PersonProducer()
  private val personBrokerConsumer = new PersonConsumer(messageCounter.add)

  override def beforeAll(): Unit = {
    personBrokerProducer.startBrokerProducer()
    personBrokerConsumer.startBrokerConsumer()
  }

  f"Kafka System" should {

    f"produce records, that can be consumed" in {

      val messages = PersonMessage.samples

      // Produce the records
      messages.foreach(message => {
        await(personBrokerProducer.produce(message.key, message.message))
      })

      // Verify that the records were successfully consumed
      eventually(() => { messageCounter.messages shouldBe messages })
    }

  }

  override def afterAll(): Unit = {
    await(personBrokerProducer.stopBrokerProducer())
    await(personBrokerConsumer.stopBrokerConsumer())
  }
}
