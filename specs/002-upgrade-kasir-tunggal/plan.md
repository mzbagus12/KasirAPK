# Rencana Implementasi Upgrade Kasir Tunggal 1.5.0

**Branch Git** `release/1.5.0` · **Tanggal** 2026-11-13 · **Fitur Spec Kit** `002-upgrade-kasir-tunggal`

**Spesifikasi** [Upgrade Kasir Tunggal](spec.md)

## Ringkasan

Pasang 1.5.0 langsung di atas APK resmi 1.0.0. Pertahankan akun kasir, identitas pelaku lama, data penjualan, dan berkas toko. Setelah upgrade, satu login kasir membuka penjualan dan semua menu pengelolaan. Pembayaran Tunai, konfirmasi QRIS manual, koreksi, dan perhitungan laporan memakai aturan 1.0.0.

## Konteks teknis

- **Platform** Android API 26 ke atas. Pertahankan `applicationId` `com.apkkasir`, `compileSdk` dan `targetSdk` 36. Setel `versionName` ke `1.5.0` dan `versionCode` ke `2`. Gunakan sertifikat APK resmi 1.0.0.
- **Teknologi** Kotlin 2.2.21, JDK 17, Android Gradle Plugin 8.13.1, Compose Material 3, Navigation Compose, dan Room 2.8.4 dari konfigurasi proyek.
- **Penyimpanan** Room `kasir.db`, gambar QRIS di `filesDir`, arsip di `filesDir/reports`, dan salinan PDF di lokasi ekspor pilihan pengguna. Skema fisik tetap sama. Nomor versi Room naik dari 1 ke 2 untuk menjalankan transisi akun sekali.
- **Pengujian** JUnit, instrumentasi Android, dan pemasangan APK resmi 1.0.0 lalu kandidat 1.5.0 pada perangkat uji. Validasi API 26 dan satu perangkat fisik untuk jalur pemasang Android.
- **Cakupan dan kinerja** Satu toko, satu perangkat, operasi lokal. Pemeriksaan migrasi tidak memuat seluruh transaksi atau PDF ke memori. Gunakan kembali skenario kinerja 1.0.0 pada perangkat acuan yang sama bila tersedia.

## Pemeriksaan konstitusi

`.specify/memory/constitution.md` masih berisi template. Pemeriksaan sebelum riset memakai persyaratan fitur sebagai acuan. Setelah desain, pemeriksaan mengonfirmasi bahwa paket dan penandatanganan dipertahankan, data lama dipertahankan, transisi akun bersifat atomik, dan operasi tetap membutuhkan sesi kasir. Uji upgrade mencakup APK resmi serta data yang dibuat lewat UI 1.0.0.

## Struktur proyek

```text
specs/002-upgrade-kasir-tunggal/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── contracts/ui.md
├── quickstart.md
└── checklists/requirements.md

app/
├── build.gradle.kts
├── schemas/com.apkkasir.data.KasirDatabase/
└── src/
    ├── main/java/com/apkkasir/
    │   ├── data/
    │   ├── payment/
    │   ├── reports/
    │   └── ui/
    ├── test/java/com/apkkasir/
    └── androidTest/java/com/apkkasir/
```

Pakai modul Android yang ada. Tempatkan migrasi dan pemeriksaan status toko bersama akses Room. Gabungkan jalur navigasi pada `AppNavigation` dan gunakan kembali layar transaksi, katalog, QRIS, laporan, dan dokumen.

## Fase 0 · Keputusan desain

[Riset](research.md) mencatat dasar kompatibilitas APK, migrasi Room, atribusi admin, pemulihan akun, dan strategi pengujian.

## Fase 1 · Desain perubahan

1. **Upgrade dan kesiapan toko.** Daftarkan `Migration(1, 2)` pada builder `kasir.db`. Sebelum mengubah baris atau commit, periksa kondisi akun, relasi pelaku sesuai aturan transaksi 1.0.0, serta keberadaan gambar QRIS aktif dan PDF yang dirujuk. Jika tidak konsisten, tahan migrasi dan pertahankan metadata serta berkas. Setelah pemeriksaan lulus, ubah identitas admin menjadi historis dan invalidasi verifikator login admin dalam transaksi Room. Jangan jalankan rekonsiliasi arsip dari jalur migrasi. Buka login atau penyiapan berdasarkan status yang sudah diverifikasi. Tampilkan **Coba lagi** untuk kegagalan sementara dan pesan yang sesuai untuk kondisi data yang menahan pembaruan. [Model data](data-model.md)
2. **Akun tunggal.** Jadikan `LocalAccounts` dan `LocalSession` satu pintu autentikasi kasir. Setup membuat akun dan pengaturan dalam satu transaksi. Recovery memakai kode 1.0.0 yang masih berlaku untuk mengganti kata sandi kasir pada ID yang sama, bersama penerbitan kode pengganti. Untuk perubahan dari sesi aktif, verifikasi kata sandi dan periksa ulang sesi serta verifikator sebelum commit.
3. **Alihkan izin dan gabungkan navigasi.** Kasir yang masuk dapat mengakses semua rute penjualan dan pengelolaan. Ikat permintaan pemilih gambar QRIS dan tujuan ekspor PDF ke sesi saat pemilih dibuka. Periksa sesi itu saat hasil diterima dan sebelum layanan mengubah data atau menulis PDF, termasuk ketika kasir yang sama sudah masuk kembali. Simpan ID kasir sebagai pelaku baru. Pertahankan nomor, status, waktu, dan atribusi historis. [Kontrak UI](contracts/ui.md)
4. **Verifikasi kandidat rilis.** Sesuaikan fixture dan pengujian 1.0.0 yang mengasumsikan dua login. Pertahankan asersi transaksi, nominal, periode, arsip dan konsistensi sesi. Bandingkan ID paket, versi, batas API, dan sidik jari sertifikat sebelum pengujian pemasangan. Buktikan update tanpa uninstall dari APK resmi yang telah diisi data uji. Catat SHA-256 kandidat yang lulus uji, gunakan berkas itu sebagai aset rilis, dan verifikasi hash aset GitHub sebelum publikasi. Sertakan panduan pemilik pada tahap rilis. [Quickstart](quickstart.md)

## Cakupan verifikasi

| Persyaratan | Bukti yang direncanakan |
| --- | --- |
| FR-001, FR-004 dan SC-001, SC-002 | Update APK resmi pada perangkat uji. Bandingkan transaksi, berkas, riwayat dan periode sebelum serta sesudah upgrade. Cocokkan hash kandidat yang lulus dengan aset rilis. |
| FR-002, FR-003, FR-005 dan SC-003 | Uji login kasir, penolakan admin, setup satu akun, akses menu, penolakan operasi tanpa sesi, dan penolakan hasil pemilih lama setelah login kembali. |
| FR-006 dan SC-005 | Uji ganti kata sandi, pemulihan dengan kode lama dan baru, penolakan kode bekas, dan kode pengganti yang belum terlihat. |
| FR-007 dan SC-004 | Suntikkan kegagalan pada salinan data uji. Periksa rollback, percobaan ulang, serta penahanan sebelum commit untuk kepemilikan tagihan yang keliru atau berkas yang dirujuk tetapi hilang. |
| FR-008 | Jalankan suite transaksi, koreksi, laporan, dan arsip. Cocokkan isi nota, aturan QRIS manual, dan agregat WIB. |
| FR-009 dan SC-006 | Ikuti panduan dari kondisi pemilik hanya mengingat login admin lama. Masuk dengan kredensial kasir yang diuji setelah upgrade. |
