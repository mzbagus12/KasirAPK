package com.apkkasir.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AccountSession internal constructor(val id: Long, val role: Role, val username: String)

class LocalSession {
    private val current = MutableStateFlow<AccountSession?>(null)
    val account = current.asStateFlow()

    internal fun enter(user: User) {
        current.value = AccountSession(user.id, user.role, user.username)
    }

    fun exit() {
        current.value = null
    }

    fun requireRole(role: Role): AccountSession =
        current.value?.takeIf { it.role == role } ?: error("Akses tidak diizinkan.")

    fun checkActive(expected: AccountSession) {
        check(current.value === expected) { "Sesi telah berakhir." }
    }
}
