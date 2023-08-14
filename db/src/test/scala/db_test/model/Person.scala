package db_test.model

import slick.jdbc.H2Profile.api._

case class Person(id: Int, firstName: String, lastName: String, age: Int)

class PersonTable(tag: Tag) extends Table[Person](tag, "PERSONS") {

  def id = column[Int]("id", O.PrimaryKey)
  def firstName = column[String]("first_name")
  def lastName = column[String]("last_name")
  def age = column[Int]("age")
  def * = (id, firstName, lastName, age) <> (Person.tupled, Person.unapply)

}

object PersonTable {
  val PersonData = TableQuery[PersonTable]
}
