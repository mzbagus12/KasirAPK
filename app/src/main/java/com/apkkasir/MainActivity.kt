package com.apkkasir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.apkkasir.data.AppContainer
import com.apkkasir.ui.KasirApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val factory = viewModelFactory { initializer { KasirViewModel(AppContainer(applicationContext)) } }
        val model = ViewModelProvider(this, factory)[KasirViewModel::class.java]
        setContent { KasirApp(model.app) }
    }
}

private class KasirViewModel(val app: AppContainer) : ViewModel()
