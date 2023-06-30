package scheduler_test.model

import akka.Done
import akka.actor.ActorSystem
import scheduler._SchedulerSystem

import scala.concurrent.Future

class CounterScheduler(implicit val system: ActorSystem) extends _SchedulerSystem {
  var counter: Int = 0
  override val action: Unit => Future[Done] = _ => {
    counter += 1
    Future.successful { Done }
  }
}
