package db;

import akka.stream.alpakka.slick.scaladsl.SlickSession
import slick.jdbc.JdbcBackend

/**
 * Abstract trait of a system that needs a DB session
 */
trait _WithDbSystem {
  val dbSession: SlickSession
  implicit lazy val db: JdbcBackend#DatabaseDef = dbSession.db
}
