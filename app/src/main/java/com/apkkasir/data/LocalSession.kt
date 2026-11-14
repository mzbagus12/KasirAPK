package com.apkkasir.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AccountSession internal constructor(val id: Long, val role: Role, val username: String)

class LocalSession {
    private val current = MutableStateFlow<AccountSession?>(null)
    val account = current.asStateFlow()

    private var generation = 0L

    @Synchronized
    internal fun signedOutGeneration(): Long {
        check(current.value == null) { "Keluar sebelum mengganti akses." }
        return generation
    }

    @Synchronized
    internal fun enter(user: User, expectedGeneration: Long) {
        check(user.role == Role.CASHIER && current.value == null && generation == expectedGeneration) {
            "Sesi telah berubah. Coba masuk kembali."
        }
        generation++
        current.value = AccountSession(user.id, user.role, user.username)
    }

    @Synchronized
    fun exit() {
        generation++
        current.value = null
    }

    fun requireRole(role: Role): AccountSession =
        current.value?.takeIf { it.role == role } ?: error("Akses tidak diizinkan.")

    fun checkActive(expected: AccountSession) {
        check(current.value === expected) { "Sesi telah berakhir." }
    }
}
