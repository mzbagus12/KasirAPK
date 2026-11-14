package com.apkkasir.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.apkkasir.data.AppContainer
import com.apkkasir.data.StoreOpening
import com.apkkasir.data.StoreReadiness

@Composable
internal fun AccountScreen(app: AppContainer) {
    var opening by remember { mutableStateOf<StoreOpening?>(null) }
    var recovering by remember { mutableStateOf(false) }
    var code by remember { mutableStateOf<String?>(null) }
    var cashier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var recoveryInput by remember { mutableStateOf("") }
    val action = rememberAction()
    LaunchedEffect(app) { opening = app.inspectStore() }
    Page(when {
        code != null -> "Kode pemulihan"
        opening?.readiness == StoreReadiness.EMPTY -> "Siapkan toko"
        opening?.readiness == StoreReadiness.HELD -> "Pembaruan ditahan"
        recovering -> "Pulihkan akun kasir"
        else -> "Masuk ke Kasir Toko"
    }, form = true) {
        when {
            code != null -> RecoveryCode(code.orEmpty()) { code = null }
            opening == null -> CircularProgressIndicator()
            opening?.readiness == StoreReadiness.HELD -> Text(opening?.message.orEmpty())
            opening?.readiness == StoreReadiness.RETRY -> {
                Text(opening?.message.orEmpty())
                Button(enabled = !action.busy, onClick = {
                    action.run { opening = app.inspectStore() }
                }) { Text("Coba lagi") }
            }
            opening?.readiness == StoreReadiness.EMPTY -> {
                AccountName("Nama masuk kasir", cashier) { cashier = it }
                PasswordField("Kata sandi kasir", password) { password = it }
                Button(enabled = !action.busy, modifier = Modifier.fillMaxWidth(), onClick = {
                    action.run {
                        code = app.accounts.setup(cashier, password)
                        opening = StoreOpening(StoreReadiness.READY)
                        password = ""
                    }
                }) { Text("Simpan akun") }
            }
            recovering -> {
                PasswordField("Kode pemulihan", recoveryInput) { recoveryInput = it }
                PasswordField("Kata sandi kasir baru", password) { password = it }
                Button(enabled = !action.busy, modifier = Modifier.fillMaxWidth(), onClick = {
                    action.run {
                        code = app.accounts.recoverCashier(recoveryInput, password)
                        recovering = false
                        password = ""
                        recoveryInput = ""
                    }
                }) { Text("Pulihkan akses") }
                TextButton(enabled = !action.busy, onClick = {
                    recovering = false
                    password = ""
                    recoveryInput = ""
                    action.error = null
                }) { Text("Kembali ke masuk") }
            }
            else -> {
                AccountName("Nama masuk", cashier) { cashier = it }
                PasswordField("Kata sandi", password) { password = it }
                Button(enabled = !action.busy, modifier = Modifier.fillMaxWidth(),
                    onClick = { action.run { app.accounts.login(cashier, password) } }) {
                    Text("Masuk")
                }
                TextButton(enabled = !action.busy, onClick = {
                    recovering = true
                    password = ""
                    action.error = null
                }) { Text("Lupa kata sandi") }
            }
        }
        if (action.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        ErrorText(action.error)
    }
}

@Composable
internal fun AccessSettingsScreen(app: AppContainer, back: () -> Unit) {
    var cashierPassword by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var recoveryPassword by remember { mutableStateOf("") }
    var code by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val action = rememberAction()
    Page("Akun dan pemulihan", back) {
        Text("Ganti kata sandi", style = MaterialTheme.typography.titleMedium)
        PasswordField("Kata sandi saat ini", currentPassword) { currentPassword = it }
        PasswordField("Kata sandi kasir baru", cashierPassword) { cashierPassword = it }
        Button(enabled = !action.busy, onClick = {
            action.run {
                app.accounts.changePassword(currentPassword, cashierPassword)
                currentPassword = ""
                cashierPassword = ""
                message = "Kata sandi kasir diperbarui."
            }
        }) { Text("Simpan kata sandi kasir") }
        HorizontalDivider()
        Text("Kode pemulihan", style = MaterialTheme.typography.titleMedium)
        Text("Membuat kode baru menonaktifkan kode lama.")
        PasswordField("Kata sandi untuk kode pemulihan", recoveryPassword) { recoveryPassword = it }
        Button(enabled = !action.busy, onClick = {
            action.run {
                code = app.accounts.rotateCode(recoveryPassword)
                recoveryPassword = ""
            }
        }) { Text("Buat kode pemulihan baru") }
        code?.let { RecoveryCode(it) { code = null } }
        message?.let { Text(it) }
        ErrorText(action.error)
    }
}

@Composable
private fun AccountName(label: String, value: String, change: (String) -> Unit) {
    OutlinedTextField(value, change, modifier = Modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true)
}

@Composable
private fun PasswordField(label: String, value: String, change: (String) -> Unit) {
    OutlinedTextField(
        value, change, modifier = Modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    )
}

@Composable
private fun RecoveryCode(code: String, done: () -> Unit) {
    Text("Simpan kode ini di tempat aman. Kode ini hanya bisa dipakai satu kali.")
    androidx.compose.foundation.text.selection.SelectionContainer {
        Text(code, style = MaterialTheme.typography.titleMedium)
    }
    Button(onClick = done) { Text("Selesai") }
}
