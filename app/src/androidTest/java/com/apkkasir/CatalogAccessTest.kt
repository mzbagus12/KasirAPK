package com.apkkasir

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CatalogAccessTest {
    @Test fun onlySignedInCashierCanChangeCatalog() = runBlocking {
        KasirFixture().use { f ->
            f.setup()
            val product = f.item()
            fails { f.products.add("Gratis", 0) }
            f.products.changePrice(product, 34_000)
            f.cashier()
            assertEquals(34_000L, f.products.products.first().single().price)
            f.products.changePrice(product, 99_000)
            f.session.exit()
            fails { f.products.changePrice(product, 50_000) }
            fails { f.products.add("Teh", 5_000) }
        }
    }
}
