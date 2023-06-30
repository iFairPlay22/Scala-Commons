package scheduler_test

import commons.actor._ActorSystem
import commons.testing.TestUtils
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import scheduler_test.model.CounterScheduler
import scala.concurrent.duration._

class SchedulerSpecs
    extends AnyWordSpecLike
    with Matchers
    with _ActorSystem
    with TestUtils
    with BeforeAndAfterAll {

  val scheduler = new CounterScheduler()

  override def beforeAll(): Unit = {
    await(scheduler.startScheduler())
  }

  f"Scheduler" should {

    f"execute an action according to the configurations" in {

      val count1 = scheduler.counter;

      await(5.seconds)

      val count2 = scheduler.counter;
      count1 should be < count2

      await(5.seconds)

      val count3 = scheduler.counter;
      count2 should be < count3

    }

  }

  override def afterAll(): Unit = {
    await(scheduler.stopScheduler())
  }

}
