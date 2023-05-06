package commons.system.database

import akka.Done
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.alpakka.cassandra.scaladsl.CassandraSession
import com.typesafe.scalalogging.Logger
import commons.exceptions._AlreadyStoppedCassandraSessionException
import io.circe
import io.circe.Decoder
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

import scala.concurrent.duration._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.io.Source
import scala.language.postfixOps

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
      session: CassandraSession,
      executor: ExecutionContext): Future[Done] =
    dropKeyspace(keyspace)
      .flatMap(_ => session.executeDDL(createKeyspaceIfNotExistsQuery(keyspace)))
      .flatMap(_ => session.executeDDL(useKeyspaceQuery(keyspace)))
      .flatMap(_ => session.executeDDL(schema))

  private def dropKeyspace(keyspace: String)(implicit
      session: CassandraSession,
      executor: ExecutionContext): Future[Done] =
    session.executeDDL(dropKeyspaceIfExistsQuery(keyspace))

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
    with ScalatestRouteTest
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with _CassandraSystemForTests {

  final implicit val awaitDuration: Duration = 2 minutes
  override implicit lazy val executor: ExecutionContextExecutor = system.dispatcher

  def decodeResponse[T: Decoder](resp: String): T = {
    val eitherErrorOrT = circe.jawn
      .decode[T](resp)

    eitherErrorOrT match {
      case Right(r) => r
      case Left(err) =>
        failTest("Unable to decode API response")
        throw err
    }
  }

  def await[T](f: Future[T]): T =
    Await.result(f, awaitDuration)

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
