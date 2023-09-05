package db

import akka.Done
import akka.stream.alpakka.slick.scaladsl._
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import commons.actor._WithActorSystem
import commons.exceptions._AlreadyStoppedDbSessionException
import org.slf4j.LoggerFactory
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.Future

/**
 * System that provides a msql session, related to the current configuration
 * (datastax-java-driver.basic)
 */
trait _DbSystem extends _WithActorSystem with _WithDbSystem {

  private val logger = LoggerFactory.getLogger(getClass)
  private val dbConfig = ConfigFactory.load().getConfig("db")

  logger.info(
    f"Starting session with driver ${dbConfig.getString("driver")} referring to ${dbConfig
        .getString("properties.serverName")}:${dbConfig.getString("properties.portNumber")}")

  implicit override final val dbSession: SlickSession =
    SlickSession.forDbAndProfile(Database.forConfig("db"), slick.jdbc.H2Profile)

  var isDbStopped: Boolean = false;

  def stopDb(): Future[Done] =
    if (isDbStopped) {
      throw new _AlreadyStoppedDbSessionException()
    } else {
      logger.info("Stopping DB session")
      dbSession.close()
      isDbStopped = true
      logger.info("DB session was stopped!")
      Future.successful(Done)
    }
}
