---
description: "Daftar tugas implementasi Kasir Toko 2.0.0"
---

# Tugas Kasir Toko 2.0.0

**Branch** `codex/release-2.0.0`

**Acuan** [Spesifikasi](spec.md), [plan](plan.md), [riset](research.md), [model data](data-model.md), [kontrak UI](contracts/ui.md), dan [panduan validasi](quickstart.md).

**Format** Label `[P]` menandai tugas yang bisa berjalan bersamaan dengan pasangannya setelah prasyarat bersama selesai. Label `[US1]` hingga `[US4]` merujuk pada cerita pengguna. Path relatif terhadap root repositori.

## Fase 1. Persiapan

- [X] T001 Verifikasi checkout `codex/release-2.0.0`, JDK, Android SDK, dan emulator API 26 sesuai `specs/003-beranda-keamanan/quickstart.md`. Siapkan profil uji terpisah untuk instrumentasi dan update APK.
- [X] T002 Cocokkan hash, ID, kode versi, dan sidik jari sertifikat APK resmi 1.5.0 dengan `specs/003-beranda-keamanan/research.md`. Nilai asal, penyimpanan, kendali akses, dan indikasi paparan kunci sesuai R4. Catat dasar penilaian, status, atau hambatan tanpa merekam kunci maupun kata sandi. T004 membuktikan kompatibilitas secara terpisah.

## Fase 2. Fondasi bersama

- [X] T003 Di `app/build.gradle.kts`, atur `versionName=2.0.0` dan `versionCode=4` agar APK dapat memperbarui build uji kode 3. Pertahankan `com.apkkasir`, minimum API 26, serta compile/target 36. Siapkan build release non-debuggable dengan konfigurasi signing lokal yang eksplisit di luar Git. Build debug tetap tersedia untuk instrumentasi.
- [X] T004 Jalankan uji awal kandidat bertanda tangan pada API 26 sesuai `specs/003-beranda-keamanan/quickstart.md`, memakai APK resmi 1.5.0 yang telah diisi data sintetis. Catat metadata dan hasil update serta login. Jika kunci belum tersedia atau tidak layak dipakai, catat hambatan dan lanjutkan UI serta audit yang bisa dijalankan. Uji kandidat publik tetap menunggu jalur signing aman.

## Fase 3. US1 Melayani pembeli dari beranda (P1)

**Uji independen.** Dari beranda, buka keempat kartu. Selesaikan pembayaran tunai dan QRIS. Cocokkan nota dan rekap. Uji ringkasan kosong, gagal dimuat, setelah koreksi, dan saat tanggal WIB berganti.

- [ ] T005 [P] [US1] Tambahkan skenario beranda dan rekap pada `app/src/androidTest/java/com/apkkasir/HomeAndDailyReportTest.kt`. Cakup empat tujuan kartu, nol transaksi lunas termasuk saat baris batal ada, rekap setelah pembayaran/pembatalan/pemulihan, keadaan memuat dan gagal, buka ulang beranda, pergantian tanggal WIB saat terlihat dan setelah kembali ke foreground, serta logout dan login ulang.
- [X] T006 [P] [US1] Tambahkan aturan khusus desain halaman pada `design-system/kasir-toko/pages/home.md` sesuai `specs/003-beranda-keamanan/contracts/ui.md`. Tetapkan grid dua kolom pada 360 dan 375 dp, ikon terpusat, widget selebar konten, label membungkus hingga 200%, dan tema Kasir Toko. Gunakan ikon yang tersedia.
- [X] T007 [US1] Siapkan state rekap UI bersama di `app/src/main/java/com/apkkasir/ui/DailyReportState.kt` dan gunakan di `app/src/main/java/com/apkkasir/ui/DailyReportScreen.kt`. Pertahankan `SalesReport.observe`, `ReportSnapshot`, `ReportPeriod`, dan `formatRupiah`. Gunakan rentang awal hari inklusif hingga awal hari berikutnya eksklusif, berdasarkan waktu konfirmasi WIB. Hitung jumlah `LUNAS` dan total dengan `BigInteger` dari snapshot. Satu koleksi aktif mengikuti sesi dan tujuan yang membutuhkan data. Ganti periode saat hari berganti, periksa lagi saat foreground, dan validasi perubahan jam dalam 30 detik saat beranda terlihat. Tangani gagal, muat ulang, pembatalan koleksi, dan penghapusan nilai lama saat ganti tanggal atau sesi.
- [X] T008 [US1] Buat `app/src/main/java/com/apkkasir/ui/HomeScreen.kt` dan sambungkan ke `app/src/main/java/com/apkkasir/ui/AppNavigation.kt`. Tampilkan **Buat tagihan**, **Tagihan menunggu**, **Nota**, dan **Laporan hari ini** sesuai urutan kontrak. Di `app/src/main/java/com/apkkasir/ui/CheckoutScreen.kt`, gunakan riwayat yang ada dengan filter `MENUNGGU` atau transaksi bernomor nota. Letakkan widget **Penjualan hari ini** di bawah grid. Tampilkan total, jumlah lunas, dan ikon dekoratif `app/src/main/res/drawable/ic_kasir_register.xml`. Bedakan keadaan memuat, gagal, dan Rp0 dengan pesan kosong. Empat kartu tetap tersedia.
- [X] T009 [US1] Sesuaikan penanda tujuan di `app/src/androidTest/java/com/apkkasir/CheckoutUiTest.kt` dan `app/src/androidTest/java/com/apkkasir/ReportPerformanceTest.kt` agar label **Nota** atau angka widget tidak memenuhi asersi layar tujuan. Jalankan uji beranda, nota, dan rekap sesuai `specs/003-beranda-keamanan/quickstart.md`. Pertahankan pembayaran dan penyimpanan draf seperti 1.5.0.

## Fase 4. US2 Menemukan menu lewat navigasi bawah (P1)

**Uji independen.** Buka empat tab dan tujuan turunannya. Uji pintasan terfilter, ketukan berulang, tombol Kembali, TalkBack, penunjuk, keyboard, dan animasi sistem.

- [ ] T010 [US2] Tambahkan uji kontrak tab dan back stack di `app/src/androidTest/java/com/apkkasir/CashierNavigationTest.kt`. Uji jalur shortcut dan tab Riwayat, asal kembali dari nota lama dan setelah bayar, kembali dari detail/formulir, filter setelah rotasi, tab aktif, serta ketukan berulang tanpa layar ganda.
- [X] T011 [US2] Bangun bilah Material 3 per sesi dengan satu `NavHost` di `app/src/main/java/com/apkkasir/ui/AppNavigation.kt`. Hubungkan Beranda, Riwayat, Laporan, dan Pengaturan. Pilihan tab berasal dari rute. Gunakan `launchSingleTop`, `popUpTo`, serta simpan/pulihkan state tujuan utama. Tab Riwayat membuka semua status setelah shortcut terfilter. Tempatkan pencarian, koreksi, QRIS terlambat, PDF, barang, QRIS toko, akun, dan Keluar sesuai kontrak. Tampilkan bilah pada tujuan tingkat atas dan tangani inset tanpa penggandaan.
- [X] T012 [US2] Pertahankan konteks asal di `app/src/main/java/com/apkkasir/ui/AppNavigation.kt` dan `app/src/main/java/com/apkkasir/ui/ReceiptScreen.kt`. Setelah pembayaran dikonfirmasi, keluarkan checkout yang selesai sebelum menampilkan nota. **Selesai** dan Kembali menuju konteks sebelum checkout. Nota yang dibuka dari daftar kembali ke rute dan filter asal. Sajikan keadaan kosong, memuat, dan gagal pada tujuan riwayat serta laporan sesuai `specs/003-beranda-keamanan/contracts/ui.md`.
- [ ] T013 [US2] Lengkapi dan uji keadaan tekan, hover penunjuk, fokus keyboard, dan indikator tab terpilih di `app/src/main/java/com/apkkasir/ui/AppNavigation.kt`. Pastikan pengaturan animasi sistem dihormati, kontrol minimal 48 × 48 dp, label unik bagi TalkBack, dan kontras WCAG AA pada kedua tema. Jalankan verifikasi delapan kontrol, lebar 360/375 dp, font hingga 200%, dan inset menurut `specs/003-beranda-keamanan/quickstart.md`.

## Fase 5. US3 Menjaga keamanan data kasir (P1)

**Uji independen.** Periksa kandidat dan tiap area audit. Uji penolakan akses setelah logout dan hasil pemilih dari sesi lama. Tutup temuan tinggi dan kritis dengan uji ulang.

- [X] T014 [P] [US3] Audit izin dan komponen efektif dari APK release, `app/src/main/AndroidManifest.xml`, serta laporan merge. Dokumentasikan kebutuhan setiap izin dan komponen yang diekspor. Pastikan aktivitas uji debug tidak ikut terpaket, kandidat non-debuggable, dan `run-as` ditolak pada perangkat uji. Ikuti `specs/003-beranda-keamanan/quickstart.md`.
- [ ] T015 [US3] Audit akses, kredensial, pemulihan, dan log di `app/src/main/java/com/apkkasir/data/LocalAccounts.kt` serta `app/src/main/java/com/apkkasir/data/LocalSession.kt`. Jalankan `app/src/androidTest/java/com/apkkasir/SetupAndAccessTest.kt` dan `app/src/androidTest/java/com/apkkasir/PickerSessionTest.kt`. Pakai data sintetis untuk memeriksa startup, keluar, kembali, login ulang, kode pengganti, serta hasil pemilih setelah sesi asal berakhir.
- [ ] T016 [US3] Audit penyimpanan, backup Android, impor QRIS, dan ekspor/arsip PDF pada `app/src/main/res/xml/backup_rules.xml`, `app/src/main/res/xml/data_extraction_rules.xml`, `app/src/main/java/com/apkkasir/data/QrisImageStore.kt`, dan `app/src/main/java/com/apkkasir/reports/ReportArchive.kt`. Uji gambar rusak atau terlalu besar, akses URI hilang, batal pilih, dan gagal tulis. Cocokkan kembali gambar QRIS lama serta data arsip sesuai hasil yang diharapkan.
- [X] T017 [P] [US3] Tinjau dependensi langsung dan transitif dari `releaseRuntimeClasspath` pada `app/build.gradle.kts` serta `gradle/libs.versions.toml`. Catat tanggal, advisori, versi, dan dampak pada aplikasi sesuai `specs/003-beranda-keamanan/quickstart.md`.
- [ ] T018 [US3] Gabungkan hasil tiap area sesuai `specs/003-beranda-keamanan/quickstart.md`. Gunakan rubrik proyek dan catat dampak, pihak yang bisa memicu, prasyarat akses, alasan tingkat keparahan, langkah validasi, bukti, tindakan serta risiko terbuka. Perbaiki temuan pada berkas terkait T014–T017 dan jalankan uji regresi terfokus. Pastikan temuan tinggi dan kritis selesai dan diuji ulang sebelum uji kandidat akhir disahkan.

## Fase 6. US4 Memasang 2.0.0 dan mempertahankan data (P1)

**Uji independen.** Pada API 26 dan perangkat fisik, pasang kandidat akhir di atas APK resmi 1.5.0. Verifikasi data, login, pemulihan, serta pemasangan baru dengan Play Protect aktif pada perangkat fisik.

- [ ] T019 [US4] Siapkan data sintetis di APK resmi 1.5.0 melalui UI sesuai `specs/003-beranda-keamanan/quickstart.md`. Siapkan akun dan kode pemulihan, barang dan harga, QRIS, tagihan tunai menunggu, transaksi serta nota tunai/QRIS, riwayat koreksi dan pelaku, PDF arsip, serta salinan ekspor. Catat pembanding nomor, nominal, status, rincian nota, rekap dan hash PDF sebelum update. Siapkan fixture terpisah dari APK resmi 1.0.0 dengan pemulihan QRIS oleh admin. Update fixture itu ke APK resmi 1.5.0 dan catat nama pelaku historis serta rincian pemulihannya.
- [ ] T020 [US4] Bangun kandidat akhir untuk `app/build/outputs/apk/release/` setelah perbaikan selesai. Verifikasi metadata, status non-debuggable, sertifikat dan SHA-256 sesuai `specs/003-beranda-keamanan/quickstart.md`. Jika APK berubah, periksa ulang bagian audit terdampak dan ulangi verifikasi pemasangan T021–T022 pada hash baru.
- [ ] T021 [P] [US4] Pasang kandidat akhir di atas APK resmi 1.5.0 pada API 26 sesuai `specs/003-beranda-keamanan/quickstart.md`. Bandingkan data dan berkas dengan T019, lanjutkan tagihan tanpa duplikasi, uji login serta kode pemulihan yang berlaku setelah pemeriksaan data. Uji pemulihan pada akun fixture karena kode akan terganti. Pada fixture historis T019, cocokkan nama admin asal, alasan, nomor nota, status, dan waktu konfirmasi melalui UI.
- [ ] T022 [P] [US4] Jalankan instalasi baru dan update kandidat akhir di perangkat fisik dengan Play Protect aktif, mengikuti `specs/003-beranda-keamanan/quickstart.md`. Untuk kedua skenario, unduh aset GitHub yang diotorisasi melalui browser Android, cocokkan SHA-256, lalu buka pemasang dari Unduhan. Instalasi baru membuat satu akun kasir. Pada update cocokkan data dengan T019 dan ulangi uji login/pemulihan. Catat model, versi Android, sumber dan pesan pemasang, sertifikat, hash, serta hasil pemindaian. Jika aset belum tersedia, selesaikan bagian lokal dan catat uji unduhan sebagai tertunda. Tangani hasil yang gagal sesuai penyebabnya dan uji ulang dengan Play Protect aktif.

## Fase 7. Verifikasi lintas cerita

- [ ] T023 Jalankan unit test, lint release, instrumentasi API 26 dan pemeriksaan tampilan Android sesuai `specs/003-beranda-keamanan/quickstart.md`. Cakup regresi kedua pembayaran, pengelolaan, riwayat, koreksi, pemulihan dan arsip. Pada fixture 1.000 transaksi di `app/src/androidTest/java/com/apkkasir/ReportPerformanceTest.kt`, ukur waktu beranda dalam sesi baru hingga widget siap dan waktu menuju tampilan laporan secara terpisah. Catat apakah rekap sudah siap saat laporan dibuka. Pertahankan batas 2 detik untuk laporan harian dan 10 detik untuk PDF bulanan pada perangkat acuan setara.
- [ ] T024 Simpan ringkasan hasil validasi dan audit, perangkat, temuan terbuka, serta hash kandidat teruji dengan acuan `specs/003-beranda-keamanan/quickstart.md`. Tahan publikasi bila kendali kunci belum terbukti, ada indikasi kompromi, pembaruan gagal menjaga data, temuan tinggi/kritis masih terbuka, atau uji pemasangan dari GitHub belum lulus. Pada tahap publikasi yang diotorisasi, verifikasi SHA-256 aset GitHub cocok dengan kandidat yang lulus T020–T023.
- [X] T025 Implementasikan widget layar utama di `app/src/main/java/com/apkkasir/KasirHomeWidget.kt` dan daftarkan melalui manifest. Uji tambah widget dan ketuk untuk membuka aplikasi sebelum validasi kandidat akhir T020–T024.

## Dependensi dan urutan

- T001–T003 menyiapkan jalur kandidat. Lakukan T004 sebelum menuntaskan jalur rilis. Hambatan signing membatasi pekerjaan yang memerlukan APK bertanda tangan. Beranda, navigasi, dan pemeriksaan sumber tetap berjalan.
- Setelah persiapan, T005 dan T006 bisa dikerjakan paralel. T007 menyediakan state untuk T008, dan T009 berjalan setelah keduanya terintegrasi. US2 menggunakan rute dan perilaku beranda US1: T010 mendahului T011–T012, lalu T013 memverifikasi hasilnya.
- US3 dapat mulai dengan audit sumber dan dependensi sebelum UI selesai. Selesaikan pemeriksaan sesi UI setelah US2 terintegrasi. T018 memakai hasil T014–T017. Validasi instalasi fisik dan API 26 memerlukan kandidat akhir yang sama dari T020. Siapkan fixture T019 sejak awal tanpa menunggu kandidat.
- Jalankan T023 pada sumber yang menghasilkan kandidat T020. Jika regresi membutuhkan perubahan sumber, ulangi pembuatan kandidat dan pengujian APK sesuai T020. Uji browser di T022 memakai aset GitHub dengan akses penguji sebelum publikasi. T024 memerlukan bukti pengujian dan audit untuk kandidat akhir.

## Contoh pekerjaan paralel

- **US1** Setelah persiapan, susun uji widget T005 dan aturan desain T006 pada berkas berbeda.
- **US2** Siapkan skenario manual TalkBack dan keyboard bersama penulisan uji T010. Kerjakan perubahan navigasi pada berkas bersama secara berurutan.
- **US3** Inventaris komponen APK T014 bisa dilakukan bersamaan dengan tinjauan dependensi T017 setelah artefak release tersedia. Gabungkan catatan di T018.
- **US4** Pengujian T021 dan T022 dapat berjalan bersamaan pada profil dan perangkat uji terpisah setelah fixture dan hash kandidat siap.

## Strategi implementasi

Jadikan US1 tonggak demo internal pertama. Tambahkan navigasi dan uji regresi, lalu selesaikan audit dan validasi update. Kandidat distribusi membutuhkan semua cerita dan verifikasi lintas cerita.

## Pemetaan penerimaan

| Persyaratan | Tugas dan bukti |
| --- | --- |
| FR-001, FR-004, SC-001 | T005–T009, T013. Empat kartu, empat tab, layar sempit, pembesaran teks, kontras, serta kontrol yang dapat dijangkau. |
| FR-002, FR-003, SC-002 | T010–T013. Kembali, ketukan berulang, fokus, hover, semantik tab, dan animasi. |
| FR-005, SC-003 | T009, T011–T012, T023. Regresi operasional kasir dan konsistensi transaksi. |
| FR-006, FR-009, SC-004 | T014–T018, T024. Laporan audit, akses sesi, temuan, perbaikan, serta bukti uji ulang. |
| FR-007, FR-008, FR-009, SC-005 | T002–T004, T019–T022, T024. Signing, metadata, data, pemasang fisik, Play Protect, serta hash kandidat. |
| FR-010, SC-006 | T005, T007–T009, T013, T015. Kesesuaian rekap widget, tanggal WIB, keadaan memuat/gagal, aksesibilitas, dan sesi. |
| FR-011, SC-007 | T025. Widget layar utama tampil dan membuka aplikasi melalui alur masuk biasa. |
