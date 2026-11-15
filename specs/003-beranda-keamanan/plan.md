# Rencana implementasi Kasir Toko 2.0.0

**Tanggal** 14 November 2026 · **Fitur Spec Kit** `003-beranda-keamanan` · **Branch pengembangan** `codex/release-2.0.0` · **Baseline** `main` (`dac17ff`)

**Spesifikasi** [Beranda, navigasi, dan keamanan](spec.md)

## Ringkasan

Susun beranda 2 × 2, widget penjualan hari ini, dan empat tab berikon. Gunakan layar dan operasi kasir yang sudah ada. Audit keamanan pada APK release dan buktikan pembaruan langsung dari 1.5.0 sebelum distribusi GitHub.

## Konteks teknis

- **Platform** Android API 26 ke atas, compile/target 36, `com.apkkasir`. Kandidat `versionName=2.0.0`, `versionCode=4` mendukung pembaruan di atas build uji kode 3.
- **Teknologi** Kotlin 2.2.21, JDK 17, AGP 8.13.1, Compose Material 3, Navigation Compose 2.9.5, Room 2.8.4, serta Gradle Wrapper.
- **Penyimpanan** Room `kasir.db` versi 2, QRIS privat, PDF arsip privat, dan salinan ekspor di tujuan pilihan kasir. Desain ini menggunakan bentuk penyimpanan tersebut tanpa migrasi baru.
- **Cakupan** Operasi lokal satu kasir. Shared state rekap berlaku per sesi dan mengikuti WIB. Operasi akun, checkout, koreksi, dan ekspor tetap memakai layanan dan aturan 1.5.0.
- **Pengujian** JUnit, lint, instrumentasi API 26, pemeriksaan APK dengan alat SDK, serta pemasang Android dengan Play Protect aktif di perangkat fisik.
- **Kinerja** Hindari dua koleksi aktif untuk rekap harian. Pada fixture 1.000 transaksi, ukur beranda sampai angka widget siap dari awal tanpa cache sesi. Ukur waktu membuka laporan secara terpisah dan catat apakah rekap sudah siap saat tombol ditekan. Pertahankan ambang 2 detik untuk laporan harian dan 10 detik untuk PDF bulanan. Catat perangkat dan kondisi pengukuran.

## Pemeriksaan konstitusi

`.specify/memory/constitution.md` masih berupa template. Pemeriksaan awal mengikuti persyaratan fitur. Desain pascariset memenuhi cakupan lokal, penggunaan ulang layanan, sesi tunggal, dan retensi data. Penandatanganan, audit, aksesibilitas, serta pemasangan akhir memiliki bukti lulus yang harus dihasilkan saat implementasi dan uji rilis.

## Struktur yang dipakai

- `app/src/main/java/com/apkkasir/ui/` untuk beranda, navigasi, widget dan layar yang sudah ada.
- `app/src/main/java/com/apkkasir/reports/` untuk rekap dan periode WIB. Gunakan `data/` dan `payment/` sebagai sumber operasi dan batas sesi.
- `app/src/main/res/` serta `design-system/kasir-toko/` untuk ikon dan aturan tata letak halaman.
- `app/build.gradle.kts` dan manifest hasil merge untuk identitas dan konfigurasi release. Gunakan `app/src/test/` dan `app/src/androidTest/` untuk verifikasi.

## Fase 0. Riset

[Riset](research.md) mencatat pemeriksaan APK baseline dan keputusan navigasi, rekap, penandatanganan, keamanan, serta pemasangan.

## Fase 1. Desain

1. **Buktikan jalur pembaruan lebih awal.** Periksa APK resmi 1.5.0. Nilai asal, kendali akses, dan indikasi paparan kunci terpisah dari kecocokan sertifikat. Saat implementasi dimulai, buat kandidat release non-debuggable dengan versi 3 dan buktikan update di API 26 menggunakan data sintetis. Jika proses tertahan pada kunci atau sertifikat, catat penyebab dan lanjutkan UI serta audit yang dapat dijalankan. [Bukti kelayakan kunci](research.md#bukti-kelayakan-kunci-lama)
2. **Buat navigasi utama dalam sesi aktif.** Gunakan satu `NavHost`, tujuan utama tanpa tumpukan duplikat, dan shortcut dengan filter yang eksplisit. Pertahankan asal saat kembali dari nota, koreksi, atau detail. Keluarkan checkout yang transaksi penjualannya sudah dikonfirmasi sebelum membuka nota hasil pembayaran. Kelola inset sekali antara bilah bawah dan konten.
3. **Bangun beranda dan rekap.** Tambahkan grid empat kartu dan widget di bawahnya. Tarik total dan jumlah dari state rekap yang juga dipakai laporan. Tangani keadaan memuat, kosong, gagal, tanggal WIB baru, dan akhir sesi. Buat pembatalan atau pemulihan tercermin dengan aturan rekap yang sama. [Kontrak UI](contracts/ui.md), [Model data](data-model.md)
4. **Audit dan regresi.** Periksa akses tanpa sesi, kredensial, log, izin, komponen, penyimpanan, backup, file QRIS/PDF, dan dependensi release. Nilai temuan berdasarkan dampak dan prasyarat akses. Tambah pengujian terfokus untuk navigasi, widget, pergantian tanggal, serta temuan yang diperbaiki. Pertahankan suite yang ada. Gunakan indikator khusus layar pada pengujian yang dulu mencari label **Nota** atau jumlah transaksi secara global. [Rubrik audit](quickstart.md#penetapan-tingkat-keparahan)
5. **Verifikasi APK yang akan dirilis.** Ulangi pemeriksaan manifest, sertifikat, dan SHA-256 pada kandidat akhir. Uji data dari instalasi 1.5.0 dan satu fixture 1.0.0 yang sudah diperbarui ke 1.5.0 dengan pelaku historis. Di perangkat fisik, uji unduhan APK melalui browser dari GitHub sampai pemasangan, termasuk pemasangan bersih dan update dengan Play Protect aktif. Cocokkan hash berkas yang diuji dengan aset GitHub sebelum publikasi. [Panduan validasi](quickstart.md)

## Cakupan verifikasi

| Kebutuhan dan kriteria | Bukti yang direncanakan |
| --- | --- |
| FR-001–FR-004, SC-001–SC-002 | Delapan kontrol membuka tujuan yang benar. Uji Back, tab aktif, tema terang/gelap, lebar 360 dan 375 dp, teks hingga 200%, TalkBack, keyboard, hover, dan animasi sistem. |
| FR-005, SC-003 | Regresi alur kasir, termasuk dua metode bayar, satu nota per konfirmasi, cari/koreksi/pulihkan, barang, QRIS, ganti kredensial, laporan tiga periode, dan pengelolaan PDF. |
| FR-006, FR-009, SC-004 | Laporan audit dengan bukti per kontrol dan uji ulang perbaikan. Uji batas sesi dan file. Jumlah temuan tinggi/kritis terbuka nol. |
| FR-007–FR-009, SC-005 | Bukti metadata, SHA-256, update dari rilis resmi API 26 dan fisik, data lama, pemasangan baru, serta hasil Play Protect dari pemasang biasa. |
| FR-010, SC-006 | Cocokkan widget dengan laporan pada data kosong/berisi, setelah perubahan transaksi, dan lintas WIB. Uji kegagalan, percobaan ulang, serta logout/login ulang. |
| FR-011, SC-007 | Tambahkan widget dari pemilih layar utama Android dan ketuk untuk membuka alur masuk biasa. Gunakan `AppWidgetProvider`, `RemoteViews` dan `PendingIntent` immutable ke aktivitas utama. |
