package cassandra

import akka.Done
import akka.stream.alpakka.cassandra.scaladsl.CassandraSession
import com.typesafe.scalalogging.Logger
import commons.actor._WithActorSystem
import commons.exceptions._AlreadyStoppedCassandraSessionException
import commons.testing.TestUtils
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.io.Source
import scala.language.postfixOps

/**
 * System that provides a cassandra sessions and useful methods for testing purposes
 */
trait _CassandraSystemForTests extends _CassandraSystem {
  import _CassandraSystemForTests._

  private val logger: Logger = Logger(getClass)

  private val keyspace = "test_keyspace"
  private val schema: String = Source.fromResource("cassandra-init.cql").getLines().mkString("\n")

  def resetCassandraEnvironment(): Future[Done] = {
    logger.info("Resetting cassandra environment")
    resetKeyspace(keyspace, schema)
      .andThen(_ => logger.info("Cassandra session was reset!"))
  }

  override def stopCassandra(): Future[Done] =
    if (!stopped) {
      dropKeyspace(keyspace)
        .andThen(_ => super.stopCassandra())
    } else {
      throw new _AlreadyStoppedCassandraSessionException()
    }
}

object _CassandraSystemForTests {

  private def resetKeyspace(keyspace: String, schema: String)(implicit
      cassandraSession: CassandraSession,
      executor: ExecutionContext): Future[Done] =
    dropKeyspace(keyspace)
      .flatMap(_ => cassandraSession.executeDDL(createKeyspaceIfNotExistsQuery(keyspace)))
      .flatMap(_ => cassandraSession.executeDDL(useKeyspaceQuery(keyspace)))
      .flatMap(_ => cassandraSession.executeDDL(schema))

  private def dropKeyspace(keyspace: String)(implicit
      cassandraSession: CassandraSession,
      executor: ExecutionContext): Future[Done] =
    cassandraSession.executeDDL(dropKeyspaceIfExistsQuery(keyspace))

  private def createKeyspaceIfNotExistsQuery(keyspace: String): String =
    f"""
       > CREATE KEYSPACE IF NOT EXISTS $keyspace WITH REPLICATION = {
       >    'class' : 'SimpleStrategy',
       >    'replication_factor' : 1
       > };
       >""".stripMargin('>')

  private def useKeyspaceQuery(keyspace: String): String =
    f"""
       > USE $keyspace;
       >""".stripMargin('>')

  private def dropKeyspaceIfExistsQuery(keyspace: String): String =
    f"""
       > DROP KEYSPACE IF EXISTS $keyspace;
       >""".stripMargin('>')

}

trait _CassandraTestSystem
    extends AnyWordSpecLike
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with _CassandraSystemForTests
    with TestUtils {

  override def beforeAll(): Unit = {
    super.beforeAll()
    await(resetCassandraEnvironment())
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(resetCassandraEnvironment())
  }

  override def afterEach(): Unit = {
    super.afterEach()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    await(stopCassandra())
  }

}
