# Riset Kasir Toko 2.0.0

## R1. Navigasi dan penempatan aksi

**Keputusan.** Pakai satu `NavHost` di dalam sesi aktif, dengan `NavigationBar` Material 3 untuk empat tujuan utama. Status aktif berasal dari rute. Terapkan `launchSingleTop`, `popUpTo` dan simpan/pulihkan state tujuan utama melalui Navigation Compose. Shortcut beranda yang memakai filter membuka riwayat dengan argumen eksplisit dan kembali ke beranda. Tab Riwayat membuka semua status.

**Alasan.** `AppNavigation.kt` sudah membuat navigasi per objek sesi. Checkout, riwayat, pencarian transaksi, nota, laporan, dan pengaturan sudah ada. Saat ini `Selesai` pada nota selalu menuju beranda, sementara tombol Kembali bisa menampilkan checkout yang telah selesai. Ganti checkout yang sudah dikonfirmasi dengan nota di back stack. Nota yang dibuka dari daftar kembali ke daftar asal. Uji klik berulang dan rotasi layar agar filter dan posisi kembali konsisten.

**Alternatif.** Empat pengendali navigasi akan menggandakan pengelolaan sesi dan Kembali. Menggunakan satu layar riwayat dengan state filter global berisiko membawa filter shortcut ke tab umum. [Dukungan back stack Android](https://developer.android.com/guide/navigation/backstack/multi-back-stacks)

## R2. Grid, widget, dan aksesibilitas

**Keputusan.** Pertahankan desain Kasir Toko dan tulis aturan khusus halaman beranda di desain sistem saat implementasi. Grid dua kolom pada 360 dan 375 dp diikuti widget selebar area konten dengan ikon kasir yang sudah ada. Gunakan tata letak yang tumbuh mengikuti teks. Pakai umpan balik tekan, indikator tab terpilih, fokus keyboard, dan hover penunjuk tanpa mengubah ukuran komponen. Hormati pengaturan animasi Android.

**Alasan.** Aturan lama untuk kartu menu membuka dua kolom pada ruang 480 dp sehingga beranda 2.0.0 memerlukan aturan khusus halaman. UI UX Pro Max mendukung pesan kosong yang informatif dan ikon pendamping dekoratif. Label dan fokus memberi konteks saat ikon tak dapat dilihat. Antislop dipakai selama implementasi untuk memeriksa isi nyata, keadaan kontrol, dan alur lengkap.

**Alternatif.** Kartu dengan tinggi tetap memotong label pada skala font besar. Menambahkan gambar khusus meningkatkan ruang visual dan kebutuhan aset. Material 3 dan ikon yang ada cukup untuk fungsi ini. [Pedoman desain proyek](../../design-system/kasir-toko/MASTER.md)

## R3. Satu sumber rekap harian

**Keputusan.** Beranda dan laporan harian menggunakan state rekap bersama dalam sesi kasir, dengan satu koleksi aktif pada tujuan yang membutuhkannya. Pertahankan `SalesReport.observe`, `ReportPeriod`, `ReportSnapshot`, dan `formatRupiah`. Ambil ulang tanggal WIB saat aplikasi kembali ke foreground. Sambil terbuka, ganti periode saat tengah malam dan validasi jam secara berkala mengikuti pola laporan saat ini. Targetkan pergantian dalam 30 detik di foreground bila jam perangkat berubah. Buang angka hari atau sesi lama saat beralih.

**Alasan.** Rekap memakai rentang waktu konfirmasi WIB dan menghitung status `LUNAS`. Baris yang dibatalkan bisa tetap ada di laporan dan menghasilkan jumlah lunas nol. `observe` memvalidasi sesi saat dibuat dan setiap kali data berubah. Tangani kegagalan awal maupun dari aliran data, propagasikan pembatalan coroutine, dan mulai ulang pemuatan saat beranda dibuka kembali.

**Alternatif.** Menulis tabel ringkasan baru atau menghitung total langsung dari daftar UI menambah sumber data dan risiko hasil berbeda. Mengumpulkan ulang laporan dari dua layar sekaligus menambah pembacaan baris dan pekerjaan render.

## R4. Penandatanganan kandidat dan distribusi

**Bukti baseline.** APK lokal sama dengan hash rilis resmi 1.5.0 yang sebelumnya tercatat. SHA-256 APK `cc83286d4391a8363df298f31e35d281c867ed832e7512761eaf8688aca6871c`. Pada 14 November 2026, `apksigner` memverifikasi tanda tangan v2 dan sertifikat `Android Debug` dengan SHA-256 `14f567cbfdb8703d59a482d33c4e8279e4ecc774a5628803ba53eb7b981b937a`. APK Analyzer dan AAPT mengonfirmasi `com.apkkasir`, kode versi 2, minimum API 26, target 36, dan `debuggable=true`. Pemeriksaan ini memakai APK dan sertifikat publik.

**Keputusan.** Uji jalur GitHub Releases dengan build release non-debuggable, `com.apkkasir`, `versionName=2.0.0`, dan `versionCode=4`. Kode 4 memungkinkan update di atas build uji kode 3. Gunakan kunci penandatangan 1.5.0 hanya setelah pemilik dapat membuktikan kendali atasnya dan audit tidak menemukan indikasi kompromi. Siapkan jalur konfigurasi penandatanganan lokal di luar Git dan verifikasi kandidat identik yang akan dipasang.

### Bukti kelayakan kunci lama

1. **Kompatibilitas.** Cocokkan sidik jari SHA-256 sertifikat APK resmi 1.5.0 dengan sertifikat pada APK uji yang ditandatangani lewat proses lokal pemilik. Buktikan update di API 26 melalui T004. Catat sidik jari sertifikat, hash APK uji, dan hasil pemasangan.
2. **Kendali.** Catat konfirmasi pemilik tentang asal kunci, penyimpanan dan cadangannya, serta siapa atau sistem apa yang bisa menggunakannya. Tinjau bukti akses yang tersedia dan riwayat paparan, termasuk apakah kunci pernah disalin ke repo, dibagikan melalui log atau artefak build, atau dapat diakses di luar kendali pemilik. Catat kesimpulan dan dasar penilaiannya tanpa merekam berkas kunci, kata sandi, atau informasi akses yang sensitif.
3. **Keputusan rilis.** Tahan penggunaan kunci untuk kandidat publik bila asal atau kendali atas kunci belum dapat ditetapkan, atau bila ada indikasi kompromi. Catat penyebab dan lanjutkan UI serta audit yang tidak bergantung pada kunci.

**Alasan.** Android mengecek ID aplikasi, versi, dan kompatibilitas sertifikat saat update. Membangun varian release dan menentukan sertifikat adalah dua langkah terpisah. Publikasi Google Play memakai persyaratan sertifikatnya sendiri. [Aturan pembaruan Android](https://developer.android.com/google/play/app-updates), [Penandatanganan Android](https://developer.android.com/studio/publish/app-signing)

**Alternatif.** Kunci baru tanpa kompatibilitas ditolak pada update. Skema rotasi v3 tersedia sejak API 28. Dengan v3.1, platform lama tetap memakai penandatangan lama sehingga rotasi tidak menggantikan kebutuhan atas penandatangan 1.5.0 untuk API 26. Jika akses kunci gagal atau ditemukan indikasi kompromi, tahan kandidat publik dan tinjau jalur upgrade bersama pemilik. [APK Signature Scheme v3](https://source.android.com/docs/security/features/apksigning/v3), [APK Signature Scheme v3.1](https://source.android.com/docs/security/features/apksigning/v3-1)

## R5. Audit keamanan dan hasil pemasang

**Keputusan.** Jadikan MASVS/MASTG acuan untuk meninjau risiko yang relevan. Bandingkan manifest gabungan dan isi APK release dengan sumber, periksa autentikasi serta pemulihan, izin dan sesi, lokasi data serta kebijakan backup, akses file QRIS/PDF, log, dan grafik dependensi runtime. Gunakan data sintetis. Catat bukti, keparahan, dampak, reproduksi, perbaikan dan uji ulang. Tahan rilis bila temuan tinggi/kritis terbuka.

**Alasan.** Manifest sumber hanya mendaftarkan launcher dan menonaktifkan backup. APK debug memiliki komponen dependensi tambahan, termasuk aktivitas uji dan receiver yang dilindungi izin. Kandidat release perlu diperiksa langsung. Kebijakan backup cloud dan transfer perangkat harus diuji sesuai aturan Android. [OWASP MASVS](https://mas.owasp.org/MASVS/03-Using_the_MASVS/), [Android Auto Backup](https://developer.android.com/identity/data/autobackup)

**Alternatif.** Membaca manifest sumber saja atau memeriksa pohon dependensi saja tidak mencakup APK yang diterima pengguna. Pembaruan dependensi dipilih berdasarkan temuan yang relevan dan uji regresi.

**Pemasang.** Pemilik memilih distribusi 2.0.0 via GitHub tanpa pendaftaran verifikasi developer Google. Gunakan pemasang Android di perangkat fisik dengan Play Protect aktif. Catat izin sumber APK, pesan verifikasi developer bila muncul, hasil pemindaian keamanan, dan kompatibilitas update. Pemindaian biasa dapat diminta untuk APK luar Google Play. Jika muncul peringatan berbahaya atau pemasangan diblokir, catat skenario sebagai gagal, tangani penyebabnya, lalu uji ulang. [Google Play Protect](https://support.google.com/accounts/answer/2812853?hl=en), [FAQ verifikasi pengembang Android](https://developer.android.com/developer-verification/guides/faq)
