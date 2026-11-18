# Kasir Toko

Kasir Toko adalah aplikasi kasir Android untuk toko kecil dengan satu perangkat dan satu akun kasir. Tagihan, nota, katalog barang, dan laporan disimpan di perangkat.

## Fitur

- **Tagihan dan nota.** Pilih barang, hitung total belanja, dan buka kembali tagihan yang masih menunggu pembayaran. Setelah pembayaran dikonfirmasi, aplikasi menampilkan nota dan memasukkan transaksi ke laporan penjualan.
- **Pembayaran tunai dan QRIS.** Untuk tunai, masukkan uang yang diterima agar aplikasi menghitung kembalian. Untuk QRIS, tampilkan kode QRIS statis toko. Pembeli memasukkan nominal, lalu kasir memeriksa notifikasi dari penyedia QRIS dan mengonfirmasi pembayaran di aplikasi.
- **Pengelolaan toko.** Akun kasir dapat mengelola barang dan harga, mengganti gambar QRIS toko, serta mencatat koreksi transaksi.
- **Laporan penjualan.** Lihat rekap harian dengan rincian metode pembayaran. Buat, arsipkan, dan ekspor laporan harian, mingguan, atau bulanan sebagai PDF.

## Pasang di Android

1. Unduh APK dari [GitHub Releases](https://github.com/mzbagus12/KasirAPK/releases/latest). Kasir Toko mendukung Android 8.0 (API 26) ke atas.
2. Buka APK di perangkat. Jika rilis resmi sudah terpasang, pilih **Update** agar data toko tetap tersedia.
3. Pada pemasangan baru, buat akun kasir dan simpan kode pemulihan di tempat yang aman.
4. Tambahkan barang dan harga, lalu pasang gambar QRIS toko jika menerima pembayaran QRIS.

## Pengembangan

Aplikasi ditulis dengan Kotlin dan menggunakan Jetpack Compose untuk antarmuka serta Room untuk penyimpanan lokal. Siapkan JDK 17 dan Android SDK Platform 36, lalu buka proyek di Android Studio. Untuk membangun APK debug dan menjalankan unit test dari PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```

APK debug tersedia di `app/build/outputs/apk/debug/`.
