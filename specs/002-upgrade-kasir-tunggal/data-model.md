# Model Data Upgrade Kasir Tunggal 1.5.0

## Identitas dan relasi

| Entitas atau penyimpanan | Keadaan setelah pembaruan |
| --- | --- |
| `User` kasir | `id`, `username`, `role = CASHIER` dan `credential` awal tetap sama. Indeks unik per nama dan per peran tetap berlaku. Setup baru hanya membuat baris ini. |
| `User` admin historis | `id` dan `username` tetap sama. `role` menjadi `HISTORICAL_ADMIN`. Kolom `credential` berisi penanda yang tidak dapat diverifikasi. Pencarian login hanya menerima `CASHIER`. |
| `StoreSettings` | Pertahankan baris tunggal ID 1, `recoveryVerifier`, dan `qrisFile`. Pemulihan mengganti verifikator kode tanpa mengganti referensi gambar. |
| `Product`, `Sale`, `SaleLine` | Pertahankan baris dan ID, harga historis, nominal Tunai, metode, status, nomor tagihan dan nota, serta waktu konfirmasi. |
| `SaleCorrection` | Pertahankan alasan, nilai awal dan akhir, waktu, serta `adminId`. Nama fisik kolom tetap. Maknanya adalah ID pelaku, termasuk kasir untuk koreksi baru. |
| `ReportDocument` dan PDF | Pertahankan ID dokumen, periode, nama, waktu pembuatan, dan file di `filesDir/reports`. Salinan ekspor tetap di lokasi pengguna dan tidak disentuh migrasi. |
| Gambar QRIS | Pertahankan `StoreSettings.qrisFile` dan file terkait di `filesDir`. |

`Sale.cashierId`, `Sale.confirmedBy` dan `SaleCorrection.adminId` tetap mereferensikan `User.id`. Kueri riwayat mengambil nama admin historis dan kasir. Aksi baru mengacu pada ID kasir yang sedang aktif.

## Transisi penyimpanan

| Kondisi saat dibuka | Hasil |
| --- | --- |
| Instalasi baru atau 1.0.0 belum disiapkan | Penyiapan tersedia jika pengguna, pengaturan, dan catatan toko kosong serta tidak ada gambar QRIS atau arsip PDF yang menunjukkan toko lama. Setup menyimpan kasir dan pengaturan dalam satu transaksi. |
| 1.0.0 sudah disiapkan | Dalam `Migration(1, 2)`, validasi pengaturan tunggal, satu kasir, satu admin, bentuk verifikator, serta pemeriksaan relasi dan berkas di bawah sebelum perubahan. Jika semua valid, perbarui peran dan verifikator admin. Setelah commit, buka layar masuk. |
| Basis data versi 2 yang sudah siap | Lanjutkan ke login tanpa mengulang perubahan. Validasi status akun sebelum menampilkan setup atau login. |
| Pembukaan atau migrasi terputus | Jika transaksi belum commit, Room mengulang migrasi saat pembukaan berikutnya. Jika commit sudah selesai, pembukaan berikutnya memakai versi 2. Gangguan sementara yang bisa dikenali menampilkan pilihan coba lagi. |
| Data lama tidak konsisten atau SQLite tidak dapat dibaca | Tahan akses, tampilkan penyebab yang aman, dan pertahankan data. Untuk inkonsistensi yang ditemukan saat migrasi, batalkan transaksi sebelum commit agar versi dan baris lama tetap utuh. Jangan menawarkan setup. |

### Pemeriksaan sebelum commit

- Periksa foreign key dan kepemilikan tagihan. `Sale.cashierId` harus menunjuk kasir lama, termasuk untuk status Menunggu. `Sale.confirmedBy` boleh menunjuk admin lama ketika admin mencatat pemulihan QRIS. Pertahankan atribusi admin pada riwayat koreksi. Tagihan yang menunjuk admin sebagai pembuat harus menahan migrasi tanpa pengalihan ID otomatis.
- Jika `StoreSettings.qrisFile` terisi, pastikan file yang dirujuk ada di `filesDir`. Untuk setiap `ReportDocument`, pastikan file yang dirujuk ada di `filesDir/reports`. Lakukan pemeriksaan baca saja. QRIS yang belum dipasang dan arsip kosong valid. Salinan ekspor berada di lokasi pilihan pengguna dan di luar pemeriksaan kesiapan lokal.
- Jika file yang dirujuk hilang, tahan migrasi dan pertahankan metadata serta file lain. Status tertahan mencegah pembukaan arsip, sehingga rekonsiliasi tidak sempat menghapus metadata tersebut.

Migrasi memakai skema ekspor 1.0.0, mempertahankan struktur fisik tabel dan indeks, serta menghasilkan skema versi 2. Perubahan memakai transaksi migrasi Room dan tidak menghapus atau memindahkan file. Validasi kesiapan tidak memanggil `ReportArchive.reconcile`, yang bisa menghapus entri atau file sementara. Pemulihan arsip normal tetap berjalan ketika pengguna membuka arsip dengan sesi aktif.

Pada konfigurasi Room dengan `SupportSQLiteOpenHelper`, tangani korupsi melalui callback yang mempertahankan file dan meneruskan status tertahan. Pastikan konfigurasi helper menolak kehilangan data saat recovery. Uji file rusak pada fixture untuk memeriksa bahwa basis data dan file pendamping yang ada tidak dihapus oleh jalur ini. [Callback korupsi SQLite](https://developer.android.com/reference/androidx/sqlite/db/SupportSQLiteOpenHelper.Callback#onCorruption(androidx.sqlite.db.SupportSQLiteDatabase))

## Kredensial dan sesi

- Verifikasi kata sandi dan kode mengikuti format lama. Validasi bentuk verifikator sebelum pemakaian dan tampilkan pesan aman jika data tidak dapat dibaca.
- Recovery hanya tersedia saat keluar. Dalam satu transaksi, pastikan kasir, verifikator dan status sesi masih sesuai, lalu simpan kata sandi serta verifikator kode pengganti. Setelah berhasil, kode lama ditolak dan pengguna masuk dengan kata sandi baru.
- Untuk perubahan dari sesi aktif, baca ulang identitas serta verifikator kasir sebelum commit. Gunakan satu jalur untuk menyelaraskan login, logout, recovery, perubahan kata sandi dan penerbitan kode agar hasil verifikasi yang sudah kedaluwarsa tidak bisa dipakai.
- Hanya `CASHIER` dapat menjadi sesi aktif. Pemeriksaan sesi membandingkan sesi yang memulai operasi dengan sesi saat aksi dilanjutkan atau disimpan.
