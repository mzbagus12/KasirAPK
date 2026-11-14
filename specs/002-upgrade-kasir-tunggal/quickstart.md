# Panduan Uji Upgrade Kasir Tunggal 1.5.0

Jalankan panduan ini setelah implementasi dengan data uji di emulator Android API 26 dan ulangi pemasangan melalui pemasang Android pada satu perangkat fisik uji. Gunakan gambar QRIS uji dan simulasi konfirmasi kasir.

## Prasyarat

- JDK 17, Android SDK 36, Build Tools dengan `apksigner` dan `aapt`, serta `adb` tersedia di PATH.
- Unduh APK [rilis resmi 1.0.0](https://github.com/mzbagus12/KasirAPK/releases/tag/v1.0.0). Isi `$apkLama` dengan path absolut berkas dan `$serial` dengan serial perangkat uji dari `adb devices -l`.
- Siapkan kunci penandatangan yang cocok dengan APK resmi. Jalankan build kandidat pada mesin yang memiliki kunci itu. Jalankan instrumentasi pada perangkat uji terpisah dari skenario pemasangan resmi agar Gradle tidak mengganti APK sumber pada fixture upgrade.

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
$apkBaru = (Resolve-Path -LiteralPath 'app/build/outputs/apk/debug/app-debug.apk').Path
apksigner verify --verbose --print-certs $apkLama
apksigner verify --verbose --print-certs $apkBaru
aapt dump badging $apkLama | Select-String 'package:|sdkVersion:'
aapt dump badging $apkBaru | Select-String 'package:|sdkVersion:'
Get-FileHash -Algorithm SHA256 -LiteralPath $apkBaru
adb devices -l
```

Kedua APK harus memiliki sertifikat SHA-256 yang sama dan ID `com.apkkasir`. Kandidat harus memiliki `versionName=1.5.0`, `versionCode=2` dan minimum API 26. Hentikan jalur pemasangan jika kandidat tidak cocok. [apksigner](https://developer.android.com/tools/apksigner)

## Update dari APK resmi

1. Pada perangkat uji kosong, pasang `adb -s $serial install $apkLama`. Siapkan toko dan dua login melalui UI 1.0.0.
2. Pakai 1.0.0 untuk menambah barang, memasang QRIS, menyimpan tagihan Tunai Menunggu dengan nominal uang diterima, dan membuat transaksi Tunai serta QRIS lunas. Batalkan satu tagihan QRIS dan pulihkan melalui admin dengan alasan. Buat PDF ketiga periode, lalu ekspor salinan satu PDF ke lokasi lokal yang dapat dibuka dari aplikasi File.
3. Catat nomor tagihan dan nota, status, nominal, metode, waktu konfirmasi, alasan dan nama pelaku, jumlah transaksi, serta total per periode. Catat nama arsip dan lokasi salinan ekspor. Untuk uji transisi, ikuti panduan reset di [kontrak UI](contracts/ui.md#panduan-pemilik-sebelum-upgrade) dengan asumsi hanya login admin tersedia.
4. Dengan 1.0.0 masih terpasang, jalankan `adb -s $serial install -r $apkBaru`. Pada perangkat fisik uji, ulangi dengan membuka APK kandidat di aplikasi File dan menyetujui pembaruan. Gunakan profil uji terpisah untuk tiap pengulangan. [ADB install](https://developer.android.com/tools/adb#pm)
5. Buka aplikasi dan cocokkan data awal sebelum melakukan transaksi baru. Login kasir berfungsi, admin lama ditolak, dan nama admin masih terlihat di riwayat pemulihan. Buka kedua salinan PDF dan cocokkan dengan isi sebelum upgrade. Lanjutkan tagihan Tunai yang tersimpan, cocokkan kembaliannya, lalu konfirmasi. Hanya satu nota dan satu penjualan baru tercatat.
6. Jalankan alur Tunai dan QRIS, pengelolaan katalog, penggantian QRIS, koreksi dengan alasan, pemulihan QRIS dan pembuatan PDF untuk ketiga periode. Ekspor PDF dan hapus satu arsip setelah konfirmasi. Transaksi asal dan salinan ekspor tetap dapat dibuka. Keluar, coba kembali ke layar pengelolaan, lalu periksa bahwa login diperlukan.
7. Pada instalasi hasil upgrade, pulihkan akses dengan kode yang diterbitkan lewat UI APK resmi 1.0.0. Periksa bahwa kata sandi baru bisa dipakai, ID kasir dan data toko tetap sama, serta kode yang telah dipakai ditolak. Catat hasil tanpa menyertakan kode atau kata sandi.

## Matriks uji tambahan

| Skenario | Hasil yang harus dibuktikan |
| --- | --- |
| 1.5.0 baru dan pembaruan 1.0.0 yang belum disiapkan | Kedua jalur menampilkan penyiapan satu kasir. Satu akun dibuat dan kode pemulihan diterbitkan. |
| Pemulihan akun | Uji kode dari instalasi baru dan kode 1.0.0 yang masih berlaku pada toko hasil upgrade. Uji kode salah, kode bekas, ganti kata sandi dan penerbitan ulang setelah tampilan kode terputus. Pada uji konkurensi, tahan login lama setelah membaca verifikator, selesaikan pemulihan, lalu lanjutkan login lama. Login itu harus ditolak tanpa membentuk sesi. ID kasir dan data toko tetap sama. |
| Kegagalan sebelum commit migrasi | Pada fixture uji, injeksikan gangguan setelah perubahan baris admin dan sebelum commit. Periksa rollback versi dan nilai lama, retry berhasil, serta pembukaan berikutnya tidak menambah baris atau mengubah file. |
| Data tidak konsisten | Pada salinan fixture terpisah, uji pengaturan atau akun hilang, foreign key putus, `Sale.cashierId` menunjuk admin dengan foreign key valid, gambar QRIS aktif hilang, PDF yang dirujuk hilang, dan basis data rusak. Untuk data yang masih bisa dibaca, verifikasi versi dan nilai baris lama, termasuk metadata PDF, tetap sama setelah pembaruan tertahan. Pada semua kasus, login dan setup tidak terbuka dan file yang masih ada dipertahankan. Jalankan kontrol dengan QRIS belum dipasang, arsip kosong, serta satu toko valid dengan pemulihan QRIS oleh admin. |
| APK penandatangan berbeda | Coba pada perangkat uji terpisah yang berisi 1.0.0. Android menolak pembaruan dan aplikasi 1.0.0 tetap bisa membuka data. |
| Izin dan regresi | Uji panggilan layanan tanpa sesi. Untuk pemilih gambar dan ekspor PDF, tahan hasil dari sesi awal, lalu kirim saat kasir sudah keluar dan ulangi setelah akun yang sama masuk kembali. Pastikan hasil lama tidak memunculkan pratinjau, mengganti QRIS, atau memulai penulisan isi PDF. Buka pemilih baru dan pastikan kedua aksi tetap berhasil. Ulangi uji checkout, koreksi, periode WIB, akses arsip dan pembacaan PDF dengan fixture transaksi tanpa pembayaran sungguhan. |

Jalankan `.\gradlew.bat :app:connectedDebugAndroidTest` pada satu emulator uji yang terhubung. Simpan hasil unit, lint, instrumentasi, verifikasi signer dan pemasangan fisik bersama catatan perbandingan data. Status penerimaan upgrade bergantung pada keberhasilan pemasangan di atas APK resmi dan perbandingan data, serta uji negatif pada fixture terpisah.

## Verifikasi aset saat rilis

Simpan berkas kandidat dan SHA-256 yang dicatat sebelum uji. Setelah kandidat lulus, lampirkan berkas yang sama ke draf rilis GitHub. Ambil aset dari draf dengan akses repo, lalu isi `$apkAsetRilis` dengan path absolut berkas unduhan. Cocokkan dengan `$shaKandidatLulus`, yaitu SHA-256 yang tersimpan pada hasil uji. [Dokumentasi aset rilis GitHub](https://docs.github.com/en/rest/releases/assets#get-a-release-asset)

```powershell
$shaAsetRilis = (Get-FileHash -Algorithm SHA256 -LiteralPath $apkAsetRilis).Hash
if ($shaAsetRilis -ne $shaKandidatLulus) { throw 'Aset GitHub berbeda dari APK yang lulus uji upgrade.' }
```

Sebelum publikasi, pastikan hash cocok dan catatan rilis memuat atau menautkan [panduan pemilik sebelum upgrade](contracts/ui.md#panduan-pemilik-sebelum-upgrade). Pengguna harus bisa membaca langkah cek login kasir dan simpan kode pemulihan sebelum memasang APK. Jika kandidat dibangun ulang dan hash berubah, gunakan berkas yang lulus atau uji ulang APK pengganti sebelum menerbitkannya.

## Hasil verifikasi 2026-11-13

- Build APK debug, 6 unit test, dan 20 tes instrumentasi pada emulator API 26 selesai. Lint mencatat 0 error dan 25 peringatan. Ekspor skema Room 1 dan 2 memiliki struktur fisik dan identity hash yang sama.
- Kandidat `com.apkkasir` versi 1.5.0 dengan `versionCode=2` dan minimum API 26 memakai sertifikat yang sama dengan APK resmi 1.0.0. SHA-256 kandidat adalah `CC83286D4391A8363DF298F31E35D281C867ED832E7512761EAF8688ACA6871C`.
- Android menerima pemasangan kandidat di atas 1.0.0 pada emulator tanpa uninstall selama update. Sesudah update pertama, gambar QRIS aktif dan file PDF privat tidak ditemukan. Startup menahan migrasi. Snapshot memperlihatkan Room masih versi 1 dengan dua identitas, tiga tagihan dan tiga metadata PDF. Salinan PDF eksternal masih ada. Penyebab hilangnya berkas privat belum terbukti.
- Percobaan ulang pada emulator API 26 dengan instalasi awal 1.0.0 yang bersih berhasil. APK penandatangan berbeda ditolak dan 1.0.0 tetap terpasang. Setelah kandidat yang cocok dipasang tanpa uninstall, snapshot sebelum pembukaan pertama masih memakai Room 1. Setelah aplikasi dibuka, Room naik ke versi 2. Pada kedua titik pemeriksaan, hash gambar QRIS, ketiga PDF arsip, dan salinan ekspor cocok dengan baseline. Isi tabel toko, jumlah tagihan, dan riwayat juga cocok. PDF bulanan lama dapat dibuka dari UI 1.5.0.
- Kredensial kasir lama berhasil masuk dan kredensial admin lama ditolak. Riwayat pemulihan QRIS tetap menampilkan nama admin lama. Tagihan Tunai Menunggu memulihkan Rp50.000, menampilkan kembalian Rp17.000, dan menerbitkan satu nota. Laporan hari ini menunjukkan 3 transaksi lunas dengan total Rp99.000.
- Kode yang terbit dari UI 1.0.0 memulihkan akun kasir pada ID yang sama. Kata sandi baru berhasil dipakai, kode yang telah dipakai ditolak, dan laporan serta data toko tetap tersedia.
- Instalasi bersih 1.5.0 dan upgrade dari 1.0.0 yang belum disiapkan menampilkan setup satu akun. Pada jalur upgrade, kode diterbitkan dan kasir baru berhasil masuk. Dua tes pemilih pada layanan dan UI Compose lulus. Uji perangkat fisik ditunda sesuai arahan pengguna.
- Pada fixture terpisah, admin 1.0.0 mereset kata sandi kasir dan menerbitkan kode pemulihan pengganti melalui UI. Login kasir dengan kata sandi hasil reset berhasil sebelum dan sesudah update.

## Hasil pemasangan 2026-11-14

- Package Installer di emulator Android API 26 menerima 1.5.0 sebagai pembaruan dari 1.0.0. Hash satu PDF arsip yang tersedia tetap cocok. Pengujian sebelumnya memakai APK resmi 1.0.0 untuk verifikasi data yang lebih lengkap.
- Package Installer di Infinix X6857, Android API 36, menerima kandidat 1.5.0 tanpa uninstall. Play Protect memindai APK sebelum pemasangan selesai. Hash APK 1.0.0 yang terpasang berbeda dari aset GitHub, dengan ID paket, versi, dan sertifikat yang cocok.
- Snapshot sebelum update memakai Room 1. Snapshot setelah pemasangan sudah memakai Room 2, sehingga tahap sebelum migrasi tidak teramati di Infinix. Hash APK 1.5.0 terpasang cocok dengan kandidat. Hash data toko dan gambar QRIS cocok setelah memperhitungkan penonaktifan kredensial admin. Baseline fisik berisi dua barang dan satu transaksi dengan satu detail. Baseline fisik tidak memiliki PDF arsip.
- Pengguna mengonfirmasi login kasir berhasil di Infinix. Pemeriksaan UI melihat menu tagihan, riwayat, laporan hari ini, barang, dokumen PDF, dan QRIS toko. Pemeriksaan isi layar riwayat dan QRIS melalui ADB terhenti saat layar terkunci. Hash data dan gambar sudah diverifikasi melalui snapshot.
