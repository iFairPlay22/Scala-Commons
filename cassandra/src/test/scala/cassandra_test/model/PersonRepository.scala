package cassandra_test.model

import akka.Done
import akka.actor.ActorSystem
import akka.stream.alpakka.cassandra.scaladsl.CassandraSession
import cassandra._CassandraRepositorySystem
import com.datastax.oss.driver.api.core.cql.Row

import scala.concurrent.Future
import PersonKeyspace._

class PersonRepository(
    implicit val system: ActorSystem,
    implicit val cassandraSession: CassandraSession)
    extends _CassandraRepositorySystem {

  import PersonRepository._

  def insertOrEdit(person: Person): Future[Done] =
    queryToEmptyResult(insertOrEditQuery, List(person.firstName, person.lastName, person.age))

  def selectAllByAge(age: Int): Future[Seq[Person]] =
    queryToMultipleResult(selectByAgeQuery, List(age), mapRowToEntity)

  private def mapRowToEntity(row: Row): Person =
    Person(
      row.getString(table_persons_field_first_name),
      row.getString(table_persons_field_last_name),
      row.getInt(table_persons_field_age))
}

object PersonRepository {

  private val insertOrEditQuery: String =
    f"""
       > INSERT INTO $table_persons_name
       > ($table_persons_field_first_name, $table_persons_field_last_name, $table_persons_field_age)
       > VALUES (?, ?, ?);
       > """.stripMargin('>')

  private val selectByAgeQuery: String =
    f"""
       > SELECT * FROM $table_persons_name WHERE $table_persons_field_age = ?;
       > """.stripMargin('>')
}
