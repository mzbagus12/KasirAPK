# Checklist mutu spesifikasi upgrade 1.5.0

**Fitur** [Upgrade Kasir Tunggal](../spec.md)

- [x] Sumber APK lama, tujuan upgrade, dan cakupan satu akun didefinisikan.
- [x] Instalasi baru, instalasi lama belum disiapkan, dan toko yang sudah berjalan tercakup.
- [x] Aturan login kasir lama, penolakan login admin, dan atribusi riwayat dapat diuji.
- [x] Pemeriksaan sebelum upgrade mencakup reset kata sandi kasir dan penerbitan ulang kode saat pemilik hanya punya akses admin 1.0.0.
- [x] Perbandingan data sebelum dan sesudah upgrade mencakup tagihan Tunai Menunggu, QRIS yang dipulihkan admin, PDF arsip, dan salinan ekspor.
- [x] Pembaruan terputus dan data lama tidak konsisten memiliki hasil uji yang berbeda dan sama-sama melindungi data lama.
- [x] Kasir dapat memakai seluruh fitur operasional admin 1.0.0, termasuk koreksi, pemulihan QRIS, dan pengelolaan PDF harian, mingguan, dan bulanan.
- [x] Penggantian kata sandi dan pemulihan akun tunggal dapat diuji, termasuk penerbitan ulang kode yang belum sempat disimpan.
- [x] Prasyarat kompatibilitas APK dicatat. Strategi migrasi database disusun pada tahap perencanaan.

Audit ulang mencocokkan persyaratan dan skenario dengan spesifikasi serta fitur admin 1.0.0. Checklist menilai kelengkapan spesifikasi. Pengujian APK dilakukan setelah implementasi.
