package commons.system.database

import akka.stream.alpakka.cassandra.scaladsl.CassandraSession

trait _WithCassandraSystem {
  val session: CassandraSession
}
