package com.apkkasir.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apkkasir.data.PaymentMethod
import com.apkkasir.data.Sale
import com.apkkasir.data.SaleStatus
import com.apkkasir.payment.CheckoutService
import com.apkkasir.payment.parseRupiah
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

internal class CheckoutViewModel(
    private val service: CheckoutService,
    initial: Sale?,
) : ViewModel() {
    private data class Edit(val sequence: Long, val id: Long, val method: PaymentMethod, val cash: Long?)

    var sale by mutableStateOf(initial)
        private set
    var method by mutableStateOf(initial?.method)
        private set
    var cashText by mutableStateOf(initial?.cash?.toString().orEmpty())
        private set
    var busy by mutableStateOf(false)
        private set
    var saving by mutableStateOf(false)
        private set
    var saveFailed by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    private var sequence = 0L
    private val edits = Channel<Edit>(Channel.CONFLATED)

    init {
        viewModelScope.launch {
            for (edit in edits) {
                try {
                    val updated = service.updateDraft(edit.id, edit.method, edit.cash)
                    if (edit.sequence == sequence) {
                        sale = updated
                        saving = false
                        saveFailed = false
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (failure: Exception) {
                    if (edit.sequence == sequence) {
                        saving = false
                        saveFailed = true
                        error = failure.message ?: "Perubahan belum tersimpan."
                    }
                }
            }
        }
    }

    fun create(cart: Map<Long, Int>) = action {
        sale = service.create(cart)
    }

    fun select(selected: PaymentMethod) {
        if (busy || selected == method) return
        method = selected
        cashText = ""
        saveDraft()
    }

    fun cash(input: String) {
        if (busy) return
        cashText = input
        saveDraft()
    }

    fun retry() = saveDraft()

    fun canConfirm(qrisAvailable: Boolean): Boolean {
        val bill = sale ?: return false
        if (busy || saving || saveFailed || bill.status != SaleStatus.MENUNGGU || bill.method != method) return false
        return when (method) {
            PaymentMethod.TUNAI -> parseRupiah(cashText)?.let { it >= bill.total && it == bill.cash } == true
            PaymentMethod.QRIS -> qrisAvailable
            null -> false
        }
    }

    fun confirm(qrisAvailable: Boolean, onReceipt: (Long) -> Unit) {
        if (!canConfirm(qrisAvailable)) return
        val bill = sale ?: return
        val selected = method ?: return
        action {
            val receipt = service.confirm(bill.id, selected, bill.cash)
            sale = receipt.sale
            onReceipt(bill.id)
        }
    }

    fun cancel(back: () -> Unit) {
        val bill = sale ?: return
        if (saving) return
        action {
            service.cancel(bill.id)
            back()
        }
    }

    private fun saveDraft() {
        val bill = sale ?: return
        val selected = method ?: return
        saving = true
        saveFailed = false
        error = null
        edits.trySend(Edit(++sequence, bill.id, selected, parseRupiah(cashText)))
    }

    private fun action(work: suspend () -> Unit) {
        if (busy) return
        busy = true
        error = null
        viewModelScope.launch {
            try {
                work()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                error = failure.message ?: "Operasi gagal. Coba lagi."
            } finally {
                busy = false
            }
        }
    }
}
