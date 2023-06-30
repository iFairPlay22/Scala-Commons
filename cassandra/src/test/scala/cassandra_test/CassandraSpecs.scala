package cassandra_test

import cassandra._CassandraTestSystem
import cassandra_test.model.{Person, PersonRepository}
import commons.actor._ActorSystem
import org.scalatest.matchers.should.Matchers
import org.scalatest.concurrent.ScalaFutures
class CassandraSpecs
    extends _ActorSystem
    with _CassandraTestSystem
    with Matchers
    with ScalaFutures {

  private val personRepository: PersonRepository = new PersonRepository()

  f"return empty persons list" in {

    val persons = await(personRepository.selectAllByAge(0))
    persons shouldBe Seq()

  }

  f"return persons list of length 1" in {

    await(personRepository.insertOrEdit(Person("Joe", "Smith", 42)))

    val vehicles = await(personRepository.selectAllByAge(42))
    vehicles shouldBe Seq(Person("Joe", "Smith", 42))

  }

  f"return vehicles list of length 2, with same ages" in {

    await(personRepository.insertOrEdit(Person("Joe", "Smith", 42)))
    await(personRepository.insertOrEdit(Person("Daniel", "Smith", 42)))

    val vehicles = await(personRepository.selectAllByAge(42))
    vehicles shouldBe Seq(Person("Daniel", "Smith", 42), Person("Joe", "Smith", 42))

  }

  f"return vehicles list of length 2, with different ages" in {

    await(personRepository.insertOrEdit(Person("Joe", "Smith", 42)))
    await(personRepository.insertOrEdit(Person("Daniel", "Smith", 42)))
    await(personRepository.insertOrEdit(Person("Philipp", "Wright", 63)))

    val vehicles = await(personRepository.selectAllByAge(42))
    vehicles shouldBe Seq(Person("Daniel", "Smith", 42), Person("Joe", "Smith", 42))

  }

  f"return vehicles list of length 1, with duplicates" in {

    await(personRepository.insertOrEdit(Person("Joe", "Smith", 42)))
    await(personRepository.insertOrEdit(Person("Joe", "Smith", 42)))
    await(personRepository.insertOrEdit(Person("Joe", "Smith", 42)))

    val vehicles = await(personRepository.selectAllByAge(42))
    vehicles shouldBe Seq(Person("Joe", "Smith", 42))

  }
}
