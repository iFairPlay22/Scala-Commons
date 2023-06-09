package cassandra;

import akka.stream.alpakka.cassandra.scaladsl.CassandraSource
import akka.stream.scaladsl.{Sink, Source}
import akka.{Done, NotUsed}
import com.datastax.oss.driver.api.core.cql.Row
import commons.actor._WithActorSystem

import scala.concurrent.Future

/**
 * System that provides few methods to use cassandra queries
 */
trait _CassandraRepositorySystem extends _WithActorSystem with _WithCassandraSystem {

  private def createSource(stmt: String, params: List[Any]): Source[Row, NotUsed] =
    CassandraSource(stmt, params.map(e => e.asInstanceOf[AnyRef]): _*)(cassandraSession)

  protected def queryToEmptyResult(stmt: String, params: List[Any]): Future[Done] =
    handle(
      createSource(stmt, params)
        .runWith(Sink.ignore))

  protected def queryToSingleResult[T](
      stmt: String,
      params: List[Any],
      castFunction: Row => T): Future[T] =
    handle(
      createSource(stmt, params)
        .runWith(Sink.head)
        .map(castFunction))

  protected def queryToOptionalSingleResult[T](
      stmt: String,
      params: List[Any],
      castFunction: Row => T): Future[Option[T]] =
    handle(
      createSource(stmt, params)
        .runWith(Sink.headOption)
        .map { row =>
          if (row.nonEmpty) {
            Some(castFunction(row.get))
          } else {
            None
          }
        })

  protected def queryToMultipleResult[T](
      stmt: String,
      params: List[Any],
      castFunction: Row => T): Future[Seq[T]] =
    handle(
      createSource(stmt, params)
        .runWith(Sink.seq)
        .map(_.map(castFunction)))

  private def handle[T](source: Future[T]): Future[T] =
    source
      .recover(err => {
        err.printStackTrace()
        throw err
      })
}
