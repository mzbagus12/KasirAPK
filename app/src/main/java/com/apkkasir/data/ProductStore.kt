package com.apkkasir.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.flow

class ProductStore(private val database: KasirDatabase, private val session: LocalSession) {
    private val dao = database.products()
    val products = flow {
        val actor = session.requireRole(Role.CASHIER)
        dao.observe().collect { rows ->
            session.checkActive(actor)
            emit(rows)
        }
    }

    suspend fun add(name: String, price: Long) {
        val actor = session.requireRole(Role.CASHIER)
        require(name.isNotBlank() && price > 0) { "Isi nama barang dan harga lebih dari nol." }
        database.withTransaction {
            session.checkActive(actor)
            dao.insert(Product(name = name.trim(), price = price))
        }
    }

    suspend fun changePrice(id: Long, price: Long) {
        val actor = session.requireRole(Role.CASHIER)
        require(price > 0) { "Harga harus lebih dari nol." }
        database.withTransaction {
            session.checkActive(actor)
            check(dao.changePrice(id, price) == 1) { "Barang tidak ditemukan." }
        }
    }
}
