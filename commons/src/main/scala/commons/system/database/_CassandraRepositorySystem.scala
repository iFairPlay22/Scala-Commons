package commons.system.database

import akka.stream.alpakka.cassandra.scaladsl.CassandraSource
import akka.stream.scaladsl.{Sink, Source}
import akka.{Done, NotUsed}
import com.datastax.oss.driver.api.core.cql.Row
import commons.system.actor._WithActorSystem

import scala.concurrent.Future

trait _CassandraRepositorySystem extends _WithActorSystem with _WithCassandraSystem {

  private def createSource(stmt: String, params: List[Any]): Source[Row, NotUsed] =
    CassandraSource(stmt, params.map(e => e.asInstanceOf[AnyRef]): _*)(session)

  protected def queryToEmptyResult(stmt: String, params: List[Any]): Future[Done] =
    createSource(stmt, params)
      .runWith(Sink.ignore)

  protected def queryToSingleResult[T](
      stmt: String,
      params: List[Any],
      castFunction: Row => T): Future[T] =
    createSource(stmt, params)
      .runWith(Sink.head)
      .map(castFunction)

  protected def queryToOptionalSingleResult[T](
      stmt: String,
      params: List[Any],
      castFunction: Row => T): Future[Option[T]] =
    createSource(stmt, params)
      .runWith(Sink.headOption)
      .map { row =>
        if (row.nonEmpty) {
          Some(castFunction(row.get))
        } else {
          None
        }
      }

  protected def queryToMultipleResult[T](
      stmt: String,
      params: List[Any],
      castFunction: Row => T): Future[Seq[T]] =
    createSource(stmt, params)
      .runWith(Sink.seq)
      .map(_.map(castFunction))
}
