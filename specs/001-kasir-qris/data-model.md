# Model data lokal

## Entitas

| Entitas | Data dan aturan |
|---|---|
| `User` | ID, nama masuk unik, peran `ADMIN` atau `CASHIER`, verifier kredensial, salt, dan parameter. Satu akun per peran. Reset kredensial mempertahankan ID. |
| `StoreSettings` | Satu baris untuk referensi gambar QRIS aktif dan verifier kode pemulihan admin. Pembuatan akun awal dan verifier tersimpan dalam satu transaksi. Pemulihan mengganti verifier kredensial dan verifier kode secara atomik. Admin yang masuk dan berhasil memverifikasi ulang kredensial aktif dapat mengganti verifier kode dalam satu transaksi. Kode sebelumnya berlaku sampai transaksi penggantian berhasil. |
| `Product` | ID, nama terisi, harga positif dalam rupiah bulat. Hanya admin dapat menambah dan mengubah harga. |
| `Sale` | ID, nomor tagihan unik, ID kasir, waktu pembuatan, total, status, metode, nominal tunai opsional, nomor nota unik opsional, waktu konfirmasi pertama opsional, dan ID akun konfirmasi pertama. Nomor nota tetap setelah koreksi. |
| `SaleLine` | ID tagihan, urutan, ID barang, salinan nama dan harga satuan, kuantitas, serta subtotal. Baris dan total dibekukan saat tagihan dibuat. |
| `SaleCorrection` | ID, ID tagihan, jenis tindakan, nilai sebelum dan sesudah, alasan, ID admin, dan waktu. Riwayat ditambahkan setiap koreksi yang berhasil. |
| `ReportDocument` | ID, jenis periode, batas periode, waktu dibuat, nama tampilan, dan lokasi file privat. Satu dokumen per pembuatan. File yang sudah tersimpan mempertahankan isinya. |

Room menegakkan referensi pengguna, tagihan, baris, dan riwayat. Tambahkan indeks untuk nomor tagihan, nomor nota, dan waktu konfirmasi pertama. Laporan harian merupakan hasil query, sedangkan PDF menyimpan hasil laporan saat dibuat.

## Transisi tagihan

| Awal | Tindakan | Hasil |
|---|---|---|
| Keranjang | Kasir membuat tagihan valid | `MENUNGGU`. Simpan total, rincian, dan nomor tagihan sebelum layar pembayaran. |
| `MENUNGGU` | Kasir memilih metode atau mengisi nominal | Simpan metode dan nominal Tunai yang valid. Input kosong, input tidak valid, atau pindah ke QRIS menyimpan nominal kosong. Proses perubahan untuk tagihan yang sama sesuai urutan input. Konfirmasi memakai hasil penyimpanan perubahan terakhir. |
| `MENUNGGU` | Kasir mengonfirmasi Tunai atau QRIS | `LUNAS`. Tetapkan satu nomor nota dan waktu konfirmasi pertama. Tunai memerlukan nominal ≥ total. QRIS memerlukan gambar toko yang tersedia dan konfirmasi kasir. |
| `MENUNGGU` | Kasir membatalkan tagihan yang belum dibayar | `DIBATALKAN`. Tetap dapat ditemukan untuk pemulihan yang sesuai. Tagihan tanpa konfirmasi pertama tidak masuk laporan. |
| `LUNAS` | Admin memperbaiki metode dengan alasan | Tetap `LUNAS`. Simpan riwayat perubahan. Nomor nota, rincian, total, dan waktu konfirmasi tetap. Jika koreksi berakhir pada Tunai tanpa nominal uang diterima yang diketahui, nilai itu tetap kosong. |
| `LUNAS` | Admin membatalkan status lunas yang keliru | `DIBATALKAN`. Simpan alasan dan riwayat. Baris laporan mempertahankan informasi tagihan dan dikeluarkan dari agregat penjualan. |
| `DIBATALKAN` | Admin memulihkan QRIS terlambat pada tagihan asal | `LUNAS` dengan metode QRIS. Simpan alasan dan riwayat. Gunakan nomor nota dan waktu pertama yang sudah ada. Jika belum ada, buat satu nota dengan waktu pemulihan. |

Jalankan pemeriksaan status dan mutasi nota atau riwayat dalam transaksi database yang sama. Konfirmasi kasir yang diulang pada tagihan Lunas mengembalikan nota lama. Konfirmasi biasa pada tagihan Dibatalkan ditolak. Pemulihan yang sudah berhasil hanya mengembalikan hasil yang tersimpan saat permintaan diulang. Status, nominal, dan waktu bersumber dari penyimpanan lokal.

## Periode dan file

- Hitung batas periode dalam WIB, lalu query rentang waktu konfirmasi pertama sebagai `[awal, akhir)` dalam UTC. Gunakan tanggal konfirmasi pertama untuk menempatkan transaksi. Hitung jumlah dan total dari status Lunas.
- Buat satu hasil query konsisten untuk baris dan ringkasan PDF. Tulis dokumen ke file sementara, finalisasi file, lalu daftarkan dokumen. Saat tahap gagal, bersihkan file yang belum terdaftar.
- Untuk penghapusan oleh admin, hapus file privat dan metadata terkait secara dapat dilanjutkan. Jika penghapusan file gagal, tampilkan kegagalan dan pertahankan entri. Jika file sudah hilang, pengulangan cukup membersihkan metadata. Riwayat transaksi dan salinan ekspor berada di luar operasi ini.
- Saat aplikasi dibuka ulang, selaraskan file dan metadata sebelum menampilkan arsip. Jalankan pemeriksaan secara eksklusif terhadap pembuatan, ekspor, dan penghapusan arsip. Hapus file sementara dan PDF privat yang belum terdaftar. Hapus metadata yang file privatnya sudah hilang. Jika pemeriksaan gagal, tampilkan pesan dan sediakan percobaan ulang. Batasi pemeriksaan pada direktori arsip privat milik aplikasi.
- Untuk QRIS, simpan file baru dengan nama terpisah dan ganti referensi aktif setelah validasi dan penyalinan selesai. Bersihkan file lama setelah referensi baru tersimpan. Impor gagal mempertahankan referensi aktif.
