package db_test.model

import akka.Done
import akka.actor.ActorSystem
import db_test.model.PersonTable.PersonData
import db._DbSystem
import slick.jdbc.H2Profile.api._

import scala.concurrent.Future

class PersonRepository(implicit val system: ActorSystem) extends _DbSystem {
  def insertOrEdit(person: Person): Future[Done] =
    db.run(PersonData += person)
      .map(_ => Done)

  def selectAllByAge(age: Int): Future[Seq[Person]] =
    db.run(PersonData.filter(p => p.age === age).result)
}
