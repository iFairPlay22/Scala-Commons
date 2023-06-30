package broker_test.model

class PersonMessageCounter {
  var messages: List[PersonMessage] = List()

  def add(personId: PersonId, person: Person): Unit = {
    messages = messages ++ List(PersonMessage(personId, person))
  }
}
