package db

import akka.Done
import com.typesafe.scalalogging.Logger
import commons.testing.TestUtils
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

import scala.concurrent.Future
import scala.language.postfixOps

trait _DbTestSystem
    extends AnyWordSpecLike
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with Matchers
    with TestUtils
    with _DbSystem {

  private val logger: Logger = Logger(getClass)

  def reset(): Future[Any]

  def clean(): Future[Any]

  override def beforeEach(): Unit = {
    super.beforeEach()

    logger.info("Resetting DB environment")
    await(reset().andThen(_ => logger.info("DB session was reset!")))
  }

  override def afterAll(): Unit = {
    super.afterAll()

    logger.info("Cleaning DB environment")
    await(clean().andThen(_ => logger.info("DB session was cleaned!")))
  }

}
