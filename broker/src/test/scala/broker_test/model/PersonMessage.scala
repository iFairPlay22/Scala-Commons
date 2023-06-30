package broker_test.model

case class PersonId(id: Int)

case class Person(name: String, age: Int)

case class PersonMessage(key: PersonId, message: Person)

object PersonMessage {
  val samples: List[PersonMessage] = List(
    PersonMessage(PersonId(1), Person("John", 30)),
    PersonMessage(PersonId(1), Person("Eric", 40)),
    PersonMessage(PersonId(1), Person("Bob", 60)))
}


