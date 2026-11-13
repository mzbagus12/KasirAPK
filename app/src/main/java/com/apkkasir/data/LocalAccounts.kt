package com.apkkasir.data

import android.util.Base64
import androidx.room.withTransaction
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalAccounts(private val database: KasirDatabase, private val session: LocalSession) {
    private val dao = database.accounts()
    private val random = SecureRandom()

    suspend fun isConfigured(): Boolean = dao.settings() != null

    suspend fun setup(admin: String, adminPassword: String, cashier: String, cashierPassword: String): String =
        withContext(Dispatchers.Default) {
            val adminName = normalize(admin)
            val cashierName = normalize(cashier)
            require(adminName.isNotBlank() && cashierName.isNotBlank() && adminName != cashierName) {
                "Nama masuk admin dan kasir harus terisi dan berbeda."
            }
            val adminVerifier = passwordVerifier(adminPassword)
            val cashierVerifier = passwordVerifier(cashierPassword)
            val code = randomText(24)
            val codeVerifier = verifier(code)
            database.withTransaction {
                check(dao.settings() == null) { "Aplikasi sudah disiapkan." }
                dao.insert(User(username = adminName, role = Role.ADMIN, credential = adminVerifier))
                dao.insert(User(username = cashierName, role = Role.CASHIER, credential = cashierVerifier))
                dao.insertSettings(StoreSettings(recoveryVerifier = codeVerifier))
            }
            code
        }

    suspend fun login(name: String, password: String) = withContext(Dispatchers.Default) {
        val user = dao.byName(normalize(name))
        require(user != null && matches(password, user.credential)) { "Nama masuk atau kata sandi salah." }
        session.enter(user)
    }

    suspend fun resetCashier(password: String) = withContext(Dispatchers.Default) {
        val actor = session.requireRole(Role.ADMIN)
        val credential = passwordVerifier(password)
        database.withTransaction {
            session.checkActive(actor)
            val cashier = checkNotNull(dao.byRole(Role.CASHIER))
            dao.update(cashier.copy(credential = credential))
        }
    }

    suspend fun recoverAdmin(code: String, password: String): String = withContext(Dispatchers.Default) {
        check(session.account.value == null) { "Keluar sebelum memulihkan akun." }
        val settings = checkNotNull(dao.settings())
        require(matches(code.trim(), settings.recoveryVerifier)) { "Kode pemulihan salah atau sudah digunakan." }
        val credential = passwordVerifier(password)
        val replacement = randomText(24)
        val replacementVerifier = verifier(replacement)
        database.withTransaction {
            val current = checkNotNull(dao.settings())
            check(session.account.value == null && current.recoveryVerifier == settings.recoveryVerifier) {
                "Kode pemulihan sudah berubah."
            }
            val admin = checkNotNull(dao.byRole(Role.ADMIN))
            dao.update(admin.copy(credential = credential))
            dao.updateSettings(current.copy(recoveryVerifier = replacementVerifier))
        }
        replacement
    }

    suspend fun rotateCode(password: String): String = withContext(Dispatchers.Default) {
        val actor = session.requireRole(Role.ADMIN)
        val admin = checkNotNull(dao.byId(actor.id))
        require(matches(password, admin.credential)) { "Kata sandi salah." }
        val replacement = randomText(24)
        val replacementVerifier = verifier(replacement)
        database.withTransaction {
            session.checkActive(actor)
            check(dao.byId(actor.id)?.credential == admin.credential) { "Kredensial sudah berubah." }
            val settings = checkNotNull(dao.settings())
            dao.updateSettings(settings.copy(recoveryVerifier = replacementVerifier))
        }
        replacement
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
        if (value.length > 128) return false
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
