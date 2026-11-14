# Kontrak UI Upgrade Kasir Tunggal 1.5.0

## Startup dan akun

| Keadaan | Perilaku |
| --- | --- |
| Aplikasi baru dibuka | Tampilkan status pemeriksaan hingga pembukaan data selesai. |
| Toko belum disiapkan | Formulir nama masuk dan kata sandi untuk satu kasir. Tampilkan kode pemulihan setelah penyimpanan berhasil. |
| Toko sudah disiapkan | Layar masuk kasir dan **Lupa kata sandi**. Kredensial admin lama ditolak dengan pesan kredensial salah. |
| Pembukaan data gagal sementara | Tampilkan pesan singkat dan **Coba lagi**. Selesaikan pemeriksaan lagi sebelum membuka form akun. |
| Data lama tidak konsisten | Tampilkan alasan yang bisa ditindaklanjuti tanpa kredensial atau detail internal. Bedakan kepemilikan tagihan yang tidak sesuai, gambar QRIS hilang, dan berkas PDF arsip hilang. Tahan login dan setup serta pertahankan metadata dan berkas. |
| Pemulihan | Minta kode valid dan kata sandi baru. Setelah commit, tampilkan kode pengganti dan arahkan ke login. |
| Akun dan pemulihan saat masuk | Sediakan **Ganti kata sandi** dan **Buat kode pemulihan baru**. Keduanya memverifikasi kata sandi aktif. |
| Keluar | Akhiri sesi, kembali ke login, dan tutup jalur kembali ke layar yang memerlukan autentikasi. |

## Menu untuk kasir yang sudah masuk

- **Buat tagihan** dan **Tagihan dan nota** memakai alur Tunai dan QRIS 1.0.0. Untuk QRIS, tampilkan gambar toko dan tunggu kasir mencocokkan notifikasi BRI sebelum **Konfirmasi**.
- **Barang** menambah barang dan mengubah harga. **QRIS toko** memasang atau mengganti gambar dengan pratinjau.
- **Laporan hari ini** menampilkan total, metode, status dan detail transaksi. **Cari transaksi** mempertahankan pencarian untuk koreksi serta pemulihan QRIS terlambat, termasuk tagihan yang belum punya nota. Alasan, waktu dan nama pelaku terlihat di riwayat.
- **Dokumen PDF** memilih tanggal acuan untuk periode harian, mingguan Senin–Minggu, atau bulanan dalam WIB. Kasir bisa membuat, membuka, mengekspor, dan menghapus PDF arsip. Tampilkan konfirmasi sebelum penghapusan. PDF yang sudah dibuat mempertahankan isi saat dibuat. Detail aturan periode dan transaksi mengikuti [kontrak 1.0.0](../../001-kasir-qris/contracts/ui.md).
- **Akun dan pemulihan** memakai kontrak akun di atas. Aksi layanan dan hasil pemilih file tetap memerlukan sesi kasir yang memulainya.
- Hasil pemilih gambar QRIS dan tujuan ekspor PDF hanya berlaku untuk sesi saat pemilih dibuka. Setelah logout, abaikan hasil lama meski kasir yang sama sudah masuk kembali. Pembaruan gambar, pratinjau, dan penulisan isi PDF dari hasil lama tidak dijalankan. Kasir dapat membuka pemilih baru dari sesi aktif.

## Panduan pemilik sebelum upgrade

1. Saat masih memakai 1.0.0, pastikan nama pengguna dan kata sandi kasir bisa dipakai masuk.
2. Jika login kasir gagal, masuk sebagai admin 1.0.0 dan buka **Akun dan pemulihan**. Pada bagian **Reset akses kasir**, isi kata sandi baru lalu tekan **Simpan kata sandi kasir**. Keluar lalu uji ulang login kasir.
3. Simpan kode pemulihan yang masih berlaku. Jika hilang, gunakan **Buat kode pemulihan baru** dari akun admin 1.0.0 dan simpan kode pengganti. Tunda pemasangan jika akses kasir belum terverifikasi atau kode belum tersimpan.
4. Buka APK resmi 1.5.0 pada perangkat yang masih memiliki 1.0.0, pilih pembaruan, lalu masuk dengan kredensial kasir yang sudah diuji. Jika pemasang menolak APK, biarkan aplikasi lama terpasang dan peroleh APK pembaruan yang cocok. Setelah upgrade, kode 1.0.0 yang masih berlaku memulihkan akun kasir.
