package commons.system.scheduler

import akka.Done
import akka.actor.Cancellable
import com.typesafe.scalalogging.Logger
import commons.exceptions.{_AlreadyStartedSchedulerException, _AlreadyStoppedSchedulerException, _NotStartedSchedulerException, _UnableToStopSchedulerException}
import commons.system.actor._WithActorSystem

import java.time.Duration
import scala.concurrent.Future
abstract class _SchedulerSystem extends _WithActorSystem {

  private val logger: Logger = Logger(getClass)

  // Settings
  val initialDelay: Duration
  val refreshDelay: Duration

  // Scheduler
  private var scheduler: Option[Cancellable] = None

  val action: Unit => Future[Done]

  def startScheduler(): Future[Done] =
    scheduler match {
      case Some(_) =>
        throw new _AlreadyStartedSchedulerException()
      case None =>
        logger.info(
          f"Starting producer scheduler with initialDelay=$initialDelay s and refreshDelay=$refreshDelay")

        scheduler = Some(
          system.scheduler
            .scheduleAtFixedRate(
              initialDelay,
              refreshDelay,
              () =>
                Future
                  .successful()
                  .andThen { _ =>
                    logger.info("Starting data injection task in producer")
                  }
                  .flatMap(action)
                  .andThen { _ =>
                    logger.info("Ending data injection task in producer")
                  },
              executor))

        Future.successful(Done)
    }

  private var stopped: Boolean = false

  def stopScheduler(): Future[Done] =
    scheduler match {
      case None =>
        throw new _NotStartedSchedulerException()
      case Some(s) =>
        if (!stopped) {
          logger.info("Stopping scheduler")
          if (s.cancel()) {
            logger.info("Stopping scheduler")
            stopped = true
            Future.successful(Done)
          } else {
            logger.info("Unable to stop scheduler")
            throw new _UnableToStopSchedulerException()
          }
        } else {
          throw new _AlreadyStoppedSchedulerException()
        }
    }

}
