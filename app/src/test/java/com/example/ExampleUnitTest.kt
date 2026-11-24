package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testStatutoryLeaveCalculation() {
    // 4857 Sayılı İş Kanunu Madde 53:
    // 1-5 yıl: 14 gün, 6-14 yıl: 20 gün, 15+ yıl: 26 gün
    fun calculateStatutoryLeave(years: Int): Int = when {
        years < 1 -> 0
        years in 1..5 -> 14
        years in 6..14 -> 20
        else -> 26
    }

    assertEquals(0, calculateStatutoryLeave(0))
    assertEquals(14, calculateStatutoryLeave(1))
    assertEquals(14, calculateStatutoryLeave(5))
    assertEquals(20, calculateStatutoryLeave(6))
    assertEquals(20, calculateStatutoryLeave(14))
    assertEquals(26, calculateStatutoryLeave(15))
    assertEquals(26, calculateStatutoryLeave(25))
  }
}
