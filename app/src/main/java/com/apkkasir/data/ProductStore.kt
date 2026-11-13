package com.apkkasir.data

class ProductStore(private val dao: ProductDao, private val session: LocalSession) {
    val products = dao.observe()

    suspend fun add(name: String, price: Long) {
        session.requireRole(Role.ADMIN)
        require(name.isNotBlank() && price > 0) { "Isi nama barang dan harga lebih dari nol." }
        dao.insert(Product(name = name.trim(), price = price))
    }

    suspend fun changePrice(id: Long, price: Long) {
        session.requireRole(Role.ADMIN)
        require(price > 0) { "Harga harus lebih dari nol." }
        check(dao.changePrice(id, price) == 1) { "Barang tidak ditemukan." }
    }
}
