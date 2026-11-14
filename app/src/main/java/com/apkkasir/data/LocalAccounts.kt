package com.apkkasir.data

import android.util.Base64
import androidx.room.withTransaction
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class LocalAccounts(private val database: KasirDatabase, private val session: LocalSession) {
    private val dao = database.accounts()
    private val random = SecureRandom()
    private val transitions = Mutex()

    suspend fun setup(cashier: String, password: String): String = withContext(Dispatchers.Default) {
        val cashierName = normalize(cashier)
        require(cashierName.isNotBlank()) { "Isi nama masuk kasir." }
        val credential = passwordVerifier(password)
        val code = randomText(24)
        val codeVerifier = verifier(code)
        transitions.withLock {
            session.signedOutGeneration()
            database.withTransaction {
                check(dao.settings() == null && dao.byRole(Role.CASHIER) == null) { "Aplikasi sudah disiapkan." }
                dao.insert(User(username = cashierName, role = Role.CASHIER, credential = credential))
                dao.insertSettings(StoreSettings(recoveryVerifier = codeVerifier))
            }
        }
        code
    }

    suspend fun login(name: String, password: String) = login(name, password) {}

    internal suspend fun login(name: String, password: String, afterVerification: suspend () -> Unit) =
        withContext(Dispatchers.Default) {
            val generation = session.signedOutGeneration()
            val user = dao.cashierByName(normalize(name))
            require(user != null && matches(password, user.credential)) { "Nama masuk atau kata sandi salah." }
            afterVerification()
            transitions.withLock {
                require(dao.byId(user.id)?.credential == user.credential) { "Nama masuk atau kata sandi salah." }
                session.enter(user, generation)
            }
        }

    suspend fun recoverCashier(code: String, password: String): String = withContext(Dispatchers.Default) {
        val generation = session.signedOutGeneration()
        val cashier = checkNotNull(dao.byRole(Role.CASHIER))
        val settings = checkNotNull(dao.settings())
        require(matches(code.trim(), settings.recoveryVerifier)) { "Kode pemulihan salah atau sudah digunakan." }
        val credential = passwordVerifier(password)
        val replacement = randomText(24)
        val replacementVerifier = verifier(replacement)
        transitions.withLock {
            database.withTransaction {
                val current = checkNotNull(dao.settings())
                check(session.signedOutGeneration() == generation &&
                    current.recoveryVerifier == settings.recoveryVerifier &&
                    dao.byId(cashier.id)?.credential == cashier.credential
                ) { "Akses atau kode pemulihan sudah berubah." }
                dao.update(cashier.copy(credential = credential))
                dao.updateSettings(current.copy(recoveryVerifier = replacementVerifier))
            }
        }
        replacement
    }

    suspend fun changePassword(password: String, replacement: String) = withContext(Dispatchers.Default) {
        val actor = session.requireRole(Role.CASHIER)
        val cashier = verifyPassword(actor, password)
        val credential = passwordVerifier(replacement)
        updateAccount(actor, cashier) { dao.update(cashier.copy(credential = credential)) }
    }

    suspend fun rotateCode(password: String): String = withContext(Dispatchers.Default) {
        val actor = session.requireRole(Role.CASHIER)
        val cashier = verifyPassword(actor, password)
        val replacement = randomText(24)
        val replacementVerifier = verifier(replacement)
        updateAccount(actor, cashier) {
            val settings = checkNotNull(dao.settings())
            dao.updateSettings(settings.copy(recoveryVerifier = replacementVerifier))
        }
        replacement
    }

    private suspend fun verifyPassword(actor: AccountSession, password: String): User {
        val cashier = checkNotNull(dao.byId(actor.id))
        require(matches(password, cashier.credential)) { "Kata sandi salah." }
        return cashier
    }

    private suspend fun updateAccount(actor: AccountSession, cashier: User, update: suspend () -> Unit) {
        transitions.withLock {
            database.withTransaction {
                session.checkActive(actor)
                check(dao.byId(actor.id)?.credential == cashier.credential) { "Kredensial sudah berubah." }
                update()
            }
        }
    }

    private fun normalize(name: String): String = name.trim().lowercase(java.util.Locale.ROOT)

    private fun passwordVerifier(password: String): String {
        require(password.length in 8..128) { "Kata sandi harus terdiri dari 8–128 karakter." }
        return verifier(password)
    }

    private fun verifier(value: String): String {
        val salt = ByteArray(16).also(random::nextBytes)
        val iterations = 600_000
        return listOf(iterations.toString(), encode(salt), encode(derive(value, salt, iterations))).joinToString(":")
    }

    private fun matches(value: String, encoded: String): Boolean {
        if (value.length > 128 || !validVerifier(encoded)) return false
        val (iterations, salt, expected) = encoded.split(':')
        return MessageDigest.isEqual(
            derive(value, Base64.decode(salt, Base64.NO_WRAP), iterations.toInt()),
            Base64.decode(expected, Base64.NO_WRAP),
        )
    }

    private fun derive(value: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(value.toCharArray(), salt, iterations, 256)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun encode(bytes: ByteArray) = Base64.encodeToString(bytes, Base64.NO_WRAP)
    private fun randomText(size: Int) = Base64.encodeToString(
        ByteArray(size).also(random::nextBytes),
        Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING,
    )
}

internal fun validVerifier(encoded: String): Boolean {
    val fields = encoded.split(':', limit = 4)
    if (fields.size != 3 || fields[0] != "600000") return false
    return try {
        Base64.decode(fields[1], Base64.NO_WRAP).size == 16 &&
            Base64.decode(fields[2], Base64.NO_WRAP).size == 32
    } catch (_: IllegalArgumentException) {
        false
    }
}
