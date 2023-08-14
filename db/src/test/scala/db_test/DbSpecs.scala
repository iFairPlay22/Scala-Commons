package db_test

import akka.Done
import db._DbTestSystem
import db_test.model.{Person, PersonRepository, PersonTable}
import commons.actor._ActorSystem
import db_test.model.PersonTable.PersonData
import org.scalatest.matchers.should.Matchers
import org.scalatest.concurrent.ScalaFutures

import slick.jdbc.H2Profile.api._
import scala.concurrent.Future

class DbSpecs extends _ActorSystem with _DbTestSystem {

  private val personRepository: PersonRepository = new PersonRepository()

  f"return empty persons list" in {

    val persons = await(personRepository.selectAllByAge(0))
    persons shouldBe Seq()

  }

  f"return persons list of length 1" in {

    await(personRepository.insertOrEdit(Person(1, "Joe", "Smith", 42)))

    val vehicles = await(personRepository.selectAllByAge(42))
    vehicles shouldBe Seq(Person(1, "Joe", "Smith", 42))

  }

  f"return vehicles list of length 2, with same ages" in {

    await(personRepository.insertOrEdit(Person(1, "Joe", "Smith", 42)))
    await(personRepository.insertOrEdit(Person(2, "Daniel", "Smith", 42)))

    val vehicles = await(personRepository.selectAllByAge(42))
    vehicles shouldBe Seq(Person(1, "Joe", "Smith", 42), Person(2, "Daniel", "Smith", 42))

  }

  f"return vehicles list of length 2, with different ages" in {

    await(personRepository.insertOrEdit(Person(1, "Joe", "Smith", 42)))
    await(personRepository.insertOrEdit(Person(2, "Daniel", "Smith", 42)))
    await(personRepository.insertOrEdit(Person(3, "Philipp", "Wright", 63)))

    val vehicles = await(personRepository.selectAllByAge(42))
    vehicles shouldBe Seq(Person(1, "Joe", "Smith", 42), Person(2, "Daniel", "Smith", 42))

  }

  override def reset(): Future[Any] = {
    db.run(
      DBIO
        .sequence(Seq(PersonData.schema.dropIfExists, PersonData.schema.createIfNotExists)))
  }
  override def clean(): Future[Any] =
    db.run(PersonData.schema.dropIfExists)
}
