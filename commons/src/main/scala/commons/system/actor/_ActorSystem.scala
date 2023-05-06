package commons.system.actor

import akka.Done
import akka.actor.{ActorSystem, Terminated}
import com.typesafe.config.Config
import com.typesafe.scalalogging.Logger
import commons.exceptions._AlreadyStoppedActorException
import commons.system._System

import scala.concurrent.{ExecutionContextExecutor, Future}

trait _ActorSystem extends _System with _WithActorSystem {

  private val logger: Logger = Logger(getClass)

  // Actor
  final implicit val system: ActorSystem = ActorSystem(validSystemName)

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
