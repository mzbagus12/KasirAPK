package com.apkkasir.payment

import java.math.BigInteger
import java.text.NumberFormat
import java.util.Locale

fun parseRupiah(input: String): Long? =
    input.takeIf { it.isNotEmpty() && it.all { char -> char in '0'..'9' } }?.toLongOrNull()

fun subtotal(price: Long, quantity: Int): Long {
    require(price > 0 && quantity > 0) { "Harga dan jumlah harus lebih dari nol." }
    return Math.multiplyExact(price, quantity.toLong())
}

fun totalRupiah(amounts: Iterable<Long>): Long = amounts.fold(0L, Math::addExact)

fun formatRupiah(amount: Long): String =
    "Rp" + NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID")).format(amount)

fun formatRupiah(amount: BigInteger): String =
    "Rp" + NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID")).format(amount)
