# Panduan verifikasi setelah implementasi

## Prasyarat

- Android Studio dengan Android SDK, JDK 17, serta emulator atau perangkat API 26 atau lebih baru.
- Perangkat toko atau perangkat uji untuk pemindaian QRIS dan pemilih file. Gunakan gambar uji untuk validasi biasa. Gunakan gambar QRIS toko untuk verifikasi pemindaian di perangkat.
- Jalankan dari akar workspace. Pastikan `JAVA_HOME` menunjuk ke JDK 17 dan `ANDROID_HOME` menunjuk ke Android SDK. Untuk SDK pada lokasi standar Windows, gunakan contoh berikut dan sesuaikan lokasi JDK.

```powershell
$env:JAVA_HOME = 'C:/Program Files/Eclipse Adoptium/jdk-17.0.20.8-hotspot'
$env:ANDROID_HOME = Join-Path $env:LOCALAPPDATA 'Android/Sdk'
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat :app:installDebug
```

## Skenario penerimaan

| Skenario | Hasil yang diperiksa |
|---|---|
| Penyiapan, pergantian akun, dan pemulihan | Buat admin serta kasir. Sebelum QRIS dipasang, Tunai tersedia dan QRIS menampilkan alasan. Setelah admin keluar, kasir hanya dapat membuka fitur kasir. Reset kasir mempertahankan akun. Kode admin memulihkan akses, menolak penggunaan ulang, dan mempertahankan data. |
| Gambar QRIS | Pasang gambar, periksa pratinjau dan pindai dengan perangkat lain. Coba ganti dengan file tidak valid atau simulasikan gagal simpan. Gambar sebelumnya tetap aktif. |
| Tunai dan pergantian metode | Buat tagihan Rp33.000. Rp30.000 menampilkan kekurangan Rp3.000. Rp33.000 menampilkan Rp0. Rp50.000 menampilkan Rp17.000. Buka ulang tagihan Tunai dan periksa pemulihan. Beralih ke QRIS lalu kembali ke Tunai membuat input kosong. |
| Pembayaran manual dan nota | Selesaikan satu transaksi Tunai dan satu QRIS. Untuk uji aplikasi, simulasikan keputusan kasir setelah notifikasi. Sebelum konfirmasi, laporan tidak bertambah. Setelah tersimpan, nota dan laporan mencatat metode yang sama. |
| Penyimpanan dan konfirmasi ulang | Simulasikan gagal simpan sebelum commit dan aplikasi tertutup setelah commit. Percobaan ulang pada tagihan yang sama menghasilkan paling banyak satu nota dan satu baris. Uji juga dalam mode pesawat untuk alur internal. |
| Koreksi dan pemulihan QRIS | Admin mengoreksi metode tanpa mengubah total, membatalkan pencatatan keliru lunas, lalu memulihkan tagihan asal ketika pembayaran QRIS terlambat. Periksa alasan, waktu, aktor, identitas nota, dan total. Kasir ditolak untuk tindakan ini. |
| Periode WIB | Siapkan transaksi pada kedua sisi tengah malam, akhir minggu, dan akhir bulan. Periksa batas harian, Senin–Minggu, dan bulan kalender. Baris yang dibatalkan setelah lunas terlihat dan tidak menambah agregat. |
| Arsip dan ekspor PDF | Buat PDF kosong dan PDF multi-halaman, buka dari arsip, lalu ekspor melalui pemilih Android. Setelah koreksi, PDF baru mencerminkan data terbaru dan PDF lama tetap sama. Admin dapat menghapus arsip lokal. Salinan di tujuan ekspor dan transaksi asal tetap tersedia. |
| Kode pemulihan belum sempat tampil | Hentikan aplikasi setelah kredensial dan verifier tersimpan, sebelum kode terlihat. Masuk dengan kredensial baru, lalu buat kode pengganti dengan memasukkan ulang kredensial. Pastikan kredensial salah ditolak, kode sebelumnya ditolak, dan data toko tetap tersedia. |
| Input Tunai dihapus | Simpan Rp50.000 untuk tagihan Rp33.000, lalu kosongkan kolom atau ganti dengan nilai tidak valid. Buka ulang setelah perubahan tersimpan. Nominal lama dan kembalian tidak muncul. **Konfirmasi** tetap nonaktif. |
| Proses arsip PDF terputus | Hentikan aplikasi saat file masih sementara, setelah file difinalisasi sebelum metadata tersimpan, dan setelah file dihapus sebelum metadata dibersihkan. Buka ulang. Daftar tidak memuat entri yang file-nya hilang dan file tak terdaftar dibersihkan. Arsip lain, transaksi, dan salinan ekspor tetap tersedia. |

Periksa navigasi dan label utama dengan TalkBack. Ukur tabel dan PDF untuk 1.000 transaksi serta catat perangkat acuan. Rincian aturan tersedia di [kontrak UI](contracts/ui.md) dan [model data](data-model.md).

## Hasil implementasi 2026-11-12

- APK debug `com.apkkasir` versi `1.0.0` ada di `app/build/outputs/apk/debug/app-debug.apk`. Tanda tangan APK v2 valid. Minimum API 26 dan target API 36. APK tidak meminta izin Internet.
- Build dan 6 unit test lulus. Seluruh 12 tes instrumentasi lulus di emulator Android API 26. Termasuk pemulihan akun, konfirmasi Tunai setelah gagal simpan, QRIS dengan gambar uji, koreksi admin, dan pemulihan arsip.
- Pengukuran 1.000 transaksi pada emulator API 26 mencatat tabel hari ini 400 ms dan pembuatan PDF bulanan 1.025 ms.
- Uji melalui UI emulator berhasil masuk setelah aplikasi dihentikan dan dibuka ulang, membuat laporan harian, serta menyimpan salinan melalui pemilih file Android. Salinan berukuran 32.174 byte dan memiliki header PDF valid.
- APK berhasil dipasang dan aktivitas utama dapat diluncurkan pada Infinix X6857 API 36. Suite instrumentasi HP fisik terhenti dan tidak memberikan hasil lengkap.
- Uji checkout dan pemulihan arsip lulus dalam mode pesawat di emulator API 26. Android melaporkan mode pesawat aktif dan tidak ada jaringan default.
- Gambar QRIS toko berhasil diimpor lewat UI. Kode dari tangkapan layar pembayaran didekode secara lokal dan hasilnya identik dengan lampiran. Tidak ada pembayaran BRI nyata.
- TalkBack aktif di Infinix X6857 dan fokus aksesibilitas terlihat pada judul layar penyiapan. Perpindahan fokus ke kolom formulir belum terverifikasi. Pengaturan aksesibilitas dikembalikan ke kondisi awal. T035 tetap terbuka untuk verifikasi perangkat yang tersisa.
