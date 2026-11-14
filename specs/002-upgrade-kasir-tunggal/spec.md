# Feature Specification: Upgrade Kasir Tunggal 1.5.0

**Branch** `release/1.5.0`

**Dibuat** 2026-11-13

**Status** Persyaratan disepakati dan siap untuk perencanaan.

**Acuan** [Spesifikasi 1.0.0](../001-kasir-qris/spec.md) dan [rilis v1.0.0](https://github.com/mzbagus12/KasirAPK/releases/tag/v1.0.0).

## User Scenarios & Testing

### User Story 1 - Memperbarui APK dan melanjutkan pekerjaan (P1)

Sebagai pemilik toko, saya ingin memasang APK 1.5.0 di atas 1.0.0 dan langsung memakai data toko yang sudah ada.

**Uji independen**. Pasang APK 1.0.0 pada perangkat uji, buat data lewat aplikasi, lalu pasang 1.5.0 sebagai pembaruan dan cocokkan data sebelum serta sesudahnya.

1. **Diberikan** 1.0.0 sudah terpasang dan toko sudah disiapkan, **ketika** 1.5.0 dipasang sebagai pembaruan, **maka** aplikasi membuka layar masuk dan kasir dapat masuk dengan nama pengguna serta kata sandi lamanya.
2. **Diberikan** tagihan Tunai Menunggu dengan nominal uang diterima, **ketika** kasir melanjutkannya setelah upgrade, **maka** nomor, isi, total, metode, dan nominal tetap sama. Aplikasi menghitung kembali kembalian dan konfirmasi menerbitkan satu nota untuk tagihan itu.
3. **Diberikan** admin pernah memulihkan pembayaran QRIS yang terlambat, **ketika** kasir membuka riwayat di 1.5.0, **maka** transaksi tetap Lunas dan nomor nota, waktu konfirmasi, serta nama admin pada catatan pemulihan tetap sama. Kredensial admin lama ditolak untuk masuk.
4. **Diberikan** PDF tersedia dalam arsip dan salinannya sudah diekspor, **ketika** upgrade selesai, **maka** PDF arsip bisa dibuka dari aplikasi dan salinan ekspor bisa dibuka di lokasi semula. Isi keduanya tetap sama.
5. **Diberikan** pemilik hanya ingat login admin 1.0.0 dan kehilangan kode pemulihan, **ketika** mengikuti panduan upgrade, **maka** pemilik dapat memakai admin di 1.0.0 untuk mereset kata sandi kasir dan menerbitkan kode pemulihan pengganti. Pemilik menguji login kasir dan menyimpan kode sebelum memasang 1.5.0, lalu masuk di 1.5.0 dengan kredensial kasir yang sudah diuji.

### User Story 2 - Menjalankan seluruh operasional dari akun kasir (P1)

Sebagai kasir, saya ingin menjalankan penjualan dan pengelolaan toko dengan satu akun.

**Uji independen**. Masuk sebagai kasir dan jalankan satu transaksi serta setiap kelompok tindakan yang sebelumnya tersedia bagi admin.

1. **Diberikan** kasir sudah masuk, **ketika** kasir menambah barang, mengubah harga, atau memasang maupun mengganti gambar QRIS dengan pratinjau, **maka** transaksi berikutnya memakai katalog dan gambar yang sudah disimpan. Tagihan dan nota lama mempertahankan harga yang tercatat.
2. **Diberikan** kasir sudah masuk, **ketika** kasir membuka transaksi dari laporan hari ini atau mencari transaksi untuk dikoreksi, **maka** kasir dapat memperbaiki metode pembayaran atau membatalkan transaksi yang keliru ditandai Lunas. Aplikasi meminta alasan dan mencatat perubahan serta identitas kasir sesuai aturan 1.0.0.
3. **Diberikan** pengguna belum masuk atau sudah keluar, **ketika** pengguna mencoba membuka kembali layar atau menjalankan aksi pengelolaan, **maka** aplikasi meminta login.
4. **Diberikan** kasir sudah masuk, **ketika** kasir membuat PDF laporan harian, mingguan, atau bulanan, **maka** setiap PDF mencakup periode WIB yang dipilih dan tersedia di arsip. Kasir dapat membuka dan mengekspor PDF, serta menghapusnya dari arsip setelah konfirmasi. Penghapusan PDF menjaga transaksi, nota, dan riwayat koreksi tetap tersedia.
5. **Diberikan** kasir menemukan tagihan QRIS yang dibatalkan dan mencocokkannya dengan notifikasi BRI, **ketika** kasir menyimpan pemulihan dengan alasan, **maka** tagihan asal berstatus Lunas dan dihitung sekali. Riwayat mencatat kasir yang melakukan pemulihan. Jika nota sudah pernah terbit, nomor nota tetap sama.

### User Story 3 - Menyiapkan atau memulihkan akun tunggal (P1)

Sebagai pemilik toko, saya ingin menyiapkan satu akun kasir dan dapat memulihkan akses jika kata sandinya terlupa.

**Uji independen**. Pasang 1.5.0 pada perangkat bersih, buat akun kasir, lalu pulihkan kata sandinya. Ulangi pemulihan pada instalasi hasil upgrade.

1. **Diberikan** instalasi baru atau 1.0.0 yang belum pernah disiapkan, **ketika** 1.5.0 pertama dibuka, **maka** penyiapan meminta satu akun kasir dan menerbitkan kode pemulihan lokal.
2. **Diberikan** pemilik menyimpan kode pemulihan 1.0.0 yang masih berlaku, **ketika** pemilik memakainya setelah upgrade, **maka** pemilik dapat mengganti kata sandi akun kasir yang sama. Aplikasi menerbitkan kode pengganti dan kode lama tidak bisa dipakai lagi.
3. **Diberikan** kasir sudah masuk, **ketika** kasir meminta kode pemulihan baru dan memasukkan kata sandi aktif yang benar, **maka** aplikasi menerbitkan kode baru dan menonaktifkan kode sebelumnya.
4. **Diberikan** kasir sudah masuk, **ketika** kasir memverifikasi kata sandi aktif lalu menyimpan kata sandi baru, **maka** login berikutnya menerima kata sandi baru dan menolak kata sandi lama pada akun yang sama.
5. **Diberikan** pemulihan sudah menyimpan kata sandi baru tetapi aplikasi tertutup sebelum kode pengganti sempat disimpan pemilik, **ketika** pemilik masuk dengan kata sandi baru, **maka** pemilik dapat menerbitkan kode pengganti dengan memverifikasi kata sandi tersebut. Data toko tetap tersedia.

### Edge Cases

- Jika pembaruan data gagal sementara atau terputus, aplikasi menampilkan pesan dan menyediakan percobaan ulang. Data toko tetap utuh dan aplikasi tidak membuka penyiapan toko baru di atas data lama.
- Jika data lama tidak lengkap atau tidak konsisten, aplikasi menampilkan penyebab yang dapat dikenali dan menahan pembaruan. Data dan berkas toko dipertahankan. Aplikasi tidak membuka penyiapan baru, memilih akun pengganti, atau menghapus catatan secara otomatis.
- Jika APK pembaruan ditolak karena sertifikat tidak cocok, hentikan proses. Instruksi upgrade harus mengarahkan pengguna untuk memperoleh APK pembaruan yang cocok tanpa mencopot aplikasi.

## Requirements

### Functional Requirements

- **FR-001** Paket 1.5.0 harus dapat dipasang di atas APK resmi 1.0.0 yang sudah terpasang, tanpa uninstall, hapus data, atau ekspor dan impor manual.
- **FR-002** Pada toko yang sudah disiapkan, login kasir lama dan identitasnya harus tetap berlaku. Kredensial admin lama harus ditolak. Riwayat transaksi dan koreksi tetap menampilkan pelaku asal.
- **FR-003** Dengan sesi aktif, akun kasir harus dapat menjalankan penjualan, membuka nota dan laporan hari ini, menambah barang dan mengubah harga, memasang atau mengganti QRIS dengan pratinjau, mencari transaksi, mengoreksi metode pembayaran, membatalkan transaksi yang keliru ditandai Lunas, melihat riwayat koreksi, dan memulihkan pembayaran QRIS yang terlambat. Kasir juga dapat membuat PDF harian, mingguan, dan bulanan, membuka serta mengekspor arsip, dan menghapus PDF arsip setelah konfirmasi tanpa menghapus transaksi, nota, atau riwayat koreksi.
- **FR-004** Upgrade harus mempertahankan akun kasir, barang dan harga, gambar QRIS aktif, seluruh transaksi dan statusnya, detail dan nomor tagihan serta nota, waktu konfirmasi, identitas pelaku, riwayat koreksi, dan file PDF dalam arsip. Salinan PDF yang sudah diekspor tetap tersedia di lokasi ekspornya.
- **FR-005** Instalasi baru dan instalasi 1.0.0 yang belum disiapkan harus meminta tepat satu akun kasir. Aplikasi tidak lagi menawarkan penyiapan atau login admin.
- **FR-006** Pemulihan harus mengganti kata sandi akun kasir yang sama tanpa mengubah data toko. Kode 1.0.0 yang masih berlaku dapat digunakan untuk memulihkan akun kasir dan langsung diganti setelah berhasil dipakai. Kasir dengan sesi aktif dapat mengganti kata sandinya sendiri atau menerbitkan kode pengganti setelah memverifikasi kata sandi aktif. Penerbitan kode pengganti menonaktifkan kode sebelumnya.
- **FR-007** Aplikasi harus menampilkan pesan yang membedakan pembaruan yang dapat dicoba ulang dari kondisi data lama yang menghalangi pembaruan. Kedua kondisi menjaga data dan berkas toko serta mencegah penyiapan toko baru di atas data lama. Percobaan ulang atau membuka ulang aplikasi setelah pembaruan berhasil tidak boleh menggandakan akun aktif, tagihan, nota, koreksi, atau PDF.
- **FR-008** Alur Tunai, konfirmasi QRIS manual, status transaksi, aturan koreksi, dan periode laporan WIB harus menghasilkan keluaran yang sama untuk data dan tindakan yang setara.
- **FR-009** Panduan pemasangan harus meminta pemilik toko yang sudah disiapkan untuk menguji login kasir di 1.0.0 dan menyimpan kode pemulihan yang masih berlaku sebelum upgrade. Jika akses kasir gagal, panduan harus menjelaskan reset kata sandi lewat admin 1.0.0. Jika kode hilang, panduan harus menjelaskan penerbitan kode pengganti di 1.0.0. Jika akses belum bisa diverifikasi, panduan harus meminta pemilik menunda pemasangan.

### Key Entities

- **Akun kasir** memiliki akses operasional dan pengelolaan serta mempertahankan identitas akun lama saat upgrade.
- **Identitas historis admin** mempertahankan atribusi pada transaksi dan koreksi lama. Identitas ini tidak memberikan akses masuk.
- **Data toko lokal** mencakup katalog, QRIS aktif, tagihan, transaksi, nota, riwayat koreksi, dan arsip PDF.
- **Kode pemulihan lokal** memulihkan akses akun kasir dan diganti setiap kali dipakai atau diterbitkan ulang.

## Success Criteria

### Measurable Outcomes

- **SC-001** Pada perangkat uji, APK 1.5.0 terpasang sebagai update atas APK 1.0.0 rilis GitHub. Tidak ada uninstall, penghapusan data, atau pemindahan data manual.
- **SC-002** Pada data yang dibuat lewat 1.0.0, cocokkan jumlah catatan, katalog, gambar QRIS, detail transaksi, nomor dan isi nota, riwayat dan pelaku, serta total harian, mingguan, dan bulanan sebelum dan sesudah upgrade. Data uji memuat tagihan Tunai Menunggu dengan nominal tersimpan, transaksi QRIS yang pernah dipulihkan admin, dan PDF arsip yang sudah diekspor. Tagihan bisa dilanjutkan dengan nomor dan nominal yang sama, identitas admin tetap tercatat, dan kedua salinan PDF bisa dibuka dengan isi yang sama seperti sebelum upgrade.
- **SC-003** Pada instalasi baru dan 1.0.0 yang belum disiapkan, penyiapan membuat tepat satu akun kasir. Pada instalasi baru dan hasil upgrade, kasir dapat melakukan seluruh tindakan pada FR-003 dan pengguna tanpa sesi ditolak. Pada instalasi hasil upgrade, login admin lama ditolak.
- **SC-004** Pada uji pembaruan yang terputus, percobaan ulang berhasil tanpa kehilangan atau duplikasi data. Pada uji terpisah dengan data lama yang tidak konsisten, aplikasi menampilkan penyebab, menahan pembaruan, menjaga data dan berkas yang ada, serta tidak membuka penyiapan toko baru.
- **SC-005** Pada instalasi baru dan hasil upgrade, penggantian kata sandi dan pemulihan tetap memakai identitas kasir yang sama dan mempertahankan data toko. Kode sebelumnya ditolak setelah dipakai atau diganti. Pemilik dapat menerbitkan kode pengganti jika tampilan kode sebelumnya terputus setelah kata sandi baru tersimpan.
- **SC-006** Pada uji transisi saat pemilik hanya ingat login admin 1.0.0, panduan membantu pemilik mereset kata sandi kasir, menerbitkan dan menyimpan kode pengganti, lalu menguji login kasir sebelum upgrade. Kredensial kasir yang diuji dapat dipakai untuk masuk setelah upgrade.

## Assumptions

- Pada toko yang sudah disiapkan, identitas kasir lama dipertahankan dan admin lama disimpan sebagai identitas historis. Kode pemulihan 1.0.0 yang masih berlaku memulihkan akses akun kasir setelah upgrade.
- Cakupan upgrade adalah APK `v1.0.0` resmi dari GitHub. Untuk kompatibilitas Android API 26, 1.5.0 memakai `applicationId` `com.apkkasir`, sertifikat yang sama, dan `versionCode` lebih tinggi dari 1. Sertifikat APK rilis dan kunci penandatangan lokal sudah dicocokkan. [Aturan pembaruan Android](https://developer.android.com/google/play/app-updates)
- Satu toko, satu perangkat Android, dan penyimpanan lokal mengikuti cakupan 1.0.0. Jalur downgrade dan pemulihan setelah uninstall berada di luar cakupan upgrade ini.
