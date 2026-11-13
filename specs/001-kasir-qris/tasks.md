# Tasks: Kasir QRIS dan Laporan Harian

**Input**: [Spesifikasi](spec.md), [rencana](plan.md), [riset](research.md), [model data](data-model.md), [kontrak UI](contracts/ui.md), dan [panduan verifikasi](quickstart.md).

**Format**: `[ID] [P?] [US?] Deskripsi dan path`. `[P]` dapat berjalan paralel setelah prasyaratnya selesai. Path relatif ke akar workspace. Tugas uji berasal dari skenario penerimaan dan rencana verifikasi.

## Phase 1: Setup

**Tujuan**. Siapkan toolchain dan kerangka aplikasi Android.

- [X] T001 Temukan atau siapkan Android SDK, platform kompilasi, dan emulator/perangkat. Inisialisasi proyek Kotlin/Compose dengan minimum API 26, Gradle Wrapper, JDK 17, namespace awal `com.apkkasir`, dan versi plugin yang kompatibel di `settings.gradle.kts`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, serta `app/build.gradle.kts`. Jalankan `:app:assembleDebug` dan cek perangkat sebelum fase uji instrumentasi.
- [X] T002 Tambahkan Material 3, Navigation Compose, ViewModel, Room dengan pemroses skema, JUnit dan tes instrumentasi. Siapkan aplikasi satu modul, aktivitas utama dan resource Bahasa Indonesia di `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`, `app/src/main/java/com/apkkasir/MainActivity.kt`, dan `app/src/main/res/values/strings.xml`.

## Phase 2: Fondasi bersama

**Tujuan**. Siapkan akses, aturan uang dan waktu, serta penyimpanan lokal.

- [X] T003 Siapkan Room, registrasi entitas per fase, ekspor skema, dan penyedia dependensi lokal di `app/src/main/java/com/apkkasir/data/KasirDatabase.kt` dan `app/src/main/java/com/apkkasir/data/AppContainer.kt`. Simpan data di ruang privat aplikasi dan kecualikan database serta file privat dari backup dan transfer otomatis di `app/src/main/AndroidManifest.xml`, `app/src/main/res/xml/backup_rules.xml`, dan `app/src/main/res/xml/data_extraction_rules.xml`.
- [X] T004 [P] Implementasikan parser rupiah bulat, aritmetika 64-bit dengan penolakan luapan, format rupiah, dan kalkulator batas periode WIB `[awal, akhir)` dalam UTC di `app/src/main/java/com/apkkasir/payment/Rupiah.kt` dan `app/src/main/java/com/apkkasir/reports/ReportPeriod.kt`. Minggu dimulai Senin dan bulan memakai kalender.
- [X] T005 [P] Siapkan sesi lokal dengan otorisasi peran pada operasi data, penyedia peran untuk UI, dan pembersihan sesi ketika keluar di `app/src/main/java/com/apkkasir/data/LocalSession.kt` dan `app/src/main/java/com/apkkasir/ui/AppNavigation.kt`. Aksi yang kembali dari pemilih file memeriksa sesi aktif sebelum melanjutkan.
- [X] T006 Uji validasi angka dan luapan, rentang tengah malam WIB, Senin–Minggu, akhir bulan, dan perubahan zona HP di `app/src/test/java/com/apkkasir/MoneyAndPeriodTest.kt`.

## Phase 3: US5 - Penyiapan toko dan akun (P1)

**Tujuan**. Siapkan admin, kasir, kode pemulihan, dan gambar QRIS.

**Uji independen**. Pada instalasi baru, buat kedua akun, uji izin, keluar dan masuk, pulihkan akses tanpa menghapus data, serta pasang dan ganti gambar QRIS.

- [X] T007 [US5] Tulis uji penyiapan, pergantian akun, reset kasir, kode sekali pakai, gangguan setelah kredensial tersimpan sebelum kode terlihat, dan impor QRIS gagal di `app/src/androidTest/java/com/apkkasir/SetupAndAccessTest.kt`.
- [X] T008 [US5] Tambahkan entitas dan DAO di `app/src/main/java/com/apkkasir/data/AccountEntities.kt`, `app/src/main/java/com/apkkasir/data/AccountDao.kt`, dan `app/src/main/java/com/apkkasir/data/KasirDatabase.kt`. `User` menyimpan ID, nama masuk unik, peran `ADMIN` atau `CASHIER`, verifier, salt, dan parameter. Satu akun per peran. Reset mempertahankan ID. `StoreSettings` satu baris menyimpan referensi QRIS aktif dan verifier kode admin.
- [X] T009 [US5] Implementasikan verifier PBKDF2-HMAC-SHA256, salt unik, kode pemulihan acak sekali pakai, setup atomik, reset kasir dan pemulihan admin di `app/src/main/java/com/apkkasir/data/LocalAccounts.kt`. Rotasi verifier secara atomik. Admin aktif dapat membuat kode pengganti setelah verifikasi ulang kredensial. Tampilkan kode setelah simpan dan tolak kode lama setelah rotasi berhasil.
- [X] T010 [US5] Implementasikan layar penyiapan, masuk, keluar, reset dan buat kode pemulihan baru di `app/src/main/java/com/apkkasir/ui/AccountScreens.kt` dan daftarkan rute di `app/src/main/java/com/apkkasir/ui/AppNavigation.kt`. Back tidak membuka layar berizin setelah keluar.
- [X] T011 [US5] Implementasikan pemilihan satu gambar, validasi keterbacaan, pratinjau, dan pergantian referensi QRIS setelah salinan privat berhasil di `app/src/main/java/com/apkkasir/data/QrisImageStore.kt` dan `app/src/main/java/com/apkkasir/ui/QrisSettingsScreen.kt`. Impor gagal mempertahankan gambar lama dan memberi pesan.

## Phase 4: US4 - Katalog barang (P1)

**Tujuan**. Admin menambah barang dan mengubah harga yang dipakai kasir.

**Uji independen**. Admin membuat barang dan mengganti harga. Kasir melihat harga baru, tetapi perubahan katalog dari akun kasir ditolak.

- [X] T012 [US4] Tulis uji tambah barang, harga valid, dan penolakan perubahan katalog oleh kasir di `app/src/androidTest/java/com/apkkasir/CatalogAccessTest.kt`.
- [X] T013 [US4] Tambahkan `Product` dengan ID, nama terisi, dan harga positif dalam rupiah bulat, beserta DAO dan mutasi yang hanya mengizinkan admin di `app/src/main/java/com/apkkasir/data/ProductEntity.kt`, `app/src/main/java/com/apkkasir/data/ProductDao.kt`, `app/src/main/java/com/apkkasir/data/ProductStore.kt`, dan `app/src/main/java/com/apkkasir/data/KasirDatabase.kt`.
- [X] T014 [US4] Bangun layar tambah barang dan ubah harga dengan validasi dan pembaruan daftar di `app/src/main/java/com/apkkasir/ui/CatalogScreen.kt` dan hubungkan rute di `app/src/main/java/com/apkkasir/ui/AppNavigation.kt`.

## Phase 5: US1 - Tagihan dan pembayaran (P1)

**Tujuan**. Kasir membuat dan menyelesaikan tagihan Tunai atau QRIS manual.

**Uji independen**. Tagihan Rp33.000 menerima Tunai pas atau lebih dan menghitung kembalian. QRIS tersedia setelah gambar dipasang. Konfirmasi tersimpan menghasilkan satu nomor nota.

- [X] T015 [US1] Tulis uji subtotal, total, nominal tidak valid, kurang, pas dan lebih, serta perubahan metode di `app/src/test/java/com/apkkasir/CheckoutRulesTest.kt`.
- [X] T016 [US1] Tambahkan `Sale` dan `SaleLine` di `app/src/main/java/com/apkkasir/data/SaleEntities.kt`, `app/src/main/java/com/apkkasir/data/SaleDao.kt`, dan `app/src/main/java/com/apkkasir/data/KasirDatabase.kt`. Nomor tagihan unik. Nominal tunai, nomor nota unik, waktu dan akun konfirmasi pertama kosong sebelum tersedia. Simpan ID kasir, waktu pembuatan, total, status dan metode. Baris menyimpan ID tagihan, urutan, ID barang, salinan nama dan harga, kuantitas serta subtotal. Bekukan baris dan total saat tagihan dibuat dan indeks waktu konfirmasi pertama.
- [X] T017 [US1] Implementasikan pembuatan tagihan valid dan transisi `MENUNGGU` ke `LUNAS` dengan verifikasi peran kasir di `app/src/main/java/com/apkkasir/payment/CheckoutService.kt`. Mutasikan status, waktu konfirmasi pertama, akun dan nomor nota dalam satu transaksi Room. Konfirmasi ulang tagihan Lunas mengembalikan nota yang sama. Tolak konfirmasi biasa pada tagihan Dibatalkan.
- [X] T018 [US1] Implementasikan keranjang dan layar pembayaran di `app/src/main/java/com/apkkasir/ui/CheckoutScreen.kt`, `app/src/main/java/com/apkkasir/ui/CheckoutViewModel.kt`, dan `app/src/main/java/com/apkkasir/ui/AppNavigation.kt`. Tampilkan nomor tagihan dan total, nonaktifkan QRIS jika gambar belum tersedia, tampilkan QR toko serta tombol Konfirmasi di bawah gambar, dan tampilkan kekurangan atau kembalian Tunai. Konfirmasi QRIS berasal dari tindakan kasir setelah melihat notifikasi BRI.

## Phase 6: US6 - Pemulihan tagihan (P1)

**Tujuan**. Tagihan aktif bisa dilanjutkan setelah aplikasi ditutup atau penyimpanan gagal.

**Uji independen**. Ulangi pembayaran setelah interupsi sebelum dan sesudah commit. Tagihan tetap sama, nominal lama yang sudah dihapus tidak muncul lagi, dan nota tidak ganda.

- [X] T019 [US6] Tulis uji Room dan UI untuk penyimpanan draft, input kosong atau tidak valid, pergantian metode, gangguan sebelum/sesudah commit, pembatalan belum bayar, dan pemulihan setelah keluar di `app/src/androidTest/java/com/apkkasir/CheckoutRecoveryTest.kt`. Simpan nominal cukup, buat penyimpanan perubahan terbaru gagal, lalu pastikan konfirmasi tetap nonaktif. Setelah percobaan ulang berhasil, periksa bahwa konfirmasi memakai perubahan terbaru dan hanya menghasilkan satu nota bila pembayaran dapat dikonfirmasi.
- [X] T020 [US6] Simpan perubahan metode dan nominal sesuai urutan input untuk tagihan yang sama di `app/src/main/java/com/apkkasir/payment/CheckoutService.kt` dan `app/src/main/java/com/apkkasir/data/SaleDao.kt`. Input kosong atau tidak valid dan perpindahan ke QRIS menyimpan nominal kosong. Tolak konfirmasi selama perubahan terbaru belum tersimpan atau gagal disimpan. Validasi metode dan nominal terbaru saat konfirmasi. Tambahkan pembatalan tagihan Menunggu yang belum dibayar dan pertahankan rekornya.
- [X] T021 [US6] Tambahkan akses tagihan Menunggu, pemulihan metode dan nominal Tunai yang masih tersimpan, hitung ulang kembalian, dan pesan percobaan ulang setelah gagal simpan pada tagihan sama di `app/src/main/java/com/apkkasir/ui/CheckoutScreen.kt` dan `app/src/main/java/com/apkkasir/ui/CheckoutViewModel.kt`. Nonaktifkan Konfirmasi saat perubahan terbaru sedang disimpan atau gagal disimpan. Untuk Tunai, aktifkan kembali hanya setelah perubahan terbaru berhasil tersimpan dan nominalnya mencukupi.

## Phase 7: US2 - Nota dan rekap hari ini (P1)

**Tujuan**. Kasir melihat nota. Admin melihat tabel dan total hari ini.

**Uji independen**. Selesaikan Tunai dan QRIS, buka nota serta laporan, lalu cocokkan nomor, waktu, metode dan total. Tagihan belum pernah lunas tidak masuk laporan.

- [X] T022 [US2] Tulis uji konsistensi nota dan laporan, ketahanan harga setelah katalog berubah, status Dibatalkan setelah lunas, serta batas WIB di `app/src/androidTest/java/com/apkkasir/ReceiptAndDailyReportTest.kt`.
- [X] T023 [US2] Implementasikan kueri nota dan baris laporan dari transaksi tersimpan di `app/src/main/java/com/apkkasir/data/SaleDao.kt` dan `app/src/main/java/com/apkkasir/reports/SalesReport.kt`. Satu baris per transaksi yang pernah dikonfirmasi pada periode. Hitung jumlah dan total dari status Lunas. Keluarkan tagihan tanpa konfirmasi pertama.
- [X] T024 [US2] Bangun layar nota yang dapat dibuka ulang oleh kasir yang melayani. Tampilkan nomor, waktu konfirmasi, nama barang, harga satuan, kuantitas, subtotal, total, metode dan status di `app/src/main/java/com/apkkasir/ui/ReceiptScreen.kt` dan `app/src/main/java/com/apkkasir/ui/AppNavigation.kt`.
- [X] T025 [US2] Bangun tabel hari ini untuk admin, keadaan kosong, agregat serta tautan nomor nota ke detail di `app/src/main/java/com/apkkasir/ui/DailyReportScreen.kt` dan `app/src/main/java/com/apkkasir/ui/AppNavigation.kt`. Tampilkan waktu konfirmasi, total, metode dan status. Pastikan data mengikuti tanggal berjalan WIB.

## Phase 8: US7 - Koreksi admin (P1)

**Tujuan**. Admin memperbaiki metode, membatalkan status lunas yang keliru, dan memulihkan QRIS terlambat.

**Uji independen**. Koreksi metode tidak mengubah total. Batalkan transaksi yang keliru lunas, pulihkan QRIS yang sah, lalu cocokkan jejak audit dan nomor nota.

- [X] T026 [US7] Tulis uji izin, koreksi metode, pembatalan, pemulihan QRIS dengan dan tanpa nota, pengulangan, dan jejak audit di `app/src/androidTest/java/com/apkkasir/SaleCorrectionTest.kt`.
- [X] T027 [US7] Tambahkan `SaleCorrection` dengan ID, ID tagihan, jenis tindakan, nilai sebelum/sesudah, alasan, ID admin dan waktu di `app/src/main/java/com/apkkasir/data/SaleCorrectionEntity.kt`, `app/src/main/java/com/apkkasir/data/SaleCorrectionDao.kt`, dan `app/src/main/java/com/apkkasir/data/KasirDatabase.kt`. Tambahkan relasi dan riwayat untuk setiap koreksi berhasil.
- [X] T028 [US7] Implementasikan koreksi metode, pembatalan lunas keliru, dan pemulihan QRIS terlambat oleh admin dengan alasan dalam transaksi Room yang sama dengan riwayat di `app/src/main/java/com/apkkasir/payment/SaleCorrectionService.kt`. Pertahankan nomor nota dan waktu pertama jika ada. Jika belum ada, gunakan satu nota dengan waktu pemulihan. Pengulangan pemulihan tidak menambah penjualan.
- [X] T029 [US7] Bangun detail koreksi, pencarian nomor nota atau tagihan, pencarian tagihan dibatalkan berdasarkan tanggal, dan tampilan riwayat di `app/src/main/java/com/apkkasir/ui/SaleCorrectionScreen.kt` dan `app/src/main/java/com/apkkasir/ui/AppNavigation.kt`. Admin mencocokkan nominal dan waktu notifikasi BRI secara manual.

## Phase 9: US3 - PDF tiga periode dan arsip (P2)

**Tujuan**. Admin membuat, membuka, mengekspor, dan menghapus PDF privat.

**Uji independen**. Buat laporan harian, mingguan dan bulanan, ekspor salinan, koreksi transaksi dan buat ulang PDF. Hapus arsip sebagai admin dan cocokkan data asal.

- [X] T030 [US3] Tulis uji snapshot PDF, agregat periode, akses admin, arsip lama, dan pemulihan interupsi file sementara, file selesai sebelum metadata, serta file terhapus sebelum metadata dibersihkan di `app/src/androidTest/java/com/apkkasir/ReportArchiveTest.kt`.
- [X] T031 [US3] Tambahkan `ReportDocument` dengan ID, jenis dan batas periode, waktu dibuat, nama tampilan, dan lokasi file privat di `app/src/main/java/com/apkkasir/data/ReportDocumentEntity.kt`, `app/src/main/java/com/apkkasir/data/ReportDocumentDao.kt`, dan `app/src/main/java/com/apkkasir/data/KasirDatabase.kt`. Satu dokumen per pembuatan. File arsip mempertahankan isi aslinya.
- [X] T032 [US3] Render hasil kueri periode yang konsisten ke PDF multi-halaman dengan tajuk berulang di `app/src/main/java/com/apkkasir/reports/PdfReportWriter.kt`. Sertakan rentang, waktu pembuatan, seluruh baris dan status, metode, serta jumlah dan total. Tangani periode kosong dan baris dibatalkan.
- [X] T033 [US3] Implementasikan pembuatan file sementara, finalisasi, registrasi, buka PDF privat, ekspor salinan, hapus setelah konfirmasi, dan pemeriksaan arsip privat sebelum daftar tampil di `app/src/main/java/com/apkkasir/reports/ReportArchive.kt`. Pemeriksaan eksklusif terhadap pembuatan, ekspor, dan penghapusan. Bersihkan file tak terdaftar serta entri yang filenya hilang dan sediakan percobaan ulang.
- [X] T034 [US3] Bangun pemilihan periode/tanggal acuan, daftar PDF, penampil, ekspor melalui pemilih Android, dan penghapusan khusus admin di `app/src/main/java/com/apkkasir/ui/ReportDocumentsScreen.kt` dan `app/src/main/java/com/apkkasir/ui/AppNavigation.kt`. Penolakan akses kasir, kegagalan pembuatan/ekspor, dan status kosong tampil dengan jelas.

## Phase 10: Verifikasi lintas fitur

**Tujuan**. Validasi paket implementasi pada emulator dan perangkat.

- [ ] T035 Jalankan build dan unit test, instrumentasi Room/Compose, serta validasi API 26 dan perangkat acuan melalui `gradlew.bat` dan `specs/001-kasir-qris/quickstart.md`. Cek TalkBack, mode pesawat untuk alur internal, pemindaian QR toko, dan ekspor PDF. Dengan 1.000 transaksi pada perangkat acuan, ukur target tabel hari ini tampil ≤2 detik dan PDF bulanan selesai ≤10 detik. Catat hasil pengukuran, perangkat, dan batas pengujian pada `specs/001-kasir-qris/quickstart.md`.

## Dependensi dan urutan pengerjaan

- Setup → Fondasi. Setelah T003, T004 dan T005 dapat dikerjakan paralel pada file terpisah. T006 memeriksa hasil T004.
- Fondasi → US5 → US4 → US1. US5 menyediakan akun dan gambar QRIS. US4 menyediakan data barang untuk kasir.
- US1 → US6 → US2 → US7 → US3. US6 menyelesaikan siklus tagihan. US2 menyediakan query laporan dan detail. US7 menambah koreksi. US3 menggunakan status transaksi terbaru untuk membuat arsip.
- Verifikasi lintas fitur berjalan setelah seluruh alur terintegrasi. Pada setiap fase, siapkan skenario uji sebelum implementasi dan jalankan kembali saat alur siap.
- Contoh kerja paralel pada fase cerita setelah fondasi: tulis kasus uji aritmetika US1 dan kasus uji batas tanggal laporan dari fixture lokal pada file tes terpisah. Integrasi yang mengubah `KasirDatabase.kt`, `SaleDao.kt`, atau `AppNavigation.kt` dikerjakan sesuai urutan fase.

## Strategi implementasi

1. Bangun fondasi, penyiapan toko, katalog, dan pembayaran melalui Setup, Fondasi, US5, US4, dan US1.
2. **MVP demonstrasi setelah US2**. Selesaikan US6 dan US2 agar pembayaran Tunai atau QRIS dapat didemokan dari pembuatan tagihan, penyimpanan input dan konfirmasi, hingga nota tampil dan transaksi masuk laporan hari ini.
3. Selesaikan cakupan versi pertama dengan koreksi admin, arsip PDF tiga periode dan verifikasi perangkat.
4. Pertahankan [batas versi pertama](spec.md#batas-versi-pertama) saat implementasi.
