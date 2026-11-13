package com.apkkasir

import com.apkkasir.payment.*
import org.junit.Assert.*
import org.junit.Test

class CheckoutRulesTest {
    @Test fun itemQuantitiesDetermineTotalAndTenderDifference() {
        val total = totalRupiah(listOf(subtotal(12_500, 2), subtotal(8_000, 1)))
        assertEquals(33_000L, total)
        val under = requireNotNull(parseRupiah("30000"))
        val exact = requireNotNull(parseRupiah("33000"))
        val over = requireNotNull(parseRupiah("50000"))
        assertEquals(-3_000L, under - total)
        assertEquals(0L, exact - total)
        assertEquals(17_000L, over - total)
    }
}
