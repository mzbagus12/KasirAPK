# Panduan validasi Kasir Toko 2.0.0

Jalankan setelah perubahan aplikasi tersedia. Gunakan JDK 17, Android SDK 36, Build Tools, dan Command-Line Tools. Sediakan emulator API 26 serta perangkat fisik uji dengan Google Play Protect aktif. Pakai toko dan QRIS sintetis di profil uji terpisah. Ambil APK lama dari [rilis resmi 1.5.0](https://github.com/mzbagus12/KasirAPK/releases/tag/v1.5.0).

## 1. Jalankan suite dan buat kandidat

Dari akar proyek, hubungkan emulator API 26 untuk instrumentasi. Gunakan profil tanpa APK resmi agar build debug pengujian dapat dipasang secara terpisah. Siapkan akses ke penandatangan yang telah diverifikasi secara lokal. Ikuti konfigurasi release yang dihasilkan implementasi. Simpan kredensial di luar Git dan di luar log.

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:lintRelease :app:connectedDebugAndroidTest
.\gradlew.bat :app:assembleRelease
.\gradlew.bat :app:dependencies --configuration releaseRuntimeClasspath
```

Setel `$apkLama` dan `$apkKandidat` ke path absolut APK resmi dan APK release yang sudah ditandatangani. Jalankan alat berikut dari PATH SDK.

```powershell
Get-FileHash -Algorithm SHA256 -LiteralPath $apkLama,$apkKandidat
apksigner verify --verbose --print-certs --min-sdk-version 26 --max-sdk-version 36 $apkLama
apksigner verify --verbose --print-certs --min-sdk-version 26 --max-sdk-version 36 $apkKandidat
apkanalyzer apk summary $apkKandidat
apkanalyzer manifest min-sdk $apkKandidat
apkanalyzer manifest target-sdk $apkKandidat
apkanalyzer manifest debuggable $apkKandidat
apkanalyzer manifest print $apkKandidat
adb devices -l
```

Isi `$serial` dengan serial perangkat yang sedang diperiksa. Pilih serial emulator API 26 saat menguji jalur ADB.

Hash APK lama dan sertifikat harus cocok dengan [baseline riset](research.md#r4-penandatanganan-kandidat-dan-distribusi). Pada jalur kompatibilitas ini sertifikat kandidat harus cocok dengan APK lama. Kandidat harus menunjukkan `com.apkkasir`, versi `2.0.0` dengan kode `4`, minimum 26, target 36, dan `debuggable=false`. Jika pemeriksaan gagal, selesaikan penyebabnya sebelum memakai APK pada uji rilis. [apksigner](https://developer.android.com/tools/apksigner), [APK Analyzer](https://developer.android.com/tools/apkanalyzer)

## 2. Uji beranda, navigasi dan rekap

1. Pada toko uji kosong, selesaikan penyiapan satu kasir. Periksa empat kartu, widget **Rp0** dan **0 transaksi lunas**, serta keempat tab. Ulangi perpindahan tab dan uji Kembali dari detail, nota, checkout, hasil pembayaran, dan pengaturan.
2. Buat tagihan menunggu dan lanjutkan dari shortcut. Bayar tunai sampai kembalian tampil, lalu konfirmasi. Buat transaksi QRIS terpisah, tampilkan QRIS uji, dan konfirmasi manual. Buka nota dari hasil pembayaran dan dari daftar. Cocokkan nomor nota, laporan, dan asal navigasi. Uji ulang dengan tagihan tertunda dan beberapa status riwayat.
3. Cocokkan angka widget dengan laporan setelah setiap perubahan. Batalkan transaksi lunas dan pulihkan transaksi QRIS sesuai alur yang berlaku. Periksa kasus ketika laporan memuat baris batal dengan nol transaksi lunas. Simulasikan kegagalan rekap pada pengujian terfokus, periksa widget saat memuat atau gagal, lalu buka kembali beranda. Simulasikan pergantian tanggal WIB dengan beranda tetap terbuka dan saat kembali dari latar belakang.
4. Uji tema terang/gelap, lebar 360/375 dp, skala font 200%, TalkBack, keyboard, serta hover bila tersedia. Periksa fokus, tab aktif, kontras, area sentuh, inset sistem, label panjang dan nominal besar. Matikan animasi melalui pengaturan sistem untuk satu kali uji dan kembalikan pengaturannya setelah selesai.
5. Lanjutkan suite regresi untuk barang, QRIS, akun, pencarian dan koreksi, laporan, ekspor dan hapus arsip. Pada fixture 1.000 transaksi, mulai timer beranda sebelum render pertama dalam sesi baru tanpa cache rekap. Hentikan saat total dan jumlah lunas yang benar terlihat pada widget. Ukur laporan secara terpisah dari ketuk **Laporan hari ini** sampai ringkasan dan setidaknya satu baris transaksi terlihat di layar laporan. Catat apakah rekap sudah siap saat ketuk, serta model perangkat, API, dan konfigurasi build. Ukur waktu pembuatan PDF bulanan seperti uji yang ada. Pertahankan batas laporan 2 detik dan PDF 10 detik pada perangkat acuan setara.

## 3. Audit kandidat

| Area | Pemeriksaan |
| --- | --- |
| Paket dan komponen | Inventaris semua izin dan komponen efektif. Pastikan aktivitas uji dari varian debug tidak ikut terbawa. Periksa proteksi receiver/provider yang relevan. Pada perangkat uji, `adb -s $serial shell run-as com.apkkasir id` harus ditolak karena kandidat non-debuggable. |
| Sesi, akun dan pemulihan | Uji startup, keluar, Kembali dan login ulang. Uji kode lama setelah penggantian atau pemakaian. Jalankan regresi hasil pemilih yang terlambat dan pastikan perubahan data ditolak jika sesi asal sudah berakhir. |
| Penyimpanan dan backup | Periksa lokasi Room, QRIS, dan PDF. Tinjau aturan backup sebelum Android 12 serta pengecualian cloud dan transfer perangkat pada Android 12 ke atas sesuai cakupan perangkat uji. Cek kebocoran data sensitif di log dengan nilai sintetis. |
| Impor dan ekspor | Uji gambar rusak, terlalu besar, akses URI hilang, pembatalan pemilih, dan tujuan PDF yang gagal ditulis. Cocokkan QRIS aktif sebelum dan sesudah impor gagal. Periksa bahwa hapus arsip dan ekspor mengikuti aturan 1.5.0. |
| Dependensi | Cocokkan komponen langsung dan transitif `releaseRuntimeClasspath` dengan advisori terkini. Catat tanggal, sumber, versi, jangkauan pada aplikasi, dan keputusan per temuan. |

Catat area yang diperiksa, bukti, dampak, tingkat keparahan, cara reproduksi, tindakan, dan hasil uji ulang. Tuntaskan temuan tinggi dan kritis sebelum rilis. [OWASP MASVS](https://mas.owasp.org/MASVS/03-Using_the_MASVS/), [Android Auto Backup](https://developer.android.com/identity/data/autobackup)

### Penetapan tingkat keparahan

Gunakan kategori tertinggi yang sesuai dengan dampak dan prasyarat yang dibuktikan pada konfigurasi yang didukung.

| Tingkat | Kriteria proyek |
| --- | --- |
| Kritis | Pengambilalihan akun atau pembacaan, perubahan, atau penghapusan data toko dalam skala luas dapat terjadi tanpa sesi kasir, hak istimewa Android, atau interaksi kasir. |
| Tinggi | Kredensial, kode pemulihan, atau data transaksi dapat terekspos, akses kasir dapat dilewati, atau data penting dapat diubah atau hilang melalui alur yang dapat dijangkau dengan prasyarat praktis seperti akses fisik biasa, izin pengguna yang lazim, atau interaksi kasir. Dampak besar dengan prasyarat tambahan masuk tingkat ini jika prasyarat itu masuk akal untuk konfigurasi yang didukung. |
| Sedang | Dampak terbatas pada sebagian data atau fungsi, atau prasyarat khusus membatasi dampak yang didukung bukti. Contohnya temuan yang hanya muncul lewat kondisi uji tertentu dengan jangkauan terbatas. |
| Rendah | Kelemahan penguatan dengan dampak langsung yang kecil atau jalur ke data toko yang belum terbukti. Catat keterbatasan bukti dan risiko yang masih perlu ditinjau. |

Untuk tiap temuan, jelaskan siapa yang bisa memicunya, hak dan interaksi yang diperlukan, konfigurasi serta data yang terdampak, dan alasan tingkatnya. Catat skor advisori sebagai masukan bila ada. Jika bukti belum cukup, tandai hasil sementara dan selesaikan penilaian sebelum memutuskan status rilis. Temuan tinggi/kritis harus diperbaiki dan diuji ulang. Catat disposisi dan dasar penerimaan risiko lain.

## 4. Buktikan pembaruan dan pemasangan biasa

1. Gunakan fixture yang sama pada setiap percobaan upgrade. Setelah APK resmi 1.5.0 terpasang, siapkan akun dan kode pemulihan, katalog, gambar QRIS uji, tagihan tunai menunggu dengan uang diterima, transaksi lunas tunai dan QRIS, nota, serta koreksi beserta pelakunya melalui UI. Buat dan ekspor PDF dari tiap periode. Catat nomor, nominal, status, rekaman pelaku, isi laporan, dan SHA-256 berkas yang dapat diakses secara normal.
2. Pada emulator API 26, pasang APK lama pada profil update kosong dengan perintah pertama. Siapkan data langkah 1, kemudian jalankan perintah kedua. Pertahankan data yang sama saat instalasi pembaruan.

   ```powershell
   adb -s $serial install $apkLama
   adb -s $serial install -r $apkKandidat
   ```

3. Jalankan percobaan tambahan pada emulator API 26 dengan fixture historis terpisah. Pasang APK resmi 1.0.0, buat pembayaran QRIS yang dibatalkan dan dipulihkan oleh admin, lalu update ke APK resmi 1.5.0. Buka riwayat di 1.5.0 dan catat nama pelaku historis, alasan, nomor nota, status, dan waktu konfirmasi. Pasang kandidat 2.0.0 di atas 1.5.0 dan cocokkan kembali nilai tersebut melalui UI. [Prosedur fixture 1.0.0](../002-upgrade-kasir-tunggal/quickstart.md)
4. Pada perangkat fisik, siapkan profil uji kosong untuk pemasangan baru dan profil dengan APK resmi 1.5.0 beserta fixture langkah 1 untuk pembaruan. Untuk kedua skenario, unduh APK final melalui browser Android dari aset GitHub yang aksesnya sudah diotorisasi, misalnya draf rilis yang bisa diakses akun penguji. Cocokkan SHA-256 berkas yang diunduh sebelum membuka pemasang dari jalur Unduhan yang akan dipakai pengguna. Catat sumber yang ditampilkan Android, pesan pemasang, dan hasil Play Protect. Konfirmasi Play Protect aktif dan selesaikan pemindaian jika ditawarkan. Akses draf bisa memerlukan akun penguji. Jika aset belum tersedia melalui browser, catat uji sumber GitHub sebagai tertunda dan selesaikan pengujian yang bisa dilakukan dengan APK lokal.
5. Masuk dengan akun kasir lama. Cocokkan data lewat UI, lanjutkan tagihan tanpa menggandakan transaksi, baca riwayat koreksi, cocokkan total di widget dan laporan, serta buka PDF dari arsip dan lokasi ekspor awal. Bila perlu, ekspor ulang PDF dari arsip dan bandingkan dengan isi atau hash fixture yang sesuai. Uji kode pemulihan yang masih berlaku setelah pemeriksaan data selesai, karena penggunaan kode menggantinya.
6. Bedakan izin pemasangan dari sumber, verifikasi pengembang, kegagalan kecocokan paket atau tanda tangan, dan hasil Play Protect. Jika ada peringatan berbahaya atau pemblokiran, pertahankan Play Protect aktif, periksa penyebab, dan uji ulang setelah penanganan. Kaitkan laporan dengan hash kandidat. Sebelum publikasi, cocokkan hash APK yang lulus dengan aset GitHub yang akan diunduh pengguna. [ADB](https://developer.android.com/tools/adb#pm), [Google Play Protect](https://support.google.com/accounts/answer/2812853?hl=en), [Verifikasi pengembang Android](https://developer.android.com/developer-verification/guides/faq)

## Bukti pengujian lokal 14 November 2026

- Build release tanpa tanda tangan dan build debug berhasil. Lint release mencatat 0 error dan 14 peringatan. Delapan unit test dan 23 tes instrumentasi pada emulator API 26 lulus.
- Pada fixture 1.000 transaksi, widget siap dalam 1.155 ms. Rekap sudah tersedia saat tombol laporan ditekan. Laporan tampil dalam 408 ms dan PDF bulanan selesai dalam 1.859 ms. Hasil ini berasal dari build debug di emulator API 26.
- APK release tanpa tanda tangan terdeteksi sebagai `com.apkkasir` 2.0.0, kode versi 3, dan `debuggable=false`. Manifest mengumumkan launcher dan receiver AndroidX ProfileInstaller dengan proteksi izin `DUMP`. Aktivitas dari dependensi pengujian debug tidak terpaket. `allowBackup=false` dan kedua konfigurasi pengecualian backup tetap terpasang.
- OSV tidak mengembalikan advisori yang cocok untuk 105 koordinat Maven dari `releaseRuntimeClasspath` yang diperiksa. Hasil ini mencakup advisori pada basis data tersebut saat pemeriksaan, 14 November 2026. [OSV](https://google.github.io/osv.dev/post-v1-querybatch/)
- Pemilik mengonfirmasi kunci 1.5.0 berada dalam kendalinya dan belum pernah dibagikan. Keystore lokal Android menghasilkan kandidat release dengan sidik jari yang cocok dengan APK lama. Pemeriksaan riwayat Git berdasarkan nama berkas keystore tidak menemukan berkas keystore yang terlacak.
- Kandidat lokal bertanda tangan memiliki SHA-256 `640a8a64c9c7b8cd4532384584076561777fa64695e4f0e3395da6893f2a7748` dan sertifikat SHA-256 `14f567cbfdb8703d59a482d33c4e8279e4ecc774a5628803ba53eb7b981b937a`. ADB menerima update dari APK resmi 1.5.0 di emulator API 26 dengan `install -r`. Android melaporkan 2.0.0 kode versi 3 dan menolak `run-as` karena paket non-debuggable. Pengujian fisik dengan Play Protect masih terbuka.
- Pada fixture awal, akun sintetis 1.5.0 berhasil masuk setelah update. Widget menampilkan Rp17.000 dan satu transaksi lunas. Nomor nota, nama barang, jumlah, metode tunai, uang diterima, dan kembalian cocok melalui UI sebelum dan sesudah update. Kode pemulihan dari 1.5.0 diterima di 2.0.0. Login dengan kata sandi pengganti berhasil dan total tetap cocok. Hash APK terpasang cocok dengan kandidat.
- Hierarki UI emulator menampilkan empat kartu beranda, widget, dan empat label tab pada viewport sekitar 360 dan 375 dp dengan skala font 200%. Pemeriksaan terpisah dengan mode gelap aktif mengonfirmasi widget dan navigasi tampil. Pengaturan emulator dikembalikan setelah uji. TalkBack, hover, dan pengukuran kontras masih terbuka.
- Fixture upgrade lengkap untuk tagihan menunggu, QRIS, riwayat dengan pelaku historis, dan PDF masih terbuka. Pengujian fisik ditunda atas arahan pengguna.

## Uji pemasang Infinix 14 November 2026

- Infinix X6857 menjalankan Android 16 (API 36). APK 1.5.0 terpasang cocok byte-per-byte dengan rilis resmi. Pemilik mengonfirmasi Play Protect aktif sebelum update.
- Kandidat lokal dipindahkan melalui USB ke Unduhan dan dibuka di Files by Google. Android menerima update langsung ke 2.0.0 kode versi 3. APK terpasang cocok dengan SHA-256 kandidat `640a8a64c9c7b8cd4532384584076561777fa64695e4f0e3395da6893f2a7748`. Inode direktori data yang dilaporkan Android cocok sebelum dan sesudah. Empat berkas data difingerprint sebelum update. Pemeriksaan tersebut menunjukkan kontinuitas direktori data dan belum membuktikan isi berkas seluruhnya cocok setelah update.
- Pemilik mengonfirmasi update selesai dengan Google Play Protect aktif dan tanpa permintaan pemindaian Play Protect. Android menampilkan pemberitahuan Keamanan & privasi bahwa developer aplikasi tidak dapat ditemukan. Pemberitahuan tersebut tidak memblokir update ini. HP juga menampilkan pemindaian bawaan Transsion dan keterangan tentang sumber pemasangan. Hasil akhir pemindaian Transsion tidak tercatat.
- Setelah ADB terputus, pemilik mengonfirmasi login dengan akun lama berhasil dan data lama yang diperiksa masih ada. Pemeriksaan ini terbatas pada data yang dilihat pemilik.
- Untuk rilis 2.0.0 via GitHub, pemilik memilih distribusi tanpa pendaftaran verifikasi developer Google. Jika pemberitahuan verifikasi muncul dan pemasangan selesai, catat pemberitahuan dan hasilnya. Jika pemasangan diblokir, catat penyebabnya dan nyatakan skenario tersebut gagal.
- Instalasi bersih dan alur unduhan APK final dari GitHub tetap perlu diuji sesuai T022.

## Uji widget layar utama 15 November 2026

- APK bertanda tangan 2.0.0 kode 4 memiliki SHA-256 `987d1d09004da515b71d86159daa7908a7feb99f8f4aca9c31b9e8448c6f0977` dengan sertifikat yang sama seperti kode 3. Build release dan lint berhasil dengan 0 error dan 17 peringatan.
- Pada emulator API 26, pemilih Widget menampilkan Kasir Toko berukuran 2 × 1. Widget berhasil ditempatkan di layar utama. Ketukan membuka layar **Masuk ke Kasir Toko** melalui `MainActivity`.
- Uji ulang dengan `adb install -r` menerima APK kode 4 dan mempertahankan direktori data emulator. Widget yang sudah ditambahkan tetap membuka layar masuk. SHA-256 APK yang ditarik dari emulator cocok dengan kandidat.
- Uji pemasang di Infinix di atas memakai kode 3. Verifikasi pemasangan kode 4 melalui jalur GitHub masih terbuka.
