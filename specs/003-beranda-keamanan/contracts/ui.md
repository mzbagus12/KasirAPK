# Kontrak UI Kasir Toko 2.0.0

Semua tujuan di sini memerlukan sesi kasir. Login dan pemulihan menggunakan layar yang ada. Saat sesi berakhir, layar kasir dan state terkait dilepas. Tombol Kembali setelah keluar tidak membuka lagi data toko.

## Beranda

| Posisi | Label | Tujuan |
| --- | --- | --- |
| Kiri atas | Buat tagihan | Checkout baru. |
| Kanan atas | Tagihan menunggu | Riwayat dengan filter `MENUNGGU`. Lanjutkan tagihan yang dipilih dengan draf yang tersedia. |
| Kiri bawah | Nota | Riwayat transaksi yang memiliki nomor nota, termasuk nota yang statusnya berubah setelah penerbitan. |
| Kanan bawah | Laporan hari ini | Halaman laporan hari ini dengan tab Laporan aktif. |

Kartu memiliki radius dan jarak konsisten. Ikon terpusat di atas label. Warna dan tipografi mengikuti tema Kasir Toko. Di bawah grid, widget baca-saja **Penjualan hari ini** memakai ikon kasir dekoratif, total rupiah dan jumlah transaksi lunas. Keadaan kosong menggunakan pesan spesifikasi. Keadaan memuat dan gagal memberikan status teks yang bisa dibaca tanpa memblokir kartu. Setelah pengguna membuka beranda lagi, ringkasan dimuat ulang. [State ringkasan](../data-model.md#state-ringkasan-penjualan)

## Tab dan kembali

| Tab | Tujuan tingkat atas | Aksi lanjutan |
| --- | --- | --- |
| Beranda | Grid dan widget | Checkout, riwayat terfilter, atau laporan. |
| Riwayat | Semua status transaksi | Lanjutkan, lihat nota, cari transaksi, koreksi, dan pemulihan QRIS terlambat. |
| Laporan | Laporan hari ini | Detail penjualan dan dokumen PDF harian, mingguan, bulanan, termasuk arsip. |
| Pengaturan | Pengelolaan toko | Barang dan harga, QRIS toko, akun dan pemulihan, serta Keluar. |

- Tampilkan bilah pada tujuan tingkat atas, termasuk riwayat saat dibuka dari shortcut. Pada riwayat terfilter, memilih tab Riwayat menampilkan semua status. Kembali dari shortcut sebelum memilih tab mengembalikan beranda.
- Sembunyikan bilah pada checkout, nota, detail, pencarian koreksi, formulir pengaturan dan pengelolaan PDF. Kembali dari layar tersebut mengembalikan rute dan filter asal. Setelah pembayaran berhasil, `Selesai` dan Kembali dari nota menuju konteks sebelum checkout. Nota yang dibuka dari daftar kembali ke daftar itu.
- Pemilihan tab berulang membuka satu salinan tujuan. Kembali pada tab tingkat atas mengarah ke beranda. Kembali dari beranda mengikuti perilaku Android.

## Interaksi dan aksesibilitas

- Jaga dua kolom pada 360/375 dp. Izinkan label dan nominal membungkus secara terbaca hingga skala teks 200%. Hormati inset dan sediakan gulir vertikal.
- Kontrol memiliki area sentuh minimal 48 × 48 dp. Ikon dekoratif tidak menambah pembacaan TalkBack. Urutan mengikuti empat kartu, widget sesuai urutan baca, lalu tab. Label tetap terlihat dan status aktif terbaca oleh TalkBack.
- Tekan memberi respons visual. Transisi indikator tab singkat dan dihentikan saat animasi sistem dinonaktifkan. Hover penunjuk bersifat sementara. Fokus keyboard terlihat dan kontrol dapat diaktifkan. Perubahan keadaan tidak menggeser tata letak.
- Periksa kontras WCAG AA pada kedua tema. Alur grid dan tab tetap dapat dijalankan saat widget kosong, memuat, atau gagal.
