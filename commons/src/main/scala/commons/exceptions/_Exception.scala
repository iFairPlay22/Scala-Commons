package commons.exceptions

abstract class _Exception(msg: String) extends Exception(msg)

// Actor
class _AlreadyStoppedActorException extends _Exception("Actor was already stopped...")

// Cassandra
class _AlreadyStoppedCassandraSessionException
    extends _Exception("Cassandra session was already stopped...")

// Server
class _AlreadyStartedServerException extends _Exception("Server was already started...")
class _UnableToStartServerException extends _Exception("Unable to start server...")
class _NotStartedServerException extends _Exception("Server was not started yet...")
class _AlreadyStoppedServerException extends _Exception("Server was already stopped...")

// Scheduler
class _NotStartedSchedulerException extends _Exception("Scheduler was not started yet...")
class _AlreadyStartedSchedulerException extends _Exception("Scheduler was already started...")
class _UnableToStopSchedulerException extends _Exception("Unable to stop scheduler...")
class _AlreadyStoppedSchedulerException extends _Exception("Scheduler was already stopped...")

// Broker consumer

class _NotYetStartedBrokerConsumerException
    extends _Exception("Broker consumer was not started yet...") {}
class _AlreadyStartedBrokerConsumerException
    extends _Exception("Broker consumer was already started...") {}
class _UnableToLaunchBrokerConsumerException
    extends _Exception("Unable to launch broker consumer...") {}

class _AlreadyStoppedBrokerConsumerException
    extends _Exception("Broker consumer was already stopped...")

// Broker producer

class _NotYetStartedBrokerProducerException
    extends _Exception("Broker producer was not started yet...") {}
class _AlreadyStartedBrokerProducerException
    extends _Exception("Broker producer was already started...") {}
class _UnableToProduceInBrokerException extends _Exception("Unable to produce in broker...") {}
class _AlreadyStoppedBrokerProducerException
    extends _Exception("Broker producer was already stopped...") {}
