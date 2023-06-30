package commons.random

trait _RandomGen[T] extends _RandomUtils {

  def randomValid(): T

  def randomInvalid(): T

  def randomValid(n: Int): Iterator[T] =
    randomMany(n, randomValid)

  def randomInvalid(n: Int): Iterator[T] =
    randomMany(n, randomInvalid)

  private def randomMany(n: Int, f: () => T): Iterator[T] =
    Iterator(0, n)
      .map(_ => f())

}
