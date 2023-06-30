package cassandra;

import akka.stream.alpakka.cassandra.scaladsl.CassandraSession

/**
 * Abstract trait of a system that needs a cassandra session
 */
trait _WithCassandraSystem {
  val cassandraSession: CassandraSession
}
