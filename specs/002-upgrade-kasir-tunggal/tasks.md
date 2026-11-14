# Tugas Upgrade Kasir Tunggal 1.5.0

**Branch** `release/1.5.0`

**Acuan** [Spesifikasi](spec.md), [plan](plan.md), [model data](data-model.md), [riset](research.md), [kontrak UI](contracts/ui.md), dan [panduan uji](quickstart.md).

**Format** `[P]` menandai tugas yang bisa dikerjakan paralel dengan tugas pasangannya setelah prasyarat bersama selesai. `[US1]`–`[US3]` mengacu pada skenario pengguna di spesifikasi. Jalur berkas relatif terhadap root repo.

## Fase 1 · Persiapan

- [X] T001 Ambil APK resmi 1.0.0 sebagai baseline. Catat SHA-256 APK, metadata paket, dan sidik jari sertifikatnya. Cocokkan sertifikat dengan kunci build kandidat. Ikuti prasyarat di `specs/002-upgrade-kasir-tunggal/quickstart.md`.
- [X] T002 Setel `versionName` ke `1.5.0` dan `versionCode` ke `2` di `app/build.gradle.kts`. Pertahankan `applicationId = "com.apkkasir"`, minimum API 26, SDK 36, dan penandatangan yang cocok dengan T001.

## Fase 2 · Fondasi bersama

- [X] T003 Siapkan fixture Room berbasis `app/schemas/com.apkkasir.data.KasirDatabase/1.json` di `app/src/androidTest/java/com/apkkasir/LegacyStoreFixture.kt`. Buat basis data dan direktori berkas uji yang terisolasi. Sediakan toko kosong, toko valid dengan pemulihan QRIS oleh admin, dan variasi data rusak. Simpan skema versi 1 dan daftarkan aset skema untuk instrumentasi di `app/build.gradle.kts`.
- [X] T004 Siapkan representasi `HISTORICAL_ADMIN`, pencarian akun login terbatas ke `CASHIER`, serta pemeriksaan identitas sesi aktif yang bisa dipakai oleh layanan di `app/src/main/java/com/apkkasir/data/AccountEntities.kt`, `app/src/main/java/com/apkkasir/data/AccountDao.kt`, dan `app/src/main/java/com/apkkasir/data/LocalSession.kt`. Pastikan sesi baru berbeda dari sesi lama meski ID kasir sama. Pertahankan struktur tabel dan indeks.

## Fase 3 · US1 Memperbarui APK dan melanjutkan pekerjaan (P1)

**Uji independen.** Pasang kandidat di atas APK resmi dengan data yang dibuat melalui UI 1.0.0. Login kasir, cocokkan riwayat dan berkas, lalu lanjutkan tagihan Tunai dengan nomor dan nominal yang sama.

- [X] T005 [P] [US1] Tulis uji migrasi dan startup di `app/src/androidTest/java/com/apkkasir/StoreUpgradeTest.kt` dengan fixture T003. Periksa retensi ID, kredensial kasir, pengaturan, katalog, transaksi, pelaku, QRIS dan PDF. Uji toko kosong, hilangnya akun atau pengaturan, foreign key putus, tagihan atas ID admin, QRIS aktif atau PDF yang hilang, serta basis data korup. Uji gangguan setelah perubahan baris sebelum commit dan buktikan rollback, retry, serta pembukaan ulang tanpa duplikasi. Periksa status tertahan, versi dan metadata lama serta berkas yang masih ada.
- [X] T006 [P] [US1] Siapkan panduan pemilik di `specs/002-upgrade-kasir-tunggal/contracts/ui.md` dan langkah validasi di `specs/002-upgrade-kasir-tunggal/quickstart.md`. Cocokkan label reset kata sandi kasir dan pembuatan kode di APK 1.0.0. Cantumkan uji login, penyimpanan kode, penundaan jika akses belum siap, dan tindakan saat pemasang menolak APK.
- [X] T007 [US1] Terapkan `Migration(1, 2)` di `app/src/main/java/com/apkkasir/data/StoreUpgrade.kt` dan naikkan versi Room di `app/src/main/java/com/apkkasir/data/KasirDatabase.kt`. Validasi pengaturan tunggal, satu kasir, satu admin, bentuk verifikator dan integritas referensi. Terapkan aturan “`Sale.cashierId` harus menunjuk kasir lama” dan izinkan `Sale.confirmedBy` menunjuk admin lama untuk pemulihan QRIS. Jika `StoreSettings.qrisFile` terisi, periksa berkas di `filesDir`. Untuk setiap `ReportDocument`, periksa berkas di `filesDir/reports`. Tahan data tidak konsisten sebelum commit tanpa rekonsiliasi atau penghapusan. Ubah hanya peran dan verifikator admin, pertahankan ID serta nama. Ekspor `app/schemas/com.apkkasir.data.KasirDatabase/2.json`.
- [X] T008 [US1] Daftarkan migrasi pada builder `kasir.db` di `app/src/main/java/com/apkkasir/data/AppContainer.kt`. Tangani callback korupsi tanpa menghapus basis data dan file pendamping. Pertahankan konfigurasi tanpa fallback destruktif atau kehilangan data saat recovery. Petakan hasil pembukaan ke siap masuk, belum disiapkan, dapat dicoba lagi, atau tertahan. Setup hanya tersedia untuk data akun, toko, QRIS, dan arsip yang kosong sesuai `specs/002-upgrade-kasir-tunggal/data-model.md`.
- [X] T009 [US1] Sambungkan startup dan login di `app/src/main/java/com/apkkasir/ui/AccountScreens.kt` dan `app/src/main/java/com/apkkasir/data/LocalAccounts.kt`. Tunggu pemeriksaan data, tampilkan **Coba lagi** untuk gangguan sementara, dan tahan login serta setup untuk data tidak konsisten dengan pesan yang sesuai. Gunakan kredensial kasir lama dan tolak kredensial admin lama.
- [X] T010 [US1] Jalankan `app/src/androidTest/java/com/apkkasir/StoreUpgradeTest.kt` setelah T007–T009. Verifikasi skema versi 2 setara secara fisik dengan skema 1 dan status startup terhubung ke hasil pemeriksaan. Jalankan uji upgrade APK resmi serta skenario pemilik hanya ingat login admin pada T023 setelah alur kasir terintegrasi.

## Fase 4 · US2 Menjalankan seluruh operasional dari akun kasir (P1)

**Uji independen.** Masuk sebagai kasir dan selesaikan penjualan, pengelolaan katalog dan QRIS, koreksi, pemulihan QRIS, serta laporan tiga periode. Setelah logout, layanan menolak akses dan hasil pemilih lama tetap ditolak setelah login ulang.

- [X] T011 [P] [US2] Perbarui skenario akses dan identitas pelaku di `app/src/androidTest/java/com/apkkasir/CatalogAccessTest.kt`, `app/src/androidTest/java/com/apkkasir/SaleCorrectionTest.kt`, `app/src/androidTest/java/com/apkkasir/ReportArchiveTest.kt`, dan `app/src/androidTest/java/com/apkkasir/ReceiptAndDailyReportTest.kt`. Hapus pemanggilan login admin dari empat tes tersebut. Uji kasir aktif, penolakan tanpa sesi, serta akses laporan, koreksi, ekspor dan penghapusan tanpa kehilangan data asal. Pertahankan asersi perilaku 1.0.0 yang masih berlaku.
- [X] T012 [P] [US2] Tulis uji kontrak pemilih berkas di `app/src/androidTest/java/com/apkkasir/PickerSessionTest.kt`. Uji hasil dari sesi lama setelah logout dan setelah kasir yang sama masuk lagi. Pastikan pratinjau, perubahan QRIS dan penulisan isi PDF dari sesi lama tidak terjadi. Uji hasil dari pemilih baru pada sesi aktif.
- [X] T013 [P] [US2] Alihkan otorisasi katalog dan pengaturan QRIS ke sesi kasir aktif di `app/src/main/java/com/apkkasir/data/ProductStore.kt` dan `app/src/main/java/com/apkkasir/data/QrisImageStore.kt`. Pertahankan validasi harga dan gambar, pratinjau, serta gambar lama bila impor gagal. Periksa ulang sesi asal saat menyimpan.
- [X] T014 [P] [US2] Alihkan pencarian, detail, koreksi metode, pembatalan Lunas dan pemulihan QRIS ke sesi kasir di `app/src/main/java/com/apkkasir/payment/SaleCorrectionService.kt`. Simpan ID kasir sebagai pelaku baru di `SaleCorrection.adminId`. Pertahankan alasan, status, nomor nota, waktu konfirmasi, dan atribusi admin historis. Pertahankan batas akses checkout di `app/src/main/java/com/apkkasir/payment/CheckoutService.kt`.
- [X] T015 [P] [US2] Alihkan akses laporan dan arsip ke sesi kasir di `app/src/main/java/com/apkkasir/reports/SalesReport.kt` dan `app/src/main/java/com/apkkasir/reports/ReportArchive.kt`. Ikat pembacaan dan penulisan ke sesi asal. Pertahankan periode harian, Senin–Minggu, dan bulanan dalam WIB, snapshot PDF, ekspor, serta penghapusan arsip setelah konfirmasi.
- [X] T016 [US2] Ikat permintaan pemilih ke sesi saat diluncurkan di `app/src/main/java/com/apkkasir/ui/QrisSettingsScreen.kt` dan `app/src/main/java/com/apkkasir/ui/ReportDocumentsScreen.kt`. Periksa sesi ketika hasil diterima dan saat layanan melanjutkan operasi. Gunakan perubahan layanan T013 dan T015.
- [X] T017 [US2] Gabungkan menu dan rute penjualan serta pengelolaan di `app/src/main/java/com/apkkasir/ui/AppNavigation.kt` setelah T013–T016. Kasir dapat membuka tagihan, nota, barang, QRIS, laporan hari ini, pencarian, PDF, dan pengaturan akun. Logout menutup jalur kembali ke halaman terlindung.

## Fase 5 · US3 Menyiapkan atau memulihkan akun tunggal (P1)

**Uji independen.** Setup satu kasir pada instalasi bersih dan 1.0.0 yang belum disiapkan. Uji ganti kata sandi serta pemulihan pada instalasi baru dan hasil upgrade. Akun tetap memakai ID yang sama dan kode lama ditolak setelah digunakan atau diganti.

- [X] T018 [P] [US3] Sesuaikan setup serta login fixture satu kasir di `app/src/androidTest/java/com/apkkasir/KasirFixture.kt`, `app/src/androidTest/java/com/apkkasir/CheckoutUiTest.kt`, dan `app/src/androidTest/java/com/apkkasir/ReportPerformanceTest.kt`. Pertahankan fungsi fixture `setup()`, `cashier()`, dan `item()` yang dipakai tes lain. Pertahankan pengujian nominal, nota, laporan dan kinerja.
- [X] T019 [P] [US3] Tulis uji akun di `app/src/androidTest/java/com/apkkasir/SetupAndAccessTest.kt` untuk setup satu akun, ganti kata sandi sendiri, penolakan kata sandi salah, dan penerbitan ulang kode ketika tampilan terputus. Uji recovery dengan kode 1.0.0 pada fixture T003, penolakan pemakaian ulang, serta dua permintaan yang memakai kode sama. Uji secara deterministik login dengan kata sandi lama yang sudah membaca verifikator, tetapi belum membentuk sesi saat pemulihan selesai. Setelah login itu dilanjutkan, pastikan sesi tidak terbentuk dan kata sandi baru bisa dipakai. Periksa ID kasir dan data toko.
- [X] T020 [US3] Terapkan setup satu kasir, perubahan kata sandi sendiri, recovery, dan penerbitan kode di `app/src/main/java/com/apkkasir/data/LocalAccounts.kt`. Buat akun dan pengaturan dalam satu transaksi. Recovery hanya saat keluar dan mengganti kata sandi serta verifikator kode secara atomik. Untuk perubahan saat masuk, verifikasi kata sandi aktif lalu periksa ulang sesi serta verifikator sebelum commit. Selaraskan commit pemulihan dengan pembentukan sesi di `app/src/main/java/com/apkkasir/data/LocalSession.kt`. Periksa ulang verifikator terkini tepat sebelum membentuk sesi agar login tertunda dengan kata sandi lama ditolak jika pemulihan sudah selesai lebih dulu.
- [X] T021 [US3] Lengkapi formulir setup, **Lupa kata sandi**, tampilan kode setelah commit, **Ganti kata sandi**, dan **Buat kode pemulihan baru** di `app/src/main/java/com/apkkasir/ui/AccountScreens.kt`. Gunakan rute pengaturan akun T017 dan hilangkan formulir akun admin lama.

## Fase 6 · Verifikasi lintas skenario

- [X] T022 Jalankan unit test, lint, build, dan instrumentasi sesuai `specs/002-upgrade-kasir-tunggal/quickstart.md`. Jalankan kontrol positif dan negatif migrasi, uji akun dan sesi, serta regresi checkout, koreksi, WIB, PDF dan arsip. Catat perangkat, perintah dan hasil.
- [X] T023 Verifikasi sertifikat, paket, versi dan SHA-256 kandidat akhir. Jalankan uji pemasangan 1.0.0→1.5.0 pada emulator API 26 dan pemasang Android perangkat fisik uji. Isi data melalui UI APK resmi 1.0.0 dan bandingkan sebelum serta sesudah upgrade. Pada instalasi hasil upgrade, pulihkan akses dengan kode yang diterbitkan melalui UI APK resmi 1.0.0. Pastikan kata sandi baru bisa dipakai, ID kasir dan data toko tetap sama, serta kode yang sudah dipakai ditolak. Uji instalasi bersih, 1.0.0 belum disiapkan, reset kasir dengan admin sebelum upgrade, dan APK penandatangan berbeda pada fixture terpisah. Catat hasil pada `specs/002-upgrade-kasir-tunggal/quickstart.md`.
- [ ] T024 Saat tahap rilis, cocokkan SHA-256 aset draf GitHub dengan APK yang lulus T023 sesuai `specs/002-upgrade-kasir-tunggal/quickstart.md`. Pastikan catatan rilis memuat atau menautkan panduan pemilik di `specs/002-upgrade-kasir-tunggal/contracts/ui.md`, termasuk cek login kasir dan simpan kode sebelum memasang APK. Jika hash berbeda, gunakan APK yang lulus atau ulangi T023 pada kandidat pengganti sebelum publikasi.

**Status verifikasi 2026-11-14.** T023 lulus pada emulator dengan APK resmi 1.0.0 dan pada pemasang Android Infinix X6857 dengan APK 1.0.0 terpasang yang bersertifikat sama. T024 berlaku saat rilis. Lihat [hasil verifikasi](quickstart.md#hasil-pemasangan-2026-11-14).

## Dependensi dan urutan

- T001–T002 mendahului fondasi T003–T004. Fixture skema lama menjadi dasar uji upgrade dan akun.
- Pada US1, T005 dan T006 dapat berjalan bersamaan. T007 → T008 → T009 → T010 menghubungkan migrasi, pembukaan data dan login.
- Pada US2, T011 dan T012 dapat disiapkan bersama. Setelah T004, T013, T014 dan T015 dapat dikerjakan bersamaan. T016 memakai layanan T013 dan T015. T017 menyatukan rute.
- Pada US3, T018 dan T019 dapat disiapkan bersama setelah T003. Jalankan tes keduanya setelah T020–T021 terintegrasi. Kerjakan perubahan akun T020–T021 setelah T009 agar berkas akun yang sama dikerjakan berurutan.
- T022 menunggu integrasi ketiga cerita. T023 memakai kandidat yang sudah melewati T022. T024 hanya berjalan pada tahap rilis.

## Strategi implementasi

Buktikan migrasi US1 pada fixture lebih dulu sebagai tonggak internal. Lanjutkan dengan akses penuh kasir dan pemulihan akun. Kandidat 1.5.0 siap dinilai setelah ketiga cerita dan uji pemasangan lulus.

## Pemetaan penerimaan

| Persyaratan | Tugas |
| --- | --- |
| FR-001, SC-001 | T001, T002, T023, T024 |
| FR-002 | T004, T005, T007, T009, T014, T023 |
| FR-003 | T011–T017, T022–T023 |
| FR-004, SC-002 | T005, T007–T010, T023 |
| FR-005, SC-003 | T004, T008–T009, T011–T021, T023 |
| FR-006, SC-005 | T003, T019–T023 |
| FR-007, SC-004 | T003, T005, T007–T010, T022 |
| FR-008 | T011, T013–T018, T022–T023 |
| FR-009, SC-006 | T006, T023–T024 |
