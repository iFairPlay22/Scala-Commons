package commons.utils

import scala.collection.mutable

trait _RandomUtils {
  private val rand = new scala.util.Random

  def randomInt(start: Int = 0, end: Int = Int.MaxValue - 1): Int = {
    if (start < 0)
      throw new IllegalArgumentException("start must be greater than 0")
    if (end < 0)
      throw new IllegalArgumentException("end must be greater than 0")
    if (end < start)
      throw new IllegalArgumentException("end must be greater than start")
    rand.between(start, end + 1)
  }

  def randomBool(): Boolean =
    rand.nextBoolean()

  def randomString(length: Int) = {
    if (length < 0)
      throw new IllegalArgumentException("length must be greater than 0")
    rand.alphanumeric.take(length).mkString
  }
}
