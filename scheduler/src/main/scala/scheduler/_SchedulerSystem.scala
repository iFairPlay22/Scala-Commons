package scheduler

import akka.Done
import akka.actor.Cancellable
import com.typesafe.scalalogging.Logger
import commons.actor._WithActorSystem
import commons.exceptions.{_AlreadyStartedSchedulerException, _AlreadyStoppedSchedulerException, _NotStartedSchedulerException, _UnableToStopSchedulerException}
import org.slf4j.LoggerFactory

import java.time.Duration
import scala.concurrent.Future
abstract class _SchedulerSystem extends _WithActorSystem {

  private val logger = LoggerFactory.getLogger(getClass)

  // Scheduler configuration
  private var scheduler: Option[Cancellable] = None
  private val initialDelay: Duration =
    config.getDuration("scheduler.initial-delay");
  private val refreshDelay: Duration =
    config.getDuration("scheduler.refresh-delay");
  val action: Unit => Future[Done]

  def startScheduler(): Future[Done] =
    scheduler match {
      case Some(_) =>
        throw new _AlreadyStartedSchedulerException()
      case None =>
        logger.info(
          f"Starting scheduler with initialDelay = ${initialDelay}s and refreshDelay = ${refreshDelay}s")

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
        if (stopped) {
          throw new _AlreadyStoppedSchedulerException()
        } else {
          logger.info("Stopping scheduler")
          if (s.cancel()) {
            logger.info("Scheduler stopped")
            stopped = true
            Future.successful(Done)
          } else {
            logger.info("Unable to stop scheduler")
            throw new _UnableToStopSchedulerException()
          }
        }
    }

}
