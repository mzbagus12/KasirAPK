# Model data Kasir Toko 2.0.0

Desain memakai entitas Room versi 2. Ringkasan dan pilihan navigasi merupakan state UI yang berasal dari data toko dan sesi aktif.

## Data persisten yang diverifikasi

| Entitas atau aset | Relasi dan data yang harus bertahan |
| --- | --- |
| `User`, `StoreSettings` | Identitas kasir, verifikator kata sandi dan pemulihan yang berlaku, serta referensi gambar QRIS aktif. Identitas historis tetap tersedia untuk atribusi transaksi atau koreksi. |
| `Product`, `SaleLine` | Barang dan harga aktif. Tiap baris tagihan tetap memakai nama, harga satuan, jumlah, dan subtotal yang disimpan pada transaksi. |
| `Sale` | Pemilik, nomor tagihan dan nota, metode bayar, uang tunai bila ada, total, status, waktu konfirmasi, dan pelaku. Tagihan `MENUNGGU` dapat dilanjutkan. |
| `SaleCorrection` | Riwayat perubahan dan relasi transaksi dengan pelaku asal, termasuk pembatalan dan pemulihan. |
| QRIS dan `ReportDocument` | Gambar QRIS aktif, metadata arsip, PDF privat, dan salinan PDF pada tujuan ekspor yang sudah dipilih pengguna. |

## State ringkasan penjualan

- **Pemilik state.** Objek sesi aktif. Dua sesi berbeda tetap terpisah walaupun keduanya memakai akun kasir yang sama.
- **Periode.** Tanggal WIB dengan rentang awal hari inklusif hingga awal hari berikutnya eksklusif. Baris laporan berasal dari transaksi dengan waktu konfirmasi pada rentang tersebut.
- **Nilai.** Periode, daftar baris untuk laporan, jumlah `LUNAS`, dan jumlah total rupiah dengan presisi `BigInteger` dari `ReportSnapshot`. Widget membaca jumlah dan total saja. Pembatalan dapat meninggalkan baris dalam laporan dengan jumlah lunas nol. Pemulihan memakai waktu konfirmasi sesuai aturan layanan yang ada.
- **Transisi.** Masuk ke tujuan rekap memulai pemuatan. Keberhasilan menghasilkan data, termasuk hasil nol. Jika gagal, tampilkan status gagal dan muat ulang saat tujuan dibuka kembali. Perubahan tanggal menghapus tampilan nilai hari lama sampai data baru siap. Keluar membatalkan koleksi dan membuang state sesi.

## State navigasi

Destinasi menentukan tab aktif. Riwayat memiliki tiga pilihan tampilan: semua status, `MENUNGGU`, dan transaksi dengan nomor nota. Shortcut mengirim nilai filter eksplisit, termasuk saat pemulihan state. Back stack menyimpan tujuan asal dan ID transaksi bila diperlukan. Transaksi atau nominal widget tidak disalin ke argumen navigasi.

## Bukti verifikasi

Laporan audit mencatat cakupan, temuan, tingkat keparahan, bukti reproduksi dengan data sintetis, keputusan dan uji ulang. Catatan pemasangan mengikat hasil ke kombinasi sumber pemasangan, model perangkat, versi Android, jenis pemasangan, identitas sertifikat, SHA-256 APK, hasil Play Protect, dan hasil retensi data.
