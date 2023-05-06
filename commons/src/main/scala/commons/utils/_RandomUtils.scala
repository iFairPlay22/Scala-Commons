package commons.utils

import scala.collection.mutable

trait _RandomUtils {
  private val rand = new scala.util.Random

  def randomInt(start: Int = 0, end: Int = Int.MaxValue - 1): Int =
    rand.between(start, end + 1)

  def randomBool(): Boolean =
    rand.nextBoolean()

  def randomString(length: Int) =
    rand.alphanumeric.take(length).mkString
}
