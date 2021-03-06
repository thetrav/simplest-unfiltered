package the.trav

// Use H2Driver to connect to an H2 database
import scala.slick.driver.H2Driver.simple._

object DB {

  def withDatabase(program: PersonStore => Unit) {
    Database.forURL("jdbc:h2:mem:test1", driver = "org.h2.Driver") withSession { implicit session =>
      class People(tag: Tag) extends Table[(String, Int)](tag, "PEOPLE") {
        def name = column[String]("NAME")
        def years = column[Int]("YEARS")
        // Every table needs a * projection with the same type as the table's type parameter
        def * = (name, years)
      }
      val people = TableQuery[People]

      people.ddl.create

      people += ("first person", 1)
      val personStore = new PersonStore {
        def all: List[Person] = {
          people.list.map(Person.tupled)
        }

        def add(person: Person) {
          people += (person.name, person.years)
        }

        def delete(name: String) {
          people.filter(_.name.like(name)).delete
        }
      }
      program(personStore)
    }
  }

  trait PersonStore {
    def all: List[Person]
    def add(person: Person): Unit
    def delete(name: String): Unit
  }
}
