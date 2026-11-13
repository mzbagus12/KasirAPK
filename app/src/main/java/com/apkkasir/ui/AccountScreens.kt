package com.apkkasir.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.apkkasir.data.AppContainer

@Composable
internal fun AccountScreen(app: AppContainer) {
    var configured by remember { mutableStateOf<Boolean?>(null) }
    var recovering by remember { mutableStateOf(false) }
    var code by remember { mutableStateOf<String?>(null) }
    var admin by remember { mutableStateOf("") }
    var cashier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var cashierPassword by remember { mutableStateOf("") }
    var recoveryInput by remember { mutableStateOf("") }
    val action = rememberAction()
    LaunchedEffect(app) { action.run { configured = app.accounts.isConfigured() } }
    Page(when {
        code != null -> "Kode pemulihan"
        configured == false -> "Siapkan toko"
        recovering -> "Pulihkan akun admin"
        else -> "Masuk ke Kasir Toko"
    }, form = true) {
        when {
            code != null -> RecoveryCode(code.orEmpty()) { code = null }
            configured == null -> {
                if (action.busy) CircularProgressIndicator()
                else Button(onClick = { action.run { configured = app.accounts.isConfigured() } }) { Text("Coba lagi") }
            }
            configured == false -> {
                Text("Buat akun admin dan satu akun kasir di HP ini.")
                AccountName("Nama masuk admin", admin) { admin = it }
                PasswordField("Kata sandi admin", password) { password = it }
                AccountName("Nama masuk kasir", cashier) { cashier = it }
                PasswordField("Kata sandi kasir", cashierPassword) { cashierPassword = it }
                Button(enabled = !action.busy, modifier = Modifier.fillMaxWidth(), onClick = {
                    action.run {
                        code = app.accounts.setup(admin, password, cashier, cashierPassword)
                        configured = true
                        password = ""
                        cashierPassword = ""
                    }
                }) { Text("Simpan akun") }
            }
            recovering -> {
                PasswordField("Kode pemulihan", recoveryInput) { recoveryInput = it }
                PasswordField("Kata sandi admin baru", password) { password = it }
                Button(enabled = !action.busy, modifier = Modifier.fillMaxWidth(), onClick = {
                    action.run {
                        code = app.accounts.recoverAdmin(recoveryInput, password)
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
                AccountName("Nama masuk", admin) { admin = it }
                PasswordField("Kata sandi", password) { password = it }
                Button(enabled = !action.busy, modifier = Modifier.fillMaxWidth(),
                    onClick = { action.run { app.accounts.login(admin, password) } }) {
                    Text("Masuk")
                }
                TextButton(enabled = !action.busy, onClick = {
                    recovering = true
                    password = ""
                    action.error = null
                }) { Text("Lupa kata sandi admin") }
            }
        }
        if (action.busy && configured != null) LinearProgressIndicator(Modifier.fillMaxWidth())
        ErrorText(action.error)
    }
}

@Composable
internal fun AccessSettingsScreen(app: AppContainer, back: () -> Unit) {
    var cashierPassword by remember { mutableStateOf("") }
    var adminPassword by remember { mutableStateOf("") }
    var code by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val action = rememberAction()
    Page("Akun dan pemulihan", back) {
        Text("Reset akses kasir", style = MaterialTheme.typography.titleMedium)
        PasswordField("Kata sandi kasir baru", cashierPassword) { cashierPassword = it }
        Button(enabled = !action.busy, onClick = {
            action.run {
                app.accounts.resetCashier(cashierPassword)
                cashierPassword = ""
                message = "Kata sandi kasir diperbarui."
            }
        }) { Text("Simpan kata sandi kasir") }
        HorizontalDivider()
        Text("Kode pemulihan admin", style = MaterialTheme.typography.titleMedium)
        Text("Membuat kode baru menonaktifkan kode lama.")
        PasswordField("Kata sandi admin saat ini", adminPassword) { adminPassword = it }
        Button(enabled = !action.busy, onClick = {
            action.run {
                code = app.accounts.rotateCode(adminPassword)
                adminPassword = ""
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
