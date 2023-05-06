package commons.system.actor

import akka.actor.ActorSystem
import com.typesafe.config.Config

import scala.concurrent.ExecutionContextExecutor

trait _WithActorSystem {
  implicit val system: ActorSystem
  implicit lazy val executor: ExecutionContextExecutor = system.dispatcher
  implicit lazy val config: Config = system.settings.config
}
