package commons.actor

import akka.Done
import akka.actor.{ActorSystem, Terminated}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger
import commons.exceptions._AlreadyStoppedActorException

import scala.concurrent.{ExecutionContextExecutor, Future}
trait _ActorSystem extends _WithActorSystem {

  private val logger: Logger = Logger(getClass)

  // Actor
  logger.info(f"Starting actor system $validSystemName")
  final override implicit val system: ActorSystem =
    ActorSystem(validSystemName, ConfigFactory.load())

  private lazy val validSystemName = {
    val tmp = getClass.getName
      .replaceAll("[^A-Za-z]+", "_")
      .toLowerCase()

    (tmp.startsWith("_"), tmp.endsWith("_")) match {
      case (true, true) => tmp.slice(1, tmp.length() - 1)
      case (true, false) => tmp.slice(1, tmp.length())
      case (false, true) => tmp.slice(0, tmp.length() - 1)
      case (false, false) => tmp
    }
  }

  private var stopped: Boolean = false

  def stopActor(): Future[Done] =
    if (stopped) {
      throw new _AlreadyStoppedActorException()
    } else {
      logger.info("Stopping actor system")
      system
        .terminate()
        .andThen(_ => stopped = true)
        .andThen(_ => "Actor system is stopped!")
        .map(_ => Done)
    }

}
