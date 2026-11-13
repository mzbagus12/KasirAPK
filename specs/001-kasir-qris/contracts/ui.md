# Kontrak antarmuka Android

## Masuk, penyiapan, dan peran

- Instalasi baru membuka penyiapan admin dan satu akun kasir. Setelah berhasil, pemilik menerima kode pemulihan. Layar masuk membuka fitur sesuai peran. **Keluar** mengakhiri akses akun dan mengembalikan aplikasi ke layar masuk. Navigasi kembali harus tetap meminta akun aktif.
- Admin mengelola katalog, gambar QRIS, reset kasir, laporan, koreksi, dan PDF. Kasir mengelola keranjang, tagihan, pembayaran, dan nota yang dilayaninya. Periksa peran pada setiap operasi terkait, termasuk penyelesaian operasi pemilih dokumen setelah aplikasi kembali ke layar.
- Pemulihan admin meminta kode valid sebelum menetapkan kredensial baru. Tampilkan kode pengganti setelah penyimpanan berhasil. Jika tampilan kode terlewat, admin dapat masuk, memilih **Buat kode pemulihan baru**, lalu memasukkan ulang kredensial aktif. Tampilkan kode setelah penggantian tersimpan. Reset dan pemulihan mempertahankan data toko.

## Katalog, tagihan, dan nota

- Katalog menampilkan harga rupiah. Admin dapat menambah barang dengan nama terisi dan harga bulat positif atau mengubah harga. Kasir dapat memilih barang, mengubah kuantitas, atau menghapus item dari keranjang. Keranjang tidak valid menahan pembuatan tagihan dan menampilkan alasan.
- Setelah tagihan tersimpan, tampilkan nomor, total, dan metode. **Tunai** langsung tersedia. **QRIS** tersedia setelah gambar berhasil dipasang. Sebelumnya tampilkan alasan pada pilihan QRIS.
- **Tunai** menampilkan input uang diterima, kekurangan jika kurang, dan kembalian jika cukup. Aktifkan **Konfirmasi** setelah nominal valid yang mencukupi tersimpan. Mengosongkan input, memasukkan nilai tidak valid, atau beralih ke QRIS menghapus nominal dari tagihan. Kasir menekan Konfirmasi setelah menyerahkan kembalian atau menerima uang pas.
- **QRIS** menampilkan gambar yang dapat dipindai, total, dan tombol **Konfirmasi** di bawahnya. Kasir menekan tombol setelah melihat notifikasi BRI yang sesuai. Aplikasi hanya mencatat tindakan kasir.
- Tagihan Menunggu dapat dibuka ulang dengan isi tersimpan dan dapat dibatalkan jika pembayaran belum diterima. Pada kegagalan penyimpanan tampilkan pesan dan pertahankan tagihan yang sama. Nota muncul setelah konfirmasi tersimpan dan dapat dibuka kembali.
- Impor gambar oleh admin meminta pratinjau sebelum simpan. Jika gambar tidak terbaca atau gagal disalin, tampilkan alasan dan pertahankan gambar sebelumnya. Gambar baru hanya digunakan setelah penyimpanan berhasil.

## Laporan, koreksi, dan PDF

- Admin melihat tabel hari ini dalam WIB. Baris berisi nomor nota, waktu konfirmasi, total tagihan, metode, dan status. Tampilkan jumlah serta total penjualan yang sah. Baris Dibatalkan setelah pernah lunas tetap terlihat dan dikecualikan dari agregat.
- Admin membuka detail dari tabel atau mencari nomor nota. Koreksi metode, pembatalan status lunas, dan pemulihan QRIS terlambat memerlukan alasan. Untuk tagihan dibatalkan tanpa nota, pencarian memakai nomor tagihan atau tanggal. Detail menampilkan riwayat, waktu, dan akun admin.
- Pembuatan PDF meminta periode serta tanggal acuan. Tampilkan rentang hasil dan simpan PDF yang berhasil dibuat di arsip lokal. Admin dapat membuka PDF tersimpan, mengekspor salinan melalui pemilih dokumen, dan menghapus dokumen privat setelah konfirmasi.
- Arsip PDF mempertahankan isi aslinya. Pembuatan ulang periode mengambil data terbaru dan menghasilkan dokumen baru. Setelah aplikasi dibuka ulang, tampilkan daftar setelah pemeriksaan arsip selesai. Penghapusan dan pemeriksaan arsip tidak mengubah transaksi atau salinan ekspor. Tampilkan status kosong, kegagalan, dan pilihan mencoba lagi bila relevan.
