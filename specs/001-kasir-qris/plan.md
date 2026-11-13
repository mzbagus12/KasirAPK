# Implementation Plan: Kasir QRIS dan Laporan Harian

**Fitur aktif**: `001-kasir-qris`

**Branch Git**: Belum ada repositori Git. Identitas fitur berasal dari konfigurasi Spec Kit.

**Tanggal**: 2026-11-12

**Spesifikasi**: [spec.md](spec.md)

## Summary

Bangun satu aplikasi Android lokal dengan akun admin dan kasir. Kasir membuat tagihan, menerima Tunai atau menampilkan gambar QRIS BRI Merchant, lalu mengonfirmasi pembayaran secara manual. Data transaksi tersimpan menjadi sumber nota dan tabel hari ini. Admin mengelola katalog, koreksi, pemulihan QRIS terlambat, serta arsip dan ekspor PDF tiga periode.

## Technical Context

- **Bahasa dan build**. Kotlin 2.x, Gradle Kotlin DSL, dan JDK 17. Gunakan Gradle Wrapper dan versi stabil yang kompatibel dari template Android Studio saat proyek dibuat.
- **Platform**. Android 8.0/API 26 ke atas sebagai pilihan awal. Gunakan API target stabil yang kompatibel dengan toolchain saat membuat proyek.
- **UI**. Jetpack Compose Material 3, Navigation Compose, dan ViewModel. Antarmuka berbahasa Indonesia. Gunakan teks dan status yang bisa dibaca TalkBack.
- **Penyimpanan**. Room/SQLite untuk data relasional. Gambar QRIS dan arsip PDF di penyimpanan privat aplikasi. Gunakan Storage Access Framework untuk menyimpan salinan PDF ke lokasi yang dipilih admin.
- **Waktu dan nominal**. Simpan waktu dalam UTC dan turunkan batas laporan dengan `Asia/Jakarta`. Simpan rupiah sebagai bilangan bulat 64-bit dengan perhitungan yang mendeteksi luapan.
- **Pengujian**. JUnit untuk perhitungan, waktu, aturan peran, dan transisi. Tes instrumentasi Room dan Compose mencakup akses, alur kasir, pemulihan kode admin, pengosongan nominal Tunai, dan interupsi antara perubahan file dan metadata arsip. Verifikasi pemindaian QR dan ekspor pada perangkat.
- **Target kinerja awal**. Pada perangkat acuan Android, uji laporan hari ini dan PDF bulanan dengan 1.000 transaksi. Target tabel tampil ≤2 detik dan PDF selesai ≤10 detik. Catat perangkat dan hasil pengukuran.
- **Cakupan**. Satu toko, satu kasir, satu HP. Alur internal berfungsi tanpa internet. Notifikasi pembayaran BRI tetap dilihat oleh kasir di luar aplikasi. Tiga penundaan versi pertama mengikuti spesifikasi.

## Constitution Check

- **Sebelum riset**. `.specify/memory/constitution.md` masih berupa template dengan placeholder. Belum ada prinsip proyek yang disahkan untuk dinilai.
- **Setelah desain**. Model data, kontrak UI, dan quickstart mengikuti lingkup spesifikasi. Keputusan mengenai instalasi SDK, gambar QRIS toko, dan perangkat acuan dicatat sebagai prasyarat implementasi atau pengujian.

## Project Structure

### Documentation

```text
specs/001-kasir-qris/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── contracts/
│   └── ui.md
├── quickstart.md
└── checklists/
    └── requirements.md
```

### Source Code

```text
gradle/
app/
└── src/
    ├── main/
    │   ├── java/<namespace>/
    │   │   ├── data/
    │   │   ├── payment/
    │   │   ├── reports/
    │   │   └── ui/
    │   └── res/
    ├── test/
    └── androidTest/
```

**Keputusan struktur**. Satu modul `app`. ViewModel memanggil operasi lokal yang menegakkan peran dan transisi. Room menjadi sumber transaksi. UI menampilkan hasil operasi setelah penyimpanan berhasil.

## Urutan implementasi

1. Siapkan proyek Android, skema lokal, penyiapan akun, dan pengujian peran.
2. Bangun katalog, keranjang, tagihan, pembayaran Tunai dan QRIS, serta pemulihan tagihan dan nota.
3. Bangun laporan hari ini, koreksi admin, dan pemulihan pembayaran QRIS terlambat.
4. Bangun PDF tiga periode, arsip dan ekspor, lalu jalankan verifikasi pada [quickstart.md](quickstart.md).

## Artefak desain

- [Riset dan keputusan teknis](research.md)
- [Model data dan transisi](data-model.md)
- [Kontrak UI](contracts/ui.md)
- [Panduan verifikasi](quickstart.md)

## Complexity Tracking

Belum ada penyimpangan konstitusi yang memerlukan justifikasi.
