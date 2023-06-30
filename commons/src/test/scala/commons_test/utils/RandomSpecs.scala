package commons_test.utils

import commons.random._RandomUtils
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class RandomSpecs extends AnyWordSpec with Matchers with _RandomUtils {

  "Random utils" should {

    Range
      .inclusive(0, 50)
      .foreach(r => {

        s"produce valid random integers $r" in {
          val min = 0
          val max = 10
          randomInt(min, max) should be >= min
          randomInt(min, max) should be <= max
        }

        s"produce valid random booleans $r" in {
          Array(randomBool()) should contain oneOf (true, false)
        }

        s"produce valid random strings $r" in {
          randomString(5) should have length 5
          randomString(5) should fullyMatch regex """[A-Za-z0-9]+"""
        }

      })

    s"produce valid random integers with same min/max limits" in {
      randomInt(0, 0) shouldBe 0
      randomInt(10, 10) shouldBe 10
    }

    s"produce valid random strings with length of 0" in {
      randomString(0) should have length 0
    }

    s"produce valid random integers with invalid arguments" in {
      assertThrows[IllegalArgumentException] {
        randomInt(-3, -1)
      }
      assertThrows[IllegalArgumentException] {
        randomInt(-3, 0)
      }
      assertThrows[IllegalArgumentException] {
        randomInt(-3, 1)
      }
      assertThrows[IllegalArgumentException] {
        randomInt(15, 10)
      }
    }

    s"produce valid random strings with invalid arguments" in {
      assertThrows[IllegalArgumentException] {
        randomString(-1) should have length 0
      }
      assertThrows[IllegalArgumentException] {
        randomString(-10) should have length 0
      }
    }

  }

}
