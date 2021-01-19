package com.my.moneymanager

import com.my.moneymanager.xutil.MyUtil
import org.junit.Assert.assertEquals
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
    fun test() {
        val str = "20201211-100"
        val arr = MyUtil.getDateDescAndMoneyArrayByRegex(str)
        assertEquals("20201211", arr[0])
        assertEquals("", arr[1])
        assertEquals("-100", arr[2])
    }
}