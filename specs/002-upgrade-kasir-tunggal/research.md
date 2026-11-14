# Riset Upgrade Kasir Tunggal 1.5.0

## Kompatibilitas APK

**Keputusan.** Pertahankan `com.apkkasir` dan sertifikat penandatangan APK resmi 1.0.0. Tetapkan `versionCode = 2`. Verifikasi artefak lama dan kandidat dengan `apksigner` serta periksa metadata paket sebelum pemasangan.

**Alasan.** Android menilai identitas paket, penandatangan dan versi untuk menerima pembaruan. Rilis lama memakai kunci debug lokal. Kecocokan kunci lokal dengan APK rilis telah diperiksa saat penyusunan spesifikasi. Verifikasi kembali setiap kandidat, terutama jika dibangun pada mesin lain. Simpan kunci dan kata sandi di luar repo.

**Alternatif yang ditinjau.** Paket paralel, kunci baru, dan rotasi sertifikat membutuhkan jalur distribusi berbeda. Paket dan sertifikat yang sama memenuhi cakupan rilis serta kompatibilitas API 26. [Aturan pembaruan Android](https://developer.android.com/google/play/app-updates) · [apksigner](https://developer.android.com/tools/apksigner)

## Migrasi Room dan atribusi

**Keputusan.** Tambahkan migrasi eksplisit Room 1→2 dengan struktur tabel dan indeks tetap sama. Simpan admin sebagai `HISTORICAL_ADMIN` pada baris dan ID lama. Invalidasi verifikator admin dalam transaksi yang sama. Login hanya mengambil identitas kasir aktif.

**Alasan.** `Sale.cashierId`, `Sale.confirmedBy` dan `SaleCorrection.adminId` mereferensikan `User.id`. Kueri riwayat menggabungkan tabel koreksi dengan pengguna untuk menampilkan nama pelaku. Mengubah dua nilai pada baris admin menghindari rekonstruksi tabel dan mempertahankan relasi. Room menjalankan migrasi dalam transaksi. Simpan skema ekspor versi 1 dan 2 untuk membuktikan kompatibilitas.

**Alternatif yang ditinjau.** Menghapus admin membutuhkan pemindahan relasi atau perubahan bentuk riwayat. Pembaruan nilai saat layar login dibuka memerlukan penanda tambahan untuk koordinasi dan retry. Versi Room menyediakan batas migrasi yang teruji. [Panduan migrasi Room](https://developer.android.com/training/data-storage/room/migrating-db-versions) · [Transaksi Migration](https://developer.android.com/reference/androidx/room/migration/Migration)

## Akses dan pemulihan

**Keputusan.** Gunakan kembali verifikator PBKDF2 kasir dan kode pemulihan dari `StoreSettings`. Gunakan akun yang sama untuk ganti kata sandi, pemulihan dan penerbitan kode. Kendalikan persaingan antaroperasi akun dan periksa ulang keadaan sebelum menulis.

**Alasan.** Akun kasir dan pengaturan dibuat atomik oleh 1.0.0. Pemulihan dapat mengganti kata sandi dan kode dalam satu transaksi yang sudah mengikuti pola `LocalAccounts`. Penjagaan sesi di lapisan layanan tetap diperlukan karena menyembunyikan menu saja tidak mengotorisasi operasi.

**Alternatif yang ditinjau.** Mempertahankan login admin atau menambah akses transisi setelah update menambah jalur autentikasi. Pemeriksaan di 1.0.0 memakai menu reset dan pemulihan yang sudah tersedia.

## Pembuktian upgrade

**Keputusan.** Uji Room dengan fixture skema resmi untuk memeriksa kegagalan dan retry. Jalankan uji pemasangan di atas APK rilis untuk membuktikan signer, kredensial, dan berkas asli tetap kompatibel. Gunakan SHA-256 untuk mengikat hasil uji pemasangan ke aset yang diterbitkan.

**Alasan.** Fixture Room dapat menguji kondisi sulit direproduksi lewat UI. Update APK membuktikan retensi sandbox dan akses berkas ekspor yang tidak tercakup oleh fixture basis data. Uji perangkat fisik menggunakan pemasang Android, dengan ADB untuk reproduksi pada emulator. [MigrationTestHelper](https://developer.android.com/reference/androidx/room/testing/MigrationTestHelper) · [ADB install](https://developer.android.com/tools/adb#pm)
