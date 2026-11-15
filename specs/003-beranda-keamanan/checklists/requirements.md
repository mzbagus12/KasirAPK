# Checklist kualitas spesifikasi Kasir Toko 2.0.0

**Tujuan**: Memeriksa kesiapan draf untuk ditinjau sebelum plan

**Tanggal**: 14 November 2026

**Fitur**: [Beranda, navigasi, dan keamanan](../spec.md)

## Kualitas isi

- [x] Menjelaskan perilaku dan batas kompatibilitas tanpa memilih komponen UI, pustaka, atau metode migrasi.
- [x] Menjelaskan manfaat setiap alur bagi kasir dan pengelola toko.
- [x] Menggunakan label Indonesia yang sesuai dengan pekerjaan kasir.
- [x] Mengisi skenario, kasus tepi, kebutuhan, data, kriteria keberhasilan, dan asumsi.

## Kelengkapan kebutuhan

- [x] Tidak ada penanda klarifikasi yang belum diisi.
- [x] Urutan kartu, tujuan tab, dan hasil tiap uji ditentukan.
- [x] Ukuran layar, skala font, kesesuaian rekap widget, hasil audit, dan hasil pemasangan dapat diverifikasi.
- [x] Kriteria menyebut hasil yang bisa diamati pada Android.
- [x] Setiap skenario utama punya langkah penerimaan.
- [x] Kasus tepi mencakup ruang layar sempit, tombol Kembali, widget saat kosong atau gagal dimuat, pergantian hari WIB, dan kegagalan pemasangan.
- [x] Lingkup dibatasi pada penyederhanaan UI, keamanan, dan pembaruan Android.
- [x] Jalur distribusi GitHub, APK baseline, dan kondisi Play Protect dicantumkan.

## Kesiapan fitur

- [x] Cerita 1 dan 2 serta SC-001 sampai SC-003 mencakup FR-001 sampai FR-005. Cerita 1 dan SC-006 mencakup data dan keadaan widget pada FR-010.
- [x] Cerita 3 dan SC-004 mencakup audit FR-006, syarat sesi untuk widget pada FR-010, dan kondisi rilis FR-009.
- [x] Cerita 4 dan SC-005 mencakup pembaruan, Play Protect, dan kondisi rilis FR-007 sampai FR-009.
- [x] Pemilihan penandatanganan, rincian navigasi, dan pengujian kandidat menjadi pekerjaan tahap berikutnya.

## Hasil audit ulang sebelum plan

- **Rekap**. Widget mengambil total dan jumlah transaksi lunas dari rekap harian agar angka konsisten ketika transaksi berubah dan periode berganti.
- **Tata letak**. Aturan dua kolom pada 360 dan 375 dp khusus untuk beranda 2.0.0. Plan perlu menerapkannya pada pedoman desain halaman ini. Widget mengikuti lebar area konten dan berada di bawah empat kartu.
- **Akses**. Uji tanpa sesi dan setelah keluar mencakup ringkasan penjualan. Ikon kasir yang sudah tersedia bersifat dekoratif bagi TalkBack.
- **Regresi pembaruan**. Data uji mencakup tagihan menunggu, pelaku koreksi, PDF di arsip, dan salinan PDF yang telah diekspor. Instalasi baru menguji penyiapan satu akun kasir.
- **Kesiapan teknis**. Plan perlu membuktikan jalur tanda tangan untuk upgrade APK non-debuggable dari versi 1.5.0. Uji pemasangan membedakan hasil Play Protect dari kegagalan seperti ketidakcocokan tanda tangan.

## Pemeriksaan Antislop pada draf

- **PASS arah desain**. Referensi pengguna menentukan susunan ikon dan kartu. Palet dan tema mengikuti desain aplikasi.
- **PASS tujuan kontrol**. Empat kartu dan empat tab memetakan alur 1.5.0 yang ada. Riwayat memakai filter sesuai pintu masuk.
- **PASS rancangan interaksi**. Fokus, status aktif, area sentuh, dan batas gerak tercantum. Hover melayani masukan penunjuk.
- **PASS isi dan ikon widget**. Ringkasan memakai data penjualan yang ada dan membedakan kosong, memuat, dan gagal. Ikon mendampingi label dengan peran dekoratif.
- **PASS ketepatan klaim**. Status audit dan Play Protect didefinisikan sebagai syarat yang akan diuji pada kandidat rilis.

Pemeriksaan tampilan, kontras, penelusuran semua kontrol, keamanan, dan pemasangan kandidat menunggu implementasi serta APK 2.0.0.
