# Riset teknis

## Android native

**Keputusan**. Kotlin, Jetpack Compose, ViewModel, dan satu modul Android. Minimum API 26 menjadi baseline awal untuk `java.time` dan API platform. Pilih versi stabil yang kompatibel saat inisialisasi proyek.

**Alasan**. Produk hanya untuk Android. API sistem menangani pemilihan gambar, penyimpanan dokumen, dan PDF dari satu toolchain. [Arsitektur Android](https://developer.android.com/topic/architecture/recommendations) · [java.time](https://developer.android.com/reference/java/time/package-summary)

**Alternatif**. Flutter tersedia pada host dan layak jika sasaran bertambah ke platform lain. Versi ini memakai API Android secara langsung.

## Transaksi lokal

**Keputusan**. Gunakan Room/SQLite untuk akun, katalog, tagihan, dan riwayat koreksi. Simpan konfirmasi, perubahan status, nomor nota, dan koreksi yang terkait dalam transaksi database. Tabel laporan dibentuk dari query transaksi.

**Alasan**. Room memberi verifikasi kueri saat kompilasi. Batas transaksi menjaga status lunas, nomor nota, dan jejak koreksi konsisten jika operasi gagal. [Panduan Room](https://developer.android.com/training/data-storage/room) · [Room Transaction](https://developer.android.com/reference/androidx/room/Transaction)

**Alternatif**. File JSON atau penyimpanan preferensi menambah pekerjaan untuk kueri periode dan mutasi beberapa data secara konsisten.

## Akun lokal dan penyimpanan privat

**Keputusan**. Simpan verifier kredensial dengan PBKDF2-HMAC-SHA256, salt unik, dan parameter yang dapat ditingkatkan. Gunakan kode pemulihan acak berentropi tinggi yang hanya dapat dipakai satu kali dan simpan verifier-nya. Validasi peran pada operasi mutasi dan akses dokumen, serta akhiri sesi saat keluar. Simpan data privat di ruang aplikasi dan kecualikan data versi pertama dari backup serta transfer otomatis Android.

**Alasan**. Satu perangkat dapat menjalankan login dan reset tanpa jaringan. Verifier menghindari penyimpanan kredensial asli. Pengecualian backup mengikuti batas penyimpanan yang telah disetujui. [SecretKeyFactory](https://developer.android.com/reference/javax/crypto/SecretKeyFactory) · [Android Auto Backup](https://developer.android.com/identity/data/autobackup)

**Alternatif**. Login melalui layanan identitas memerlukan layanan eksternal. PIN perangkat saja tidak membedakan admin dari kasir yang menggunakan HP yang sama.

## QRIS toko

**Keputusan**. Admin memilih gambar dari pemilih foto Android. Aplikasi membaca, memvalidasi sebagai gambar, menampilkan pratinjau, lalu menyalin ke penyimpanan privat. Ganti referensi aktif setelah salinan baru berhasil disimpan. Saat gagal, pertahankan gambar sebelumnya. Sebelum pemasangan pertama berhasil, Tunai tersedia dan pilihan QRIS menampilkan alasan belum tersedia.

**Alasan**. Salinan lokal tetap tersedia saat akses URI asal berakhir. Pratinjau memungkinkan admin memastikan gambar milik toko. Kasir memeriksa notifikasi BRI secara manual. [Photo picker Android](https://developer.android.com/training/data-storage/shared/photo-picker)

**Alternatif**. URI permanen lebih bergantung pada ketersediaan dokumen asal. Integrasi gateway berada di luar versi pertama.

## PDF lokal dan ekspor

**Keputusan**. Gunakan `PdfDocument` dengan pemenggalan halaman dan judul kolom berulang. Ambil satu hasil query konsisten saat membuat dokumen. Simpan PDF jadi ke arsip privat, buka dengan `PdfRenderer`, dan gunakan Storage Access Framework untuk menyalin dokumen keluar. Gunakan file sementara agar arsip yang gagal dibuat tidak muncul sebagai PDF siap dibuka.

**Alasan**. PDF menjadi arsip tetap, sementara laporan baru dapat menyertakan koreksi. Pemilih sistem memberi admin kontrol atas lokasi salinan ekspor. [Android PdfDocument](https://developer.android.com/reference/android/graphics/pdf/PdfDocument) · [Android PdfRenderer](https://developer.android.com/reference/android/graphics/pdf/PdfRenderer) · [Storage Access Framework](https://developer.android.com/training/data-storage/shared/documents-files)

**Alternatif**. Pencetakan dari HTML menambah ketergantungan pada perender dan alur cetak. Pembuatan di server bertentangan dengan alur lokal.

## Batas periode

**Keputusan**. Hitung interval dari tengah malam WIB awal periode sampai tengah malam sesudah akhir periode, eksklusif. Query memakai waktu konfirmasi pertama dalam UTC. Senin menjadi awal minggu dan bulan memakai kalender.

**Alasan**. Satu aturan memberi batas konsisten pada tabel hari ini dan ketiga jenis PDF. Koreksi mempertahankan tanggal konfirmasi pertama. [java.time](https://developer.android.com/reference/java/time/package-summary)

**Alternatif**. Memakai zona perangkat langsung dapat menggeser hasil saat pengguna mengganti zona perangkat.
