package commons.system.database

import akka.Done
import akka.stream.alpakka.cassandra.CassandraSessionSettings
import akka.stream.alpakka.cassandra.scaladsl.{CassandraSession, CassandraSessionRegistry}
import com.typesafe.scalalogging.Logger
import commons.exceptions._AlreadyStoppedCassandraSessionException
import commons.system.actor._WithActorSystem

import scala.concurrent.Future

trait _CassandraSystem extends _WithActorSystem with _WithCassandraSystem {

  private val logger: Logger = Logger(getClass)

  implicit override final val cassandraSession: CassandraSession =
    CassandraSessionRegistry.get(system).sessionFor(CassandraSessionSettings())

  protected var stopped: Boolean = false;

  def stopCassandra(): Future[Done] =
    if (stopped) {
      throw new _AlreadyStoppedCassandraSessionException()
    } else {
      logger.info("Stopping cassandra session")
      cassandraSession
        .close(executor)
        .andThen(_ => stopped = true)
        .andThen(_ => logger.info("Cassandra session was stopped!"))
    }
}
